# NEXUS-BAY Architecture

```
                    Internet
                       │
              ┌────────▼────────┐
              │  Velocity Proxy │  :25565 (Java)
              │  + Geyser       │  :19132 (Bedrock)
              └────────┬────────┘
                       │ modern forwarding
         ┌─────────────┼─────────────┐
         │             │             │
   ┌─────▼─────┐ ┌─────▼─────┐ ┌─────▼─────┐
   │   Lobby   │ │ Survival  │ │  (später) │
   │  :25566   │ │  :25567   │ │ BedWars…  │
   └─────┬─────┘ └─────┬─────┘ └───────────┘
         │             │
         └──────┬──────┘
                │
         ┌──────▼──────┐
         │   MariaDB   │  shared profiles, economy,
         │  nexusbay   │  crews, quests, skills
         └─────────────┘
```

## Komponenten

| Komponente | Rolle |
|------------|------|
| Velocity | Proxy, Server-Routing, Auth (online-mode) |
| Paper Lobby | Spawn, Server-Selector, Social Hub |
| Paper Survival | Hauptspielwelt, Inseln, Economy |
| Geyser + Floodgate | Bedrock-Unterstützung |
| MariaDB | Zentrale Persistenz |
| NEXUS-BAY Core | Profile, Coins, Inseln, Admin, (später Crews/Quests) |
| LuckPerms | Ränge & Permissions (Proxy + Backend) |
| Systemd | Prozessverwaltung |

## Sicherheit
- Backends nur auf `127.0.0.1` gebunden
- Velocity modern forwarding + secret
- Secrets in `config/secrets.env` (chmod 600)
- Eigener System-User `nexusbay`
- Systemd: `NoNewPrivileges`, `ProtectSystem=strict`

## Erweiterbarkeit
Neue Server (BedWars, SkyWars, Dungeons) werden einfach als weitere Paper-Instanzen unter Velocity registriert und können dieselbe Datenbank und dieselben Permissions nutzen.
