#!/usr/bin/env bash
# NEXUS-BAY Network Installer
# Installiert Velocity Proxy + Lobby + Survival auf Ubuntu 24.04
# Idempotent und sicher: überschreibt keine bestehenden Installationen.
set -Eeuo pipefail

SERVICE_USER="nexusbay"
DEFAULT_DIR="/opt/nexus-bay"
PAPER_API="https://fill.papermc.io/v3"
UA="NEXUS-BAY/1.2 (https://github.com/Locosfilia10183/NEXUS-BAY)"
GEYSER_SPIGOT="https://download.geysermc.org/v2/projects/geyser/versions/latest/builds/latest/downloads/spigot"
FLOODGATE_SPIGOT="https://download.geysermc.org/v2/projects/floodgate/versions/latest/builds/latest/downloads/spigot"
GEYSER_VELOCITY="https://download.geysermc.org/v2/projects/geyser/versions/latest/builds/latest/downloads/velocity"
FLOODGATE_VELOCITY="https://download.geysermc.org/v2/projects/floodgate/versions/latest/builds/latest/downloads/velocity"

if [[ "$EUID" -ne 0 ]]; then
  echo "Bitte mit sudo ausführen: sudo bash install.sh"
  exit 1
fi

echo "=============================================="
echo "  NEXUS-BAY  |  Network Installer"
echo "  Velocity + Lobby + Survival + Crossplay"
echo "=============================================="
echo

# --- OS Check ---
if [[ -f /etc/os-release ]]; then
  . /etc/os-release
  if [[ "$ID" != "ubuntu" ]]; then
    echo "Warnung: Dieses Skript ist für Ubuntu optimiert (aktuell: $ID)."
  fi
fi

# --- Ressourcen ---
TOTAL_RAM_MB=$(free -m | awk '/^Mem:/{print $2}')
TOTAL_RAM_GB=$((TOTAL_RAM_MB / 1024))
echo "Erkannte RAM: ${TOTAL_RAM_GB} GB"
if [[ $TOTAL_RAM_GB -lt 6 ]]; then
  echo "Warnung: Weniger als 6 GB RAM. Für ein Netzwerk werden 8+ GB empfohlen."
fi

# --- Eingaben ---
read -rp "Minecraft-Version (leer = neueste stabile Paper): " MC_VERSION
if [[ -z "$MC_VERSION" ]]; then
  PROJECT=$(curl -fsSL -A "$UA" "$PAPER_API/projects/paper")
  mapfile -t VERSIONS < <(echo "$PROJECT" | jq -r '.versions | to_entries[] | .value[]' | sort -Vru)
  for candidate in "${VERSIONS[@]}"; do
    response=$(curl -fsSL -A "$UA" "$PAPER_API/projects/paper/versions/$candidate/builds") || continue
    stable=$(echo "$response" | jq -r '[.[] | select(.channel == "STABLE")] | first | .id // empty')
    if [[ -n "$stable" ]]; then
      MC_VERSION="$candidate"
      break
    fi
  done
  [[ -n "$MC_VERSION" ]] || { echo "Keine stabile Paper-Version gefunden."; exit 1; }
  echo "Gewählte Paper-Version: $MC_VERSION"
fi
[[ "$MC_VERSION" =~ ^[0-9]+\.[0-9]+(\.[0-9]+)?$ ]] || { echo "Ungültige Version."; exit 1; }

DEFAULT_RAM=$((TOTAL_RAM_GB > 10 ? 8 : 6))
read -rp "Gesamt-RAM für das Netzwerk in GB [$DEFAULT_RAM]: " RAM_GB
RAM_GB="${RAM_GB:-$DEFAULT_RAM}"
[[ "$RAM_GB" =~ ^[2-9][0-9]*$ ]] || { echo "Mindestens 2 GB angeben."; exit 1; }
if [[ $RAM_GB -gt $TOTAL_RAM_GB ]]; then
  echo "Abbruch: Angegebener RAM ($RAM_GB GB) überschreitet System-RAM ($TOTAL_RAM_GB GB)."
  exit 1
fi

read -rp "Installationsordner [$DEFAULT_DIR]: " INSTALL_DIR
INSTALL_DIR="${INSTALL_DIR:-$DEFAULT_DIR}"

