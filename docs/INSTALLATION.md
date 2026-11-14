# Installation – NEXUS-BAY Netzwerk

## Voraussetzungen

- Ubuntu 24.04 LTS (empfohlen)
- Mindestens 4 CPU-Kerne, 8–16 GB RAM (für Proxy + Lobby + Survival)
- Root/sudo
- Offene Ports beim Provider:
  - TCP 25565 (Java)
  - UDP 19132 (Bedrock)

## Schnellstart

```bash
sudo apt update && sudo apt install -y git
git clone https://github.com/Locosfilia10183/NEXUS-BAY.git
cd NEXUS-BAY
sudo bash install.sh
```

Das Skript fragt interaktiv nach:

- Minecraft-Version (leer = neueste stabile Paper)
- Gesamt-RAM, der dem Netzwerk zugewiesen werden darf
- Installationspfad (Standard: `/opt/nexus-bay`)

## Was installiert wird

1. Java (Temurin 21 oder 25, je nach Paper-Version)
2. Velocity Proxy
3. Paper für Lobby und Survival
4. Geyser + Floodgate
5. MariaDB (lokale Instanz + Schema)
6. NEXUS-BAY Core Plugin (gebaut aus diesem Repo)
7. LuckPerms + Vault (heruntergeladen)
8. Systemd-Dienste
9. Firewall-Regeln (nur wenn UFW aktiv)

## Nach der Installation

```bash
# EULA akzeptieren (nur nach Lesen)
sudo nano /opt/nexus-bay/servers/lobby/eula.txt
sudo nano /opt/nexus-bay/servers/survival/eula.txt

# Dienste starten
sudo systemctl start nexus-bay-proxy
sudo systemctl start nexus-bay-lobby
sudo systemctl start nexus-bay-survival

# Status prüfen
sudo bash /opt/nexus-bay/scripts/status.sh
```

## Verbindung

- **Java:** `DEINE_VPS_IP:25565`
- **Bedrock:** `DEINE_VPS_IP` Port `19132`

## Wichtige Befehle

```bash
sudo systemctl status nexus-bay-*
sudo journalctl -u nexus-bay-proxy -f
sudo /opt/nexus-bay/scripts/backup.sh
sudo /opt/nexus-bay/scripts/healthcheck.sh
```

## Hinweise

- Das Installationsskript überschreibt **niemals** ein nicht-leeres Installationsverzeichnis.
- Alle Secrets (Datenbank-Passwörter) werden lokal generiert und in `/opt/nexus-bay/config/secrets.env` abgelegt (nicht im Git).
- BedWars und SkyWars sind als Verzeichnis-Struktur und Systemd-Templates vorbereitet, aber noch nicht als lauffähige Arenen implementiert.
