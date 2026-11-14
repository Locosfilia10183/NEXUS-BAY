#!/usr/bin/env bash
set -Eeuo pipefail

BASE="${NEXUS_BAY_HOME:-/opt/nexus-bay}"
BACKUP_DIR="$BASE/backups"
STAMP=$(date +%Y%m%d-%H%M%S)
ARCHIVE="$BACKUP_DIR/nexus-bay-$STAMP.tar.gz"

mkdir -p "$BACKUP_DIR"

echo "NEXUS-BAY Backup starten..."

# Sanfter Stop der Backend-Server (Proxy kann laufen bleiben)
for svc in nexus-bay-lobby nexus-bay-survival; do
  if systemctl is-active --quiet "$svc" 2>/dev/null; then
    systemctl stop "$svc"
  fi
done

trap 'for svc in nexus-bay-lobby nexus-bay-survival; do systemctl start "$svc" 2>/dev/null || true; done' EXIT

ITEMS=()
for path in proxy servers config database; do
  [[ -e "$BASE/$path" ]] && ITEMS+=("$path")
done

if [[ ${#ITEMS[@]} -eq 0 ]]; then
  echo "Nichts zu sichern."
  exit 0
fi

tar -czf "$ARCHIVE" -C "$BASE" "${ITEMS[@]}"
echo "Backup erstellt: $ARCHIVE"

# Einfache Rotation: behalte die letzten 14 Backups
ls -1t "$BACKUP_DIR"/nexus-bay-*.tar.gz 2>/dev/null | tail -n +15 | xargs -r rm -f
