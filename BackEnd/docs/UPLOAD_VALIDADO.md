# Upload com rastreamento

O frontend envia somente registros aprovados pelo Pinia para
`POST /api/uploads/validadas`, com autenticação Bearer.

```json
{
  "arquivoNome": "clientes.xlsx",
  "descartadas": 2,
  "linhas": [{
    "numeroLinha": 15,
    "codigoCliente": "CTI001",
    "nomeCliente": "Empresa",
    "consultor": "Ana Souza",
    "segmento": "IND.",
    "nivelCliente": "A",
    "faturamentoAnual": 100,
    "servicosContratados": "Internet Dedicada",
    "dataContratacao": "2025-01-15",
    "cidade": "Campinas",
    "uf": "SP"
  }]
}
```

A resposta preserva `arquivoNome` e `descartadas` e inclui `totalValidadas`
e `linhas`, cada uma com `numeroLinha` e `cliente` padronizado.
As regras de campo e duplicidade indicam a linha na mensagem de erro.
JSON que não pode ser convertido (como uma data impossível) recebe erro de formato.
O número de descartadas é informado pelo navegador; não é auditado pelo servidor.
Nenhum cliente deste fluxo é persistido ou enviado ao Python.
As regras ficam no `UploadService`. O limite por envio é configurado por
`UPLOAD_MAX_LINHAS` (padrão: 5000); excesso retorna HTTP 422.
A rota anterior `/api/clientes/validar` continua aceitando a lista simples.

# Cadastro e login

O cadastro aceita `matricula` (até 30 caracteres) e `turma` (até 60), opcionais.
O DTO público do usuário devolve esses campos e `criadoEm`.
A data é registrada pelo servidor para novas contas; contas antigas podem ter data nula.
O login devolve `expiraEmSegundos`, conforme a duração configurada do JWT.
A assinatura e a expiração do token continuam sendo verificadas no backend.

O padrão do projeto é `ddl-auto=update`. Em ambientes com `validate` ou `none`,
aplique `src/main/resources/db/atualizar-perfil-academico.sql` antes de iniciar a nova versão.
O script não foi executado contra banco externo durante esta alteração.

# Administração e erros

`GET /api/admin/usuarios?status=PENDENTE` filtra pelo status opcional
(`PENDENTE`, `ATIVO` ou `BLOQUEADO`). A lista apresenta contas mais novas primeiro;
contas sem data ficam no final, com desempate por ID decrescente.
A proteção transacional da alteração de administradores permanece ativa.
O administrador inicial é criado somente se o e-mail configurado não existir;
reiniciar com a mesma configuração não altera a conta nem sua senha.

Erros de validação de DTO incluem um mapa `campos`, exibido pelo frontend.
Os campos antigos `code` e `message` continuam disponíveis.
Falhas de parâmetros, rotas e métodos recebem respostas padronizadas;
erros inesperados são registrados no servidor e retornam uma mensagem genérica.
