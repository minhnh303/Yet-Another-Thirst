# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

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

Multi-loader Minecraft mod (Minecraft 1.21, Java 21) targeting **Forge** and **NeoForge** via a shared `common` subproject.

```
common/     — Loader-agnostic game logic, events, mixins, screens, blocks, items
forge/      — Forge entry point + platform service impl
neoforge/   — NeoForge entry point + platform service impl
buildSrc/   — Shared Gradle convention plugin (multiloader-common.gradle)
```

**Platform abstraction** uses Java `ServiceLoader`: `common` defines `IPlatformHelper` in `platform/services/`, loaded via `Services.java`. Each loader provides `ForgePlatformHelper` / `NeoForgePlatformHelper` registered in `META-INF/services/`.

Key `IPlatformHelper` methods: `loadThirstData`, `saveThirstData`, `sendThirstSync`, `tryHandDrink`, `isModLoaded`.

**Mixins** in `common/src/main/resources/yet_another_thirst.mixins.json`:
- Server: `MixinAbstractFurnaceEntity`, `MixinAbstractCookingRecipe`, `MixinFoodData`, `MixinInventory`, `MixinItemStack`, `MixinStateDefinitionBuilder`
- Client: `MixinGui`, `MixinMinecraft`

## Key Packages (common)

| Package | Purpose |
|---|---|
| `core.thirst` | `ThirstState`, `ThirstConfig`, `ThirstTicker`, `ThirstEvents`, `ThirstStorage` |
| `core.purity` | `WaterPurity`, `ContainerWithPurity` |
| `core.effect` | `HydrationEffect`, `ThirstyEffect`, `ModEffects` |
| `core.item` | `DrinkableItem`, `FilterCoreItem`, `ModItems` |
| `core.block` | `AbstractFilterFrameBlock/Entity`, `IWaterBoiler`, `FilterCoreType` |
| `core.advancement` | `ModAdvancements` |
| `screen` | `WaterBoilerScreen`, `AbstractWaterBoilerMenu` |
| `client` | `ThirstHudRenderer`, `WaterBoilerRenderer`, `ThirstTooltip*` |
| `compat` | `ThirstCompat`, `JadeCompatPlugin`, `ThirstElement` |
| `api` | `YetAnotherThirstAPI` |

## Third-Party Mod Compatibility

`ThirstCompat` detects and integrates with the following mods at runtime via reflection:

| Mod | Integration |
|---|---|
| Vampirism | Vampire players can suspend thirst depletion |
| Supernatural | Vampire players can suspend thirst depletion (NBT fallback) |
| Cold Sweat | Body temperature modifies dehydration rate |
| Tough as Nails | Can auto-disable YAT in favor of TaN thirst system |
| AppleSkin | Thirst tooltip and HUD preview |
| Jade | Block info overlay via `JadeCompatPlugin` |

See [COMPATIBLE_MODS_VERSIONS.md](COMPATIBLE_MODS_VERSIONS.md) for tested mod versions.

**Development rule**: When implementing integration with a third-party mod, only develop the feature for the mod loader(s) that support that mod, as listed in `COMPATIBLE_MODS_VERSIONS.md`. Do not implement loader-specific integration code for loaders where the mod is not available.

## Key Constraints

- Any new property added to `gradle.properties` **must also be added** to `buildSrc/src/main/groovy/multiloader-common.gradle` in the `expandProps` map. Current keys: `version`, `group`, `minecraft_version`, `minecraft_version_range`, `mod_name`, `mod_author`, `mod_id`, `license`, `description`, `forge_version`, `forge_loader_version_range`, `neoforge_version`, `neoforge_version_range`, `neoforge_loader_version_range`, `jade_version`, `fabric_loader_version`, `fabric_api_version`, `fabric_minecraft_version_range`, `jade_fabric_version`.
- Common code must not import Forge/NeoForge APIs directly — use `IPlatformHelper` instead.
- Mod ID: `yet_another_thirst` | Group: `dev.minhnh.yetanotherthirst` | Version: `1.21-1.5.0`
- Loader-specific block/item registries live in each loader's subproject, not in `common`.
