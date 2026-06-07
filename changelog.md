# Changelog

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