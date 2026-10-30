# Organização do projeto

## 1. Referência

A estrutura foi reorganizada a partir do mapa de pastas do material `backend-front.pdf` da professora. Esta etapa organiza o código existente; não implementa o novo fluxo de ETL do PDF.

```text
BackEnd/
  .mvn/
  docs/
  examples/java/com/senai/provedor/pratica/
  src/
    main/
      java/com/cti/provedor/
        CtiProvedorApplication.java
        config/
        controller/
        dto/
        model/
        repository/
        service/
      resources/
    test/java/com/cti/provedor/
  .env.example
  iniciar.ps1
  pom.xml
FrontEnd/
  src/
    components/
    composables/
    layouts/
    router/
    services/
    stores/
    utils/
    views/
```

## 2. Alterações

- Pacote principal: `com.senai.provedor` passou para `com.cti.provedor`, com atualização dos imports e testes.
- `models` passou para `model`; JWT e `ApiException` passaram de `security` para `config`.
- Classes alinhadas aos nomes do material: `CtiProvedorApplication`, `AdminUsuarioController`, `UsuarioService`, `UsuarioDto`, `PerfilUsuario`, `AdminInicial` e `ErroHandler`.
- `UsuarioDetailsService` concentra a leitura de contas para o Spring Security. O filtro continua consultando o perfil e status atuais a cada requisição.
- A rota pública `GET /api/health` retorna `{"status":"ok"}`. Administração também aceita o prefixo `/api`, preservando as rotas usadas pelo Vue.
- Os exercícios de POO foram retirados do código de produção e mantidos em `examples`, com instruções próprias.
- Credenciais atuais do banco preservadas no `.env` local, ignorado pelo Git. `application.properties` carrega esse arquivo; `.env.example` contém apenas valores de exemplo.

## 3. Arquivos removidos

| Arquivo | Motivo |
|---|---|
| `FrontEnd/src/components/upload.vue` | Componente intermediário sem referências; o router usa diretamente `UploadView.vue` |
| `FrontEnd/src/composables/useClientes.js` | Compatibilidade antiga sem consumidores; as telas utilizam o store Pinia |
| `FrontEnd/src/assets/vite.svg` e `vue.svg` | Imagens do template sem referências |
| `BackEnd/HELP.md` | Documento gerado com referências a outra versão do Spring; instruções atuais estão no README |
| `BackEnd/erro-atual.log` e `erro-backend.log` | Logs de execuções antigas; novos logs são ignorados pelo Git |

O componente `NavMenu.vue`, os arquivos de planilha, os testes e as rotas existentes foram preservados porque possuem consumidores ou exercitam funcionalidades reais.

## 4. Diferenças do PDF que exigem outra implementação

O PDF usa a tabela `usuario` e o status `APROVADO`. O sistema atual usa `usuarios` e `ATIVO`, com contas existentes no Azure. Esta reorganização preserva esses nomes e os registros; não executa migrações nem exclusões no banco.

Os CRUDs de clientes, consultores, serviços, contratos, insights e telemetrias têm controllers, services e testes ativos. Eles não foram tratados como código sem uso. O material propõe transferir a gravação dos dados da planilha ao Python, mas esse serviço e seu contrato ainda precisam ser implementados e validados antes de substituir as rotas atuais.

`UploadController`, `UploadService`, `ClienteValidacaoService`, `PythonEtlService`, os DTOs de upload e seus utilitários representam funcionalidades novas. Não foram criadas classes vazias para aparentar que esse fluxo funciona. Atualmente, a leitura e o tratamento da planilha permanecem no navegador.

## 5. Executar e verificar

Na raiz do projeto:

```powershell
powershell -ExecutionPolicy Bypass -File .\BackEnd\iniciar.ps1
```

Em outro terminal:

```powershell
npm.cmd --prefix FrontEnd run dev
```

Teste a API em `http://localhost:8080/api/health`. Antes de reiniciar, encerre a execução anterior para liberar a porta 8080.

Para verificar sem alterar o Azure:

```powershell
cd BackEnd
mvn.cmd clean test
cd ../FrontEnd
node --test --experimental-test-isolation=none tests/*.test.js
npm.cmd run build
```

Os testes Java usam H2 em memória, com configuração própria de banco e JWT.
