# Custom Character and Combat Animation Pipeline

## Runtime target
GeckoLib 4 on Forge 1.20.1.

## Per-boss asset set
- Geometry model JSON and matching PNG texture atlas
- Animation JSON
- GeckoLib entity renderer and model class
- Movement and attack animation controllers
- Server-side attack state that triggers the matching client animation

## Current Kaido clips
- `animation.kaido.idle`
- `animation.kaido.walk`
- `animation.kaido.breath`
- `animation.kaido.slam`

The entity currently selects idle / walk from movement and triggers breath / slam from server combat. Damage currently resolves at attack start; the next combat pass must add wind-up and impact-frame timing.

## Planned Kaido clips
- Kanabo attack
- Hybrid / dragon transformation
- Hit reaction
- Death

Animation files alone do not create a character model or registered animated entity. Geometry, textures, renderer/model classes, animation controllers, and synchronized server attack events must be implemented and tested together.
