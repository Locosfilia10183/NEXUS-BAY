#!/usr/bin/env bash
set -euo pipefail

BASE="${NEXUS_BAY_HOME:-/opt/nexus-bay}"
ERRORS=0

check() {
  local name="$1"
  shift
  if "$@"; then
    echo "[OK]   $name"
  else
    echo "[FAIL] $name"
    ERRORS=$((ERRORS + 1))
  fi
}

echo "=== NEXUS-BAY Healthcheck ==="

check "Installationsverzeichnis" test -d "$BASE"
check "Proxy-Verzeichnis" test -d "$BASE/proxy"
check "Lobby-Verzeichnis" test -d "$BASE/servers/lobby"
check "Survival-Verzeichnis" test -d "$BASE/servers/survival"

check "Velocity JAR" test -f "$BASE/proxy/velocity.jar"
check "Lobby Paper JAR" test -f "$BASE/servers/lobby/paper.jar"
check "Survival Paper JAR" test -f "$BASE/servers/survival/paper.jar"

if command -v systemctl >/dev/null; then
  check "Proxy Service aktiv" systemctl is-active --quiet nexus-bay-proxy
  check "Lobby Service aktiv" systemctl is-active --quiet nexus-bay-lobby
  check "Survival Service aktiv" systemctl is-active --quiet nexus-bay-survival
fi

# Port-Checks (lokal)
if command -v ss >/dev/null; then
  check "Port 25565 (Proxy)" ss -tln | grep -q ':25565 '
  check "Port 25566 (Lobby)" ss -tln | grep -q ':25566 ' || true
  check "Port 25567 (Survival)" ss -tln | grep -q ':25567 ' || true
fi

echo
if [[ $ERRORS -eq 0 ]]; then
  echo "Alle Checks bestanden."
  exit 0
else
  echo "$ERRORS Check(s) fehlgeschlagen."
  exit 1
fi
