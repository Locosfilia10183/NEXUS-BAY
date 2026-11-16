# NEXUS-BAY

Independent pirate-themed Minecraft Java + Bedrock RPG server for an Ubuntu VPS. This project is separate from NEXUSCRAFT and does not modify it.

## Current core release
- Paper server with Geyser + Floodgate cross-play
- NEXUS-BAY Core plugin: persistent player levels, XP, Bay-Coins and player transfers
- Admin control GUI with world tools, status, broadcast and coin grant
- Read-only admin inventory inspection via `/bayinv <player>`
- Procedural starter islands with terrain, dock, lighthouse and outpost
- Island management: `/bayisland create <name>`, `/bayisland visit <name>`, `/bayisland list`
- Player profile: `/bayprofile`; coin transfer: `/baypay <player> <amount>`
- Pirate bounty contract: `/bayquest accept` starts a 10-monster contract; `/bayquest status` shows progress; `/bayquest abandon` cancels it. Completion grants 100 Bay-Coins and 50 XP. Progress persists across reconnects and restarts.
- Dedicated Linux user, systemd service and backup script

## Requirements
- Ubuntu 24.04 VPS recommended
- 2 CPU cores; 6–8 GB RAM recommended
- Root/sudo access
- Provider firewall: TCP 25565 and UDP 19132

## Install
```bash
sudo apt update && sudo apt install -y git
git clone https://github.com/Locosfilia10183/NEXUS-BAY.git
cd NEXUS-BAY
sudo bash install.sh
```

Leave the Minecraft version prompt empty to let the installer select the newest **stable Paper build** available from PaperMC. The installer installs Java 25, Maven, Paper, Geyser, Floodgate and builds the NEXUS-BAY Core plugin from this repository. It refuses to overwrite a non-empty install directory.

## First start
Read the Minecraft EULA before accepting it:
```bash
sudo nano /opt/nexus-bay/eula.txt
# only after reading and accepting the EULA, change eula=false to eula=true
sudo systemctl start nexus-bay
sudo systemctl status nexus-bay
```

## Update management

Successful NEXUS-BAY Core builds are versioned from `plugin/src/main/resources/plugin.yml`, packaged by GitHub Actions and published as server-update releases with an SHA-256 checksum and `update-manifest.json`. The Maven package version is checked against the plugin version before a release can be published.

The installer places the update utility at `/usr/local/bin/nexus-bay-update`:

```bash
sudo nexus-bay-update check
sudo nexus-bay-update install
```

- `check` reads the installed plugin version, finds the newest stable GitHub release and reports whether an update exists. It does not change the server.
- `install` downloads the matching release, verifies its SHA-256 and embedded plugin version, backs up the current JAR, replaces it and restarts the service. If the service fails to start, it restores the previous JAR.
- Update target defaults to `/opt/nexus-bay/plugins/NEXUS-BAY-Core.jar` and service `nexus-bay`. Advanced installs can override these with `NEXUS_BAY_DIR` and `NEXUS_BAY_SERVICE`.

This first update channel manages the NEXUS-BAY Core plugin only. Paper, Geyser, Floodgate and the full server installation remain unchanged; automated full-server upgrades will be added separately with dedicated backups and compatibility checks.
 
## Connect
- Java: `YOUR_VPS_IP:25565` (or a DNS name pointing to the VPS)
- Bedrock: DNS name or `YOUR_VPS_IP`, port `19132`

## Admin and player commands
- `/bayadmin` – admin control panel (OP / nexusbay.admin)
- `/bayinv <player>` – read-only online player inventory inspection
- `/bayisland create <name>` – create a starter island (admin)
- `/bayisland visit <name>` – teleport to an island
- `/bayisland list` – list islands
- `/bayprofile` – show RPG level, XP and coins
- `/baypay <player> <amount>` – transfer Bay-Coins
- `/bayquest accept|status|abandon` – take and track a repeatable pirate bounty contract

## Manage
```bash
sudo systemctl status nexus-bay
sudo systemctl restart nexus-bay
sudo journalctl -u nexus-bay -f
sudo /opt/nexus-bay/backup.sh
```

Open TCP 25565 and UDP 19132 in the VPS provider firewall. If UFW is already active, the installer adds both rules; it does not enable UFW or change SSH rules.

See [network guide](docs/NETWORK.md) and [gameplay roadmap](docs/ROADMAP.md). This is an evolving RPG foundation, not a finished One Piece clone: devil-fruit abilities, Haki combat, full questlines, crew wars, custom NPCs and a player marketplace remain future development systems. Paper plugins are used for cross-play; Forge/Fabric-only mods are not compatible with this Paper server.
