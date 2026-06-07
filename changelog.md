# Changelog

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