# BackEnd CTI SENAI

API Spring Boot 3.5.6 / Java 21 com PostgreSQL, Spring Security, BCrypt e JWT.

## Executar no PowerShell

Para iniciar com uma chave JWT local persistente (ignorada pelo Git), execute `powershell -ExecutionPolicy Bypass -File .\BackEnd\iniciar.ps1` na raiz do repositorio. Se estiver dentro de BackEnd, use `powershell -ExecutionPolicy Bypass -File .\iniciar.ps1`. As configuracoes de banco existentes continuam sendo utilizadas. Uma nova chave invalida tokens anteriores; entre novamente no sistema.

Na raiz do repositorio:

```powershell
cd BackEnd
$env:DB_URL = "jdbc:postgresql://localhost:5432/cti_provedor"
$env:DB_USERNAME = "postgres"
$env:DB_PASSWORD = "<senha-do-seu-banco>"
# Gere uma chave uma vez e guarde-a fora do Git. Trocar a chave invalida os tokens.
$bytes = New-Object byte[] 32
[System.Security.Cryptography.RandomNumberGenerator]::Fill($bytes)
$env:JWT_SECRET_BASE64 = [Convert]::ToBase64String($bytes)
$env:CORS_ALLOWED_ORIGINS = "http://localhost:5173"
mvn.cmd spring-boot:run
```

Em Windows PowerShell 5.1, use `([System.Security.Cryptography.RandomNumberGenerator]::Create()).GetBytes($bytes)` no lugar de `Fill`.
As variaveis sao lidas do ambiente e de BackEnd/.env, carregado pelo Spring. O arquivo .env e local e ignorado pelo Git; use .env.example como referencia. DB_USER e DB_USERNAME sao aceitos para o usuario do banco.
A senha de banco anteriormente no application.properties foi removida. Troque essa senha no provedor se ela foi compartilhada ou versionada.

## Primeiro administrador

Antes da primeira execucao, configure tambem `ADMIN_EMAIL` e `ADMIN_PASSWORD` no terminal. A senha deve ter pelo menos 8 caracteres e no maximo 72 bytes UTF-8. O provisionamento cria ADMIN/ATIVO e nao promove contas existentes. Depois de criar o administrador, pare a API e remova essas duas variaveis com `Remove-Item Env:ADMIN_EMAIL, Env:ADMIN_PASSWORD` antes de iniciar novamente. Nao grave essas credenciais no Git.

## Rotas

| Metodo | Rota | Acesso |
|---|---|---|
| POST | /auth/register | Publico: nome, email, senha; cria USER/PENDENTE |
| POST | /auth/login | Publico: email, senha; retorna token, tipo Bearer e usuario |
| GET | /auth/me | Token de usuario ativo |
| GET | /admin/usuarios | ADMIN ativo |
| PATCH | /admin/usuarios/{id}/status | ADMIN: status ATIVO ou BLOQUEADO; perfil opcional USER ou ADMIN |
| GET | /api/health | Publico: retorna status ok |
| GET | /api/publico | Publico |
| GET | /api/privado | Token de usuario ativo |

As rotas de autenticacao e administracao tambem aceitam prefixo /api e cadastro aceita /auth/cadastro como no material de referencia.
O usuario retornado contem id, nome, email, perfil e status, sem senha/hash.
Cadastro ignora perfil/status fornecidos pelo cliente. Erros de autenticacao usam JSON com code/message.
Cada chamada protegida consulta o status atual no banco; bloquear uma conta impede reutilizar o token.
Nao e permitido bloquear ou rebaixar o proprio administrador, nem remover o ultimo ADMIN ativo.
CORS permite a origem configurada, Authorization e Content-Type, incluindo preflight.

As rotas existentes de clientes, contratos, servicos, insights e telemetrias exigem autenticacao. Excluir cliente e escrever em consultores exige ADMIN. O login antigo `/consultores/login` foi removido; use `/auth/login`.
O CRUD de consultores foi mantido por ter rotas e testes ativos. Os exemplos independentes de POO foram separados em `examples/java`, fora do codigo da API; veja `examples/PRATICA_Aula05.md`. Novas senhas de consultores sao armazenadas como BCrypt, mas o consultor nao autentica a API: a conta de acesso e Usuario.

## Banco existente

Os testes nao acessam o PostgreSQL remoto. Antes de usar um banco com usuarios existentes, execute conscientemente o script `src/main/resources/db/atualizar-usuarios.sql` nesse banco. Ele adiciona o status PENDENTE aos registros anteriores e amplia o email para 254 caracteres. Verifique emails duplicados sem diferenciar maiusculas antes de criar novas contas. Contas existentes precisam de aprovacao e senhas de Usuario devem estar em BCrypt.
O Hibernate usa update por padrao para desenvolvimento. Em ambiente controlado, aplique o schema revisado e configure JPA_DDL_AUTO=validate.

## FrontEnd

O contrato de auth/admin existente em FrontEnd/src/services/CONTRATO_API.md foi implementado. Para usar a integracao, configure `VITE_API_URL=http://localhost:8080` em FrontEnd/.env.local e reinicie o Vite. O painel exige autenticacao; cadastro e login utilizam a API por padrao. Senhas acima de 72 bytes sao recusadas pelo servidor.

## Testes

```powershell
mvn.cmd test
```

Os testes existentes de clientes/integrador continuam incluidos. AuthIntegrationTest inicia a API em porta aleatoria com H2 em memoria e verifica cadastro, hash, duplicidade, aprovacao, login, permissoes, bloqueio, validacao e CORS. Nao altera dados reais. H2 nao substitui uma verificacao posterior do schema PostgreSQL e da integracao visual com o Vue.

## Organizacao conforme o material da professora

O pacote principal e `src/main/java/com/cti/provedor`.

| Pasta | Responsabilidade |
|---|---|
| controller | Rotas HTTP; AuthController, AdminUsuarioController e ErroHandler |
| service | Regras de negocio; AuthService e UsuarioService |
| repository | Consultas ao PostgreSQL |
| model | Entidades JPA e enums; Usuario, PerfilUsuario e StatusUsuario |
| dto | Dados de entrada e resposta; UsuarioDto e DTOs de autenticacao |
| config | SecurityConfig, JWT, UsuarioDetailsService, ApiException e AdminInicial |

A classe inicial e `CtiProvedorApplication`. Os testes acompanham o mesmo pacote em `src/test/java/com/cti/provedor`.

A reorganizacao nao renomeia tabelas nem altera registros do Azure. Mantivemos `usuarios` e `ATIVO`, utilizados pelo frontend e pelo banco existente. O PDF usa `usuario` e `APROVADO`; adotar esses nomes requer uma migracao separada.

O material tambem apresenta um novo fluxo de upload com UploadController, UploadService, ClienteValidacaoService, PythonEtlService e utilitarios. Esse fluxo ainda nao existe neste projeto. Nenhuma classe vazia foi criada para simular a integracao: upload, graficos e relatorios continuam sendo processados no navegador. A integracao com Python exige implementar e testar seu contrato antes de substituir os CRUDs atuais.

Detalhes da limpeza e dos arquivos preservados: [docs/ORGANIZACAO_PROJETO.md](docs/ORGANIZACAO_PROJETO.md).
