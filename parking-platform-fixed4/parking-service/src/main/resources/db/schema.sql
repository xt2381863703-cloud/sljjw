CREATE SCHEMA IF NOT EXISTS parking;
SET search_path TO parking;

CREATE TABLE IF NOT EXISTS parking_lot (
  id TEXT PRIMARY KEY,
  tenant_id TEXT NOT NULL,
  name TEXT NOT NULL,
  address TEXT,
  opening_hours_json TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS parking_space (
  id TEXT PRIMARY KEY,
  tenant_id TEXT NOT NULL,
  lot_id TEXT NOT NULL,
  code TEXT NOT NULL,
  type TEXT NOT NULL, -- NORMAL | EV
  status TEXT NOT NULL, -- AVAILABLE | OCCUPIED | RESERVED | OUT_OF_SERVICE
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT fk_space_lot FOREIGN KEY (lot_id) REFERENCES parking_lot(id)
);

CREATE INDEX IF NOT EXISTS idx_space_lot_status
    ON parking_space(tenant_id, lot_id, status);

CREATE UNIQUE INDEX IF NOT EXISTS ux_space_tenant_lot_code
    ON parking_space(tenant_id, lot_id, code);

CREATE TABLE IF NOT EXISTS users (
  id TEXT PRIMARY KEY,
  tenant_id TEXT NOT NULL,
  username TEXT NOT NULL,
  password_bcrypt TEXT NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE UNIQUE INDEX IF NOT EXISTS ux_users_tenant_username
    ON users(tenant_id, username);
