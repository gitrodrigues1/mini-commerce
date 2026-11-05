#!/usr/bin/env bash
set -euo pipefail

BASE="${BASE_URL:-http://localhost:8080}"

create_and_wait() {
  local sku="$1"
  local expected="$2"
  echo "POST $sku -> esperando $expected"
  local response
  response="$(curl -sf -X POST "$BASE/api/orders" \
    -H 'Content-Type: application/json' \
    -d "{\"sku\":\"$sku\",\"quantity\":1,\"amount\":39.90}")"
  local id
  id="$(python3 -c 'import json,sys; print(json.loads(sys.argv[1])["id"])' "$response")"
  echo "order id=$id"
  local status="PENDING"
  for _ in $(seq 1 20); do
    status="$(curl -sf "$BASE/api/orders/$id" | python3 -c 'import json,sys; print(json.load(sys.stdin)["status"])')"
    echo "  status=$status"
    if [[ "$status" == "$expected" ]]; then
      return 0
    fi
    if [[ "$status" == "REJECTED" || "$status" == "PAYMENT_FAILED" || "$status" == "PAID" ]]; then
      echo "status terminal inesperado: $status" >&2
      return 1
    fi
    sleep 1
  done
  echo "timeout esperando $expected (ultimo=$status)" >&2
  return 1
}

create_and_wait "BOOK-01" "PAID"
create_and_wait "SKU-FAIL-STOCK" "REJECTED"
create_and_wait "SKU-FAIL-PAY" "PAYMENT_FAILED"
echo "demo ok"
