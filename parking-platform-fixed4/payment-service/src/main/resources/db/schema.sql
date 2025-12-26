CREATE SCHEMA IF NOT EXISTS pay_order;
SET search_path TO pay_order;

CREATE TABLE IF NOT EXISTS pay_order (
  pay_order_no TEXT PRIMARY KEY,
  tenant_id TEXT NOT NULL,
  session_no TEXT NOT NULL,
  amount_cents INTEGER NOT NULL,
  status TEXT NOT NULL, -- CREATED | SUCCESS | FAILED
  channel TEXT NOT NULL, -- mock
  third_pay_params_json TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS idx_pay_tenant_session
ON pay_order(tenant_id, session_no);
