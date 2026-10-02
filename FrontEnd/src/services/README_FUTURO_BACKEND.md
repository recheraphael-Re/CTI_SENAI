# Integração futura — Aula 9

Nesta aula, a planilha é lida e tratada no navegador. Não há envio para API nem gravação em banco.

Fluxo atual: `UploadView.vue` → `uploadStore.js` → `planilha.js` → prévia dos dados.

Na próxima etapa, esta pasta poderá receber `api.js`, com um cliente Axios configurado para a API Spring Boot. Uma action do store poderá enviar o arquivo e receber o resultado da importação.

O Spring Boot deverá validar os dados novamente e cuidar da persistência no PostgreSQL. A configuração e publicação no Azure serão definidas nessa etapa. O Vue não acessará o banco diretamente.

Antes de implementar: definir contrato de entrada e resposta, autenticação, tratamento de erros, CORS e URL por ambiente. Não colocar credenciais do banco no front-end.

Axios não é necessário para o fluxo atual e não foi instalado nesta aula.
