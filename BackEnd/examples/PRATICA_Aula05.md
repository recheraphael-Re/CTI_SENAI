# Pratica da Aula 05 e API da Aula 08

## 1. Executar

Na pasta BackEnd, compile os exemplos separadamente da API:

```powershell
$fontesExemplo = Get-ChildItem examples/java/com/senai/provedor/pratica/*.java | Select-Object -ExpandProperty FullName
javac -d target/exemplos $fontesExemplo
java -cp target/exemplos com.senai.provedor.pratica.MainPratica
```

O pacote `pratica` demonstra Pessoa/Consultor/Cliente, Servico/Diagnostico/Consultoria e Telemetria/Sucesso/Erro. As classes nao sao entidades JPA. Observe `extends`, `super`, `@Override` e listas com referencias do tipo da classe base.

Saida esperada: nomes e dados de Daniel Vieira e Empresa Alpha, mensagens de diagnostico e consultoria, e registros SUCESSO e ERRO. O timestamp varia a cada execucao.

## 2. Experimentar

1. Acrescente uma subclasse de Servico e inclua um objeto na lista do MainPratica.
2. Remova temporariamente o override de executar: observe que o comportamento da classe base passa a ser usado.
3. Adicione um campo na TelemetriaErro e mostre-o depois de super.exibirTelemetria().

## 3. CRUD de clientes

Para iniciar a API (usa o PostgreSQL configurado):

```powershell
mvn spring-boot:run
```

Cadastre primeiro um consultor em `POST /consultores`. Use o idConsultor retornado, substituindo 1 nos exemplos.

| Operacao | Rota | Sucesso |
|---|---|---|
| Criar | POST /clientes?idConsultor=1 | 201 |
| Listar | GET /clientes | 200 |
| Buscar | GET /clientes/1 | 200 |
| Atualizar | PUT /clientes/1?idConsultor=1 | 200 |
| Excluir | DELETE /clientes/1 | 204 |

Corpo para POST e PUT (PUT substitui todos os campos):

```json
{
  "nomeEmpresa": "Empresa Alpha",
  "segmento": "Industrial",
  "faturamentoAnual": 1000.00,
  "nivel": "A",
  "status": "ATIVO"
}
```

Exemplo PowerShell:

```powershell
$dados = @{nomeEmpresa='Empresa Alpha'; segmento='Industrial'; faturamentoAnual=1000; nivel='A'; status='ATIVO'} | ConvertTo-Json
Invoke-RestMethod -Method Post -Uri 'http://localhost:8080/clientes?idConsultor=1' -ContentType 'application/json' -Body $dados
Invoke-RestMethod -Uri 'http://localhost:8080/clientes'
Invoke-RestMethod -Method Put -Uri 'http://localhost:8080/clientes/1?idConsultor=1' -ContentType 'application/json' -Body $dados
Invoke-RestMethod -Method Delete -Uri 'http://localhost:8080/clientes/1'
```

Dados obrigatorios ausentes ou faturamento negativo retornam 400; cliente ou consultor inexistente retorna 404. As respostas incluem nivel e idConsultor. Senhas nao aparecem no JSON e a lista inversa de clientes do consultor e omitida para evitar recursao.

## 4. Limites da verificacao

Os testes usam repositorios simulados e nao acessam o PostgreSQL. Verifique a API com o banco disponivel antes da entrega. Remover unique de senha na entidade pode exigir remover uma restricao UNIQUE ja existente no banco: ddl-auto=update nao garante essa remocao. O login existente compara senhas diretamente; os exemplos nao implementam autenticacao de producao.
