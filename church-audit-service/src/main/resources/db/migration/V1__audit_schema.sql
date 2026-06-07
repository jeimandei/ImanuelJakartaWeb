CREATE TABLE IF NOT EXISTS audit_logs (
    id          BIGSERIAL PRIMARY KEY,
    actor       VARCHAR(100),
    actor_role  VARCHAR(100),
    action      VARCHAR(80)  NOT NULL,
    entity_type VARCHAR(100),
    entity_id   VARCHAR(100),
    entity_name VARCHAR(255),
    service_name VARCHAR(100),
    ip_address  VARCHAR(45),
    created_at  TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_audit_actor       ON audit_logs(actor);
CREATE INDEX idx_audit_action      ON audit_logs(action);
CREATE INDEX idx_audit_entity_type ON audit_logs(entity_type);
CREATE INDEX idx_audit_service     ON audit_logs(service_name);
CREATE INDEX idx_audit_created_at  ON audit_logs(created_at DESC);