if [[ -e "$INSTALL_DIR" ]] && [[ -n "$(find "$INSTALL_DIR" -mindepth 1 -maxdepth 1 -print -quit 2>/dev/null || true)" ]]; then
  echo "ABBRUCH: $INSTALL_DIR ist nicht leer. Es wird nichts überschrieben."
  echo "Lösche das Verzeichnis manuell oder wähle einen anderen Pfad."
  exit 1
fi

# RAM-Verteilung
PROXY_RAM=1
LOBBY_RAM=2
SURVIVAL_RAM=$((RAM_GB - PROXY_RAM - LOBBY_RAM))
if [[ $SURVIVAL_RAM -lt 2 ]]; then
  SURVIVAL_RAM=2
  LOBBY_RAM=$((RAM_GB - PROXY_RAM - SURVIVAL_RAM))
  [[ $LOBBY_RAM -lt 1 ]] && LOBBY_RAM=1
fi
echo "RAM-Verteilung → Proxy: ${PROXY_RAM}G | Lobby: ${LOBBY_RAM}G | Survival: ${SURVIVAL_RAM}G"

# --- Abhängigkeiten ---
export DEBIAN_FRONTEND=noninteractive
apt-get update
apt-get install -y ca-certificates curl gnupg jq git maven mariadb-server 2>/dev/null || true

# Java
if ! command -v java >/dev/null; then
  install -d -m 0755 /etc/apt/keyrings
  curl -fsSL https://packages.adoptium.net/artifactory/api/gpg/key/public | gpg --dearmor -o /etc/apt/keyrings/adoptium.gpg
  chmod 644 /etc/apt/keyrings/adoptium.gpg
  echo "deb [signed-by=/etc/apt/keyrings/adoptium.gpg] https://packages.adoptium.net/artifactory/deb $(. /etc/os-release && echo "$VERSION_CODENAME") main" > /etc/apt/sources.list.d/adoptium.list
  apt-get update
  apt-get install -y temurin-21-jdk
fi
JAVA_BIN=$(command -v java)
echo "Java: $($JAVA_BIN -version 2>&1 | head -1)"

# --- Benutzer ---
if ! id "$SERVICE_USER" >/dev/null 2>&1; then
  useradd --system --create-home --home-dir "/home/$SERVICE_USER" --shell /usr/sbin/nologin "$SERVICE_USER"
fi

# --- Verzeichnisse ---
mkdir -p "$INSTALL_DIR"/{proxy/plugins,servers/lobby/plugins,servers/survival/plugins,servers/bedwars,servers/skywars,config,database,backups,scripts,logs,systemd}

# --- Paper herunterladen ---
echo "Lade Paper $MC_VERSION..."
BUILDS=$(curl -fsSL -A "$UA" "$PAPER_API/projects/paper/versions/$MC_VERSION/builds")
BUILD=$(echo "$BUILDS" | jq -r '[.[] | select(.channel == "STABLE")] | first | .id // empty')
PAPER_URL=$(echo "$BUILDS" | jq -r '[.[] | select(.channel == "STABLE")] | first | .downloads["server:default"].url // empty')
if [[ -z "$BUILD" || -z "$PAPER_URL" ]]; then
  # Fallback: latest build of this version
  BUILD=$(echo "$BUILDS" | jq -r '.[0].id // empty')
  PAPER_URL=$(echo "$BUILDS" | jq -r '.[0].downloads["server:default"].url // empty')
fi
[[ -n "$BUILD" && -n "$PAPER_URL" ]] || { echo "Kein Paper-Build für $MC_VERSION."; exit 1; }

curl -fL -A "$UA" "$PAPER_URL" -o "$INSTALL_DIR/servers/lobby/paper.jar"
cp "$INSTALL_DIR/servers/lobby/paper.jar" "$INSTALL_DIR/servers/survival/paper.jar"
echo "Paper Build $BUILD geladen."

