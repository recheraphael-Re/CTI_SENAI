# BackEnd · CTI Insights

API Spring Boot 3.5.16 com Java 21. Gerencia contas e permissões, revalida os registros do upload, encaminha os aprovados ao ETL e consulta as estatísticas gravadas.

## Executar

Copie `.env.example` para `.env` se ainda precisar configurá-lo. Preencha `DB_URL`, `DB_USER`, `DB_PASSWORD`, `JWT_SECRET_BASE64` e `ETL_API_KEY`. A chave do ETL deve coincidir com a do Python. `DB_URL` usa o formato JDBC, por exemplo `jdbc:postgresql://HOST:5432/BANCO?sslmode=require` para um banco com TLS.

Mantenha a chave JWT da instalação existente. Em uma instalação nova, gere uma com o PowerShell e guarde o valor em `BackEnd/.env`:

```powershell
$bytesJwt = New-Object byte[] 32
$geradorJwt = [System.Security.Cryptography.RandomNumberGenerator]::Create()
try { $geradorJwt.GetBytes($bytesJwt) } finally { $geradorJwt.Dispose() }
[Convert]::ToBase64String($bytesJwt)
```

Para criar a primeira conta administradora, defina `ADMIN_EMAIL` e `ADMIN_PASSWORD` antes da primeira execução. O provisionamento cria uma conta `ADMIN` ativa quando o e-mail ainda está disponível. O Spring inicializa o schema na partida. Veja [database](../database/README.md) para aplicar o SQL separadamente.

```powershell
mvn spring-boot:run
```

Valide o status com `GET http://localhost:8080/api/health`. Veja as demais rotas no [contrato da API](../docs/CONTRATO.md).

### Contas CTI pelo terminal

Execute em `BackEnd/`. Os comandos usam a conexão configurada em `.env`, com o schema já criado. A senha é solicitada no terminal.

```powershell
mvn -DskipTests package
java -jar target/cti-provedor-api.jar preparar-contas-cti
java -jar target/cti-provedor-api.jar resetar-senha-admin-cti
```

`preparar-contas-cti` define a senha de `admin@cti.com` e `user@cti.com` e cria como ativas as contas que ainda não existem. Perfis e status de contas existentes são preservados. `resetar-senha-admin-cti` altera exclusivamente a senha de `admin@cti.com`, desde que a conta já exista com perfil `ADMIN`.

## Build

```powershell
mvn clean verify
docker build -t cti-insights-api .
```

Os comandos partem de `BackEnd/`. O Maven gera `target/cti-provedor-api.jar`. Para iniciar o contêiner conectado a serviços já configurados, use:

```powershell
docker run --rm --env-file .env -p 8080:8080 cti-insights-api
```

Configure `DB_URL` e `ETL_URL` com endereços acessíveis dentro do contêiner. No Docker Desktop, use `host.docker.internal` para um serviço iniciado no Windows.
