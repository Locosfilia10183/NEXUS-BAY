# NEXUS-BAY – Java only: Forge 1.20.1 + Mine Mine no Mi

**Kein Bedrock. Keine Addons. Nur der Kern-Mod.**

| | |
|--|--|
| Minecraft | **1.20.1** |
| Loader | **Forge** (empfohlen 47.4.28) |
| Mod | **Mine Mine no Mi** 1.20.1-0.11.5 |
| Port | **25565** |
| Server-Pfad | `/opt/nexus-bay-mod` |
| RAM | 4–6 GB |

## Inhalt

- 70+ Devil Fruits (Logia / Paramecia / Zoan)
- Haki (Buso, Kenbun, Hao)
- Kampfstile, Doriki, Kairoseki
- Marine/Pirate-Mobs, Waffen, Schiffe

## Installation auf dem VPS

```bash
# Alten Paper/Proxy-Stack stoppen
sudo systemctl stop nexus-bay nexus-bay-proxy nexus-bay-lobby nexus-bay-survival 2>/dev/null
sudo systemctl disable nexus-bay nexus-bay-proxy nexus-bay-lobby nexus-bay-survival 2>/dev/null

# Script aus dem Repo (oder Inhalt kopieren)
curl -fsSL -o /tmp/install-mmnm.sh \
  https://raw.githubusercontent.com/Locosfilia10183/NEXUS-BAY/network-foundation/forge/install-mmnm.sh
sudo bash /tmp/install-mmnm.sh

sudo systemctl enable --now nexus-bay-mod
sudo journalctl -u nexus-bay-mod -f
```

Falls der MMNM-Download fehlschlägt: JAR manuell von  
https://www.curseforge.com/minecraft/mc-mods/mine-mine-no-mi  
nach `/opt/nexus-bay-mod/mods/` legen.

## Client

1. CurseForge App oder Forge 1.20.1 installieren  
2. **Dieselbe** `mine-mine-no-mi-1.20.1-0.11.5.jar` in den Client-`mods`-Ordner  
3. Verbinden: `nexus-bay.duckdns.org` (Port 25565)

## Wichtig

- Paper-Plugins funktionieren hier **nicht**.
- Geyser/Floodgate **nicht** installieren.
- Keine weiteren One-Piece-Addons, solange „nur MMNM“ gewünscht ist.
