"""Insights: transforma os números da análise em frases em português."""
from __future__ import annotations

def brl(valor) -> str:
    if valor is None:
        return '—'
    s = f'{valor:,.2f}'.replace(',', 'X').replace('.', ',').replace('X', '.')
    return f'R$ {s}'

def _virg(x) -> str:
    return str(x).replace('.', ',')

def gerar_insights(resumo, por_segmento, por_nivel, por_consultor, por_servico, por_mes, correlacao) -> list[str]:
    ins: list[str] = []
    top = por_segmento[0]
    ins.append(f"O segmento {top['segmento']} concentra a maior carteira: {top['clientes']} clientes ({_virg(top['percentual'])}%).")
    if resumo['media'] and resumo['mediana'] and (resumo['media'] > resumo['mediana'] * 1.1):
        ins.append(f"A média de faturamento ({brl(resumo['media'])}) supera a mediana ({brl(resumo['mediana'])}). Examine a distribuição antes de escolher uma medida representativa.")
    elif resumo['media'] and resumo['mediana']:
        ins.append(f"Média de faturamento: {brl(resumo['media'])}; mediana: {brl(resumo['mediana'])}. Essas medidas sozinhas não demonstram equilíbrio na carteira.")
    a = next((n for n in por_nivel if n['nivel'] == 'A'), None)
    if a:
        ins.append(f"Os {a['clientes']} clientes nível A representam {_virg(a['percentualFaturamento'])}% do faturamento total, com apenas {_virg(a['percentual'])}% dos clientes.")
    maior = max(por_consultor, key=lambda c: c['faturamentoTotal'])
    ins.append(f"A carteira de {maior['consultor']} reúne a maior soma de faturamento das empresas clientes ({brl(maior['faturamentoTotal'])}, {maior['clientes']} clientes); esse valor não é receita do consultor ou da CTI.")
    if por_servico:
        s = por_servico[0]
        ins.append(f"{s['servico']} é o serviço mais contratado: presente em {s['contratos']} clientes ({_virg(s['percentual'])}%).")
    if por_mes:
        pico = max((e['clientes'] for e in por_mes))
        meses = [f"{e['mes'][5:]}/{e['mes'][:4]}" for e in por_mes if e['clientes'] == pico]
        if len(meses) <= 3:
            ins.append(f"O pico de contratações foi em {', '.join(meses)} ({pico} por mês).")
        else:
            ins.append(f'As contratações ficaram distribuídas ao longo do tempo: no máximo {pico} por mês, repetido em {len(meses)} meses.')
    if correlacao['pearson'] is not None:
        ins.append(correlacao['interpretacao'].rstrip('.') + f" (r de Pearson = {_virg(correlacao['pearson'])}).")
    return ins
