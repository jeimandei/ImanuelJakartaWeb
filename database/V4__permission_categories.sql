-- ============================================================
-- Imanuel Jakarta Church Platform — DB Migration V4
-- Creates permission_categories table and seeds existing categories.
-- ============================================================

CREATE TABLE IF NOT EXISTS permission_categories (
    id   BIGSERIAL    PRIMARY KEY,
    name VARCHAR(50)  NOT NULL UNIQUE
);

INSERT INTO permission_categories (name) VALUES
    ('USERS'),
    ('CONTENT'),
    ('EVENTS'),
    ('MEDIA'),
    ('PASTORAL'),
    ('COMMUNITY'),
    ('SETTINGS'),
    ('AUDIT')
ON CONFLICT (name) DO NOTHING;

COMMIT;
