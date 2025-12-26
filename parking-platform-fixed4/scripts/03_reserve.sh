#!/usr/bin/env bash
set -euo pipefail
source scripts/00_env.sh
TOKEN=$(cat scripts/.token)

START=$(python3 - <<'PY'
from datetime import datetime, timedelta
from zoneinfo import ZoneInfo
now=datetime.now(ZoneInfo("Asia/Singapore"))
print((now+timedelta(minutes=5)).isoformat())
PY
)
END=$(python3 - <<'PY'
from datetime import datetime, timedelta
from zoneinfo import ZoneInfo
now=datetime.now(ZoneInfo("Asia/Singapore"))
print((now+timedelta(minutes=35)).isoformat())
PY
)

echo "== Create reservation prefer EV =="
RES=$(curl -sS -X POST "$BASE_RESV/api/v1/reservations" \
  -H "Content-Type: application/json" \
  -H "X-Tenant-Id: $TENANT_ID" -H "Authorization: Bearer $TOKEN" \
  -d "{"lotId":"$LOT_ID","preferEvSpace":true,"startTime":"$START","endTime":"$END"}" | jq .)

RID=$(echo "$RES" | jq -r '.data.id')
echo "reservationId=$RID"
echo "$RID" > scripts/.reservation_id
