#!/usr/bin/env bash
set -Eeuo pipefail

REPO="Locosfilia10183/NEXUS-BAY"
SERVICE="${NEXUS_BAY_SERVICE:-nexus-bay}"
SERVER_DIR="${NEXUS_BAY_DIR:-/opt/nexus-bay}"
PLUGIN_FILE="$SERVER_DIR/plugins/NEXUS-BAY-Core.jar"
API="https://api.github.com/repos/$REPO/releases/latest"
TMP_DIR="$(mktemp -d)"
cleanup() { rm -rf "$TMP_DIR"; }
trap cleanup EXIT

fail() { echo "FEHLER: $*" >&2; exit 1; }
need() { command -v "$1" >/dev/null 2>&1 || fail "Programm fehlt: $1"; }
for tool in curl jq unzip sha256sum; do need "$tool"; done

[[ "${1:-check}" == "check" || "${1:-}" == "install" ]] || {
  echo "Verwendung: sudo nexus-bay-update [check|install]"
  exit 2
}
ACTION="${1:-check}"

[[ -f "$PLUGIN_FILE" ]] || fail "Core-Plugin nicht gefunden: $PLUGIN_FILE"
CURRENT="$(unzip -p "$PLUGIN_FILE" plugin.yml 2>/dev/null | sed -n 's/^version: *//p' | head -n1 | tr -d "'\"")"
[[ -n "$CURRENT" ]] || fail "Installierte Plugin-Version konnte nicht gelesen werden."

echo "NEXUS-BAY Updateverwaltung"
echo "Installierte Version: $CURRENT"

curl -fsSL -H "Accept: application/vnd.github+json" -H "User-Agent: NEXUS-BAY-Updater/1.0" "$API" -o "$TMP_DIR/release.json"
RELEASE_TAG="$(jq -r '.tag_name // empty' "$TMP_DIR/release.json")"
LATEST="$(jq -r '.assets[] | select(.name=="update-manifest.json") | .browser_download_url' "$TMP_DIR/release.json" | head -n1)"
[[ -n "$LATEST" && "$LATEST" != "null" ]] || fail "Die neueste GitHub-Version enthält kein Update-Manifest."

curl -fsSL -H "User-Agent: NEXUS-BAY-Updater/1.0" "$LATEST" -o "$TMP_DIR/update-manifest.json"
VERSION="$(jq -r '.version // empty' "$TMP_DIR/update-manifest.json")"
EXPECTED_SHA="$(jq -r '.sha256 // empty' "$TMP_DIR/update-manifest.json")"
ASSET_NAME="$(jq -r '.asset // "NEXUS-BAY-Core.jar"' "$TMP_DIR/update-manifest.json")"
ASSET_URL="$(jq -r --arg name "$ASSET_NAME" '.assets[] | select(.name==$name) | .browser_download_url' "$TMP_DIR/release.json" | head -n1)"
[[ -n "$VERSION" && -n "$EXPECTED_SHA" && -n "$ASSET_URL" ]] || fail "Release-Metadaten sind unvollständig."

echo "Neueste verfügbare Version: $VERSION"
echo "Release: $RELEASE_TAG"

if [[ "$CURRENT" == "$VERSION" ]]; then
  echo "Status: aktuell – kein Update erforderlich."
  exit 0
fi

echo "Status: Update verfügbar ($CURRENT -> $VERSION)."
[[ "$ACTION" == "install" ]] || {
  echo "Nur Prüfung durchgeführt. Für die Installation: sudo nexus-bay-update install"
  exit 0
}

[[ "$EUID" -eq 0 ]] || fail "Installation benötigt Root-Rechte (sudo)."
command -v systemctl >/dev/null 2>&1 || fail "systemctl nicht gefunden."
command -v install >/dev/null 2>&1 || fail "install nicht gefunden."

curl -fL -H "User-Agent: NEXUS-BAY-Updater/1.0" "$ASSET_URL" -o "$TMP_DIR/$ASSET_NAME"
ACTUAL_SHA="$(sha256sum "$TMP_DIR/$ASSET_NAME" | awk '{print $1}')"
[[ "$ACTUAL_SHA" == "$EXPECTED_SHA" ]] || fail "SHA-256-Prüfung fehlgeschlagen – Installation abgebrochen."
NEW_VERSION="$(unzip -p "$TMP_DIR/$ASSET_NAME" plugin.yml 2>/dev/null | sed -n 's/^version: *//p' | head -n1 | tr -d "'\"")"
[[ "$NEW_VERSION" == "$VERSION" ]] || fail "JAR-Version und Update-Manifest stimmen nicht überein."

BACKUP_DIR="$SERVER_DIR/backups/updates"
mkdir -p "$BACKUP_DIR"
STAMP="$(date +%Y%m%d-%H%M%S)"
BACKUP="$BACKUP_DIR/NEXUS-BAY-Core-$CURRENT-$STAMP.jar"
cp -a "$PLUGIN_FILE" "$BACKUP"
OWNER="$(stat -c '%u' "$PLUGIN_FILE")"
GROUP="$(stat -c '%g' "$PLUGIN_FILE")"
MODE="$(stat -c '%a' "$PLUGIN_FILE")"

echo "Backup erstellt: $BACKUP"
systemctl stop "$SERVICE"
if ! install -o "$OWNER" -g "$GROUP" -m "$MODE" "$TMP_DIR/$ASSET_NAME" "$PLUGIN_FILE"; then
  cp -a "$BACKUP" "$PLUGIN_FILE"
  systemctl start "$SERVICE" || true
  fail "Neue Plugin-Datei konnte nicht installiert werden; Backup wiederhergestellt."
fi

if ! systemctl start "$SERVICE"; then
  echo "Start fehlgeschlagen – stelle vorherige Plugin-Version wieder her." >&2
  systemctl stop "$SERVICE" || true
  cp -a "$BACKUP" "$PLUGIN_FILE"
  systemctl start "$SERVICE" || true
  fail "Update zurückgerollt. Bitte Server-Logs prüfen."
fi

echo "Update installiert: $CURRENT -> $VERSION"
echo "Backup: $BACKUP"
