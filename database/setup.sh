#!/bin/bash
# ============================================================
# Imanuel Jakarta Church Platform - Database Setup Script
# ============================================================

echo "Setting up church_db PostgreSQL database..."

psql -U postgres <<'EOF'
-- Create application user
CREATE USER church_app WITH PASSWORD 'church_password';

-- Create database
CREATE DATABASE church_db OWNER church_app;

-- Grant privileges
GRANT ALL PRIVILEGES ON DATABASE church_db TO church_app;
EOF

echo "Running schema migration..."
psql -U church_app -d church_db -f "$(dirname "$0")/V1__initial_schema.sql"

echo "Database setup complete!"
echo ""
echo "Connection details:"
echo "  URL:      jdbc:postgresql://localhost:5432/church_db"
echo "  Username: church_app"
echo "  Password: church_password"
echo ""
echo "Default admin credentials:"
echo "  Username: admin"
echo "  Password: Admin@1234"
