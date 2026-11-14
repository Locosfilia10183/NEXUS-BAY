#!/usr/bin/env bash
set -euo pipefail

echo "Starte NEXUS-BAY Dienste..."
systemctl start nexus-bay-proxy
sleep 2
systemctl start nexus-bay-lobby
systemctl start nexus-bay-survival
echo "Fertig. Status:"
bash "$(dirname "$0")/status.sh"
