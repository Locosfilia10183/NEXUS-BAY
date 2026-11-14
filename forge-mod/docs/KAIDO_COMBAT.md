# Kaido combat implementation

This document describes the first server-side combat pass for the registered Forge entity
`nexusbay:kaido`.

## Implemented in code

- **Dragon breath:** a forward-facing cone check, evaluated on the server. It deals 22 damage
  and applies five seconds of fire; in dragon phase it deals 32 damage.
- **Ground slam:** a close-range area attack with knockback. It deals 26 damage normally
  and 38 damage in dragon phase.
- **Phase-aware cooldowns:** dragon phase shortens both attack cooldowns.
- **Boss bar:** health progress and the current phase label are updated server-side.

All damage numbers and cooldowns are gameplay-balance values, not One Piece canon.

## Current limitations

- The attacks currently use server-side hit checks, not custom projectile entities.
- There are no attack telegraphs, particle/sound effects, or GeckoLib animation triggers yet.
- The phase labels are state changes only; they do not yet transform the rendered model.
- The entity still uses its default Minecraft renderer until a GeckoLib model, texture,
  renderer, and animation controllers are added.
- This code has not yet been compiled or tested in a running Forge client/server.

## Next implementation gate

Compile against Forge 1.20.1 / 47.2.0, fix any API/compiler issues, then add the GeckoLib
model and connect attack/phase state to animation controllers. Test multiplayer damage
and boss-bar visibility before installing on the live NEXUS-BAY server.
