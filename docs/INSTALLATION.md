# NEXUS-BAY Installation

## Voraussetzungen
- Ubuntu 24.04 (empfohlen)
- Mindestens 6–8 GB RAM für das volle Netzwerk
- Root / sudo
- Offene Ports: **TCP 25565** (Java) und **UDP 19132** (Bedrock)

## Installation

```bash
sudo apt update && sudo apt install -y git
git clone -b network-foundation https://github.com/Locosfilia10183/NEXUS-BAY.git
cd NEXUS-BAY
sudo bash install.sh
```

Das Skript:
- installiert Java 21, Maven, MariaDB (falls nötig)
- lädt Velocity + Paper + Geyser/Floodgate
- baut das Core-Plugin
- legt Verzeichnisse, Configs, Systemd-Units und Secrets an
- überschreibt **keine** bestehende Installation

## Nach der Installation

1. EULA akzeptieren:
   ```bash
   sudo nano /opt/nexus-bay/servers/lobby/eula.txt
   sudo nano /opt/nexus-bay/servers/survival/eula.txt
   # eula=true setzen
   ```

2. Dienste starten:
   ```bash
   sudo systemctl start nexus-bay-proxy nexus-bay-lobby nexus-bay-survival
   ```

3. Status prüfen:
   ```bash
   sudo bash /opt/nexus-bay/scripts/status.sh
   ```

## Wichtige Pfade
| Pfad | Inhalt |
|------|--------|
| `/opt/nexus-bay/proxy` | Velocity |
| `/opt/nexus-bay/servers/lobby` | Lobby-Paper |
| `/opt/nexus-bay/servers/survival` | Survival-Paper |
| `/opt/nexus-bay/config/secrets.env` | DB-Passwort & Forwarding-Secret |
| `/opt/nexus-bay/scripts/` | start / stop / backup / restore / update |

## Datenbank aktivieren (Phase 1)

1. Schema ist bereits bei der Installation geladen.
2. In der Plugin-Config (`plugins/NEXUS-BAY-Core/config.yml`):
   ```yaml
   database:
     enabled: true
     password: "..."   # aus secrets.env
   ```
3. Server neu starten.

## Updates
```bash
sudo bash /opt/nexus-bay/scripts/update.sh
```
