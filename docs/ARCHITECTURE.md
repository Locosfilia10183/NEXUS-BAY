# NEXUS-BAY Netzwerk-Architektur

## Überblick

NEXUS-BAY ist ein eigenständiges Minecraft-Netzwerk mit Java- und Bedrock-Crossplay.
Es basiert auf Velocity (Proxy) und Paper (Backend-Server).

```
Internet
   │
   ├── TCP 25565  →  Velocity Proxy
   └── UDP 19132  →  Geyser (auf Proxy oder Lobby)
          │
          ├── Lobby      (Paper)
          ├── Survival   (Paper)
          ├── BedWars    (vorbereitet)
          ├── SkyWars    (vorbereitet)
          └── Events     (vorbereitet)
```

## Komponenten

| Komponente     | Technologie          | Port (intern) | Zweck                          |
|----------------|----------------------|---------------|--------------------------------|
| Proxy          | Velocity             | 25565         | Verbindung, Routing, Auth      |
| Lobby          | Paper                | 25566         | Spawn, Menüs, Navigation       |
| Survival       | Paper                | 25567         | Hauptspielmodus                |
| BedWars        | Paper (vorbereitet)  | 25568         | Minigame                       |
| SkyWars        | Paper (vorbereitet)  | 25569         | Minigame                       |
| Database       | MariaDB              | 3306          | Spielerdaten, Economy, Ränge   |
| Cache (opt.)   | Redis                | 6379          | Session, Cross-Server-Comm     |

## Verzeichnisstruktur auf dem VPS

```
/opt/nexus-bay/
├── proxy/                 # Velocity
├── servers/
│   ├── lobby/
│   ├── survival/
│   ├── bedwars/           # vorbereitet
│   └── skywars/           # vorbereitet
├── plugins/               # geteilte Plugins / Builds
├── config/                # zentrale Beispiel-Konfigs
├── database/              # Schema & Migrations
├── backups/
├── scripts/
├── systemd/
└── logs/
```

## Design-Prinzipien

1. **Modularität** – Jeder Spielmodus ist unabhängig start- und stoppbar.
2. **Crossplay** – Java + Bedrock über Geyser + Floodgate.
3. **Zentrale Daten** – MariaDB für Profile, Economy, Ränge, Clans, Freunde.
4. **Sicherheit** – Eigene Linux-User, Systemd-Isolation, keine Secrets im Repo.
5. **Idempotente Installation** – `install.sh` kann mehrfach laufen, ohne bestehende Daten zu zerstören.
6. **Erweiterbarkeit** – Neue Server einfach in Velocity und Systemd hinzufügen.

## Aktueller Stand (Foundation)

- Velocity + Lobby + Survival Grundgerüst
- Geyser/Floodgate Crossplay
- NEXUS-BAY Core Plugin (RPG, Inseln, Admin) auf den Backends
- Management-Skripte und Systemd-Units
- MariaDB-Schema vorbereitet
- LuckPerms + Vault als Standard-Permission/Economy-Brücke

Weitere Systeme (volle Clans, Freunde, BedWars-Arenen, Anti-Cheat-Integration, Monitoring) werden schrittweise ergänzt.
