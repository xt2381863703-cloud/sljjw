CREATE SCHEMA IF NOT EXISTS reservation;
SET search_path TO reservation;

CREATE TABLE IF NOT EXISTS reservation (
  id TEXT PRIMARY KEY,
  tenant_id TEXT NOT NULL,
  user_id TEXT NOT NULL,
  lot_id TEXT NOT NULL,
  space_id TEXT NOT NULL,
  start_time TIMESTAMPTZ NOT NULL DEFAULT now(),
  end_time TIMESTAMPTZ NOT NULL DEFAULT now(),
  status TEXT NOT NULL, -- ACTIVE | CANCELLED | EXPIRED
  lock_expire_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS idx_resv_tenant_space_status
ON reservation(tenant_id, space_id, status);
