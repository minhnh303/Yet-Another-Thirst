# AGENTS.md

This file provides guidance to AI Agents when working with code in this repository.

## Commands

```bash
# Build all subprojects (runs seam checks + common unit tests)
./gradlew build

# Build a specific loader
./gradlew :forge:build
./gradlew :neoforge:build
./gradlew :fabric:build

# Unit tests (JUnit 5 + Mockito, common only)
./gradlew :common:test

# Architectural seam checks (also run before :common:compileJava)
./gradlew :common:checkSeams

# GameTests (Fabric run config, report: fabric/build/gametest-results.xml; Forge/NeoForge register via entry point)
./gradlew :fabric:runGametest

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

## Version Branches

| Item | Value |
|---|---|
| This branch | Minecraft 1.21 / 1.21.1, Java 21 |
| Other branches | Same module layout per Minecraft version (e.g. 1.20.1); port changes with the `port-changes` skill |
| Version-specific parts | Loader APIs, mixin targets, platform/config services, compat mod availability, `expandProps` keys |
| Known divergences vs 1.20.1 | Config GUI uses `IConfigBackend` + `ConfigOption`/`ConfigSession` (1.20.1: `IPlatformHelper` config methods + `Yat*` row classes); Fabric uses `FabricMixin*` copies instead of the common mixin config; Create/IE mixins on NeoForge only (1.20.1: Forge too); Water Boiler also on Fabric; tested versions in `COMPATIBLE_MODS_VERSIONS.md` |

## Architecture

Multi-loader Minecraft mod targeting **Forge**, **NeoForge** and **Fabric** via a shared `common` subproject.

```
common/src/main/   — Server-safe shared logic (no client classes)
common/src/client/ — Client-only shared code (HUD, tooltips, screens, config GUI, Jade plugin, client mixins)
common/src/test/   — JUnit unit tests (TestPlatformHelper stubs IPlatformHelper)
forge/             — Forge entry point, configs, network, platform impl
neoforge/          — NeoForge entry point, configs, network, platform impl, Create/IE mixins
fabric/            — Fabric entry point, configs, network, platform impl, Create mixins, ModMenu (fabric-loom)
buildSrc/          — Convention plugins: multiloader-common.gradle, multiloader-loader.gradle
.claude/skills/    — Project skills (port-changes: cross-version porting)
```

### Common packages (`dev.minhnh.yetanotherthirst`)

| Source set | Package | Content |
|---|---|---|
| main | root | `CommonClass`, `Constants`, `DefaultItemValues`, `Lang` |
| main | `api` | Public API (`YetAnotherThirstAPI`) |
| main | `core/thirst` | `ThirstData` (model + Mojang `Codec`), `ThirstState`, `ThirstStorage`, `ThirstTicker`, `ThirstEvents`, `ThirstConfig`, `ThirstValue(s)` |
| main | `core/purity`, `core/item`, `core/block`, `core/effect`, `core/command`, `core/advancement` | Gameplay features (`WaterPurity`, `DrinkableItem`, `FilterCoreItem`, `AbstractFilterFrameBlock(Entity)`, `IWaterBoiler`, `ThirstCommands`, ...) |
| main | `compat` | `ThirstCompat` facade routing to providers in `temperature/` (ColdSweat, Environmentz), `status/` (Vampirism, Supernatural), `gui/` (AppleSkin) |
| main | `gametest` | `ThirstGameTests` (shared GameTests) |
| main | `mixin` | Common mixins (`yet_another_thirst.mixins.json`) |
| main | `platform`, `platform/services` | `Services` loader; `IPlatformHelper`, `IConfigBackend` |
| main | `screen` | Server-side menus (`AbstractWaterBoilerMenu`) |
| client | `client` | `ThirstHudRenderer`, `ThirstTooltip*`, `WaterBoilerScreen`, `WaterBoilerRenderer` |
| client | `client/config` | In-game config GUI (`YatConfigScreen`, `ConfigOption(s)`, `ConfigSession`, `OptionList`, `ListEditorScreen`, ...) |
| client | `compat`, `mixin` | Jade plugin (`JadeCompatPlugin`, `ThirstElement`); client mixins (`MixinGui`, `MixinMinecraft`) |

Tests (`common/src/test`): `ThirstStateTest`, `ThirstDataTest`, `ThirstStorageTest`, `ThirstConfigTest`, `ThirstValuesTest`, `ThirstCompatTest`, `WaterPurityTest`, `FilterCoreTest`, `YetAnotherThirstAPITest`, `LangKeysTest`.

### Source set wiring

- `common` exposes `commonJava`, `commonClientJava`, `commonResources`; loader builds compile these sources directly (`multiloader-loader.gradle`, Fabric in `fabric/build.gradle`).
- `common/src/client` compiles against `main`; `test` sees both `main` and `client` output.

### Platform services

Java `ServiceLoader` via `platform/Services.java`; impls registered in each loader's `META-INF/services/`.

| Interface | Impls | Methods |
|---|---|---|
| `IPlatformHelper` | `ForgePlatformHelper`, `NeoForgePlatformHelper`, `FabricPlatformHelper` (+ `TestPlatformHelper`) | `getPlatformName`, `isModLoaded`, `isDevelopmentEnvironment`, `loadThirstData`, `saveThirstData`, `loadPersistentData`, `getThirstState`, `syncThirst`, `sendThirstSync`, `tryHandDrink`, `getEnvironmentName` |
| `IConfigBackend` | `ForgeConfigBackend`, `NeoForgeConfigBackend`, `FabricConfigBackend` | `has`, `get`, `getDefault`, `set`, `save`, `reloadItemValues`; files `ConfigFile.COMMON/ITEMS/COMPAT/CLIENT` |

### Mixins

| Config | Declared in | Mixins |
|---|---|---|
| `yet_another_thirst.mixins.json` | common; Forge `mods.toml`, NeoForge `neoforge.mods.toml` | `MixinAbstractCookingRecipe`, `MixinAbstractFurnaceEntity`, `MixinFoodData`, `MixinInventory`, `MixinItemStack`, `MixinStateDefinitionBuilder`; client `MixinGui`, `MixinMinecraft` |
| `yet_another_thirst.forge.mixins.json` | Forge | Same set as common config |
| `yet_another_thirst.neoforge.mixins.json` | NeoForge | Common set + `MixinFluidTank`; client `MixinMinecraft` |
| `yet_another_thirst.create.mixins.json` | NeoForge | `create.MixinFilterFrameBlock(Entity)`, `create.MixinFluidTankBlockEntity` |
| `yet_another_thirst.ie.mixins.json` | NeoForge (`CompatMixinPlugin`) | `ie.MixinIEUtils`, `ie.MixinFluidPumpBlockEntity`, `ie.MixinWaterBoilerBlockEntity`, `ie.MixinMultiFluidTank` |
| `yet_another_thirst.fabric.mixins.json` | Fabric `fabric.mod.json` (does not use common config) | `MixinServerPlayer`, `MixinLivingEntity`, `FabricMixin*` copies of the common set; client `FabricMixinGui`, `FabricMixinMinecraft` |
| `yet_another_thirst.compat.mixins.json` | Fabric (`CompatMixinPlugin`) | `create.MixinFilterFrameBlock(Entity)`, `create.MixinFluidTankBlockEntity` |

### Config GUI

Shared `YatConfigScreen` (common client) reads/writes config through `IConfigBackend`; values cross the boundary as `Boolean`, `Number`, `String`, `List<String>`, `List<List<Object>>`.

| Loader | Screen registration | Config storage |
|---|---|---|
| Forge | `ForgeConfigScreen` (`ConfigScreenHandler.ConfigScreenFactory`) | `ForgeConfigCommon/Items/Compat`, `ForgeClientConfig`, `ConfigMigration` |
| NeoForge | `NeoForgeConfigScreen` (`IConfigScreenFactory`) | `NeoForgeConfigCommon/Items/Compat`, `NeoForgeClientConfig`, `ConfigMigration` |
| Fabric | `FabricModMenuIntegration` (ModMenu entrypoint) | `FabricConfig` |

### Per-loader classes

| Role | Forge | NeoForge | Fabric |
|---|---|---|---|
| Entry point | `YetAnotherThirstForge` | `YetAnotherThirstNeoForge` | `YetAnotherThirstFabric`, `YetAnotherThirstFabricClient` |
| Registries | `ForgeItems`, `ForgeEffects`, `ForgeCreativeTab`, `core.block.ModBlocks`, `screen.ModMenuTypes` | same pattern | `FabricItems`, `FabricEffects`, `FabricCreativeTab`, `core.block.ModBlocks` |
| Events | `ForgeGameEvents`, `ForgeClientEvents`, `ForgeClientGameEvents`, `ForgeCommonSetup` | same pattern | `FabricGameEvents`, `FabricClientEvents`, `FabricClientGameEvents` |
| Network | `ForgeNetwork`, `ForgeClientPacketHandler` | same pattern | same pattern |
| Loot | `ForgeLootModifier` (global loot modifier) | `NeoForgeLootModifier` | `FabricLootModifier` |
| Blocks | `FilterFrameBlock(Entity)`, `WaterBoilerBlock(Entity)` | same | same |
| Other | `ForgeHandDrinkClient`, `compat.FluidPurityHelper` | same pattern | `FabricFluidStorage`, `FabricHandDrinkClient`, `client.TooltipStackTracker`, `platform.FabricPlayerDataStore` |

## Third-Party Mod Compatibility

Optional-mod checks go through the `ThirstCompat` facade; mods are detected at runtime via `IPlatformHelper.isModLoaded` and reflection. New integrations add a provider under the matching `compat/` subpackage plus a facade method; non-compat code calls the facade only.

| Mod | Integration | Location |
|---|---|---|
| Vampirism | Vampire players suspend thirst depletion | `compat/status/VampirismCompat` |
| Supernatural | Vampire players suspend thirst depletion (NBT fallback) | `compat/status/SupernaturalCompat` |
| Cold Sweat | Body temperature modifies dehydration rate | `compat/temperature/ColdSweatCompat` |
| Environmentz | Body temperature modifies dehydration rate | `compat/temperature/EnvironmentzCompat` |
| Tough as Nails | Can auto-disable YAT in favor of TaN thirst | `ThirstCompat.usesExternalThirst` |
| AppleSkin | Thirst tooltip and HUD preview | `compat/gui/AppleSkinCompat` |
| Jade | Block info overlay | client `compat/JadeCompatPlugin` |
| Create | Purity-preserving fluid transfer (filter frame, fluid tank) | NeoForge/Fabric `mixin/create/` |
| Immersive Engineering | Purity-preserving pumps, tanks, water boiler | NeoForge `mixin/ie/` |

See [COMPATIBLE_MODS_VERSIONS.md](COMPATIBLE_MODS_VERSIONS.md) for tested mod versions per loader.

**Development rule**: When implementing integration with a third-party mod, only develop the feature for the mod loader(s) that support that mod, as listed in `COMPATIBLE_MODS_VERSIONS.md`. Do not implement loader-specific integration code for loaders where the mod is not available.

## Key Constraints

- Seam checks (`checkSeams`, run by `compileJava` and `check`) fail the build on:
  - `checkLoaderSeam`: `net.minecraftforge.`, `net.neoforged.`, `net.fabricmc.` anywhere in `common/src/main` or `common/src/client`.
  - `checkClientSeam`: `net.minecraft.client.`, `com.mojang.blaze3d.` in `common/src/main` (put client code in `common/src/client`).
  - `checkOptionalModSeam`: Create, IE, Jade, AppleSkin, ColdSweat, Vampirism, FarmersDelight packages outside `**/compat/**` in common.
- Common code must not import loader APIs — use `IPlatformHelper` / `IConfigBackend`.
- New `IPlatformHelper` methods must be implemented in all three loader helpers and `TestPlatformHelper`; new `IConfigBackend` methods in all three loader backends.
- New mixins must be added to every loader config that needs them; Fabric uses its own `FabricMixin*` copies, not the common config.
- Any new property added to `gradle.properties` **must also be added** to the `expandProps` map in `buildSrc/src/main/groovy/multiloader-common.gradle`. Current keys: `version`, `group`, `minecraft_version`, `minecraft_version_range`, `mod_name`, `mod_author`, `mod_id`, `license`, `description`, `forge_version`, `forge_loader_version_range`, `neoforge_version`, `neoforge_version_range`, `neoforge_loader_version_range`, `neoform_version`, `jade_version`, `credits`, `java_version`, `fabric_minecraft_version_range`, `fabric_loader_version`, `fabric_api_version`, `jade_fabric_version`, `modmenu_version`.
- Loader-specific block/item/menu registries live in each loader's subproject, not in `common`.
- Translation keys (enforced by `LangKeysTest`):

  | Kind | Format |
  |---|---|
  | Mod-owned (tooltip, jade, gui, config, command, purity, ...) | `yat.<group>.<subject>[.<detail>]`, built via `Lang.PREFIX` / `Lang.CONFIG_PREFIX` |
  | Registry-derived | `block\|item\|effect\|container.yet_another_thirst.<id>`, `itemGroup.yet_another_thirst` |
  | Advancements | `advancements.yet_another_thirst.<id>.title\|description` |
  | Death message | `death.attack.yet_another_thirst.<id>[.player]` (damage type `message_id`) |
  | Create Ponder | `yet_another_thirst.ponder.<scene>.<line>` |

  `<group>` is snake_case; config option/enum ids may mirror camelCase config keys. Every locale must match `en_us` exactly.
- Mod ID: `yet_another_thirst` | Group: `dev.minhnh.yetanotherthirst` | Version: `mod_version` in `gradle.properties` (format `1.21-<mod>`, e.g. `1.21-1.6.0`)

## Integration Reference Paths

For implementing or referencing mod integrations:
- **Create**: `~/Projects/Java/Create/`
- **Immersive Engineering** (NeoForge only on this branch): `~/Projects/Java/ImmersiveEngineering/`
