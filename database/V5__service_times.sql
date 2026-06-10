-- ============================================================
-- Imanuel Jakarta Church Platform — DB Migration V5
-- Adds service_times table and granular permissions.
-- ============================================================

BEGIN;

CREATE TABLE IF NOT EXISTS service_times (
    id          BIGSERIAL    PRIMARY KEY,
    name        VARCHAR(255) NOT NULL,
    day_of_week VARCHAR(20)  NOT NULL,
    start_time  TIME         NOT NULL,
    end_time    TIME,
    location    VARCHAR(255),
    description TEXT,
    active      BOOLEAN      NOT NULL DEFAULT TRUE,
    sort_order  INT          NOT NULL DEFAULT 0,
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- ============================================================
-- Permissions
-- ============================================================

INSERT INTO permissions (name, description, category) VALUES
    ('SERVICE_TIME_VIEW',   'View service times in admin panel',  'CONTENT'),
    ('SERVICE_TIME_CREATE', 'Create new service times',           'CONTENT'),
    ('SERVICE_TIME_EDIT',   'Edit existing service times',        'CONTENT'),
    ('SERVICE_TIME_DELETE', 'Delete service times',               'CONTENT')
ON CONFLICT (name) DO NOTHING;

-- ============================================================
-- Role → permission assignments
-- ============================================================

-- SUPER_ADMIN: all
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r CROSS JOIN permissions p
WHERE r.role_name = 'ROLE_SUPER_ADMIN'
  AND p.name IN ('SERVICE_TIME_VIEW','SERVICE_TIME_CREATE','SERVICE_TIME_EDIT','SERVICE_TIME_DELETE')
ON CONFLICT DO NOTHING;

-- ADMIN: all
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p
WHERE r.role_name = 'ROLE_ADMIN'
  AND p.name IN ('SERVICE_TIME_VIEW','SERVICE_TIME_CREATE','SERVICE_TIME_EDIT','SERVICE_TIME_DELETE')
ON CONFLICT DO NOTHING;

-- EDITOR: view + create + edit (no delete)
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p
WHERE r.role_name = 'ROLE_EDITOR'
  AND p.name IN ('SERVICE_TIME_VIEW','SERVICE_TIME_CREATE','SERVICE_TIME_EDIT')
ON CONFLICT DO NOTHING;

COMMIT;
