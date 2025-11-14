-- Initialize Event Database Schema
CREATE SCHEMA IF NOT EXISTS event_schema;

-- Grant permissions
GRANT ALL PRIVILEGES ON SCHEMA event_schema TO event_user;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA event_schema TO event_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA event_schema TO event_user;

-- Set default schema
ALTER DATABASE eventdb SET search_path TO event_schema, public;
