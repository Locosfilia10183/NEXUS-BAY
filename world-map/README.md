# NEXUS-BAY World Map

A standalone, mobile-friendly Grand Line atlas for the NEXUS-BAY Minecraft world.

## Current features
- All 28 island destinations from the Forge `IslandCatalog`, including their world X/Z anchors and recommended levels.
- Search and region filters (East Blue, Grand Line, New World).
- Selectable islands with biome/terrain profile, level requirement and coordinates.
- Pan, zoom, 2D atlas and a lightweight 3D-tilt presentation.
- Responsive layout for phones and desktop.

## GitHub Pages
The workflow `.github/workflows/deploy-world-map.yml` publishes the contents of this folder when changes are pushed to `forge-development`.

In repository settings, set **Pages → Build and deployment → Source: GitHub Actions** if Pages is not already enabled. After the first successful workflow run, GitHub Actions will expose the published page URL.

## Live Minecraft server data
The current page is an interactive atlas built from the canonical island catalogue. It does not yet claim to show live player positions, ships, boss locations, or generated chunk shapes.

To enable those live layers, the Forge server needs to publish a read-only, authenticated telemetry endpoint (for example `/api/world-map`) containing:
- world/dimension and server online state
- player UUID/display name and current X/Z
- ship/entity markers with type and coordinates
- active boss markers and island association
- generated/explored chunk data or map tiles

The web page can then poll that endpoint and render the live markers. Keep credentials server-side; do not expose admin tokens in this static page. The actual terrain image should ultimately come from generated map tiles or chunk snapshots, not guessed island silhouettes.
