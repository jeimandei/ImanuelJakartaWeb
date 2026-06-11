-- ============================================================
-- Imanuel Jakarta Church Platform — DB Migration V6
-- Adds an optional birthday column to the users table so members
-- can record their date of birth (used by the admin birthday list).
-- ============================================================

BEGIN;

ALTER TABLE users ADD COLUMN IF NOT EXISTS birthday DATE;

COMMIT;
