#!/usr/bin/env bash
set -euo pipefail

BASE="${NEXUS_BAY_HOME:-/opt/nexus-bay}"

echo "=== NEXUS-BAY Status ==="
echo

for svc in nexus-bay-proxy nexus-bay-lobby nexus-bay-survival; do
  if systemctl list-unit-files | grep -q "^${svc}.service"; then
    state=$(systemctl is-active "$svc" 2>/dev/null || echo "inactive")
    enabled=$(systemctl is-enabled "$svc" 2>/dev/null || echo "disabled")
    printf "%-28s %-12s %s\n" "$svc" "$state" "$enabled"
  else
    printf "%-28s %-12s %s\n" "$svc" "not-installed" "-"
  fi
done

echo
echo "--- Ports ---"
ss -tuln | grep -E ':(25565|25566|25567|19132)\s' || echo "Keine relevanten Ports gefunden."

echo
echo "--- Disk ---"
df -h "$BASE" 2>/dev/null || true
