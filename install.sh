#!/usr/bin/env bash
set -Eeuo pipefail
SERVICE="nexus-bay"
DEFAULT_DIR="/opt/nexus-bay"
RUN_USER="minecraft"
PAPER_API="https://fill.papermc.io/v3"

if [[ "$EUID" -ne 0 ]]; then echo "Bitte mit sudo ausführen: sudo bash install.sh"; exit 1; fi
echo "=== NEXUS-BAY | Java + Bedrock + RPG Core ==="
read -rp "Minecraft-Version (leer = neueste stabile Paper-Version): " MC_VERSION
if [[ -z "$MC_VERSION" ]]; then
  PROJECT="$(curl -fsSL -H 'User-Agent: NEXUS-BAY/1.0 (server installer)' "$PAPER_API/projects/paper")"
  mapfile -t VERSIONS < <(echo "$PROJECT" | jq -r '.versions | to_entries[] | .value[]' | sort -Vru)
  MC_VERSION=""
  for candidate in "${VERSIONS[@]}"; do
    response="$(curl -fsSL -H 'User-Agent: NEXUS-BAY/1.0 (server installer)' "$PAPER_API/projects/paper/versions/$candidate/builds")" || continue
    stable="$(echo "$response" | jq -r '[.[] | select(.channel == "STABLE")] | first | .id // empty')"
    if [[ -n "$stable" ]]; then MC_VERSION="$candidate"; break; fi
  done
  [[ -n "$MC_VERSION" ]] || { echo "Keine stabile Paper-Version gefunden."; exit 1; }
  echo "Neueste stabile Paper-Version: $MC_VERSION"
fi
[[ "$MC_VERSION" =~ ^[0-9]+\.[0-9]+(\.[0-9]+)?$ ]] || { echo "Ungültige Version."; exit 1; }
read -rp "RAM in GB [6]: " RAM_GB
RAM_GB="${RAM_GB:-6}"
[[ "$RAM_GB" =~ ^[2-9][0-9]*$ ]] || { echo "Bitte mindestens 2 GB angeben."; exit 1; }
read -rp "Installationsordner [$DEFAULT_DIR]: " INSTALL_DIR
INSTALL_DIR="${INSTALL_DIR:-$DEFAULT_DIR}"
if [[ -e "$INSTALL_DIR" ]] && [[ -n "$(find "$INSTALL_DIR" -mindepth 1 -maxdepth 1 -print -quit 2>/dev/null)" ]]; then
 echo "ABBRUCH: $INSTALL_DIR ist nicht leer. Es wird nichts überschrieben."; exit 1
fi

export DEBIAN_FRONTEND=noninteractive
apt-get update
apt-get install -y ca-certificates curl gnupg
install -d -m 0755 /etc/apt/keyrings
curl -fsSL https://packages.adoptium.net/artifactory/api/gpg/key/public | gpg --dearmor -o /etc/apt/keyrings/adoptium.gpg
chmod 644 /etc/apt/keyrings/adoptium.gpg
echo "deb [signed-by=/etc/apt/keyrings/adoptium.gpg] https://packages.adoptium.net/artifactory/deb $(. /etc/os-release && echo $VERSION_CODENAME) main" > /etc/apt/sources.list.d/adoptium.list
apt-get update
apt-get install -y curl jq unzip ca-certificates git maven temurin-25-jdk
JAVA_BIN="$(command -v java)"
BUILDS="$(curl -fsSL -H 'User-Agent: NEXUS-BAY/1.0 (server installer)' "$PAPER_API/projects/paper/versions/$MC_VERSION/builds")"
BUILD="$(echo "$BUILDS" | jq -r '[.[] | select(.channel == "STABLE")] | first | .id // empty')"
PAPER_URL="$(echo "$BUILDS" | jq -r '[.[] | select(.channel == "STABLE")] | first | .downloads["server:default"].url // empty')"
[[ -n "$BUILD" && -n "$PAPER_URL" ]] || { echo "Für Paper $MC_VERSION gibt es keinen stabilen Build."; exit 1; }

