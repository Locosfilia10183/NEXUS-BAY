# NEXUS-BAY – Status

**Branch:** `network-foundation`  
**Plugin:** `1.2.0-phase2`

## Erledigt im Repository

### Infrastruktur (Phase 0)
- Velocity / Lobby / Survival / Geyser+Floodgate Layout
- MariaDB Schema (players, islands, economy_log, crews, skill_progress, quests)
- Systemd + install.sh + Backup/Restore/Update/Healthcheck

### Plugin-Systeme (Phase 1 + 2 Code)
- **PlayerData** mit MariaDB-Persistenz und YAML-Fallback
- **CrewService**: create / invite / accept / leave / info / deposit
- **SkillService**: swords, pistols, harpoons, crew – list + unlock
- **QuestService**: Seed-Quests, list, accept, Progress-API
- DatabaseService (HikariCP)
- Admin-GUI, Inseln, Economy-Transfers + Ledger

### Docs
THEME, INSPIRATION, ARCHITECTURE, INSTALLATION, RANKS, COMMANDS, ROADMAP

## Bewusst offen (braucht VPS + Assets)
- LuckPerms live konfigurieren
- Lobby-Map + Server-Selector-GUI
- Combat-Effekte für Skills
- Weltgenerator / große Pirate-Builds (Leuchttürme, Tavernen, …)
- Dungeons, Marktplatz, weitere Spielmodi
- Echte End-to-End-Tests auf deinem Server

## So testen
```bash
git clone -b network-foundation https://github.com/Locosfilia10183/NEXUS-BAY.git
cd NEXUS-BAY
sudo bash install.sh
# eula=true setzen, dann:
sudo systemctl start nexus-bay-proxy nexus-bay-lobby nexus-bay-survival
```
In `plugins/NEXUS-BAY-Core/config.yml`: `database.enabled: true` + Passwort aus `secrets.env`.

## Ehrliche Grenze
Ein vollständiges öffentliches Pirate-RPG mit Maps, Custom Models und allen Spielmodi ist kein reines Repo-Projekt.  
**Was hier fertig ist:** installierbare Netzwerk-Foundation + lauffähige Kernsysteme (Profile, Economy, Crews, Skills, Quests) im Code.
