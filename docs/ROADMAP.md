# NEXUS-BAY – Entwicklungsroadmap

## Phase 0 – Foundation (aktuell auf Branch `network-foundation`)

- [x] Velocity Proxy Grundgerüst
- [x] Paper Lobby + Survival
- [x] Geyser + Floodgate Crossplay (Proxy + Backend)
- [x] Modern Forwarding (Velocity ↔ Paper)
- [x] NEXUS-BAY Core Plugin (Level, XP, Bay-Coins, Inseln, Admin-GUI)
- [x] MariaDB Schema (Spieler, Transaktionen, Freunde, Clans, Moderation)
- [x] Systemd-Dienste + Management-Skripte
- [x] Idempotentes install.sh
- [x] Deutsche Dokumentation

## Phase 1 – Kernsysteme (nächster Schritt)

- [ ] Core-Plugin an MariaDB anbinden (statt config.yml)
- [ ] LuckPerms installieren und Ranggruppen anlegen (Spieler → Owner)
- [ ] Vault + Economy-Bridge für Bay-Coins
- [ ] Lobby-Scoreboard + Tablist + Willkommensnachricht
- [ ] Server-Selector (Kompass / NPC / Menü)
- [ ] Befehl `/server lobby|survival`

## Phase 2 – Soziale Systeme & Survival

- [ ] Freundesystem (Anfragen, Liste, Online-Status)
- [ ] Clansystem (Erstellen, Ränge, Chat, Tag)
- [ ] Private Nachrichten + Chatfilter
- [ ] Survival: Homes, Warps, RTP, Claims (vorbereiten)
- [ ] Daily Rewards

## Phase 3 – Spielmodi

- [ ] BedWars-Modul (Arenen, Teams, Generatoren, Shop)
- [ ] SkyWars-Modul (Solo/Team, Loot)
- [ ] Events-System (zeitlich begrenzte Modi)

## Phase 4 – Administration & Sicherheit

- [ ] Erweiterte Moderation (Ban, Mute, Warn, Report) mit DB-Historie
- [ ] Staff-Chat, Vanish, Audit-Log
- [ ] Rate-Limits und Connection-Limits am Proxy
- [ ] Anti-Cheat-Integration (kompatible Lösung, Bedrock berücksichtigen)
- [ ] Wartungsmodus

## Phase 5 – Betrieb

- [ ] Automatische Backup-Rotation + Restore-Skript
- [ ] Healthcheck erweitern (DB, Geyser, Plugin-Status)
- [ ] Monitoring (Prometheus/Grafana oder einfaches Status-Dashboard)
- [ ] GitHub Actions für Plugin-Build + Config-Validierung

## Wichtige Regeln

- Kein Feature wird als „fertig“ markiert, bevor es getestet und dokumentiert ist.
- Keine Secrets im Repository.
- BedWars/SkyWars werden modular vorbereitet und erst implementiert, wenn die Foundation stabil läuft.
- Das Projekt bleibt unabhängig von jeder anderen Installation.
