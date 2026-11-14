# NEXUS-BAY – Wichtige Befehle

## System-Verwaltung (Linux)

```bash
# Status aller Dienste
sudo bash /opt/nexus-bay/scripts/status.sh

# Start / Stop / Restart
sudo bash /opt/nexus-bay/scripts/start.sh
sudo bash /opt/nexus-bay/scripts/stop.sh
sudo bash /opt/nexus-bay/scripts/restart.sh

# Backup & Restore
sudo bash /opt/nexus-bay/scripts/backup.sh
sudo bash /opt/nexus-bay/scripts/restore.sh <backup-datei.tar.gz>

# Healthcheck
sudo bash /opt/nexus-bay/scripts/healthcheck.sh

# Logs
sudo journalctl -u nexus-bay-proxy -f
sudo journalctl -u nexus-bay-lobby -f
sudo journalctl -u nexus-bay-survival -f
```

## Core-Plugin (Ingame)

| Befehl | Permission | Beschreibung |
|--------|------------|--------------|
| `/bayadmin` | `nexusbay.admin` | Admin-Kontrollpanel |
| `/bayprofile` | – | Eigenes Level, XP, Coins |
| `/baypay <spieler> <betrag>` | – | Bay-Coins überweisen |
| `/bayisland create <name>` | `nexusbay.admin` | Insel erstellen |
| `/bayisland visit <name>` | – | Insel besuchen |
| `/bayisland list` | – | Inseln auflisten |
| `/bayinv <spieler>` | `nexusbay.admin` | Inventar einsehen (read-only) |

## Velocity (Proxy)

Spieler können später mit einem Server-Selector oder Befehl zwischen Lobby und Survival wechseln.
Aktuell leitet der Proxy standardmäßig in die Lobby.

## LuckPerms (nach Installation)

```text
/lp user <name> parent set admin
/lp user <name> parent settemp vip 30d
/lp group list
```
