"""Análise: calcula as estatísticas do dashboard: média, mediana, moda, desvio padrão, quartis e correlação.

Devolve o objeto `Estatisticas` do contrato (docs/CONTRATO.md): camelCase, 100% serializável em JSON
(NaN e infinito viram null)."""
from __future__ import annotations
import math
from datetime import datetime, timezone
import pandas as pd
from scipy import stats as sp
from insights import gerar_insights
from limpeza import ResultadoTransformacao

def num(x, casas: int | None=2):
    """Número Python seguro para JSON (NaN/inf viram None)."""
    if x is None:
        return None
    try:
        f = float(x)
    except (TypeError, ValueError):
        return None
    if not math.isfinite(f):
        return None
    return round(f, casas) if casas is not None else f

def _pct(parte, todo) -> float:
    return round(100 * parte / todo, 1) if todo else 0.0

def moda(serie: pd.Series):
    m = serie.dropna().mode()
    return None if m.empty else m.iloc[0]

def _resumo_serie(serie: pd.Series) -> dict:
    n = int(serie.size)
    return {'clientes': n, 'total': num(serie.sum()), 'media': num(serie.mean()), 'mediana': num(serie.median()), 'desvioPadrao': num(serie.std(ddof=1)) if n > 1 else None, 'minimo': num(serie.min()), 'q1': num(serie.quantile(0.25)), 'q3': num(serie.quantile(0.75)), 'maximo': num(serie.max())}

def _agrupar(df: pd.DataFrame, coluna: str, nome: str) -> list[dict]:
    """Uma linha por categoria, do maior para o menor número de clientes."""
    saida = []
    total_clientes, total_fat = (len(df), df['faturamento_anual'].sum())
    for chave, g in df.groupby(coluna, sort=False):
        r = _resumo_serie(g['faturamento_anual'])
        saida.append({nome: str(chave), 'clientes': r['clientes'], 'percentual': _pct(len(g), total_clientes), 'percentualFaturamento': _pct(g['faturamento_anual'].sum(), total_fat), 'faturamentoTotal': r['total'], 'total': r['total'], 'media': r['media'], 'mediana': r['mediana'], 'desvioPadrao': r['desvioPadrao'], 'minimo': r['minimo'], 'maximo': r['maximo']})
    return sorted(saida, key=lambda x: (-x['clientes'], x[nome]))

def _por_mes(df: pd.DataFrame) -> list[dict]:
    """Meses contínuos, inclusive os sem contratação."""
    if df.empty:
        return []
    por_mes = df.groupby('mes').agg(clientes=('codigo_cliente', 'count'), faturamento=('faturamento_anual', 'sum'))
    meses = pd.period_range(df['data_contratacao'].min(), df['data_contratacao'].max(), freq='M').strftime('%Y-%m')
    por_mes = por_mes.reindex(meses, fill_value=0)
    acumulado = por_mes['clientes'].cumsum()
    return [{'mes': m, 'clientes': int(r.clientes), 'faturamento': num(r.faturamento), 'acumulado': int(acumulado[m])} for m, r in por_mes.iterrows()]

def _por_servico(df: pd.DataFrame) -> list[dict]:
    longo = df[['codigo_cliente', 'servicos', 'faturamento_anual']].explode('servicos').dropna(subset=['servicos'])
    total = len(df)
    saida = [{'servico': str(nome), 'contratos': int(g['codigo_cliente'].nunique()), 'percentual': _pct(g['codigo_cliente'].nunique(), total), 'faturamentoMedio': num(g['faturamento_anual'].mean())} for nome, g in longo.groupby('servicos')]
    return sorted(saida, key=lambda x: (-x['contratos'], x['servico']))

def _correlacao(df: pd.DataFrame) -> dict:
    x, y = (df['qtd_servicos'].astype(float), df['faturamento_anual'].astype(float))
    base = {'pearson': None, 'p': None, 'n': int(len(df)), 'spearman': None, 'spearmanP': None, 'interpretacao': None}
    if len(df) < 3 or x.nunique() < 2 or y.nunique() < 2:
        return base
    r, p = sp.pearsonr(x, y)
    rho, p_rho = sp.spearmanr(x, y)
    forca = 'forte' if abs(r) >= 0.7 else 'moderada' if abs(r) >= 0.4 else 'fraca'
    teste = 'p < 0,05: evidência contra correlação linear nula, sob as premissas do teste' if p < 0.05 else 'p ≥ 0,05: evidência insuficiente contra correlação linear nula'
    base.update({'pearson': num(r, 3), 'p': num(p, 6), 'spearman': num(rho, 3), 'spearmanP': num(p_rho, 6), 'interpretacao': f"Correlação {forca} {('positiva' if r > 0 else 'negativa')} entre quantidade de serviços e faturamento; {teste}. Análise exploratória deste lote, sem conclusão causal ou generalização automática."})
    return base

def calcular(res: ResultadoTransformacao, linhas_recebidas: int) -> dict:
    df = res.tratado
    if df.empty:
        raise ValueError('Não há clientes válidos para calcular estatísticas.')
    fat = df['faturamento_anual']
    r = _resumo_serie(fat)
    por_segmento = _agrupar(df, 'segmento', 'segmento')
    por_nivel = _agrupar(df.sort_values('nivel_cliente'), 'nivel_cliente', 'nivel')
    por_nivel.sort(key=lambda x: x['nivel'])
    por_consultor = _agrupar(df, 'consultor', 'consultor')
    por_servico = _por_servico(df)
    por_mes = _por_mes(df)
    correlacao = _correlacao(df)
    resumo = {'totalClientes': r['clientes'], 'faturamentoTotal': r['total'], 'media': r['media'], 'mediana': r['mediana'], 'desvioPadrao': r['desvioPadrao'], 'minimo': r['minimo'], 'maximo': r['maximo'], 'q1': r['q1'], 'q3': r['q3'], 'segmentoMaisFrequente': moda(df['segmento']), 'nivelMaisFrequente': moda(df['nivel_cliente']), 'servicoMaisContratado': moda(df['servicos'].explode()), 'mediaServicosPorCliente': num(df['qtd_servicos'].mean()), 'coeficienteVariacao': num(fat.std(ddof=1) / fat.mean(), 3) if len(fat) > 1 and fat.mean() else None, 'consultores': int(df['consultor'].nunique())}
    top = df.sort_values('faturamento_anual', ascending=False).head(5)
    return {'geradoEm': datetime.now(timezone.utc).isoformat(timespec='seconds'), 'resumo': resumo, 'porSegmento': por_segmento, 'porNivel': por_nivel, 'porConsultor': por_consultor, 'porServico': por_servico, 'porMes': por_mes, 'correlacao': correlacao, 'topClientes': [{'codigo': x.codigo_cliente, 'nome': x.nome_cliente, 'segmento': x.segmento, 'nivel': x.nivel_cliente, 'faturamento': num(x.faturamento_anual)} for x in top.itertuples()], 'qualidade': {'linhasRecebidas': int(linhas_recebidas), 'linhasValidas': int(len(df)), 'linhasInvalidas': int(linhas_recebidas - len(df)), 'correcoesSegmento': res.padronizacoes['segmento'], 'correcoesConsultor': res.padronizacoes['consultor'], 'correcoesNivel': res.padronizacoes['nivel_cliente'], 'segmentosOriginais': res.segmentos_originais, 'avisos': res.avisos}, 'insights': gerar_insights(resumo, por_segmento, por_nivel, por_consultor, por_servico, por_mes, correlacao)}
