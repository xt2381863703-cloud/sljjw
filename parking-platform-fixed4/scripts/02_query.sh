#!/usr/bin/env bash
set -euo pipefail
source scripts/00_env.sh
TOKEN=$(cat scripts/.token)

echo "== Lot summary =="
curl -sS "$BASE_PARKING/api/v1/parking-lots/$LOT_ID/summary" \
  -H "X-Tenant-Id: $TENANT_ID" -H "Authorization: Bearer $TOKEN" | jq .

echo "== List spaces =="
curl -sS "$BASE_PARKING/api/v1/parking-lots/$LOT_ID/spaces?limit=50" \
  -H "X-Tenant-Id: $TENANT_ID" -H "Authorization: Bearer $TOKEN" | jq .
