# NEXUS-BAY

**Eigenständiges Minecraft-Netzwerk** mit Java- und Bedrock-Crossplay.  
Maritime Fantasy / Piraten-Thema. Separate Installation – berührt keine andere NEXUSCRAFT-Installation.

## Architektur (Foundation)

```
Velocity Proxy (25565)
        │
        ├── Lobby      (Paper)
        └── Survival   (Paper)

Bedrock → Geyser + Floodgate
Daten   → MariaDB (Spieler, Economy, Ränge, Clans, Freunde)
```

BedWars, SkyWars und Events sind als Verzeichnis- und Systemd-Struktur vorbereitet und können schrittweise ausgebaut werden.

## Features der aktuellen Foundation

- Velocity + Paper Multi-Server Grundgerüst
- Java (TCP 25565) + Bedrock (UDP 19132) Crossplay
- NEXUS-BAY Core Plugin: Level, XP, Bay-Coins, Inseln, Admin-GUI
- LuckPerms + Vault vorbereitet
- MariaDB Schema für Profile, Transaktionen, Freunde, Clans, Moderation
- Systemd-Dienste mit automatischem Neustart
- Backup-, Status- und Healthcheck-Skripte
- Idempotentes Installationsskript

## Anforderungen

| Ressource     | Minimum     | Empfohlen      |
|---------------|-------------|----------------|
| OS            | Ubuntu 24.04| Ubuntu 24.04   |
| CPU           | 4 Kerne     | 6+ Kerne       |
| RAM           | 8 GB        | 12–16 GB       |
| Festplatte    | 40 GB SSD   | 80+ GB         |

## Installation

```bash
sudo apt update && sudo apt install -y git
git clone https://github.com/Locosfilia10183/NEXUS-BAY.git
cd NEXUS-BAY
sudo bash install.sh
```

Das Skript installiert Java, Velocity, Paper (Lobby + Survival), Geyser, Floodgate, MariaDB und baut das Core-Plugin.  
Es weigert sich, ein nicht-leeres Zielverzeichnis zu überschreiben.

Detaillierte Anleitung: [docs/INSTALLATION.md](docs/INSTALLATION.md)  
Architektur: [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md)  
Roadmap: [docs/ROADMAP.md](docs/ROADMAP.md)

## Nach der Installation

```bash
# EULA akzeptieren (nur nach Lesen der Minecraft EULA)
sudo nano /opt/nexus-bay/servers/lobby/eula.txt
sudo nano /opt/nexus-bay/servers/survival/eula.txt

sudo systemctl start nexus-bay-proxy nexus-bay-lobby nexus-bay-survival
sudo bash /opt/nexus-bay/scripts/status.sh
```

## Verbindung

- **Java:** `DEINE_VPS_IP:25565`
- **Bedrock:** `DEINE_VPS_IP` Port `19132`

## Wichtige Admin-Befehle (Core-Plugin)

| Befehl                    | Beschreibung                      |
|---------------------------|-----------------------------------|
| `/bayadmin`               | Admin-Kontrollpanel               |
| `/bayprofile`             | Eigenes RPG-Profil                |
| `/baypay <spieler> <betrag>` | Bay-Coins überweisen           |
| `/bayisland create <name>`| Insel erstellen (Admin)           |
| `/bayisland visit <name>` | Insel besuchen                    |
| `/bayinv <spieler>`       | Inventar einsehen (read-only)     |

## Management

```bash
sudo systemctl status nexus-bay-*
sudo journalctl -u nexus-bay-proxy -f
sudo /opt/nexus-bay/scripts/backup.sh
sudo /opt/nexus-bay/scripts/healthcheck.sh
sudo /opt/nexus-bay/scripts/restart.sh
```

## Sicherheit & Hinweise

- Keine Passwörter, Tokens oder API-Keys im Repository.
- Datenbank-Zugangsdaten werden lokal generiert.
- Firewall: TCP 25565 und UDP 19132 beim Provider freigeben.
- Das Core-Plugin speichert derzeit Spielerdaten noch dateibasiert; die MariaDB-Anbindung wird im nächsten Schritt vervollständigt.

## Roadmap (Auszug)

1. Vollständige MariaDB-Anbindung im Core-Plugin
2. Rangsystem über LuckPerms + zentrale Sync
3. Freundes- und Clansystem (UI + DB)
4. Lobby-Menüs und Server-Selector
5. Survival-Claims / Homes / Warps
6. BedWars- und SkyWars-Module
7. Monitoring und erweiterte Backups

Siehe [docs/ROADMAP.md](docs/ROADMAP.md) für Details.

---

NEXUS-BAY ist ein eigenständiges Projekt. Es teilt keine Verzeichnisse, Ports oder Datenbanken mit anderen Installationen.
