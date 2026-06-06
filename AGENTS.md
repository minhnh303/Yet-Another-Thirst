# AGENTS.md

This file provides guidance to AI Agents when working with code in this repository.

## Commands

```bash
# Build all subprojects
./gradlew build

# Build a specific loader
./gradlew :forge:build
./gradlew :neoforge:build

# Run the game with mod loaded (for testing)
./gradlew :forge:runClient
./gradlew :neoforge:runClient

# Run dedicated server
./gradlew :forge:runServer
./gradlew :neoforge:runServer

# Clean
./gradlew clean
```

## Architecture

Multi-loader Minecraft mod (Minecraft 1.20.1, Java 17) targeting **Forge** and **NeoForge** via a shared `common` subproject.

```
common/   — Loader-agnostic game logic, events, mixins
forge/    — Forge entry point + platform service impl
neoforge/ — NeoForge entry point + platform service impl
buildSrc/ — Shared Gradle convention plugin (multiloader-common.gradle)
```

**Platform abstraction** uses Java `ServiceLoader`: `common` defines `IPlatformHelper` in `platform/services/`, loaded via `Services.java`. Each loader subproject provides its own implementation (`ForgePlatformHelper`, `NeoForgePlatformHelper`) registered in `META-INF/services/`.

**Mixins** are split per-loader: common mixins live in `common/src/main/java/.../mixin/`; loader-specific mixins live under each loader's source set and are declared in their respective `*.mixins.json`.

## Key Constraints

- Any new property added to `gradle.properties` **must also be added** to `buildSrc/src/main/groovy/multiloader-common.gradle` in the `expandProps` map.
- Common code must not import Forge/NeoForge APIs directly — use the `IPlatformHelper` service interface instead.
- Mod ID: `yet_another_thirst` | Group: `dev.minhnh.yetanotherthirst`
