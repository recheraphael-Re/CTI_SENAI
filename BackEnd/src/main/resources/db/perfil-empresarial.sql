-- Perfil empresarial: aplicar depois de atualizar o backend.
BEGIN;
ALTER TABLE public.consultor DROP COLUMN IF EXISTS matricula;
ALTER TABLE public.usuarios DROP COLUMN IF EXISTS matricula;
ALTER TABLE public.usuarios DROP COLUMN IF EXISTS turma;
COMMIT;
