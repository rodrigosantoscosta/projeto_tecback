-- =============================================================
-- V3__create_refresh_tokens.sql
-- Tabela de refresh tokens para autenticação stateful parcial.
--
-- Estratégia: access token JWT curto (15 min) + refresh token
-- opaque (UUID) persistido aqui com TTL de 7 dias.
-- =============================================================

CREATE TABLE IF NOT EXISTS refresh_tokens (
    id            UUID         DEFAULT RANDOM_UUID() PRIMARY KEY,
    token         VARCHAR(36)  NOT NULL UNIQUE,          -- UUID gerado pelo backend
    funcionario_id UUID        NOT NULL,
    expira_em     TIMESTAMP    NOT NULL,
    revogado      BOOLEAN      NOT NULL DEFAULT FALSE,
    criado_em     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_refresh_funcionario
        FOREIGN KEY (funcionario_id) REFERENCES funcionarios(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_refresh_token ON refresh_tokens(token);
CREATE INDEX IF NOT EXISTS idx_refresh_funcionario ON refresh_tokens(funcionario_id);
