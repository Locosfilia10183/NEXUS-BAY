# Kaido GeckoLib model status

The Forge entity now implements GeckoLib 4's `GeoEntity` interface and has an idle/walk
animation controller. A client renderer and entity-renderer registration have been added.
The model resource contains a first blockout with body, head, limbs, horns, and kanabo bones.
The animation resource defines idle, walk, breath, and slam clips.

## Important visual limitation

The model is a rough geometry blockout, not a finished character likeness. The referenced
texture file `assets/nexusbay/textures/entity/kaido.png` has not yet been added. Until the
texture atlas exists and is UV-tested in Blockbench, the renderer will not look finished.
The breath and slam clips are authored but are not yet triggered by combat state; only idle
and walk are currently selected by the entity controller.

## Required next steps

1. Create and add the 128x128 Kaido texture atlas and verify UV mapping.
2. Refine proportions, horns, hair, facial details, dragon features, and kanabo.
3. Add synchronized attack/phase state and trigger breath/slam clips.
4. Run the GitHub Actions build and fix compile errors before any live deployment.
