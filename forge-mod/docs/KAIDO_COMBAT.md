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


## Kanabo melee animation

- A successful ordinary melee hit triggers Kaido's dedicated kanabo swing clip.
- The clip animates the right arm, body counter-rotation and the kanabo bone as one authored weapon swing.
- Vanilla melee damage remains authoritative; the animation hook does not apply a second damage event.
- The clip is cosmetic and is triggered only for a successful hit when no breath/slam special is active.
- Exact hand/weapon alignment and hit-frame synchronization still require an in-game GeckoLib test.


## Form switching — first visual implementation

- The current phase is synchronized through Minecraft entity data so clients select the same form as the server.
- Phase 1 uses the original humanoid blockout; phase 2 switches to a heavier hybrid silhouette; phase 3 switches to a long eastern-dragon silhouette with neck, horns, tail and wings.
- Hybrid and dragon transitions have dedicated GeckoLib clips; dragon form has separate idle and movement clips.
- This is a first-pass geometry pass using the existing Kaido texture atlas. It is not yet a final anime-accurate sculpt: UVs, wing silhouette, flight movement and in-game scale still require visual iteration.
