# One (1.21.11 Fabric)

Visual-only client mod plus custom knives. Not compiled or tested yet.

## Install (to build)
1. Install JDK 21.
2. Generate the Gradle wrapper once (`gradle wrapper`, or copy gradlew and gradle/ from any Fabric template).
3. Check versions in gradle.properties at fabricmc.net/develop.
4. Run `./gradlew build`. The jar lands in build/libs.
5. Drop the jar in your mods folder with Fabric Loader and Fabric API.

## Visual effects (L opens the toggle screen)
- Sky: aurora + shooting stars, galaxy dome (stars and nebula bands)
- Weather: custom snow, custom rain with splashes, blizzard (snow and rain skip covered spots)
- Glass ground: sparkling pale floor with footstep ripples
- Cyberspace: neon grid floor, rising data streams, falling code
- Glass players: ice-blue tint on every player plus sparkles
- China hat: cone hat on every player (hidden for you in first person)
- Target effects: marker on whatever you look at or just hit, plus a burst when you hit
- Trails: ribbon behind you whenever you move or fall
- Jump rings: expanding rings on takeoff and landing
- Rainbow colors toggle recolors trails, rings, hat, target marker and the grid
- `density` in config/one.json (0.25 to 2.0) scales particle counts. Lower it if FPS drops.

## Knives
- Katana, karambit, butterfly knife, greatsword
- B = signature move, V = inspect, K = knife settings

## Caveats
- Nothing is compiled or tested. Expect small name or signature fixes on first build.
- Everything visual is particle based, not real GLSL shaders. Sky particles use addAlwaysVisibleParticle so they show past 32 blocks.
- Glass players only tints the model (opaque, not see-through). The mixin uses require = 0, so if getModelTint changes it logs a warning instead of crashing.
- The hip sheath was removed: it used pre-1.21.9 render layer APIs and could not compile.
- ItemInHandRendererMixin now takes SubmitNodeCollector (changed from MultiBufferSource in 1.21.9).
- Knife animations are client-only, so other players won't see them.