id "$RUN_USER" >/dev/null 2>&1 || useradd --system --create-home --home-dir "/home/$RUN_USER" --shell /usr/sbin/nologin "$RUN_USER"
install -d -o "$RUN_USER" -g "$RUN_USER" "$INSTALL_DIR/plugins" "$INSTALL_DIR/backups"
curl -fL "$PAPER_URL" -o "$INSTALL_DIR/paper.jar"
curl -fL "https://download.geysermc.org/v2/projects/geyser/versions/latest/builds/latest/downloads/spigot" -o "$INSTALL_DIR/plugins/Geyser-Spigot.jar"
curl -fL "https://download.geysermc.org/v2/projects/floodgate/versions/latest/builds/latest/downloads/spigot" -o "$INSTALL_DIR/plugins/floodgate-spigot.jar"

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
if [[ -f "$SCRIPT_DIR/updates/nexus-bay-update.sh" ]]; then
  install -m 755 "$SCRIPT_DIR/updates/nexus-bay-update.sh" /usr/local/bin/nexus-bay-update
  echo "NEXUS-BAY Updateverwaltung installiert: nexus-bay-update check|install"
fi
if [[ -f "$SCRIPT_DIR/plugin/pom.xml" ]]; then
  echo "Baue NEXUS-BAY RPG/Admin-Plugin..."
  mvn -B -f "$SCRIPT_DIR/plugin/pom.xml" clean package
  install -m 640 "$SCRIPT_DIR/plugin/target/NEXUS-BAY-Core.jar" "$INSTALL_DIR/plugins/NEXUS-BAY-Core.jar"
else
  echo "WARNUNG: Plugin-Quellcode nicht gefunden; Core-Plugin wurde übersprungen."
fi

cat > "$INSTALL_DIR/start.sh" <<EOF
#!/usr/bin/env bash
set -euo pipefail
cd "$INSTALL_DIR"
exec "$JAVA_BIN" -Xms2G -Xmx${RAM_GB}G -XX:+UseG1GC -XX:+ParallelRefProcEnabled -jar paper.jar --nogui
EOF
chmod 750 "$INSTALL_DIR/start.sh"
cat > "$INSTALL_DIR/server.properties" <<'EOF'
motd=§6NEXUS-BAY §f| Pirate RPG | Java + Bedrock
server-port=25565
server-ip=
online-mode=true
max-players=50
view-distance=8
simulation-distance=6
enable-command-block=false
white-list=false
EOF
cat > "$INSTALL_DIR/eula.txt" <<'EOF'
# Read and accept the Minecraft EULA before setting this to true.
eula=false
EOF
cat > "$INSTALL_DIR/backup.sh" <<'EOF'
#!/usr/bin/env bash
set -Eeuo pipefail
D="$(cd "$(dirname "$0")" && pwd)"
mkdir -p "$D/backups"
systemctl stop nexus-bay
trap 'systemctl start nexus-bay' EXIT
items=()
for item in world world_nether world_the_end server.properties plugins config.yml; do
  [[ ! -e "$D/$item" ]] || items+=("$item")
done
tar -czf "$D/backups/nexus-bay-$(date +%Y%m%d-%H%M%S).tar.gz" -C "$D" "${items[@]}"
EOF
chmod 750 "$INSTALL_DIR/backup.sh"
chown -R "$RUN_USER:$RUN_USER" "$INSTALL_DIR"
cat > "/etc/systemd/system/$SERVICE.service" <<EOF
[Unit]
Description=NEXUS-BAY Minecraft Java + Bedrock RPG Server
After=network-online.target
Wants=network-online.target
[Service]
User=$RUN_USER
Group=$RUN_USER
WorkingDirectory=$INSTALL_DIR
ExecStart=$INSTALL_DIR/start.sh
Restart=on-failure
RestartSec=10
KillSignal=SIGINT
TimeoutStopSec=90
NoNewPrivileges=true
PrivateTmp=true
ProtectSystem=full
ReadWritePaths=$INSTALL_DIR
[Install]
WantedBy=multi-user.target
EOF
systemctl daemon-reload
systemctl enable "$SERVICE"
if command -v ufw >/dev/null 2>&1 && ufw status | grep -q "Status: active"; then
  ufw allow 25565/tcp
  ufw allow 19132/udp
fi
echo
echo "NEXUS-BAY installiert: Paper $MC_VERSION Build $BUILD"
echo "RPG/Admin-Plugin: $([[ -f "$INSTALL_DIR/plugins/NEXUS-BAY-Core.jar" ]] && echo installiert || echo nicht gebaut)"
echo "EULA prüfen/akzeptieren: nano $INSTALL_DIR/eula.txt"
echo "Danach starten: systemctl start $SERVICE"
echo "Provider-Firewall: TCP 25565 und UDP 19132 freigeben."
echo "Java: VPS-IP:25565 | Bedrock: VPS-IP Port 19132"
