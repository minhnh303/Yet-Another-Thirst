# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

```bash
# Build all subprojects
./gradlew build

# Build a specific loader
./gradlew :forge:build
./gradlew :neoforge:build
./gradlew :fabric:build

# Run the game with mod loaded (for testing)
./gradlew :forge:runClient
./gradlew :neoforge:runClient
./gradlew :fabric:runClient

# Run dedicated server
./gradlew :forge:runServer
./gradlew :neoforge:runServer
./gradlew :fabric:runServer

# Clean
./gradlew clean
```

## Architecture

Multi-loader Minecraft mod (Minecraft 1.20.1, Java 17) targeting **Forge** and **NeoForge** via a shared `common` subproject.

```
common/   — Loader-agnostic game logic, events, mixins
forge/    — Forge entry point + platform service impl
neoforge/ — NeoForge entry point + platform service impl
fabric/   — Fabric entry point + platform service impl (uses fabric-loom)
buildSrc/ — Shared Gradle convention plugin (multiloader-common.gradle)
```

**Platform abstraction** uses Java `ServiceLoader`: `common` defines `IPlatformHelper` in `platform/services/`, loaded via `Services.java`. Each loader subproject provides its own implementation (`ForgePlatformHelper`, `NeoForgePlatformHelper`, `FabricPlatformHelper`) registered in `META-INF/services/`.

**Mixins** are split per-loader: common mixins live in `common/src/main/java/.../mixin/`; loader-specific mixins live under each loader's source set and are declared in their respective `*.mixins.json`. Fabric uses two mixin configs: `yet_another_thirst.fabric.mixins.json` and `yet_another_thirst.compat.mixins.json` (with `CompatMixinPlugin` for conditional loading).

**Fabric-specific**: Screen/GUI classes (`ModMenuTypes`) and block entity implementations (`FilterFrameBlockEntity`) live in `fabric/` rather than `common/`, as they depend on Fabric API/Create. Fabric also owns `FabricFluidStorage` for fluid handling and `FabricLootModifier` for loot table injection. Water Boiler and other Immersive Engineering integration features are excluded from Fabric since Immersive Engineering is not available on Fabric.

## Key Constraints

- Any new property added to `gradle.properties` **must also be added** to `buildSrc/src/main/groovy/multiloader-common.gradle` in the `expandProps` map.
- Common code must not import Forge/NeoForge APIs directly — use the `IPlatformHelper` service interface instead.
- Mod ID: `yet_another_thirst` | Group: `dev.minhnh.yetanotherthirst`

## Integration Reference Paths

For implementing or referencing mod integrations:
- **Create**: `~/Projects/Java/Create/`
- **Immersive Engineering** (Forge/NeoForge only): `~/Projects/Java/ImmersiveEngineering/`
