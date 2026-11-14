#!/usr/bin/env bash
# NEXUS-BAY Restore aus Backup
set -Eeuo pipefail

BASE="${NEXUS_BAY_HOME:-/opt/nexus-bay}"
BACKUP_DIR="$BASE/backups"

if [[ "$EUID" -ne 0 ]]; then
  echo "Bitte mit sudo ausführen."
  exit 1
fi

if [[ $# -lt 1 ]]; then
  echo "Verwendung: $0 <backup-datei.tar.gz>"
  echo
  echo "Verfügbare Backups:"
  ls -1t "$BACKUP_DIR"/nexus-bay-*.tar.gz 2>/dev/null || echo "  (keine)"
  exit 1
fi

ARCHIVE="$1"
if [[ ! -f "$ARCHIVE" ]]; then
  # Versuche relativ zum Backup-Verzeichnis
  if [[ -f "$BACKUP_DIR/$ARCHIVE" ]]; then
    ARCHIVE="$BACKUP_DIR/$ARCHIVE"
  else
    echo "Datei nicht gefunden: $1"
    exit 1
  fi
fi

echo "WARNUNG: Restore überschreibt aktuelle Dateien in $BASE"
echo "Backup: $ARCHIVE"
read -rp "Fortfahren? (ja/nein): " CONFIRM
[[ "$CONFIRM" == "ja" ]] || { echo "Abgebrochen."; exit 0; }

echo "Stoppe Dienste..."
systemctl stop nexus-bay-lobby nexus-bay-survival nexus-bay-proxy 2>/dev/null || true

echo "Entpacke Backup..."
tar -xzf "$ARCHIVE" -C "$BASE"

chown -R nexusbay:nexusbay "$BASE" 2>/dev/null || true

echo "Starte Dienste wieder..."
systemctl start nexus-bay-proxy
sleep 2
systemctl start nexus-bay-lobby nexus-bay-survival

echo "Restore abgeschlossen."
bash "$(dirname "$0")/status.sh"
