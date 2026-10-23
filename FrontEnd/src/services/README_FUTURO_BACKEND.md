# Integração com o backend — Aula 11

O cliente Axios em `api.js` e o store `../stores/auth.js` implementam o lado Vue de autenticação e administração. O contrato proposto está em [CONTRATO_API.md](CONTRATO_API.md).

O backend Spring Boot ainda não está neste repositório. É necessário implementar os endpoints descritos no contrato para utilizar o painel. A API indisponível produz mensagem de erro; não há autenticação simulada.

Upload, tratamento, gráficos e relatórios continuam sendo processados no navegador. Persistência e histórico de importações serão integrados em uma próxima etapa. O Vue não acessa PostgreSQL diretamente.
