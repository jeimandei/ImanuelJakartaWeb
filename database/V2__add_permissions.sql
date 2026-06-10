-- ============================================================
-- Imanuel Jakarta Church Platform — DB Migration V2
-- Adds: permissions, role_permissions, 3 new roles, seeds
-- ============================================================

CREATE TABLE IF NOT EXISTS permissions (
    id          BIGSERIAL    PRIMARY KEY,
    name        VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255),
    category    VARCHAR(50)  NOT NULL
);

CREATE TABLE IF NOT EXISTS role_permissions (
    role_id       BIGINT NOT NULL REFERENCES roles(id)       ON DELETE CASCADE,
    permission_id BIGINT NOT NULL REFERENCES permissions(id) ON DELETE CASCADE,
    PRIMARY KEY (role_id, permission_id)
);

CREATE INDEX IF NOT EXISTS idx_role_permissions_role ON role_permissions(role_id);
CREATE INDEX IF NOT EXISTS idx_permissions_category  ON permissions(category);

-- New church-specific roles
INSERT INTO roles (role_name, description) VALUES
    ('ROLE_PASTOR',        'Pastoral care: prayer requests, testimonies, contact messages'),
    ('ROLE_MEDIA_MANAGER', 'Manage sermons, livestreams, and gallery content'),
    ('ROLE_EVENT_MANAGER', 'Manage church events and schedules')
ON CONFLICT (role_name) DO NOTHING;

-- ============================================================
-- Permission seeds
-- ============================================================

INSERT INTO permissions (name, description, category) VALUES
    ('USER_VIEW',       'View user list and profiles',               'USERS'),
    ('USER_CREATE',     'Create new user accounts',                  'USERS'),
    ('USER_EDIT',       'Edit user profiles and account status',     'USERS'),
    ('USER_DELETE',     'Deactivate user accounts',                  'USERS'),
    ('ROLE_MANAGE',     'Create, edit, and delete roles',            'USERS'),
    ('CMS_VIEW',        'View CMS pages and announcements',          'CONTENT'),
    ('CMS_CREATE',      'Create CMS pages and announcements',        'CONTENT'),
    ('CMS_EDIT',        'Edit CMS pages and announcements',          'CONTENT'),
    ('CMS_DELETE',      'Delete CMS pages and announcements',        'CONTENT'),
    ('EVENT_VIEW',      'View events',                               'EVENTS'),
    ('EVENT_CREATE',    'Create events',                             'EVENTS'),
    ('EVENT_EDIT',      'Edit events',                               'EVENTS'),
    ('EVENT_DELETE',    'Delete events',                             'EVENTS'),
    ('MEDIA_VIEW',      'View sermons, livestreams, and gallery',    'MEDIA'),
    ('MEDIA_CREATE',    'Upload sermons and gallery items',          'MEDIA'),
    ('MEDIA_EDIT',      'Edit sermons and gallery items',            'MEDIA'),
    ('MEDIA_DELETE',    'Delete media content',                      'MEDIA'),
    ('PRAYER_VIEW',     'View prayer requests',                      'PASTORAL'),
    ('PRAYER_EDIT',     'Update prayer request status',              'PASTORAL'),
    ('TESTIMONY_VIEW',  'View testimony submissions',                'PASTORAL'),
    ('TESTIMONY_EDIT',  'Approve or update testimonies',             'PASTORAL'),
    ('CONTACT_VIEW',    'View contact messages',                     'PASTORAL'),
    ('VOLUNTEER_VIEW',  'View volunteer applications',               'COMMUNITY'),
    ('VOLUNTEER_EDIT',  'Update volunteer application status',       'COMMUNITY'),
    ('NEWSLETTER_VIEW', 'View newsletter subscribers',               'COMMUNITY'),
    ('SETTINGS_VIEW',   'View site settings',                        'SETTINGS'),
    ('SETTINGS_EDIT',   'Edit site settings',                        'SETTINGS'),
    ('AUDIT_VIEW',      'View audit logs',                           'AUDIT')
ON CONFLICT (name) DO NOTHING;

-- ============================================================
-- Role → Permission assignments
-- ============================================================

-- SUPER_ADMIN: all permissions
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r CROSS JOIN permissions p
WHERE r.role_name = 'ROLE_SUPER_ADMIN'
ON CONFLICT DO NOTHING;

-- ADMIN: all permissions
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r CROSS JOIN permissions p
WHERE r.role_name = 'ROLE_ADMIN'
ON CONFLICT DO NOTHING;

-- EDITOR: content CRUD + event create/edit + media view
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p
WHERE r.role_name = 'ROLE_EDITOR'
  AND p.name IN (
      'CMS_VIEW','CMS_CREATE','CMS_EDIT','CMS_DELETE',
      'EVENT_VIEW','EVENT_CREATE','EVENT_EDIT',
      'MEDIA_VIEW'
  )
ON CONFLICT DO NOTHING;

-- PASTOR: pastoral care + community view
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p
WHERE r.role_name = 'ROLE_PASTOR'
  AND p.name IN (
      'PRAYER_VIEW','PRAYER_EDIT',
      'TESTIMONY_VIEW','TESTIMONY_EDIT',
      'CONTACT_VIEW',
      'VOLUNTEER_VIEW'
  )
ON CONFLICT DO NOTHING;

-- MEDIA_MANAGER: all media + event view
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p
WHERE r.role_name = 'ROLE_MEDIA_MANAGER'
  AND p.name IN (
      'MEDIA_VIEW','MEDIA_CREATE','MEDIA_EDIT','MEDIA_DELETE',
      'EVENT_VIEW'
  )
ON CONFLICT DO NOTHING;

-- EVENT_MANAGER: all events + media view
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p
WHERE r.role_name = 'ROLE_EVENT_MANAGER'
  AND p.name IN (
      'EVENT_VIEW','EVENT_CREATE','EVENT_EDIT','EVENT_DELETE',
      'MEDIA_VIEW'
  )
ON CONFLICT DO NOTHING;

-- ============================================================
-- Backfill: assign ROLE_MEMBER to every user with no roles yet
-- ============================================================
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u
CROSS JOIN roles r
WHERE r.role_name = 'ROLE_MEMBER'
  AND NOT EXISTS (
      SELECT 1 FROM user_roles ur WHERE ur.user_id = u.id
  )
ON CONFLICT DO NOTHING;

COMMIT;
