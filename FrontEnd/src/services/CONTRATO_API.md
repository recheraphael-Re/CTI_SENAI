> **Estado atual: demonstração para as aulas.** Sem configuração, upload, relatórios e gráficos abrem sem login nem chamadas de autenticação. A tela identifica o modo demonstração; cadastro e administração ficam indisponíveis. A planilha continua na sessão ao voltar ao site. Para retomar a integração futura, defina `VITE_MODO_ACESSO=api` em `FrontEnd/.env.local` e reinicie o Vite. As regras de autenticação descritas abaixo aplicam-se ao modo API, que depende do backend.

# 1. Contrato proposto de autenticação — Aula 11

Versão 1, 17/10/2026. Este contrato descreve o que o front-end implementado espera; o backend Spring Boot ainda precisa implementá-lo. Não representa uma API já disponível. Importação e análises continuam locais nesta etapa.

## 1.1 Endereço e transporte

URL local padrão: `http://localhost:8080`. Configure `VITE_API_URL` em `FrontEnd/.env.local`, usando `.env.example` como referência, e reinicie o Vite. Nenhum segredo pode usar o prefixo VITE_: essas variáveis são públicas no navegador. Produção deve usar HTTPS.

JSON em UTF-8. Rotas protegidas recebem `Authorization: Bearer <token>`. Login e cadastro não recebem Bearer. CORS no Spring deve permitir a origem do Vite (normalmente http://localhost:5173; confira o terminal), os métodos GET/POST/PATCH/OPTIONS e os cabeçalhos Authorization/Content-Type. Não há cookies ou refresh token neste contrato.

# 2. Objetos

`UsuarioResposta`: `id` inteiro positivo, `nome` string, `email` string, `perfil` USER ou ADMIN, `status` PENDENTE, ATIVO ou BLOQUEADO. Não retornar senha nem hash.

Exemplo:

```json
{"id":1,"nome":"Ana Silva","email":"ana@example.com","perfil":"USER","status":"ATIVO"}
```

# 3. Endpoints

| Método e caminho | Acesso | Entrada | Resposta de sucesso |
|---|---|---|---|
| POST /auth/register | Público | nome, email, senha | 201: UsuarioResposta com perfil USER e status PENDENTE |
| POST /auth/login | Público | email, senha | 200: objeto com token e usuario |
| GET /auth/me | Bearer, usuário ATIVO | Sem corpo | 200: UsuarioResposta atualizado |
| GET /admin/usuarios | Bearer, ADMIN ATIVO | Sem corpo | 200: array de UsuarioResposta, inclusive pendentes e bloqueados |
| PATCH /admin/usuarios/{id}/status | Bearer, ADMIN ATIVO | status: ATIVO ou BLOQUEADO | 200: UsuarioResposta atualizado |

O front não envia perfil nem status no cadastro. O servidor os define. Não existe criação pública de ADMIN; provisionar o administrador inicial por processo do backend, sem credenciais no Git. Proposta: impedir bloqueio do próprio administrador e do último ADMIN ativo com 409.

## 3.1 Login

Entrada:

```json
{"email":"ana@example.com","senha":"senha-ficticia-de-exemplo"}
```

Resposta:

```json
{"token":"<token-assinado-pelo-backend>","usuario":{"id":1,"nome":"Ana Silva","email":"ana@example.com","perfil":"USER","status":"ATIVO"}}
```

Senha inválida: 401. Conta pendente ou bloqueada, após verificar credenciais: 403 com código específico. O servidor deve verificar assinatura, expiração, perfil e status atual em cada endpoint protegido; bloquear usuário deve impedir reutilização do token. GET /auth/me usa 401 para token expirado ou conta que deixou de estar ativa.

# 4. Validação e erros

Validar também no servidor: nome não vazio até 120 caracteres, e-mail válido até 254 caracteres e único (normalizado com política consistente), senha com pelo menos oito caracteres. Armazenar somente hash de senha apropriado. O limite máximo de senha deve ser definido conforme o algoritmo escolhido e alinhado ao front.

Formato comum:

```json
{"code":"USUARIO_PENDENTE","message":"Aguarde a aprovação do administrador."}
```

| HTTP | code sugerido | Significado |
|---|---|---|
| 400 | VALIDACAO | Corpo ou campos inválidos |
| 401 | NAO_AUTENTICADO | Credenciais inválidas ou sessão inválida |
| 403 | USUARIO_PENDENTE | Cadastro ainda não aprovado |
| 403 | USUARIO_BLOQUEADO | Conta bloqueada |
| 403 | ACESSO_NEGADO | Usuário sem permissão de administrador |
| 404 | USUARIO_NAO_ENCONTRADO | ID inexistente |
| 409 | EMAIL_JA_CADASTRADO | E-mail já utilizado |
| 409 | ALTERACAO_NAO_PERMITIDA | Regra de administração violada |
| 500 | ERRO_INTERNO | Falha inesperada, sem detalhes internos |

# 5. Comportamento do front-end

- `/`, `/login` e `/cadastro` são públicos. `/app/*` exige login; `/app/usuarios` exige ADMIN.
- Login aprovado abre gráficos; uma URL interna solicitada antes do login é retomada, respeitando permissão.
- Cadastro só anuncia sucesso após resposta HTTP de sucesso; não autentica automaticamente.
- Token fica no sessionStorage da aba para sobreviver a F5. Senha e perfil não são persistidos. Esse armazenamento é acessível a JavaScript: prevenção de XSS continua necessária.
- Após F5, GET /auth/me confirma o perfil antes de abrir rotas privadas. Falha de validação encerra a sessão local.
- 401 em chamada protegida encerra a sessão e redireciona o painel para login. 403 informa o erro sem promover permissões.
- Logout remove token e limpa a importação local para evitar que outro usuário veja os dados anteriores. Logout é local; revogação explícita no servidor está fora desta versão.
- API indisponível exibe erro; não há login fictício ou modo de contornar a proteção. Os testes usam respostas simuladas, nunca contas reais.
- Guards e menu organizam a interface; a autorização efetiva pertence ao Spring Security.

# 6. Execução e verificação

Na raiz:

```powershell
Copy-Item FrontEnd/.env.example FrontEnd/.env.local
npm.cmd run dev
```

Em outro terminal:

```powershell
npm.cmd run build
node --test --test-isolation=none FrontEnd/tests/*.test.js
```

Verificado nesta alteração: build aprovado e 12 testes aprovados. Os quatro testes novos cobrem autenticação, resposta inválida, restauração com Bearer, 401, indisponibilidade e proteção de rotas; os oito existentes continuam passando. Não foi realizada validação visual no navegador nem integração com um Spring Boot real.

## 6.1 Checklist com backend disponível

- [ ] Cadastro cria USER/PENDENTE; e-mail repetido apresenta erro.
- [ ] Login pendente e bloqueado são recusados.
- [ ] ADMIN lista, ativa e bloqueia usuários; USER recebe 403 diretamente na API.
- [ ] Conta ativa entra; F5 confirma sessão; token expirado retorna ao login.
- [ ] Acesso direto sem login a /app/upload retorna ao login.
- [ ] USER não acessa /app/usuarios nem vê o item Usuários.
- [ ] Logout limpa a planilha; novo usuário não recebe dados anteriores.
- [ ] Upload preserva 300 linhas, 265 válidas, 35 com erro e 37 ocorrências.
- [ ] Revisar login, cadastro e tabela em desktop e celular.

# 7. Próxima implementação no backend

Criar entidade Usuario e enums Perfil/StatusUsuario, migração de banco (o schema atual não possui usuários), repository, DTOs, service, controllers de auth/admin e configuração Spring Security. Confirmar este contrato com a equipe antes de integrar. Não executar o schema automaticamente pelo front.

A instalação do Axios trouxe aviso do npm audit: três ocorrências de severidade alta na árvore atual (Vue/@vue/server-renderer e source-map-js). Axios não apareceu como vulnerável nesse relatório. Atualização dessas dependências e nova auditoria ficam registradas como pendência separada; não foi executado npm audit fix.

Referências de implementação: [interceptores Axios](https://axios-http.com/docs/interceptors) e [guards Vue Router](https://router.vuejs.org/guide/advanced/navigation-guards.html).
