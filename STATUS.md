# NEXUS-BAY – Aktueller Stand

**Branch:** `network-foundation`  
**Stand:** Foundation abgeschlossen (Phase 0)

## Was fertig ist

### Infrastruktur
- Velocity Proxy + Lobby + Survival Server-Layout
- Geyser + Floodgate (Java + Bedrock Crossplay)
- MariaDB Schema (players, islands, economy_log, crews, skill_progress, quests)
- Systemd Units (proxy, lobby, survival)
- `install.sh` (idempotent, sicher)
- Management-Skripte: start / stop / restart / status / backup / restore / update / healthcheck
- LuckPerms Rang-Gruppen Beispiel + Dokumentation

### Core-Plugin (1.1.0-foundation)
- Spielerprofile (Level, XP, Bay-Coins)
- Coin-Transfers (`/baypay`)
- Insel-System (`/bayisland create|visit|list`)
- Admin-GUI (`/bayadmin`) + Inventar-Inspektion (`/bayinv`)
- DatabaseService (HikariCP) vorbereitet – aktivierbar über `config.yml`
- YAML-Fallback solange `database.enabled = false`

### Dokumentation
- THEME.md – Pirate Bay / Cursed Bay Vision
- INSPIRATION.md – Builds & Systeme aus den Referenzen
- ROADMAP.md – Phase 0 bis 5
- ARCHITECTURE.md, INSTALLATION.md, RANKS.md, COMMANDS.md

## Was bewusst noch nicht fertig ist (nächste Phasen)

- Volle MariaDB-Persistenz im Plugin (Profile + Islands live auf DB)
- Crew-System (UI + Befehle)
- Skill-Trees (Waffen + Crew)
- Quest- / Logbuch-System
- Server-Selector / Lobby-Menü
- Große vorgefertigte Maps (Leuchttürme, Tavernen, Schatzkammern usw.)
- Custom Models / erweiterte Combat-Systeme

## Nächster empfohlener Schritt
1. Branch `network-foundation` nach `main` mergen (oder PR reviewen)
2. Auf dem VPS testen: `git clone -b network-foundation ...` → `sudo bash install.sh`
3. Phase 1 starten: `database.enabled: true` + echte Persistenz im Plugin

## Wichtig
Dieses Repository ist die **technische Foundation** für ein Pirate-Bay-Netzwerk.  
Das fertige Spielerlebnis (Crews, Skilltrees, Dungeons, große Builds) kommt in den folgenden Phasen.
