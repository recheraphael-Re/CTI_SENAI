# Modelo do projeto integrador

## 1. Entidades e relacionamentos

As entidades persistidas estao em `src/main/java/com/senai/provedor/models`. O pacote `pratica` continua contendo os exemplos independentes de POO.

- Consultor: nome, matricula (opcional), email, senha, telefone; possui varios clientes.
- Cliente: codigoCTI (opcional), nomeEmpresa, segmento, nivel A/B/C, faturamentoAnual, faixaFaturamento (texto opcional), status; pertence a um consultor e possui contratos e insights.
- Contrato: dataInicio, status; pertence a um cliente e a um servico; possui insights.
- Servico: nome, categoria; possui contratos e telemetrias.
- Telemetria: evento, status, timestamp, resultado ou mensagemErro; identifica servico e contrato executados.
- Insight: tipo, descricao, geradoEm; identifica cliente e opcionalmente contrato do mesmo cliente.

As colecoes inversas sao omitidas do JSON para evitar recursao. Os vinculos sao informados por IDs nos parametros das rotas, nao por objetos aninhados. Datas de telemetria e insight sao geradas no servidor. PUT substitui os campos editaveis; omitir os novos campos opcionais limpa seus valores.

## 2. Executar e testar

```powershell
mvn test
mvn spring-boot:run
```

A segunda linha usa o PostgreSQL do application.properties e ddl-auto=update; nao foi executada automaticamente contra o banco remoto. O Hibernate criara as novas tabelas/colunas ao iniciar com acesso ao banco. Campos adicionados a tabelas existentes sao opcionais para preservar registros anteriores. Nao foram inventadas faixas monetarias nem unicidade para matricula/codigoCTI, pois o diagrama nao define essas regras.

## 3. Rotas

| Recurso | Operacoes |
|---|---|
| /servicos | POST, GET |
| /servicos/{id} | GET, PUT, DELETE |
| /contratos?idCliente=1&idServico=2 | POST |
| /contratos | GET |
| /contratos/{id}?idCliente=1&idServico=2 | PUT |
| /contratos/{id} | GET, DELETE |
| /insights?idCliente=1&idContrato=3 | POST (idContrato opcional) |
| /insights | GET |
| /insights/{id}?idCliente=1&idContrato=3 | PUT (idContrato opcional) |
| /insights/{id} | GET, DELETE |
| /servicos/2/executar?idContrato=3 | POST: executa e grava telemetria |
| /telemetrias | GET: todos os registros |
| /telemetrias?idServico=2 | GET: registros de um servico |

POST retorna 201; DELETE retorna 204. Validacoes retornam 400, recursos inexistentes 404 e exclusoes com vinculos 409. Contratos mantem cliente e servico originais no PUT; para outro vinculo, crie outro contrato. Nao ha exclusao em cascata do historico.

## 4. Exemplo completo em PowerShell

Com a API iniciada, cadastre um consultor ou use seu ID existente:

```powershell
$base = 'http://localhost:8080'
$consultor = Invoke-RestMethod -Method Post -Uri "$base/consultores" -ContentType 'application/json' -Body (@{nome='Ana'; matricula='CON-001'; email='ana.modelo@example.com'; senha='exemplo'; telefone='123'} | ConvertTo-Json)
$cliente = Invoke-RestMethod -Method Post -Uri "$base/clientes?idConsultor=$($consultor.idConsultor)" -ContentType 'application/json' -Body (@{nomeEmpresa='Alpha'; codigoCTI='CLI-001'; segmento='Industrial'; nivel='A'; faturamentoAnual=1000; faixaFaturamento='Faixa informada pela empresa'; status='ATIVO'} | ConvertTo-Json)
$servico = Invoke-RestMethod -Method Post -Uri "$base/servicos" -ContentType 'application/json' -Body (@{nome='Diagnostico de Processos'; categoria='DIAGNOSTICO'} | ConvertTo-Json)
$contrato = Invoke-RestMethod -Method Post -Uri "$base/contratos?idCliente=$($cliente.idCliente)&idServico=$($servico.id)" -ContentType 'application/json' -Body (@{dataInicio='2026-10-05'; status='ATIVO'} | ConvertTo-Json)
Invoke-RestMethod -Method Post -Uri "$base/servicos/$($servico.id)/executar?idContrato=$($contrato.id)"
Invoke-RestMethod -Uri "$base/telemetrias?idServico=$($servico.id)"
Invoke-RestMethod -Method Post -Uri "$base/insights?idCliente=$($cliente.idCliente)&idContrato=$($contrato.id)" -ContentType 'application/json' -Body (@{tipo='Oportunidade'; descricao='Melhorar o fluxo de producao'} | ConvertTo-Json)
```

## 5. Resultado e limites

O executor demonstra DIAGNOSTICO e CONSULTORIA (aceita acentos e diferencas de maiusculas). Contratos ATIVO geram SUCESSO com resultado; contratos em outros estados ou categorias sem executor geram ERRO com mensagemErro. Ambas as respostas retornam 201 porque o registro da tentativa foi criado. Nao se trata de uma integracao externa nem de uma analise real da empresa. Erros de IDs/vinculos retornam 400/404 antes da execucao e nao criam telemetria; falhas de persistencia nao podem ser registradas no mesmo banco indisponivel.

Insight e cadastrado explicitamente; nao e gerado automaticamente por essas execucoes demonstrativas. GeradoEm e timestamp usam o horario local do servidor. Os testes usam repositorios simulados: conexao, criacao de esquema e fluxo HTTP completo ainda precisam ser verificados com um banco disponivel.
