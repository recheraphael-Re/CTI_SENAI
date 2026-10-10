# 1 Próximas melhorias do CTI Insights

Atualizado em 01/10/2026 após a Aula 9. Leia [GUIA_DE_ESTUDOS_Aula09.md](GUIA_DE_ESTUDOS_Aula09.md) para estudar o fluxo atual.

# 2 Etapas concluídas

| Etapa | Situação |
|---|---|
| Roteador | Sintaxe corrigida; verificação visual das rotas pendente. |
| Build | Passou com --configLoader native nos scripts do Vite. |
| Upload | XLSX, XLS e CSV; seleção e arrastar e soltar delegam ao Pinia. |
| Contrato | Colunas obrigatórias e ambiguidade verificadas. |
| Store da Aula 9 | Guarda arquivo, dados originais, tratados, erros e carregamento. |
| Prévia | Dez linhas com validação e totais de válidos e nível A. |
| Testes | Sete aprovados, incluindo a base oficial e o store. |
| Documentação | README e planejamento em src/services atualizados. |
| Imagem | hero.png substituída por ilustração temática. |

# 3 Próximos passos no front end

## 3.1 Testar o fluxo da Aula 9

Conferir seleção, arrastar e soltar, arquivo inválido, prévia, F5 e descarte. A base oficial deve produzir 300 linhas, 265 válidas, 35 com erro e 37 ocorrências. Registrar evidências no navegador; testes e build não comprovam a aparência.

## 3.2 Conferir navegação e responsividade

Abrir /, /login, /app/upload, /app/relatorios e /app/graficos em largura ampla e estreita. Conferir menu, tema, tabelas e ausência de tela branca.

## 3.3 Melhorar entrada e relatórios

Definir limite de tamanho de arquivo. Revisar contagem e estado vazio da busca. Avaliar paginação de 20 ou 25 linhas, depois dos filtros; voltar à primeira página quando a busca mudar.

# 4 Backend posterior

Definir contrato da API antes de implementar Axios e Spring Boot. Planejar PostgreSQL, autenticação, autorização, histórico e configuração por ambiente. Azure fica para essa etapa.

Login e contato continuam demonstrativos. sessionStorage não substitui banco. Preservar o período do faturamento e identificar faixas como estimativas.

# 5 Comandos e resultados

Na raiz do projeto:

```powershell
npm.cmd run dev
```

Em outro terminal na raiz:

```powershell
npm.cmd run build
node --test --test-isolation=none FrontEnd/tests/*.test.js
git status --short
```

Resultado verificado: build concluído e sete testes aprovados. Base oficial em Data/CTI_Insights_Base_Validacao_300_Registros.xlsx.

# 6 Erros comuns e soluções

| Erro | Solução |
|---|---|
| npm.ps1 bloqueado | Usar npm.cmd. |
| spawn EPERM nos testes | Rodar sem isolamento conforme seção 5. |
| Configuração do Vite falha | Usar scripts atuais com carregador nativo. |
| Colunas ausentes ou ambíguas | Corrigir cabeçalhos conforme a mensagem. |
| Resumo diferente do gabarito | Conferir base e testes antes de alterar regras. |

# 7 Checklist de continuidade

- [x] Build e testes aprovados.
- [x] Store alinhado à Aula 9.
- [x] Prévia e documentação implementadas.
- [ ] Registrar teste manual do upload.
- [ ] Conferir rotas em duas larguras.
- [ ] Avaliar paginação e limite de arquivo.
- [ ] Revisar alterações antes do commit.
- [ ] Definir contrato da API na próxima etapa.