# --- Velocity (fill API: .versions is object grouped by family) ---
echo "Lade Velocity..."
VEL_PROJECT=$(curl -fsSL -A "$UA" "$PAPER_API/projects/velocity")
# Newest family first, then first version in that family (API lists newest first)
VEL_VERSION=$(echo "$VEL_PROJECT" | jq -r '.versions | to_entries[0] | .value[0] // empty')
[[ -n "$VEL_VERSION" ]] || { echo "Keine Velocity-Version gefunden."; exit 1; }
echo "Velocity-Version: $VEL_VERSION"

VEL_BUILDS=$(curl -fsSL -A "$UA" "$PAPER_API/projects/velocity/versions/$VEL_VERSION/builds")
# Prefer STABLE, else newest build
VEL_URL=$(echo "$VEL_BUILDS" | jq -r '[.[] | select(.channel == "STABLE")] | first | .downloads["server:default"].url // .downloads.application.url // empty')
if [[ -z "$VEL_URL" ]]; then
  VEL_URL=$(echo "$VEL_BUILDS" | jq -r '.[0].downloads["server:default"].url // .[0].downloads.application.url // empty')
fi
# Alternate key names used by some fill responses
if [[ -z "$VEL_URL" ]]; then
  VEL_URL=$(echo "$VEL_BUILDS" | jq -r '.[0].downloads | to_entries[0] | .value.url // empty')
fi
[[ -n "$VEL_URL" ]] || {
  echo "Velocity-Download-URL nicht gefunden. API-Antwort (Auszug):"
  echo "$VEL_BUILDS" | jq -c '.[0] // .' | head -c 500
  echo
  exit 1
}
curl -fL -A "$UA" "$VEL_URL" -o "$INSTALL_DIR/proxy/velocity.jar"
echo "Velocity geladen."

# --- Geyser / Floodgate ---
echo "Lade Geyser + Floodgate..."
curl -fL -A "$UA" "$GEYSER_VELOCITY" -o "$INSTALL_DIR/proxy/plugins/Geyser-Velocity.jar" || true
curl -fL -A "$UA" "$FLOODGATE_VELOCITY" -o "$INSTALL_DIR/proxy/plugins/floodgate-velocity.jar" || true
curl -fL -A "$UA" "$GEYSER_SPIGOT" -o "$INSTALL_DIR/servers/lobby/plugins/Geyser-Spigot.jar" || true
curl -fL -A "$UA" "$FLOODGATE_SPIGOT" -o "$INSTALL_DIR/servers/lobby/plugins/floodgate-spigot.jar" || true
curl -fL -A "$UA" "$FLOODGATE_SPIGOT" -o "$INSTALL_DIR/servers/survival/plugins/floodgate-spigot.jar" || true

# --- Core Plugin bauen ---
SCRIPT_DIR=$(cd "$(dirname "$0")" && pwd)
if [[ -f "$SCRIPT_DIR/plugin/pom.xml" ]]; then
  echo "Baue NEXUS-BAY Core Plugin..."
  if mvn -B -f "$SCRIPT_DIR/plugin/pom.xml" clean package -q; then
    if [[ -f "$SCRIPT_DIR/plugin/target/NEXUS-BAY-Core.jar" ]]; then
      cp "$SCRIPT_DIR/plugin/target/NEXUS-BAY-Core.jar" "$INSTALL_DIR/servers/lobby/plugins/"
      cp "$SCRIPT_DIR/plugin/target/NEXUS-BAY-Core.jar" "$INSTALL_DIR/servers/survival/plugins/"
      echo "Core-Plugin installiert."
    else
      echo "Warnung: JAR nicht gefunden nach Build."
    fi
  else
    echo "Warnung: Core-Plugin-Build fehlgeschlagen (Server laufen trotzdem ohne Custom-Plugin)."
  fi
fi

# --- Velocity Config ---
FORWARDING_SECRET=$(openssl rand -hex 16)
echo -n "$FORWARDING_SECRET" > "$INSTALL_DIR/proxy/forwarding.secret"

cat > "$INSTALL_DIR/proxy/velocity.toml" <<EOF
config-version = "2.7"
bind = "0.0.0.0:25565"
motd = "<gold>NEXUS-BAY</gold> <gray>|</gray> <aqua>Java + Bedrock</aqua>"
show-max-players = 200
online-mode = true
force-key-authentication = true
player-info-forwarding-mode = "modern"
forwarding-secret-file = "forwarding.secret"
try = ["lobby"]

