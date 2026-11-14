# Kaido combat implementation

## Implemented

- Server-side dragon-breath cone attack and ground slam with knockback.
- GeckoLib attack triggers for breath and slam.
- Damage is now delayed until the animation impact frame: 12 ticks (0.6 seconds) after the attack starts.
- Breath animation lock: 24 ticks (1.2 seconds); slam animation lock: 18 ticks (0.9 seconds).
- Only one special attack can run at a time, preventing breath and slam from overlapping.
- Phase-aware damage and cooldowns; phase labels update the boss bar.

## Combat timing

The server owns the attack state and damage timing. Clients receive the GeckoLib trigger; damage is not applied on the trigger tick. The current impact point is a tick-based approximation aligned with the authored animation pose. Fine alignment still requires in-game visual testing.

## Current limitations

- Attacks still use immediate area / cone hit checks at the impact tick, not custom projectile entities.
- No attack telegraphs, particles, or sound effects yet.
- Phase labels change at 65% and 30% health, but the rendered geometry does not yet transform into hybrid or dragon forms.
- The model is a first-pass blockout and the texture atlas requires in-game UV verification.
- CI compilation is not a substitute for a live Forge client/server test.
