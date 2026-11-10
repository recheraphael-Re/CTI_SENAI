# Preparar a implantação

## 1. Serviços e configuração

O sistema publica três serviços: frontend Vue, API Java e ETL Python. O PostgreSQL continua
sendo o servidor configurado nos `.env` locais. Não foi criada uma segunda instância de banco.

Os arquivos em `deploy/` e `compose.yaml` permitem executar os três serviços em containers.
O frontend tem proxy para Java e fallback das rotas Vue para `index.html`.

Antes de iniciar:

- Configure `BackEnd/.env` e `analytics-python/.env`, com o mesmo banco e `ETL_API_KEY`.
- Aplique a migração de analytics e as migrações JPA necessárias conforme o README do backend.
- Copie `.env.deploy.example` para `.env.deploy` e preencha `JWT_SECRET_BASE64` com uma chave
  Base64 de pelo menos 32 bytes. Pode reutilizar a chave local gerada em `BackEnd/.jwt-secret.local`.
- O padrão `JPA_DDL_AUTO=validate` exige que as tabelas antigas de cadastros já existam.
  Não habilite atualização automática sem revisar as alterações previstas no banco.

## 2. Preparar as tabelas

Na raiz, com acesso ao PostgreSQL:

```powershell
analytics-python/.venv/Scripts/python.exe analytics-python/migrar.py
analytics-python/.venv/Scripts/python.exe analytics-python/verificar.py
```

Ou, usando a imagem Python, depois de preparar os arquivos de ambiente:

```powershell
docker compose --env-file .env.deploy build python
docker compose --env-file .env.deploy run --rm python python migrar.py
```

A migração cria somente as tabelas de analytics e não substitui as tabelas JPA existentes.

## 3. Iniciar e consultar logs

Com Docker Desktop/Engine iniciado:

```powershell
docker compose --env-file .env.deploy config --quiet
docker compose --env-file .env.deploy up --build -d
docker compose --env-file .env.deploy logs --tail 100 backend python
```

Abra `http://localhost:8088`. O proxy envia `/api/*` e `/auth/*` ao Java. O Java se comunica
com `http://python:8000/etl` pela rede dos containers. `VITE_API_URL=/` é definido durante o
build; não é necessário acrescentar `/api` à URL-base.

Para parar os serviços:

```powershell
docker compose --env-file .env.deploy down
```

Esse comando não apaga o PostgreSQL remoto.

## 4. Verificar a implantação

Defina no terminal `TEST_EMAIL` e `TEST_PASSWORD` de uma conta ativa. Depois execute:

```powershell
analytics-python/.venv/Scripts/python.exe testes/testar_tudo.py --back http://localhost:8088
```

No Compose, o Python não publica uma porta para o host. Para verificar os três serviços
pela rede interna, execute o verificador dentro do container Python:

```powershell
docker compose --env-file .env.deploy exec python python -c "import urllib.request; print(urllib.request.urlopen('http://127.0.0.1:8000/saude').read().decode())"
```

Para o verificador completo em um ambiente em que Java e Python estejam acessíveis a quem
executa o teste, informe suas URLs:

```powershell
analytics-python/.venv/Scripts/python.exe testes/testar_tudo.py --back https://URL-JAVA --python https://URL-PYTHON --gravar --exigir-todos
```

`--gravar` cria um lote de demonstração com uma linha válida e uma rejeitada.
Sem essa opção, o verificador não envia importações. `--exigir-todos` retorna erro se houver
falha ou teste pulado. Não considere um resultado com testes pulados como validação completa.

## 5. Conferência da planilha do professor

Importe `sample-data/dados.xlsx` pela interface. O resultado esperado por lote é:

| Medida | Resultado |
|---|---:|
| Linhas da planilha | 38 |
| Clientes válidos | 32 |
| Linhas inválidas | 6 |
| Contratos | 61 |
| Segmentos | 6 |
| Consultores | 4 |
| Serviços distintos | 6 |
| Média anual | R$ 2.261.250,00 |

## 6. Publicação em domínio externo

Para publicar em Azure ou outro provedor, configure os recursos de execução, domínio/HTTPS,
portas e variáveis do serviço escolhido. Se os serviços forem separados, `ETL_URL` precisa
apontar para o Python acessível ao Java. Caso publique o Vue sem o proxy Nginx fornecido,
defina `VITE_API_URL` antes de gerar o build e configure `CORS_ALLOWED_ORIGINS` no Java,
além do fallback das rotas Vue no servidor estático.

Os containers são uma opção de empacotamento, não uma publicação automática no Azure.
A validação de sintaxe do Compose não confirma o build das imagens nem o acesso ao banco.
