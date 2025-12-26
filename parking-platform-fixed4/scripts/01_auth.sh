#!/usr/bin/env bash
set -euo pipefail
source scripts/00_env.sh

echo "== Register user (ignore error if exists) =="
curl -sS -X POST "$BASE_PARKING/api/v1/auth/register" \
  -H "Content-Type: application/json" -H "X-Tenant-Id: $TENANT_ID" \
  -d '{"username":"alice","password":"alice123"}' | jq .

echo "== Login =="
TOKEN=$(curl -sS -X POST "$BASE_PARKING/api/v1/auth/login" \
  -H "Content-Type: application/json" -H "X-Tenant-Id: $TENANT_ID" \
  -d '{"username":"alice","password":"alice123"}' | jq -r '.data.token')

echo "TOKEN=$TOKEN"
echo "$TOKEN" > scripts/.token