[servers]
lobby = "127.0.0.1:25566"
survival = "127.0.0.1:25567"

[advanced]
compression-threshold = 256
login-ratelimit = 3000
connection-timeout = 5000
read-timeout = 30000
EOF

# --- Paper server.properties, eula, start.sh ---
for server in lobby survival; do
  PORT=25566
  [[ "$server" == "survival" ]] && PORT=25567

  cat > "$INSTALL_DIR/servers/$server/server.properties" <<EOF
motd=NEXUS-BAY $server
server-port=$PORT
server-ip=127.0.0.1
online-mode=false
max-players=80
view-distance=8
simulation-distance=6
enable-command-block=false
white-list=false
spawn-protection=0
EOF

  cat > "$INSTALL_DIR/servers/$server/eula.txt" <<EOF
# By changing the setting below to TRUE you are indicating your agreement to our EULA (https://aka.ms/MinecraftEULA).
eula=false
EOF

  mkdir -p "$INSTALL_DIR/servers/$server/config"
  cat > "$INSTALL_DIR/servers/$server/config/paper-global.yml" <<EOF
_version: 29
proxies:
  velocity:
    enabled: true
    online-mode: true
    secret: "$FORWARDING_SECRET"
EOF
done

cat > "$INSTALL_DIR/servers/lobby/start.sh" <<EOF
#!/usr/bin/env bash
set -euo pipefail
cd "\$(dirname "\$0")"
exec $JAVA_BIN -Xms1G -Xmx${LOBBY_RAM}G -XX:+UseG1GC -XX:+ParallelRefProcEnabled -XX:MaxGCPauseMillis=200 -jar paper.jar --nogui
EOF

cat > "$INSTALL_DIR/servers/survival/start.sh" <<EOF
#!/usr/bin/env bash
set -euo pipefail
cd "\$(dirname "\$0")"
exec $JAVA_BIN -Xms1G -Xmx${SURVIVAL_RAM}G -XX:+UseG1GC -XX:+ParallelRefProcEnabled -XX:MaxGCPauseMillis=200 -jar paper.jar --nogui
EOF

chmod 750 "$INSTALL_DIR/servers/lobby/start.sh" "$INSTALL_DIR/servers/survival/start.sh"

# --- MariaDB ---
echo "Konfiguriere MariaDB..."
systemctl enable --now mariadb 2>/dev/null || true
DB_PASS=$(openssl rand -base64 24 | tr -d '/+=' | head -c 24)
mysql -e "CREATE DATABASE IF NOT EXISTS nexusbay CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;" 2>/dev/null || true
mysql -e "CREATE USER IF NOT EXISTS 'nexusbay'@'localhost' IDENTIFIED BY '$DB_PASS';" 2>/dev/null || true
mysql -e "GRANT ALL PRIVILEGES ON nexusbay.* TO 'nexusbay'@'localhost'; FLUSH PRIVILEGES;" 2>/dev/null || true

if [[ -f "$SCRIPT_DIR/database/schema.sql" ]]; then
  mysql nexusbay < "$SCRIPT_DIR/database/schema.sql" 2>/dev/null || true
fi

mkdir -p "$INSTALL_DIR/config"
cat > "$INSTALL_DIR/config/secrets.env" <<EOF
# Generiert vom NEXUS-BAY Installer – nicht committen!
DB_HOST=localhost
DB_PORT=3306
DB_NAME=nexusbay
DB_USER=nexusbay
DB_PASSWORD=$DB_PASS
FORWARDING_SECRET=$FORWARDING_SECRET
EOF
chmod 600 "$INSTALL_DIR/config/secrets.env"

# --- Scripts kopieren ---
if [[ -d "$SCRIPT_DIR/scripts" ]]; then
  cp -a "$SCRIPT_DIR/scripts/." "$INSTALL_DIR/scripts/"
  chmod 750 "$INSTALL_DIR/scripts/"*.sh 2>/dev/null || true
fi

# --- Systemd ---
cat > /etc/systemd/system/nexus-bay-proxy.service <<EOF
[Unit]
Description=NEXUS-BAY Velocity Proxy
After=network-online.target
Wants=network-online.target

