-- ============================================================
-- Imanuel Jakarta Church Platform — DB Migration V6
-- Adds an optional birthday column to the users table so members
-- can record their date of birth (used by the admin birthday list),
-- plus the permission catalog entry for the admin birthday view.
-- ============================================================

BEGIN;

-- Schema: optional date of birth
ALTER TABLE users ADD COLUMN IF NOT EXISTS birthday DATE;

-- ============================================================
-- Permissions
-- ============================================================

INSERT INTO permissions (name, description, category) VALUES
    ('USER_BIRTHDAY_VIEW', 'View the member birthday list', 'USERS')
ON CONFLICT (name) DO NOTHING;

-- ============================================================
-- Role → permission assignments
-- ============================================================

-- SUPER_ADMIN
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r CROSS JOIN permissions p
WHERE r.role_name = 'ROLE_SUPER_ADMIN'
  AND p.name IN ('USER_BIRTHDAY_VIEW')
ON CONFLICT DO NOTHING;

-- ADMIN
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p
WHERE r.role_name = 'ROLE_ADMIN'
  AND p.name IN ('USER_BIRTHDAY_VIEW')
ON CONFLICT DO NOTHING;

COMMIT;
