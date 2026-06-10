-- ============================================================
-- Imanuel Jakarta Church Platform — DB Migration V3
-- Adds granular permissions for every app capability not
-- covered by V2's generic MEDIA_*, CMS_*, etc. buckets.
-- Safe to run on top of V2 (all inserts use ON CONFLICT).
-- ============================================================

-- ============================================================
-- New permissions
-- ============================================================

INSERT INTO permissions (name, description, category) VALUES
    -- USERS: role/status management (distinct from profile edits)
    ('USER_MANAGE_ROLES',       'Assign or remove roles from users',                'USERS'),
    ('USER_MANAGE_STATUS',      'Activate or deactivate user accounts',             'USERS'),
    ('PERMISSION_MANAGE',       'Create, edit, and delete permission definitions',  'SETTINGS'),

    -- CONTENT: announcements and news as distinct from CMS pages
    ('ANNOUNCEMENT_VIEW',       'View all announcements in admin panel',            'CONTENT'),
    ('ANNOUNCEMENT_CREATE',     'Create new announcements',                         'CONTENT'),
    ('ANNOUNCEMENT_EDIT',       'Edit existing announcements',                      'CONTENT'),
    ('ANNOUNCEMENT_DELETE',     'Delete announcements',                             'CONTENT'),
    ('NEWS_VIEW',               'View all news articles in admin panel',            'CONTENT'),
    ('NEWS_CREATE',             'Create news articles',                             'CONTENT'),
    ('NEWS_EDIT',               'Edit news articles',                               'CONTENT'),
    ('NEWS_PUBLISH',            'Publish or unpublish news articles',               'CONTENT'),
    ('NEWS_DELETE',             'Delete news articles',                             'CONTENT'),

    -- EVENTS: publish/cancel and featured toggle
    ('EVENT_PUBLISH',           'Publish or cancel events',                         'EVENTS'),
    ('EVENT_FEATURED',          'Toggle event featured status',                     'EVENTS'),

    -- MEDIA: granular per resource (sermons, gallery, livestreams)
    ('SERMON_VIEW',             'View sermons list in admin panel',                 'MEDIA'),
    ('SERMON_CREATE',           'Upload and create new sermons',                    'MEDIA'),
    ('SERMON_EDIT',             'Edit sermon details',                              'MEDIA'),
    ('SERMON_DELETE',           'Delete sermons',                                   'MEDIA'),
    ('GALLERY_VIEW',            'View gallery items in admin panel',                'MEDIA'),
    ('GALLERY_CREATE',          'Upload new gallery items',                         'MEDIA'),
    ('GALLERY_EDIT',            'Edit gallery items and toggle visibility',         'MEDIA'),
    ('GALLERY_DELETE',          'Delete gallery items',                             'MEDIA'),
    ('LIVESTREAM_VIEW',         'View livestreams list in admin panel',             'MEDIA'),
    ('LIVESTREAM_CREATE',       'Create new livestream entries',                    'MEDIA'),
    ('LIVESTREAM_EDIT',         'Edit livestream details',                          'MEDIA'),
    ('LIVESTREAM_MANAGE',       'Activate or deactivate a livestream',              'MEDIA'),
    ('LIVESTREAM_DELETE',       'Delete livestream entries',                        'MEDIA'),

    -- COMMUNITY: contact and newsletter management
    ('CONTACT_MANAGE',          'Update contact message status (read/replied)',     'COMMUNITY'),
    ('NEWSLETTER_MANAGE',       'Manage newsletter subscriptions',                  'COMMUNITY')
ON CONFLICT (name) DO NOTHING;

-- ============================================================
-- Role → new permission assignments
-- ============================================================

-- SUPER_ADMIN: all permissions (including new ones)
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r CROSS JOIN permissions p
WHERE r.role_name = 'ROLE_SUPER_ADMIN'
ON CONFLICT DO NOTHING;

-- ADMIN: all except PERMISSION_MANAGE
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p
WHERE r.role_name = 'ROLE_ADMIN'
  AND p.name != 'PERMISSION_MANAGE'
ON CONFLICT DO NOTHING;

-- EDITOR: announcements full, news create/edit/publish, events publish/featured,
--         sermons view/create/edit
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p
WHERE r.role_name = 'ROLE_EDITOR'
  AND p.name IN (
      'ANNOUNCEMENT_VIEW','ANNOUNCEMENT_CREATE','ANNOUNCEMENT_EDIT',
      'NEWS_VIEW','NEWS_CREATE','NEWS_EDIT','NEWS_PUBLISH',
      'EVENT_PUBLISH','EVENT_FEATURED',
      'SERMON_VIEW','SERMON_CREATE','SERMON_EDIT',
      'GALLERY_VIEW'
  )
ON CONFLICT DO NOTHING;

-- PASTOR: contact management on top of existing pastoral permissions
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p
WHERE r.role_name = 'ROLE_PASTOR'
  AND p.name IN ('CONTACT_MANAGE')
ON CONFLICT DO NOTHING;

-- MEDIA_MANAGER: full granular media access
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p
WHERE r.role_name = 'ROLE_MEDIA_MANAGER'
  AND p.name IN (
      'SERMON_VIEW','SERMON_CREATE','SERMON_EDIT','SERMON_DELETE',
      'GALLERY_VIEW','GALLERY_CREATE','GALLERY_EDIT','GALLERY_DELETE',
      'LIVESTREAM_VIEW','LIVESTREAM_CREATE','LIVESTREAM_EDIT',
      'LIVESTREAM_MANAGE','LIVESTREAM_DELETE'
  )
ON CONFLICT DO NOTHING;

-- EVENT_MANAGER: publish/featured control + announcements full
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p
WHERE r.role_name = 'ROLE_EVENT_MANAGER'
  AND p.name IN (
      'EVENT_PUBLISH','EVENT_FEATURED',
      'ANNOUNCEMENT_VIEW','ANNOUNCEMENT_CREATE',
      'ANNOUNCEMENT_EDIT','ANNOUNCEMENT_DELETE'
  )
ON CONFLICT DO NOTHING;

COMMIT;
