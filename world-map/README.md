# NEXUS-BAY World Map

A standalone, mobile-friendly Grand Line atlas for the NEXUS-BAY Minecraft world.

## Current features
- All 28 island destinations from the Forge `IslandCatalog`, including world X/Z anchors and recommended levels.
- Search and region filters (East Blue, Grand Line, New World).
- Selectable islands with terrain profile, level requirement and coordinates.
- Pan, zoom, 2D atlas and a lightweight 3D-tilt presentation.
- Optional real terrain samples and player markers from a server snapshot.
- Responsive layout for phones and desktop.

## Automatic server snapshot
The Forge mod now creates and refreshes `nexusbay-map.json` automatically every 20 seconds while the Overworld server is running. No in-game command or manual action is required.

The snapshot contains canonical island positions and levels, actual surface height/top-block samples around each island, and current Overworld player positions. Only already-loaded chunks are inspected through `getChunkNow`; unloaded chunks are skipped. No chunks are loaded, blocks placed, or terrain regenerated. The file is written atomically into the active world folder.

The website can read the optional `nexusbay-map.json` when that file is served beside `index.html`. A GitHub Pages site cannot directly read a file inside the Minecraft server's private world folder. Continuous display on the public website therefore still needs a secure read-only bridge from the Minecraft host to the website (or a server API/reverse proxy). Do not publish player coordinates publicly unless that is intended.

## GitHub Pages
The workflow `.github/workflows/deploy-world-map.yml` publishes the contents of this folder when changes are pushed to `forge-development`.

In repository settings, set **Pages → Build and deployment → Source: GitHub Actions** if Pages is not already enabled. After the first successful workflow run, GitHub Actions will expose the published page URL.

## Next live-server step
Connect the automatic snapshot to a read-only server endpoint so the map can refresh without manual file copying. Later layers can include ship, boss and NPC markers plus actual generated/explored chunk tiles. Keep all write/admin credentials server-side.
