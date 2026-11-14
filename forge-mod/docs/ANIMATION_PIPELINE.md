# Custom Character and Combat Animation Pipeline

## Runtime target
Use GeckoLib 4 on Forge 1.20.1 for animated entity and item models.

## Per-boss asset set
Each boss requires:
- Geometry model JSON
- Texture PNG
- Animation JSON
- GeckoLib entity renderer and model class
- Animation controller with named triggers
- Server-side attack state that triggers the matching client animation

## Kaido animation IDs
- `animation.kaido.idle`
- `animation.kaido.walk`
- `animation.kaido.kanabo_attack`
- `animation.kaido.dragon_transform`
- `animation.kaido.flame_breath`
- `animation.kaido.hit`
- `animation.kaido.death`

Animation files alone do not create a character model or register an animated entity. Geometry, textures, renderer/model classes, animation controllers, and synchronized server attack events must be implemented and tested together.
