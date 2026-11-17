# CTI Insights no Figma

Este plugin local gera sete frames desktop (1440 × 1000), com textos e formas editáveis: início, login, cadastro, upload, relatórios, gráficos e usuários. Baseado nas rotas, componentes e cores do FrontEnd deste projeto. É uma representação visual simplificada, com dados fictícios; os botões não executam as funções do sistema nem possuem navegação de protótipo.

## Utilizar

1. Abra um arquivo de design no aplicativo Figma para desktop.
2. No menu de plugins de desenvolvimento (Plugins > Development), escolha importar um plugin pelo manifesto (Import plugin from manifest).
3. Selecione o arquivo `manifest.json` desta pasta. Mantenha `code.js` na mesma pasta.
4. Execute o plugin “CTI Insights — Telas do sistema” pelo menu de desenvolvimento.
5. Uma nova página com sete telas será criada. Você pode editar os textos, cores e formas normalmente.

O JavaScript é código de plugin: não deve ser colado em uma caixa de texto do design nem no Figma Make. Cada execução cria uma página nova, preservando o conteúdo existente. O plugin não usa rede ou dados reais.

## Arquivos

- `code.js`: código completo da representação.
- `manifest.json`: configuração para importar no Figma.

Validação local: sintaxe JavaScript e estrutura do manifesto. A execução visual precisa ser conferida no Figma.
