#!/usr/bin/env bash
# NEXUS-BAY – Forge 1.20.1 + Mine Mine no Mi ONLY
# Java only · no Bedrock · no addons
set -euo pipefail

INSTALL_DIR="${INSTALL_DIR:-/opt/nexus-bay-mod}"
FORGE_VER="1.20.1-47.4.28"
MMNM_FILE_ID="8648285"   # mine-mine-no-mi-1.20.1-0.11.5
RAM_XMS="${RAM_XMS:-4G}"
RAM_XMX="${RAM_XMX:-6G}"

echo "=== NEXUS-BAY Forge + Mine Mine no Mi ==="
echo "Ziel: $INSTALL_DIR"

sudo mkdir -p "$INSTALL_DIR/mods"
cd "$INSTALL_DIR"

# Java 17+ (Forge 1.20.1)
if ! command -v java >/dev/null; then
  sudo apt-get update -qq
  sudo apt-get install -y openjdk-17-jre-headless curl unzip
fi

# Forge server installer
if [[ ! -f "libraries/net/minecraftforge/forge/$FORGE_VER/forge-$FORGE_VER-server.jar" ]] && [[ ! -f "run.sh" ]]; then
  echo "Lade Forge $FORGE_VER …"
  curl -fsSL -o forge-installer.jar \
    "https://maven.minecraftforge.net/net/minecraftforge/forge/$FORGE_VER/forge-$FORGE_VER-installer.jar"
  java -jar forge-installer.jar --installServer
  rm -f forge-installer.jar
fi

# Mine Mine no Mi only
echo "Lade Mine Mine no Mi 1.20.1-0.11.5 …"
curl -fsSL -o "mods/mine-mine-no-mi-1.20.1-0.11.5.jar" \
  "https://www.curseforge.com/api/v1/mods/78726/files/${MMNM_FILE_ID}/download" \
  || curl -fsSL -o "mods/mine-mine-no-mi-1.20.1-0.11.5.jar" \
  "https://edge.forgecdn.net/files/8648/285/mine-mine-no-mi-1.20.1-0.11.5.jar" \
  || echo "WARN: MMNM Download fehlgeschlagen – JAR manuell nach mods/ legen"

# EULA
echo "eula=true" > eula.txt

# server.properties (Java only)
if [[ ! -f server.properties ]]; then
cat > server.properties <<'EOF'
server-port=25565
motd=NEXUS-BAY | Mine Mine no Mi 1.20.1
max-players=40
online-mode=true
view-distance=8
simulation-distance=6
spawn-protection=0
allow-flight=true
difficulty=hard
gamemode=survival
EOF
fi

# start script
cat > start.sh <<EOF
#!/bin/bash
cd "$(dirname "\$0")"
if [[ -f run.sh ]]; then
  export JAVA_ARGS="-Xms${RAM_XMS} -Xmx${RAM_XMX}"
  exec ./run.sh nogui
elif [[ -f forge-${FORGE_VER}-*.jar ]]; then
  exec java -Xms${RAM_XMS} -Xmx${RAM_XMX} @user_jvm_args.txt @libraries/net/minecraftforge/forge/${FORGE_VER}/unix_args.txt nogui
else
  echo "Forge run script not found"
  exit 1
fi
EOF
chmod +x start.sh

# systemd
sudo tee /etc/systemd/system/nexus-bay-mod.service >/dev/null <<EOF
[Unit]
Description=NEXUS-BAY Forge 1.20.1 + Mine Mine no Mi
After=network.target

[Service]
Type=simple
User=root
WorkingDirectory=${INSTALL_DIR}
ExecStart=${INSTALL_DIR}/start.sh
Restart=on-failure
RestartSec=15

[Install]
WantedBy=multi-user.target
EOF

sudo systemctl daemon-reload
echo ""
echo "Fertig. Mods in: $INSTALL_DIR/mods"
ls -la "$INSTALL_DIR/mods" || true
echo ""
echo "Start:  sudo systemctl start nexus-bay-mod"
echo "Status: sudo systemctl status nexus-bay-mod"
echo "Log:    sudo journalctl -u nexus-bay-mod -f"
echo "IP:     nexus-bay.duckdns.org:25565"
echo ""
echo "Client: Forge 1.20.1 + dieselbe MMNM-JAR"
