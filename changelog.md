# Changelog

## [1.4.3] - 2026-06-08

### Added
- Unified developer API class (`YetAnotherThirstAPI`) with helpers for thirst state, exhaustion, enabled state, drink/food values, water purity, and purity effects.
- Default helper methods in `IPlatformHelper` for loader-neutral thirst and water purity integration.
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
- Updated `pack.mcmeta` to pack format `15`.
- Bumped mod version to `1.20.1-1.4.3`.
- Renamed NeoForge metadata to `META-INF/mods.toml` and limited Gradle resource expansion to `mods.toml`.
- Added loader-specific archive names to Gradle build output.

### Fixed
- Fixed Hunger effect thirst exhaustion so Hunger increases thirst depletion instead of reducing it.
- Fixed cauldron default purity initialization to update block state only on the server.
- Synced cloned player thirst data after clone events.

### Removed
- Removed legacy `QuenchnessEffect` registration and localization keys.
- Removed the stack-aware potion max-stack mixin override path, leaving the base water bottle stack-size hook.

---

## [1.4.2] - 2026-06-07

### Added
- Rebuilt mod using Multi-loader architecture supporting both Forge and NeoForge.
- NeoForge support including events, networking, configurations, and client HUD rendering.
- Wooden water bowl item.
- NBT-aware custom furnace smelting and campfire cooking recipes for water purification.
- Dynamic configurations to suspend thirst, pause depletion, or regenerate thirst via active mob effects (Tombstone, Farmer's Delight, Let's Do mods).
- AppleSkin compatibility displaying thirst values on tooltips and HUD preview.
- Jade compatibility displaying water source and cauldron purity.
- Tough As Nails compatibility with auto-disable option.
- Cold Sweat compatibility modifying depletion based on body temperature.
- Supernatural/Vampirism integration to suspend thirst for vampire players.

---

## [1.4.1] - 2026-06-03

### Fixed
- Refactored loot table modifier context to resolve recursive calls and added build artifacts to gitignore.

### Added
- Implemented mod-specific loot table conditions.
- Upgraded Gradle to `8.10.2`.
- Added Vietnamese localization support.
- Refactored dispenser behaviors and water purity handling.
