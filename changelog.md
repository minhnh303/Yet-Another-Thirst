# Changelog

## [1.4.3] - 2026-06-09

### Added
- Unified developer API class (`YetAnotherThirstAPI`) with helpers for thirst state, exhaustion, enabled state, drink/food values, water purity, and purity effects.
- Default helper methods in `IPlatformHelper` for loader-neutral thirst state and sync integration.
- Hydration (`yet_another_thirst:hydration`) and Thirsty (`yet_another_thirst:thirsty`) mob effects.
  - Hydration restores thirst and quenched values over time.
  - Thirsty increases thirst exhaustion over time and is applied by dirty water nausea effects.
- Configurable effect rates for Hydration and Thirsty in Forge and NeoForge common configs.
- Loader-neutral `ModEffects` bindings for Forge and NeoForge effect registrations.
- Custom status effect icon textures for Hydration and Thirsty.
- Jade icon rendering for thirst values and Jade support for water containers displayed in item frames.
- Updated localization translations for all supported languages to support the new effects.

### Changed
- Replaced the old Quenchness effect model with Hydration and Thirsty.
- Updated thirst HUD, AppleSkin preview, and visual tooltip rendering for the new `thirst_icons.png` atlas layout.
- Restored text thirst tooltips when AppleSkin thirst tooltips are disabled.
- Reload drink and food config values after server start and tag updates.
- Kept built-in Terracotta Water Bowl and Wooden Water Bowl thirst values registered after config reloads.
- Updated default compatibility item IDs for Brewery, Bakery, and Cold Sweat.
- Kept 1.21 resource pack metadata at pack format `34` with supported formats `34` through `48`.
- Bumped mod version to `1.21-1.4.3`.
- Kept NeoForge metadata at `META-INF/neoforge.mods.toml` and expanded both loader metadata templates.
- Added loader-specific archive names to Gradle build output.

### Fixed
- Fixed Hunger effect thirst exhaustion so Hunger increases thirst depletion instead of reducing it.
- Fixed cauldron default purity initialization to update block state only on the server.
- Synced cloned player thirst data after clone events.

### Removed
- Removed legacy `QuenchnessEffect` registration and localization keys.
- Removed the stack-aware potion max-stack mixin override path, leaving the base water bottle stack-size hook.

---

## [1.4.2] - 2026-06-08

### Optimized
- **File Size Reduction:** Reduced compiled JAR file sizes down to ~150 KB (an ~80% reduction from the previous version's 742.89 KB) through dependency refactoring and asset optimization.

### Added
- **Multi-Loader Support:** Rebuilt from the ground up to support both **Forge** and **NeoForge** loaders using a shared architecture.
- **Wooden Water Bowl:** Added a new wooden bowl item for storing and drinking water.
- **Purification Recipes:** Implemented NBT-aware custom furnace smelting and campfire cooking recipes to boil and purify water.
- **Developer API:** Unified integration via `YetAnotherThirstAPI` and helper methods in `IPlatformHelper`.

### Compatibility & Integrations
*(Note: Except for Corail Tombstone, other compatible mods might not support Minecraft 1.21 on the Forge loader yet; integrations are fully functional on NeoForge).*

- **AppleSkin:** Display thirst values on tooltips and preview replenishment on the HUD.
- **Jade:** Show water source status and cauldron purity.
- **Tough As Nails:** Added option to automatically disable thirst when TAN is present.
- **Cold Sweat:** Thirst depletion scales dynamically with body temperature.
- **Supernatural / Vampirism:** Automatically suspends thirst depletion for vampire players.
- **Mob Effects:** Pauses depletion or regenerates thirst via active mob effects (supporting Corail Tombstone, Farmer's Delight, and Let's Do series).

---

## [1.4.1] - 2026-06-04

- **Compatibility:** Ported and updated the mod to fully support **Minecraft 1.21** and **1.21.1** (Forge).
- **Codebase:** Refactored internal systems to ensure stability on the new game versions.

### Fixed
- Refactored loot table modifier context to resolve recursive calls and added build artifacts to gitignore.

### Added
- Implemented mod-specific loot table conditions.
- Upgraded Gradle to `8.10.2`.
- Added Vietnamese localization support.
- Refactored dispenser behaviors and water purity handling.
