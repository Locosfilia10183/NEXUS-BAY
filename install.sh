#!/usr/bin/env bash
set -Eeuo pipefail
SERVICE="nexus-bay"
DEFAULT_DIR="/opt/nexus-bay"
RUN_USER="minecraft"
PAPER_API="https://api.papermc.io/v2"

if [[ "$EUID" -ne 0 ]]; then echo "Bitte mit sudo ausführen: sudo bash install.sh"; exit 1; fi
echo "=== NEXUS-BAY | Neuer Java + Bedrock Server ==="
read -rp "Minecraft-Version (z.B. 1.21.4): " MC_VERSION
[[ "$MC_VERSION" =~ ^[0-9]+\.[0-9]+(\.[0-9]+)?$ ]] || { echo "Ungültige Version."; exit 1; }
read -rp "RAM in GB [4]: " RAM_GB
RAM_GB="${RAM_GB:-4}"
[[ "$RAM_GB" =~ ^[2-9][0-9]*$ ]] || { echo "Bitte mindestens 2 GB angeben."; exit 1; }
read -rp "Installationsordner [$DEFAULT_DIR]: " INSTALL_DIR
INSTALL_DIR="${INSTALL_DIR:-$DEFAULT_DIR}"
if [[ -e "$INSTALL_DIR" ]] && [[ -n "$(find "$INSTALL_DIR" -mindepth 1 -maxdepth 1 -print -quit 2>/dev/null)" ]]; then
 echo "ABBRUCH: $INSTALL_DIR ist nicht leer. Es wird nichts überschrieben."; exit 1
fi

export DEBIAN_FRONTEND=noninteractive
apt-get update
apt-get install -y curl jq ca-certificates openjdk-21-jre-headless
JAVA_BIN="$(command -v java)"
VERSIONS="$(curl -fsSL "$PAPER_API/projects/paper/versions")"
echo "$VERSIONS" | jq -e --arg v "$MC_VERSION" '.versions | index($v)' >/dev/null || { echo "Paper unterstützt diese Version nicht."; exit 1; }
BUILDS="$(curl -fsSL "$PAPER_API/projects/paper/versions/$MC_VERSION/builds")"
BUILD="$(echo "$BUILDS" | jq -r '[.builds[] | select(.channel == "STABLE")] | last | .build // empty')"
[[ -n "$BUILD" ]] || { echo "Kein stabiler Paper-Build gefunden."; exit 1; }
JAR="$(echo "$BUILDS" | jq -r --argjson b "$BUILD" '.builds[] | select(.build == $b) | .downloads.application.name')"
[[ -n "$JAR" && "$JAR" != "null" ]] || { echo "Paper-Dateiname fehlt."; exit 1; }

id "$RUN_USER" >/dev/null 2>&1 || useradd --system --create-home --home-dir "/home/$RUN_USER" --shell /usr/sbin/nologin "$RUN_USER"
install -d -o "$RUN_USER" -g "$RUN_USER" "$INSTALL_DIR/plugins" "$INSTALL_DIR/backups"
curl -fL "$PAPER_API/projects/paper/versions/$MC_VERSION/builds/$BUILD/downloads/$JAR" -o "$INSTALL_DIR/paper.jar"
curl -fL "https://download.geysermc.org/v2/projects/geyser/versions/latest/builds/latest/downloads/spigot" -o "$INSTALL_DIR/plugins/Geyser-Spigot.jar"
curl -fL "https://download.geysermc.org/v2/projects/floodgate/versions/latest/builds/latest/downloads/spigot" -o "$INSTALL_DIR/plugins/floodgate-spigot.jar"

cat > "$INSTALL_DIR/start.sh" <<EOF
#!/usr/bin/env bash
set -euo pipefail
cd "$INSTALL_DIR"
exec "$JAVA_BIN" -Xms1G -Xmx${RAM_GB}G -XX:+UseG1GC -XX:+ParallelRefProcEnabled -jar paper.jar --nogui
EOF
chmod 750 "$INSTALL_DIR/start.sh"
cat > "$INSTALL_DIR/server.properties" <<'EOF'
motd=§6NEXUS-BAY §f| Java + Bedrock
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
tar -czf "$D/backups/nexus-bay-$(date +%Y%m%d-%H%M%S).tar.gz" -C "$D" world world_nether world_the_end server.properties plugins
EOF
chmod 750 "$INSTALL_DIR/backup.sh"
chown -R "$RUN_USER:$RUN_USER" "$INSTALL_DIR"
cat > "/etc/systemd/system/$SERVICE.service" <<EOF
[Unit]
Description=NEXUS-BAY Minecraft Server
After=network.target
[Service]
User=$RUN_USER
Group=$RUN_USER
WorkingDirectory=$INSTALL_DIR
ExecStart=$INSTALL_DIR/start.sh
Restart=on-failure
RestartSec=10
KillSignal=SIGINT
TimeoutStopSec=60
NoNewPrivileges=true
PrivateTmp=true
[Install]
WantedBy=multi-user.target
EOF
systemctl daemon-reload
systemctl enable "$SERVICE"
echo
echo "Dateien installiert: Paper $MC_VERSION Build $BUILD"
echo "EULA prüfen/akzeptieren: nano $INSTALL_DIR/eula.txt"
echo "Danach starten: systemctl start $SERVICE"
echo "Firewall: TCP 25565 und UDP 19132 freigeben."
echo "Java: VPS-IP:25565 | Bedrock: VPS-IP Port 19132"
