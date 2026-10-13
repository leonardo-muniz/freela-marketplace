CREATE TABLE IF NOT EXISTS reputacoes (freelancer_id UUID PRIMARY KEY, contratos_concluidos INT NOT NULL, valor_total NUMERIC(19,2) NOT NULL);
CREATE TABLE IF NOT EXISTS reputacao_inbox (event_id UUID PRIMARY KEY, processed_at TIMESTAMP WITH TIME ZONE NOT NULL);