[Service]
Type=simple
User=$SERVICE_USER
Group=$SERVICE_USER
WorkingDirectory=$INSTALL_DIR/proxy
ExecStart=$JAVA_BIN -Xms512M -Xmx${PROXY_RAM}G -XX:+UseG1GC -jar velocity.jar
Restart=on-failure
RestartSec=8
KillSignal=SIGINT
TimeoutStopSec=30
NoNewPrivileges=true
PrivateTmp=true
ProtectSystem=strict
ReadWritePaths=$INSTALL_DIR/proxy $INSTALL_DIR/logs

[Install]
WantedBy=multi-user.target
EOF

cat > /etc/systemd/system/nexus-bay-lobby.service <<EOF
[Unit]
Description=NEXUS-BAY Lobby
After=network-online.target nexus-bay-proxy.service
Wants=network-online.target

[Service]
Type=simple
User=$SERVICE_USER
Group=$SERVICE_USER
WorkingDirectory=$INSTALL_DIR/servers/lobby
ExecStart=$INSTALL_DIR/servers/lobby/start.sh
Restart=on-failure
RestartSec=10
KillSignal=SIGINT
TimeoutStopSec=90
NoNewPrivileges=true
PrivateTmp=true
ProtectSystem=strict
ReadWritePaths=$INSTALL_DIR/servers/lobby $INSTALL_DIR/logs

[Install]
WantedBy=multi-user.target
EOF

cat > /etc/systemd/system/nexus-bay-survival.service <<EOF
[Unit]
Description=NEXUS-BAY Survival
After=network-online.target nexus-bay-proxy.service
Wants=network-online.target

[Service]
Type=simple
User=$SERVICE_USER
Group=$SERVICE_USER
WorkingDirectory=$INSTALL_DIR/servers/survival
ExecStart=$INSTALL_DIR/servers/survival/start.sh
Restart=on-failure
RestartSec=10
KillSignal=SIGINT
TimeoutStopSec=90
NoNewPrivileges=true
PrivateTmp=true
ProtectSystem=strict
ReadWritePaths=$INSTALL_DIR/servers/survival $INSTALL_DIR/logs

[Install]
WantedBy=multi-user.target
EOF

systemctl daemon-reload
systemctl enable nexus-bay-proxy nexus-bay-lobby nexus-bay-survival

# --- Rechte ---
chown -R "$SERVICE_USER:$SERVICE_USER" "$INSTALL_DIR"

# --- Firewall (nur wenn UFW aktiv) ---
if command -v ufw >/dev/null && ufw status 2>/dev/null | grep -q "Status: active"; then
  ufw allow 25565/tcp comment 'NEXUS-BAY Java' || true
  ufw allow 19132/udp comment 'NEXUS-BAY Bedrock' || true
fi

echo
echo "=============================================="
echo "  NEXUS-BAY Installation abgeschlossen"
echo "=============================================="
echo "Pfad:          $INSTALL_DIR"
echo "Paper:         $MC_VERSION (Build $BUILD)"
echo "Velocity:      $VEL_VERSION"
echo "Proxy RAM:     ${PROXY_RAM}G"
echo "Lobby RAM:     ${LOBBY_RAM}G"
echo "Survival RAM:  ${SURVIVAL_RAM}G"
echo
echo "Nächste Schritte:"
echo "1. EULA akzeptieren:"
echo "   sudo nano $INSTALL_DIR/servers/lobby/eula.txt"
echo "   sudo nano $INSTALL_DIR/servers/survival/eula.txt"
echo "2. Dienste starten:"
echo "   sudo systemctl start nexus-bay-proxy nexus-bay-lobby nexus-bay-survival"
echo "3. Status prüfen:"
echo "   sudo bash $INSTALL_DIR/scripts/status.sh"
echo
echo "Java-Verbindung:    DEINE_IP:25565"
echo "Bedrock-Verbindung: DEINE_IP Port 19132"
echo
echo "Secrets liegen in: $INSTALL_DIR/config/secrets.env"
echo "Provider-Firewall: TCP 25565 + UDP 19132 freigeben."
