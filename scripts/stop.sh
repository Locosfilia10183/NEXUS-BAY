#!/usr/bin/env bash
set -euo pipefail

echo "Stoppe NEXUS-BAY Dienste..."
systemctl stop nexus-bay-lobby nexus-bay-survival 2>/dev/null || true
sleep 2
systemctl stop nexus-bay-proxy 2>/dev/null || true
echo "Gestoppt."
