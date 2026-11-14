#!/usr/bin/env bash
set -euo pipefail

bash "$(dirname "$0")/stop.sh"
sleep 3
bash "$(dirname "$0")/start.sh"
