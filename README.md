# NEXUS-BAY

Fresh Minecraft Java + Bedrock cross-play server for an Ubuntu VPS.

## Included
- Paper Minecraft server
- Geyser-Spigot + Floodgate-Spigot for Bedrock cross-play
- Dedicated Linux user and systemd service
- Separate installation directory, backup helper, and network guide

This is a **new, separate server**. It does not modify NEXUSCRAFT or any existing Minecraft server.

## Requirements
- Ubuntu 22.04/24.04 VPS
- 2 CPU cores and 4 GB RAM minimum (6–8 GB recommended)
- Root/sudo access
- TCP 25565 and UDP 19132 open in the VPS firewall

## Install
```bash
sudo apt update && sudo apt install -y git
git clone https://github.com/Locosfilia10183/NEXUS-BAY.git
cd NEXUS-BAY
sudo bash install.sh
```

The installer asks for the Minecraft version and RAM, downloads Paper, Geyser and Floodgate, and creates a systemd service. Choose a Paper-supported version compatible with Java 21.

## First start
After installation, accept the Minecraft EULA only after reading it:
```bash
sudo nano /opt/nexus-bay/eula.txt
# change eula=false to eula=true, save
sudo systemctl start nexus-bay
```

## Connect
- Java: `YOUR_VPS_IP:25565`
- Bedrock: `YOUR_VPS_IP`, port `19132`

## Manage
```bash
sudo systemctl status nexus-bay
sudo systemctl restart nexus-bay
sudo journalctl -u nexus-bay -f
sudo /opt/nexus-bay/backup.sh
```

The installer refuses to overwrite a non-empty target directory. Backups are stored in `/opt/nexus-bay/backups`.
