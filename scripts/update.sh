#!/usr/bin/env bash
# NEXUS-BAY Update-Skript (sicher, mit Backup)
set -Eeuo pipefail

BASE="${NEXUS_BAY_HOME:-/opt/nexus-bay}"
SCRIPT_DIR=$(cd "$(dirname "$0")" && pwd)
REPO_DIR=$(cd "$SCRIPT_DIR/.." && pwd)

if [[ "$EUID" -ne 0 ]]; then
  echo "Bitte mit sudo ausführen."
  exit 1
fi

echo "=== NEXUS-BAY Update ==="
echo "1. Backup erstellen..."
bash "$SCRIPT_DIR/backup.sh"

echo "2. Repository aktualisieren (falls vorhanden)..."
if [[ -d "$REPO_DIR/.git" ]]; then
  cd "$REPO_DIR"
  git pull --ff-only || echo "Git-Pull fehlgeschlagen oder nicht nötig."
fi

echo "3. Core-Plugin neu bauen (falls Quellcode vorhanden)..."
if [[ -f "$REPO_DIR/plugin/pom.xml" ]]; then
  mvn -B -f "$REPO_DIR/plugin/pom.xml" clean package -q || true
  if [[ -f "$REPO_DIR/plugin/target/NEXUS-BAY-Core.jar" ]]; then
    for server in lobby survival; do
      if [[ -d "$BASE/servers/$server/plugins" ]]; then
        cp "$REPO_DIR/plugin/target/NEXUS-BAY-Core.jar" "$BASE/servers/$server/plugins/"
        echo "  → Core-Plugin auf $server aktualisiert"
      fi
    done
  fi
fi

echo "4. Skripte aktualisieren..."
if [[ -d "$REPO_DIR/scripts" ]]; then
  cp -a "$REPO_DIR/scripts/." "$BASE/scripts/"
  chmod 750 "$BASE/scripts/"*.sh 2>/dev/null || true
fi

echo
echo "Update abgeschlossen."
echo "Starte die Dienste bei Bedarf neu:"
echo "  sudo bash $BASE/scripts/restart.sh"
