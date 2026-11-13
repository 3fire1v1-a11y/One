# One (1.21.11 Fabric)

Visual-only client mod plus custom knives. Not compiled or tested yet.

## Install (to build)
1. Install JDK 21.
2. Generate the Gradle wrapper once (`gradle wrapper`, or copy gradlew and gradle/ from any Fabric template).
3. Check versions in gradle.properties at fabricmc.net/develop.
4. Run `./gradlew build`. The jar lands in build/libs.
5. Drop the jar in your mods folder with Fabric Loader and Fabric API.

## Features
- HUD clock and FPS pill
- Jump rings, sprint trails, fireflies, shockwaves
- Aurora and shooting stars (L toggles), blizzard (L toggles)
- Four knives: katana, karambit, butterfly knife, greatsword
  - B = signature move, V = inspect, each knife with its own style
  - K = knife settings (blade, handle, accent colors, animation speed)
  - Inventory icons, and a knife on the hip when it isn't in your hand

## Controls
- K: knife settings
- L: aurora and blizzard toggles
- V: inspect, B: signature move (hold a knife in the main hand)

## Caveats
- Nothing is compiled or tested. Expect small name or signature fixes on first build (Identifier, KeyMapping, Button, EditBox, render signatures, mixin signature, sheath render layer).
- Sky and weather are particle approximations, not real shaders.
- Knife animations and the sheath are client-only, so other players won't see them.
- Models are blocky placeholders. Refine in Blockbench.
- Features are original implementations inspired by the videos, not copied code or assets.
