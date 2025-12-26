#!/usr/bin/env bash
set -euo pipefail
source scripts/00_env.sh
TOKEN=$(cat scripts/.token)

echo "== Vehicle enter =="
ENTER=$(curl -sS -X POST "$BASE_BILLING/api/v1/events/vehicle-enter" \
  -H "Content-Type: application/json" \
  -H "X-Tenant-Id: $TENANT_ID" -H "Authorization: Bearer $TOKEN" \
  -d "{"lotId":"$LOT_ID","spaceId":"sp-001","plateNo":"SGX1234A"}" | jq .)
echo "$ENTER"
SESSION=$(echo "$ENTER" | jq -r '.data.session_no')
echo "sessionNo=$SESSION"

sleep 2

echo "== Vehicle exit =="
EXIT=$(curl -sS -X POST "$BASE_BILLING/api/v1/events/vehicle-exit" \
  -H "Content-Type: application/json" \
  -H "X-Tenant-Id: $TENANT_ID" -H "Authorization: Bearer $TOKEN" \
  -d "{"sessionNo":"$SESSION"}" | jq .)
echo "$EXIT"
AMOUNT=$(echo "$EXIT" | jq -r '.data.amount_cents')
echo "amount_cents=$AMOUNT"

echo "== Create payment order =="
PO=$(curl -sS -X POST "$BASE_PAY/api/v1/payments" \
  -H "Content-Type: application/json" \
  -H "X-Tenant-Id: $TENANT_ID" -H "Authorization: Bearer $TOKEN" \
  -d "{"sessionNo":"$SESSION"}" | jq .)
echo "$PO"
PAY_ORDER=$(echo "$PO" | jq -r '.data.pay_order_no')
echo "payOrderNo=$PAY_ORDER"

echo "== Notify payment success (mock) =="
curl -sS -X POST "$BASE_PAY/api/v1/payments/notify/mock" \
  -H "Content-Type: application/json" \
  -H "X-Tenant-Id: $TENANT_ID" -H "Authorization: Bearer $TOKEN" \
  -d "{"payOrderNo":"$PAY_ORDER"}" | jq .

echo "Now check iot-service logs (gate.open event consumed)."
