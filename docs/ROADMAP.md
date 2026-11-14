# NEXUS-BAY – Roadmap

## Phase 0 – Network Foundation ✅
- [x] Velocity + Lobby + Survival + Geyser/Floodgate
- [x] MariaDB Schema
- [x] Systemd + install.sh + Management-Skripte
- [x] Theme & Docs

## Phase 1 – Core systems on the network ✅ (Code)
- [x] Core plugin: MariaDB-Anbindung (HikariCP) + YAML-Fallback
- [x] Shared player profile (level, XP, Bay-Coins) mit DB-Persistenz
- [x] Economy-Log bei Transfers
- [ ] LuckPerms live auf dem VPS konfigurieren (Beispieldatei vorhanden)
- [ ] Lobby-Spawn + Server-Selector (Plugin-seitig vorbereitet, Map/GUI später)
- [ ] Cross-server messaging (Velocity-Plugin separat)

## Phase 2 – Pirate progression & social ✅ (Code-Grundlage)
- [x] Crew-System: create, invite, accept, leave, info, bank deposit
- [x] Skill-Trees: swords, pistols, harpoons, crew (Unlock-Logik, noch ohne Combat-Effekte)
- [x] Quest/Logbuch: Seed-Quests, accept, list, Progress-API
- [ ] Klassen / Rollen (nächster Feinschliff)
- [ ] Combat-Effekte an freigeschaltete Skills koppeln

## Phase 3 – World & content (offen)
- [ ] Erweiterter Insel-/Archipel-Generator
- [ ] Struktur-Templates (Leuchtturm, Taverne, Schatzkammer, Portal, Schiffwrack)
- [ ] Erstes Dungeon / Boss
- [ ] Schatz- und Schlüssel-System

## Phase 4 – Economy & quality (offen)
- [ ] Spielermarkt / Auktionshaus
- [ ] Shops, Steuern, Anti-Dupe (Ledger-Tabelle existiert)
- [ ] Tab, Scoreboard, Hologramme, Chat-Format
- [ ] Performance- und Anti-Cheat-Baseline

## Phase 5 – Polish & scale (offen)
- [ ] Weitere Spielmodi (BedWars/SkyWars Layout-Slots)
- [ ] Große Builds & Biome
- [ ] Public-Release-Vorbereitung

## Hinweis
Phase 1–2 sind im **Repository als lauffähiger Plugin-Code** umgesetzt.  
Phase 3–5 brauchen Maps, Assets, Live-Tests auf dem VPS und können nicht rein aus dem GitHub-Repo „fertig“ sein.
