# NEXUS-BAY – Roadmap

## Phase 0 – Network Foundation (current branch)
- [x] Velocity proxy + Lobby + Survival layout
- [x] Geyser / Floodgate (Java + Bedrock)
- [x] MariaDB schema (players, islands, economy, crews, quests placeholders)
- [x] Systemd units, install.sh, management scripts (start/stop/status/backup/restore/update/healthcheck)
- [x] LuckPerms rank examples + documentation
- [x] Theme & architecture docs

## Phase 1 – Core systems on the network
- [ ] Core plugin: MariaDB connection (replace pure YAML persistence)
- [ ] Shared player profile across Lobby + Survival (level, XP, Bay-Coins)
- [ ] LuckPerms fully configured (ranks from Spieler → Owner)
- [ ] Lobby spawn + basic server selector (Lobby ↔ Survival)
- [ ] Basic /spawn, /hub and cross-server messaging

## Phase 2 – Pirate progression & social
- [ ] Crew system (create, invite, ranks, crew bank, crew skill tree)
- [ ] Weapon mastery skill trees (Säbel, Büchsen, Pistolen, Harpunen)
- [ ] Quest / Logbook framework (daily, weekly, crew, story)
- [ ] Player classes / roles (simple first version)

## Phase 3 – World & content
- [ ] Improved island / archipelago generator (tropical + cursed/ash themes)
- [ ] Structure templates: Lighthouse, Tavern, Treasure chamber, Pirate portal, Shipwreck dock
- [ ] First dungeon / boss instance
- [ ] Treasure and key system

## Phase 4 – Economy & quality
- [ ] Player market / auction house
- [ ] Shops + taxes + anti-dupe ledger
- [ ] Tab, scoreboard, holograms, better chat formatting
- [ ] Performance & anti-cheat baseline

## Phase 5 – Polish & scale
- [ ] Additional game modes (BedWars / SkyWars placeholders already in layout)
- [ ] More biomes and large builds
- [ ] Public release preparation, rules, Discord integration

## Design notes
Theme: Pirate Bay / Cursed Bay (see `docs/THEME.md`).  
All custom combat systems stay Paper-plugin compatible so Java + Bedrock players can play together.
