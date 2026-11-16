# NEXUS-BAY World Map

A standalone, mobile-friendly Grand Line atlas for the NEXUS-BAY Minecraft world.

## Current features
- All 28 island destinations from the Forge `IslandCatalog`, including world X/Z anchors and recommended levels.
- Search and region filters (East Blue, Grand Line, New World).
- Selectable islands with terrain profile, level requirement and coordinates.
- Pan, zoom, 2D atlas and a lightweight 3D-tilt presentation.
- Optional real terrain samples and player markers from a server snapshot.
- Responsive layout for phones and desktop.

## GitHub Pages
The workflow `.github/workflows/deploy-world-map.yml` publishes the contents of this folder when changes are pushed to `forge-development`.

In repository settings, set **Pages → Build and deployment → Source: GitHub Actions** if Pages is not already enabled. After the first successful workflow run, GitHub Actions will expose the published page URL.

## Read-only server snapshot

The Forge mod provides the admin command:

```text
/nexusmap export
```

It writes `nexusbay-map.json` into the active world folder. The export contains:
- canonical island positions and required levels
- actual surface height and top-block ID at sampled positions around each island
- current Overworld player names and coordinates
- counts for sampled loaded terrain and skipped unloaded terrain

**Safety:** this exporter only calls `getChunkNow` and reads already-loaded chunks. It does not request chunk loads, place blocks, edit chunk data, or regenerate terrain. Unloaded chunks are deliberately omitted. Run it again to create a newer snapshot.

To display that snapshot in the website, copy `nexusbay-map.json` into this `world-map/` folder and publish the updated Pages site. The page automatically loads the optional file and overlays the real surface samples and player markers. This is a manual snapshot workflow, not continuous live streaming.

## Next live-server step
For continuous updates without manually copying files, the Forge server still needs a read-only HTTP telemetry endpoint (for example `/api/world-map`) and the website needs a configured server URL. That endpoint can later expose online state, player positions, ships, boss markers and generated/explored chunk tiles. Keep any write/admin credentials server-side; never put them in this static page. Actual terrain should come from chunk data, never guessed coastlines.
