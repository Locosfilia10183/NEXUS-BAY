# Kaido combat implementation

## Implemented

- Server-side dragon-breath cone attack, triggered through GeckoLib controller `attack` / `breath`.
- Server-side ground slam with knockback, triggered through `attack` / `slam`.
- Phase-aware damage and cooldowns; phase labels update the boss bar.
- Boss bar health progress and player tracking.

## Current limitations

- Hit checks resolve immediately when the attack starts; there is no wind-up / impact-frame timing yet.
- Attacks have no projectile entity, telegraph, particles, or sound effects.
- Phase labels change at 65% and 30% health, but the geometry does not yet transform into hybrid or dragon forms.
- The current model is a blockout and its texture atlas is an initial pass; UVs and in-game appearance still need visual verification.
- CI compilation is not a substitute for an in-game client/server test.

## Next steps

1. Verify the texture atlas and refine the model proportions / UVs in Blockbench.
2. Add attack wind-up and apply damage at the animation impact frame.
3. Add hit, death, transformation, breath and kanabo-specific clips.
4. Test boss-bar tracking, combat and animations with multiple players.
