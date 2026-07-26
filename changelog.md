# Changelog

## [1.5.1] - 2026-07-27

### Added
- **EnvironmentZ Compatibility**: Integration with EnvironmentZ for temperature-based dehydration modifiers and HUD integration:
  - Dehydration rate modifiers based on player body temperature (`playerTemperature`) with configurable temperature tiers (`environmentzTemperatureTiers`).
  - Option `environmentzReplacesEnvironmentModifiers` allowing EnvironmentZ body temperature to override standard environmental dehydration factors (biomes, altitude, heat sources).
  - Customizable temperature text overlay rendered alongside EnvironmentZ's thermometer HUD icon with options for display type (`PLAYER` body temperature or `THERMOMETER` ambient temperature), temperature unit (e.g. `°C`), position offsets, and text color (`environmentzShowTemperatureText`, `environmentzTemperatureTextType`, `environmentzTemperatureTextUnit`, `environmentzTemperatureTextXOffset`, `environmentzTemperatureTextYOffset`, `environmentzTemperatureTextColor`).
  - Automatic alignment with EnvironmentZ's thermometer HUD icon positions (`thermometerIconX`, `thermometerIconY`).
  - Expanded `/thirst query` command output to display EnvironmentZ body temperature, thermometer reading, and active dehydration modifier.

### Changed
- Reduced default Water Boiler capacity (`waterBoilerCapacity`) from 16,000 mB to 2,000 mB (Forge & NeoForge).

### Fixed
- **Fabric HUD Rendering**: Shifted vanilla underwater air bubble icons 10px upward when the thirst bar is visible to prevent overlapping elements above the food bar.

---

## [1.5.0] - 2026-06-21

### Forge

#### Added
- **Water Boiler**: A new water purification machine that heats and processes dirty water into safe, purified water.
  - Features dynamic in-world fluid level rendering.
  - Custom GUI screen with energy, temperature, and fluid level indicators, styled with Immersive Engineering-inspired orange titles.
  - Integrates with Immersive Engineering's external heaters (automatically active if Immersive Engineering is installed).
- Advancement system with 11 advancements guiding players through the full thirst progression:
  - **Stay Hydrated** — craft a Clay Bowl to begin managing your thirst (root).
  - **Desperate Measures** — drink water directly from a source block using your bare hands.
  - **Singin' in the Rain** — drink rain by looking straight up during a storm.
  - **Risk Taker** — drink dirty water and suffer the consequences.
  - **Pure Mechanics** — craft a Filter Frame to start purifying your water supply (requires Create).
  - **Wear and Tear** — have a filter core wear out completely and become clogged.
  - **Reduce, Reuse, Recycle** — wash a clogged Fabric Filter Core in a cauldron.
  - **Boiling Point** — craft a Water Boiler to heat and purify water.
  - **Liquid Gold** — consume a drink with Purified quality.
  - **Brink of Death** — survive dehydration damage while at 0 thirst.
  - **Crimson Thirst** — suspend thirst mechanics by becoming a Vampire (Vampirism / Supernatural).
- Expanded built-in default hydration values for popular mods, registered out of the box without any configuration required:
  - **Farmer's Respite**: teas (green, yellow, black, rose hip, dandelion, gambler's), coffee, melon juice, apple cider.
  - **Create**: Builder's Tea.
  - **Farmer's Delight**: apple cider, melon juice, milk bottle, fruit salad, soups, stews, and baked goods.
  - **Brewin' & Chewin'**: beers, spirits, cocktails, and specialty brews.
  - **Collector's Reap**: limeades, popsicles, and teas.
  - **Farm & Charm**: herbal teas, soups, and fruit dishes.
  - **Tough As Nails**: all canteens (dirty, normal, purified across all tiers), juices, and ice cream.
  - **Supernatural**: Blood Bottle.
  - **Cold Sweat**: Filled Waterskin.
  - **Brewery**: beers and whiskeys.
  - **Bakery**: cakes, pies, cupcakes, and pastries.
- New configuration options for fine-tuning water purification machines:
  - Filter Frame (requires Create): base throughput speed, per-tier durability and maximum purity caps, and contamination-based decay multipliers.
  - Water Boiler: internal tank capacity.
- Localization Updates: Standardized and updated "Front of the machine" and other block state messages across all supported languages (English, French, Russian, Japanese, Simplified Chinese, Traditional Chinese, Korean, Polish, and Vietnamese).

#### Changed
- Cauldron water purity mixing now uses the worst-case (minimum) purity of the two sources instead of a weighted average blend. Adding lower-quality water to a cauldron that already contains cleaner water will now always pull the result down to the dirtier level.

#### Fixed
- Hand drinking now correctly cancels the vanilla item use action on the client side, preventing unwanted double-interactions when drinking from water source blocks.

---

### NeoForge

#### Added
- **Water Boiler**: A new water purification machine that heats and processes dirty water into safe, purified water.
  - Features dynamic in-world fluid level rendering.
  - Custom GUI screen with energy, temperature, and fluid level indicators, styled with Immersive Engineering-inspired orange titles.
  - Integrates with Immersive Engineering's external heaters (automatically active if Immersive Engineering is installed).
