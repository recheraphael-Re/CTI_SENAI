-- Execute em bancos existentes que usam ddl-auto=validate ou none.
ALTER TABLE usuarios ADD COLUMN IF NOT EXISTS matricula VARCHAR(30);
ALTER TABLE usuarios ADD COLUMN IF NOT EXISTS turma VARCHAR(60);
ALTER TABLE usuarios ADD COLUMN IF NOT EXISTS criado_em TIMESTAMP WITH TIME ZONE;
-- A data real de contas antigas permanece desconhecida (NULL).
