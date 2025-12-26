CREATE SCHEMA IF NOT EXISTS billing_session;
SET search_path TO billing_session;

CREATE TABLE IF NOT EXISTS billing_session (
  session_no TEXT PRIMARY KEY,
  tenant_id TEXT NOT NULL,
  user_id TEXT,
  lot_id TEXT NOT NULL,
  space_id TEXT,
  plate_no TEXT NOT NULL,
  enter_time TIMESTAMPTZ NOT NULL DEFAULT now(),
  exit_time TIMESTAMPTZ,
  status TEXT NOT NULL, -- ACTIVE | WAIT_PAY | PAID
  amount_cents INTEGER NOT NULL DEFAULT 0,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS idx_billing_tenant_status
    ON billing_session(tenant_id, status);
