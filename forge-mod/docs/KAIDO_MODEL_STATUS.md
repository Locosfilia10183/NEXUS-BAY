# Kaido GeckoLib model status

## Implemented

- Registered Forge entity, GeckoLib `GeoEntity`, model and client renderer.
- Geometry blockout with body, chest, head, horns, limbs and kanabo bones.
- 128x128 texture atlas at `assets/nexusbay/textures/entity/kaido.png`.
- GeckoLib animation resource with idle, walk, breath and slam clips.
- Movement controller selects idle / walk; attack controller exposes named breath / slam triggers.
- Server combat calls the matching GeckoLib triggers.

## Visual limitations

The model is still a first-pass blockout, not a final Kaido likeness. The texture is a hand-authored first-pass pixel atlas and requires an in-game UV review. The current geometry reuses UV regions for mirrored limbs and horns.

## Next steps

1. Inspect the atlas on the live geometry in a Forge client and refine UV placement / detail.
2. Improve face, hair, horns, dragon scales, waist armor and kanabo silhouette.
3. Add hybrid / dragon geometry or layered form models and synchronize phase transitions.
4. Add attack wind-up / impact timing, hit, transformation and death clips.
5. Run multiplayer visual and combat tests before any live deployment.
