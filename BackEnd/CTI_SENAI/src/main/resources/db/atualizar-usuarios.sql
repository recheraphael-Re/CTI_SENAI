-- Executar manualmente somente em banco existente, apos revisar e fazer backup.
-- Nao e executado automaticamente pela aplicacao.
BEGIN;
ALTER TABLE usuarios ADD COLUMN IF NOT EXISTS status varchar(20);
UPDATE usuarios SET status = 'PENDENTE' WHERE status IS NULL;
ALTER TABLE usuarios ALTER COLUMN status SET NOT NULL;
ALTER TABLE usuarios ALTER COLUMN email TYPE varchar(254);
COMMIT;
