-- Compatibilidade com instalacoes anteriores. Mantem apenas a data de criacao.
ALTER TABLE usuarios ADD COLUMN IF NOT EXISTS criado_em TIMESTAMP WITH TIME ZONE;
