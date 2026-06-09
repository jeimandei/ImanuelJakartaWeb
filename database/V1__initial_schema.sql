-- ============================================================
-- Imanuel Jakarta Church Platform - Database Schema
-- PostgreSQL DDL - Flyway Migration V1
-- ============================================================
-- Setup instructions:
-- psql -U postgres -f setup.sh
-- or run: psql -U church_app -d church_db -f V1__initial_schema.sql
-- ============================================================

-- Users and Authentication
CREATE TABLE IF NOT EXISTS roles (
    id          BIGSERIAL PRIMARY KEY,
    role_name   VARCHAR(50)  NOT NULL UNIQUE,
    description VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS users (
    id                BIGSERIAL PRIMARY KEY,
    username          VARCHAR(50)  NOT NULL UNIQUE,
    email             VARCHAR(100) NOT NULL UNIQUE,
    password_hash     VARCHAR(255) NOT NULL,
    full_name         VARCHAR(100),
    phone_number      VARCHAR(20),
    profile_image_url VARCHAR(500),
    status            VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    created_at        TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP             DEFAULT CURRENT_TIMESTAMP,
    last_login_at     TIMESTAMP,
    CONSTRAINT chk_users_status CHECK (status IN ('ACTIVE','INACTIVE','LOCKED'))
);

CREATE TABLE IF NOT EXISTS user_roles (
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role_id BIGINT NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);

-- CMS Pages
CREATE TABLE IF NOT EXISTS cms_pages (
    id               BIGSERIAL PRIMARY KEY,
    title            VARCHAR(255) NOT NULL,
    slug             VARCHAR(255) NOT NULL UNIQUE,
    page_type        VARCHAR(50),
    content          TEXT,
    meta_title       VARCHAR(255),
    meta_description VARCHAR(500),
    status           VARCHAR(20)  NOT NULL DEFAULT 'DRAFT',
    published_at     TIMESTAMP,
    created_by       VARCHAR(100),
    created_at       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP             DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_cms_pages_status CHECK (status IN ('DRAFT','PUBLISHED','ARCHIVED'))
);

CREATE TABLE IF NOT EXISTS cms_content_blocks (
    id            BIGSERIAL PRIMARY KEY,
    page_id       BIGINT       NOT NULL REFERENCES cms_pages(id) ON DELETE CASCADE,
    block_type    VARCHAR(50)  NOT NULL,
    title         VARCHAR(255),
    body          TEXT,
    image_url     VARCHAR(500),
    display_order INT          NOT NULL DEFAULT 0,
    active        BOOLEAN      NOT NULL DEFAULT TRUE
);

-- Events
CREATE TABLE IF NOT EXISTS events (
    id          BIGSERIAL PRIMARY KEY,
    title       VARCHAR(255) NOT NULL,
    slug        VARCHAR(255) NOT NULL UNIQUE,
    description TEXT,
    location    VARCHAR(255),
    event_start TIMESTAMP,
    event_end   TIMESTAMP,
    image_url   VARCHAR(500),
    status      VARCHAR(20)  NOT NULL DEFAULT 'DRAFT',
    featured    BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP             DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_events_status CHECK (status IN ('DRAFT','PUBLISHED','CANCELLED','COMPLETED'))
);

-- Media: Sermons
CREATE TABLE IF NOT EXISTS sermons (
    id                  BIGSERIAL PRIMARY KEY,
    title               VARCHAR(255) NOT NULL,
    speaker             VARCHAR(100) NOT NULL,
    sermon_date         DATE         NOT NULL,
    description         TEXT,
    youtube_url         VARCHAR(500),
    audio_url           VARCHAR(500),
    scripture_reference VARCHAR(255),
    series_name         VARCHAR(255),
    created_at          TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP             DEFAULT CURRENT_TIMESTAMP
);

-- Media: Livestreams
CREATE TABLE IF NOT EXISTS livestreams (
    id                BIGSERIAL PRIMARY KEY,
    title             VARCHAR(255) NOT NULL,
    youtube_embed_url VARCHAR(500) NOT NULL,
    description       TEXT,
    active            BOOLEAN      NOT NULL DEFAULT FALSE,
    scheduled_start   TIMESTAMP,
    created_at        TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP             DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_livestream_embed CHECK (youtube_embed_url LIKE 'https://www.youtube.com/embed/%')
);

-- Announcements
CREATE TABLE IF NOT EXISTS announcements (
    id         BIGSERIAL PRIMARY KEY,
    title      VARCHAR(255) NOT NULL,
    message    TEXT,
    start_date DATE         NOT NULL,
    end_date   DATE         NOT NULL,
    active     BOOLEAN      NOT NULL DEFAULT TRUE,
    priority   INT          NOT NULL DEFAULT 5,
    created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP             DEFAULT CURRENT_TIMESTAMP
);

-- Interaction: Prayer Requests
CREATE TABLE IF NOT EXISTS prayer_requests (
    id           BIGSERIAL PRIMARY KEY,
    name         VARCHAR(100) NOT NULL,
    email        VARCHAR(150),
    phone        VARCHAR(20),
    message      TEXT         NOT NULL,
    confidential BOOLEAN      NOT NULL DEFAULT FALSE,
    status       VARCHAR(20)  NOT NULL DEFAULT 'NEW',
    created_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_prayer_status CHECK (status IN ('NEW','REVIEWED','PRAYED','ARCHIVED'))
);

-- Interaction: Contact Messages
CREATE TABLE IF NOT EXISTS contact_messages (
    id         BIGSERIAL PRIMARY KEY,
    name       VARCHAR(100) NOT NULL,
    email      VARCHAR(150) NOT NULL,
    subject    VARCHAR(255) NOT NULL,
    message    TEXT         NOT NULL,
    status     VARCHAR(20)  NOT NULL DEFAULT 'NEW',
    created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_contact_status CHECK (status IN ('NEW','REVIEWED','PRAYED','ARCHIVED'))
);

-- News Articles
CREATE TABLE IF NOT EXISTS news_articles (
    id           BIGSERIAL PRIMARY KEY,
    title        VARCHAR(255) NOT NULL,
    slug         VARCHAR(255) NOT NULL UNIQUE,
    content      TEXT         NOT NULL,
    excerpt      VARCHAR(500),
    image_url    VARCHAR(500),
    author_id    BIGINT       REFERENCES users(id) ON DELETE SET NULL,
    status       VARCHAR(20)  NOT NULL DEFAULT 'DRAFT',
    published_at TIMESTAMP,
    created_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP             DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_news_status CHECK (status IN ('DRAFT','PUBLISHED','ARCHIVED'))
);

-- Media: Gallery
CREATE TABLE IF NOT EXISTS gallery_items (
    id            BIGSERIAL PRIMARY KEY,
    title         VARCHAR(255) NOT NULL,
    description   TEXT,
    image_url     VARCHAR(500) NOT NULL,
    album_name    VARCHAR(100) NOT NULL DEFAULT 'General',
    display_order INT          NOT NULL DEFAULT 0,
    active        BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Site Settings
CREATE TABLE IF NOT EXISTS site_settings (
    id            BIGSERIAL PRIMARY KEY,
    setting_key   VARCHAR(100) NOT NULL UNIQUE,
    setting_value TEXT,
    description   VARCHAR(500),
    updated_at    TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);

-- Interaction: Newsletter Subscriptions
CREATE TABLE IF NOT EXISTS newsletter_subscriptions (
    id            BIGSERIAL PRIMARY KEY,
    email         VARCHAR(150) NOT NULL UNIQUE,
    name          VARCHAR(100),
    active        BOOLEAN      NOT NULL DEFAULT TRUE,
    subscribed_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Interaction: Testimony Submissions
CREATE TABLE IF NOT EXISTS testimony_submissions (
    id         BIGSERIAL PRIMARY KEY,
    name       VARCHAR(100) NOT NULL,
    email      VARCHAR(150),
    testimony  TEXT         NOT NULL,
    status     VARCHAR(20)  NOT NULL DEFAULT 'NEW',
    approved   BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_testimony_status CHECK (status IN ('NEW','REVIEWED','PRAYED','ARCHIVED'))
);

-- Interaction: Volunteer Applications
CREATE TABLE IF NOT EXISTS volunteer_applications (
    id         BIGSERIAL PRIMARY KEY,
    full_name  VARCHAR(100) NOT NULL,
    email      VARCHAR(150) NOT NULL,
    phone      VARCHAR(20),
    ministry   VARCHAR(100) NOT NULL,
    message    TEXT,
    status     VARCHAR(20)  NOT NULL DEFAULT 'NEW',
    created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_volunteer_status CHECK (status IN ('NEW','REVIEWED','PRAYED','ARCHIVED'))
);

-- ============================================================
-- INDEXES
-- ============================================================

CREATE INDEX IF NOT EXISTS idx_users_email        ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_username     ON users(username);
CREATE INDEX IF NOT EXISTS idx_users_status       ON users(status);
CREATE INDEX IF NOT EXISTS idx_cms_pages_slug     ON cms_pages(slug);
CREATE INDEX IF NOT EXISTS idx_cms_pages_status   ON cms_pages(status);
CREATE INDEX IF NOT EXISTS idx_events_slug        ON events(slug);
CREATE INDEX IF NOT EXISTS idx_events_status      ON events(status);
CREATE INDEX IF NOT EXISTS idx_events_start       ON events(event_start);
CREATE INDEX IF NOT EXISTS idx_events_featured    ON events(featured);
CREATE INDEX IF NOT EXISTS idx_sermons_date       ON sermons(sermon_date DESC);
CREATE INDEX IF NOT EXISTS idx_sermons_speaker    ON sermons(speaker);
CREATE INDEX IF NOT EXISTS idx_news_slug          ON news_articles(slug);
CREATE INDEX IF NOT EXISTS idx_news_status        ON news_articles(status);
CREATE INDEX IF NOT EXISTS idx_announcements_active ON announcements(active, start_date, end_date);
CREATE INDEX IF NOT EXISTS idx_gallery_album      ON gallery_items(album_name);
CREATE INDEX IF NOT EXISTS idx_livestreams_active ON livestreams(active);
CREATE INDEX IF NOT EXISTS idx_prayer_status      ON prayer_requests(status);
CREATE INDEX IF NOT EXISTS idx_contact_status     ON contact_messages(status);

-- ============================================================
-- SEED DATA
-- ============================================================

-- Roles
INSERT INTO roles (role_name, description) VALUES
    ('ROLE_SUPER_ADMIN', 'Full system access, including user management and system configuration'),
    ('ROLE_ADMIN',       'Manage CMS, events, media, and users (except super admin)'),
    ('ROLE_EDITOR',      'Manage CMS pages, announcements, sermons, and events'),
    ('ROLE_MEMBER',      'Authenticated church member with profile management access'),
    ('ROLE_GUEST',       'Public visitor with view-only access')
ON CONFLICT (role_name) DO NOTHING;

-- Admin user (password: Admin@1234, BCrypt hash)
INSERT INTO users (username, email, password_hash, full_name, phone_number, status) VALUES
    ('admin', 'admin@gmimimanueljakarta.or.id',
     '$2a$10$MaY6esYDAkJLAtXMf8QySOtrk1XFFNGJHpVPcN2XqMQ5.Z9aNCcHO',
     'System Administrator', '+62-21-123-4567', 'ACTIVE')
ON CONFLICT (username) DO NOTHING;

-- Assign ROLE_SUPER_ADMIN to admin user
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r
WHERE u.username = 'admin' AND r.role_name = 'ROLE_SUPER_ADMIN'
ON CONFLICT DO NOTHING;

-- Site Settings
INSERT INTO site_settings (setting_key, setting_value, description) VALUES
    ('church_name',            'Imanuel Jakarta Church',                          'Church official name'),
    ('church_tagline',         'A community of faith, hope, and love',            'Church tagline'),
    ('church_address',         'Jl. Imanuel No. 1, Jakarta Pusat 10110',          'Full church address'),
    ('church_phone',           '+62 21 123-4567',                                 'Main phone number'),
    ('church_email',           'info@gmimimanueljakarta.or.id',                   'Main email address'),
    ('church_logo_url',        '/images/logo.png',                                'Church logo URL'),
    ('church_youtube_channel', 'https://youtube.com/@imanueljakarta',             'YouTube channel URL'),
    ('default_livestream_url', 'https://www.youtube.com/embed/dQw4w9WgXcQ',       'Default livestream embed URL'),
    ('facebook_url',           'https://facebook.com/imanueljakarta',             'Facebook page URL'),
    ('instagram_url',          'https://instagram.com/imanueljakarta',            'Instagram profile URL'),
    ('about_us_text',          'Imanuel Jakarta Church has been serving the Jakarta community since the 1970s. We are a multicultural, welcoming congregation committed to sharing the love of Christ.', 'About us text'),
    ('vision_statement',       'To be a Christ-centered church that transforms lives and communities in Jakarta and beyond.', 'Vision statement'),
    ('mission_statement',      'Worship God. Grow Together. Serve Others. Share Christ.',                                    'Mission statement'),
    ('giving_bank_name',       'Bank BRI',                                        'Bank name for giving'),
    ('giving_account_name',    'GMIM Imanuel Jakarta Barat',                      'Bank account name'),
    ('giving_account_number',  '123-456-7890',                                    'Bank account number'),
    ('giving_branch',          'Jakarta Pusat',                                   'Bank branch')
ON CONFLICT (setting_key) DO NOTHING;

-- Sample announcement
INSERT INTO announcements (title, message, start_date, end_date, active, priority) VALUES
    ('Welcome to Imanuel Jakarta Church Website!',
     'Explore our new website. Join us this Sunday for worship at 08:00 and 10:30 WIB.',
     CURRENT_DATE,
     CURRENT_DATE + INTERVAL '30 days',
     TRUE, 10)
ON CONFLICT DO NOTHING;

-- Sample CMS home page
INSERT INTO cms_pages (title, slug, page_type, content, status, created_by) VALUES
    ('Home', 'home', 'HOME',
     '<p>Welcome to Imanuel Jakarta Church — a community of faith, hope, and love. We would love to have you join us for worship!</p>',
     'PUBLISHED', 'admin')
ON CONFLICT (slug) DO NOTHING;

-- Sample event
INSERT INTO events (title, slug, description, location, event_start, event_end, status, featured) VALUES
    ('Sunday Worship Service',
     'sunday-worship-service',
     'Join us every Sunday for a time of worship, prayer, and the Word of God. All are welcome!',
     'Jl. Imanuel No. 1, Jakarta Pusat',
     (NOW() + INTERVAL '3 days')::TIMESTAMP,
     (NOW() + INTERVAL '3 days' + INTERVAL '2 hours')::TIMESTAMP,
     'PUBLISHED', TRUE)
ON CONFLICT (slug) DO NOTHING;

-- Sample sermon
INSERT INTO sermons (title, speaker, sermon_date, description, scripture_reference, series_name) VALUES
    ('Walking in Faith',
     'Rev. Dr. Imanuel Santoso',
     CURRENT_DATE - INTERVAL '7 days',
     'A powerful message about trusting God in every circumstance of life.',
     'Hebrews 11:1-6',
     'Faith Foundations')
ON CONFLICT DO NOTHING;
