# FrontEnd · CTI Insights

Aplicação em Vue 3 com Pinia, Vue Router, Axios, Tailwind e Chart.js.

```powershell
npm ci
npm run dev
```

Acesse http://localhost:5173. `VITE_API_URL` aponta para o endereço base da API, como `http://localhost:8080`, conforme `.env.example`.

| Rota | Tela |
| --- | --- |
| `/` | Início |
| `/login`, `/cadastro` | Acesso e criação de conta |
| `/upload` | Seleção e leitura da planilha |
| `/relatorio` | Validação por linha e confirmação de envio |
| `/dashboard` | Indicadores, histórico e contratos por lote |
| `/admin/usuarios` | Aprovação de contas e perfis de acesso |

`npm run build` gera o site em `dist/`. A configuração em `vercel.json` permite acessar as rotas do Vue diretamente após publicar na Vercel.

Veja [testes](../docs/TESTES.md) para validar as planilhas e o fluxo.