- Advancement system with 11 advancements guiding players through the full thirst progression:
  - **Stay Hydrated** — craft a Clay Bowl to begin managing your thirst (root).
  - **Desperate Measures** — drink water directly from a source block using your bare hands.
  - **Singin' in the Rain** — drink rain by looking straight up during a storm.
  - **Risk Taker** — drink dirty water and suffer the consequences.
  - **Pure Mechanics** — craft a Filter Frame to start purifying your water supply (requires Create).
  - **Wear and Tear** — have a filter core wear out completely and become clogged.
  - **Reduce, Reuse, Recycle** — wash a clogged Fabric Filter Core in a cauldron.
  - **Boiling Point** — craft a Water Boiler to heat and purify water.
  - **Liquid Gold** — consume a drink with Purified quality.
  - **Brink of Death** — survive dehydration damage while at 0 thirst.
  - **Crimson Thirst** — suspend thirst mechanics by becoming a Vampire (Vampirism / Supernatural).
- Expanded built-in default hydration values for popular mods, registered out of the box without any configuration required:
  - **Farmer's Respite**: teas (green, yellow, black, rose hip, dandelion, gambler's), coffee, melon juice, apple cider.
  - **Create**: Builder's Tea.
  - **Farmer's Delight**: apple cider, melon juice, milk bottle, fruit salad, soups, stews, and baked goods.
  - **Brewin' & Chewin'**: beers, spirits, cocktails, and specialty brews.
  - **Collector's Reap**: limeades, popsicles, and teas.
  - **Farm & Charm**: herbal teas, soups, and fruit dishes.
  - **Tough As Nails**: all canteens (dirty, normal, purified across all tiers), juices, and ice cream.
  - **Supernatural**: Blood Bottle.
  - **Cold Sweat**: Filled Waterskin.
  - **Brewery**: beers and whiskeys.
  - **Bakery**: cakes, pies, cupcakes, and pastries.
- New configuration options for fine-tuning water purification machines:
  - Filter Frame (requires Create): base throughput speed, per-tier durability and maximum purity caps, and contamination-based decay multipliers.
  - Water Boiler: internal tank capacity.
- Localization Updates: Standardized and updated "Front of the machine" and other block state messages across all supported languages (English, French, Russian, Japanese, Simplified Chinese, Traditional Chinese, Korean, Polish, and Vietnamese).

#### Changed
- Cauldron water purity mixing now uses the worst-case (minimum) purity of the two sources instead of a weighted average blend. Adding lower-quality water to a cauldron that already contains cleaner water will now always pull the result down to the dirtier level.

#### Fixed
- Hand drinking now correctly cancels the vanilla item use action on the client side, preventing unwanted double-interactions when drinking from water source blocks.

---

### Fabric

#### Added
- **Fabric Loader Support**: Fabric loader is now supported. Note that integration features with 3rd-party mods may not function because those mods do not have a version for Fabric.

---

## [1.4.4] - 2026-06-11

### Added
- Modular Filter Frame block and block entity supporting customizable water filtration (requires Create).
- Three tiers of filter cores: Fabric (Tier 1), Sand (Tier 2), and Carbon (Tier 3), each with distinct durability, maximum purity capability, and wear mechanics.
- Filter core lifespan tracking via NBT tags, complete with dynamic item damage-bar rendering.
- Cauldron interaction to wash clogged Fabric filter cores back into clean cores.
- Create compatibility features:
  - Wrench support (`IWrenchable`) to toggle input/output flow directions on the Filter Frame.
  - Engineer's Goggles support (`IHaveGoggleInformation`) displaying Filter Frame tank capacities, fluid purity levels, and filter core durability.
  - Engineer's Goggles overlay showing stored water purity inside Create's Fluid Tanks.
  - Purity preservation when draining water from the world using Open-Ended Pipes and mechanical pumps.
  - Purity preservation when filling or emptying containers via Spouts or generic filling/emptying systems.
  - Splashing recipes to wash clogged Fabric and Sand filter cores back into clean cores.
- Immersive Engineering compatibility:
  - Drained water blocks harvested by IE fluid pumps preserve the position-based water purity level.
- Jade compatibility:
  - Prepend water purity level (e.g. "Purified", "Acceptable", "Slightly Dirty", "Dirty") to the fluid name tooltip for any water stored in fluid handlers.
- Custom crafting and recycling recipes:
  - Shaped recipes to craft Fabric, Sand, and Carbon filter cores.
  - Shaped recipe using copper casings and fluid tanks to craft the Filter Frame (if Create is loaded).
  - Shapeless water bucket recipe to wash clogged Fabric filters.
  - Smelting and campfire cooking recipes to recycle clogged Carbon filters back into clean cores.
- Detailed localization and translation keys for all supported languages, including a standardized "Clogged - Needs Cleaning" tooltip.
- Common configuration spec options to customize filter throughput speed, durability limits, max purity thresholds, and durability decay multipliers based on the input water's contamination.

### Changed
- Replaced the static Sand Filter block with the new dynamic, modular Filter Frame block.
- Registered Fabric, Sand, and Carbon filter core items, as well as their clogged variants.
- Registered the compatibility mixin configuration (`yet_another_thirst.compat.mixins.json`) in the jar manifest for both Forge and NeoForge.
- Added compile-only dependency on Create and Immersive Engineering to support optional integrations in loader configurations.
- Optimized the `filter_frame.png` block texture to clean up assets and reduce file size.

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
