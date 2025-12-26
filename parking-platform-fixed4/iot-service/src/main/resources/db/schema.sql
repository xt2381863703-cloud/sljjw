CREATE SCHEMA IF NOT EXISTS device;
SET search_path TO device;

CREATE TABLE IF NOT EXISTS device (
  id TEXT PRIMARY KEY,
  tenant_id TEXT NOT NULL,
  name TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
