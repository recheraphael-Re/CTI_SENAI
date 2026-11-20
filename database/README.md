# Banco de dados · CTI Insights

Java e Python usam o mesmo PostgreSQL. A API Java gerencia as contas em `public.usuarios`. O Python grava os lotes, clientes, contratos e indicadores em `cti_analytics`. O Java lê os resultados desse esquema para o dashboard.

O Spring aplica `BackEnd/src/main/resources/schema.sql` na partida. [schema.sql](schema.sql) contém a mesma estrutura para execução manual. O script pode ser reaplicado, conserva as tabelas existentes e acrescenta os campos opcionais de matrícula e turma em `public.usuarios`.

Para preparar o banco separadamente, execute `database/schema.sql` com uma conexão direta de administração. Com o esquema preparado, configure `SQL_INIT_MODE=never` no backend se o usuário da aplicação tiver acesso apenas à leitura e à gravação de registros.

Use [consultas-neon.sql](consultas-neon.sql) para conferir os lotes, contagens e estatísticas salvas.
