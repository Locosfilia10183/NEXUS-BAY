# NEXUS-BAY

**Pirate-themed Minecraft network** (Java + Bedrock) for Ubuntu VPS.  
Independent project – not related to NEXUSCRAFT.

Theme: Caribbean bays, cursed islands, crews, skill trees, quests and exploration.  
See [`docs/THEME.md`](docs/THEME.md) and [`docs/ROADMAP.md`](docs/ROADMAP.md).

## Current status (branch `network-foundation`)

Network foundation is in place:

- Velocity proxy + Lobby + Survival
- Geyser + Floodgate (Java & Bedrock)
- MariaDB schema prepared
- Systemd services + full management scripts
- Core plugin (profiles, coins, islands, admin GUI)
- LuckPerms rank examples
- Theme & architecture documentation

This is still a **foundation**, not the finished pirate RPG.

## Requirements
- Ubuntu 24.04 recommended
- 6–8 GB+ RAM for the full network
- Root/sudo
- Firewall: TCP 25565 + UDP 19132

## Install (from this branch)
```bash
sudo apt update && sudo apt install -y git
git clone -b network-foundation https://github.com/Locosfilia10183/NEXUS-BAY.git
cd NEXUS-BAY
sudo bash install.sh
```

The installer sets up proxy, lobby, survival, database, plugins and systemd units. It refuses to overwrite a non-empty install directory.

## After install
1. Accept EULA on lobby + survival
2. `sudo systemctl start nexus-bay-proxy nexus-bay-lobby nexus-bay-survival`
3. `sudo bash /opt/nexus-bay/scripts/status.sh`

## Connect
- Java: `YOUR_IP:25565`
- Bedrock: `YOUR_IP` port `19132`

## Key commands
| Command | Description |
|---------|-------------|
| `/bayadmin` | Admin panel |
| `/bayprofile` | Level, XP, Coins |
| `/baypay <player> <amount>` | Transfer coins |
| `/bayisland create/visit/list` | Island tools |
| `/bayinv <player>` | Read-only inventory view |

## Docs
- [Theme](docs/THEME.md)
- [Roadmap](docs/ROADMAP.md)
- [Architecture](docs/ARCHITECTURE.md)
- [Installation](docs/INSTALLATION.md)
- [Ranks](docs/RANKS.md)
- [Commands](docs/COMMANDS.md)
- [Inspiration](docs/INSPIRATION.md)
