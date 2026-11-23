# Changelog

## [1.6.0] - 2026-11-23

Initial release for **Minecraft 26.2** (Java 25) across **Forge**, **NeoForge**, and **Fabric**.

### Added
- **Core Thirst Mechanics**:
  - Thirst and quenched values with custom HUD bar, held-item hydration preview, and dehydration damage (`yet_another_thirst.dehydrate`).
  - `Hydration` and `Thirsty` status effects, configurable effect-based suspension/pause/regeneration rules, and `/thirst` commands (`query`, `set`, `enable`).
  - Bare-hand water drinking, rain drinking, and 11 progression advancements.
- **Water Purity & Containers**:
  - Four purity tiers (`Dirty`, `Slightly Dirty`, `Acceptable`, `Purified`) determined by biome and water source, preserved across buckets, bottles, bowls, and cauldrons.
  - Clay Bowl, Terracotta Bowl, Terracotta Water Bowl, and Wooden Water Bowl items.
  - Smelting and campfire cooking recipes for water purification.
- **Water Purification Machines**:
  - **Water Boiler** (Forge & NeoForge): Heats and purifies water with dynamic fluid rendering, FE/t energy gauge with hover tooltip, wrench/hammer port configuration with action-bar feedback, and optional Immersive Engineering external heater support.
  - **Filter Frame & Filter Cores**: Modular filtration with Fabric, Sand, and Carbon filter cores (and clogged/recyclable variants) with optional Create integration.
- **In-game Config Screen**:
  - Edit every setting without touching TOML/JSON, accessible from Forge/NeoForge's mod list (Config button) or Mod Menu on Fabric.
  - Sidebar pages with item icons, search across all pages, per-setting tooltips (description, default, range), and reset buttons.
  - Toggle switches, sliders with units (ticks/seconds, hearts, %, ×, mB, purity names), validated number fields, and steppers.
  - List editor for drinks, foods, blacklist, effect rules, and EnvironmentZ tiers with item/effect icons, Tab-completion from registries, and live validation.
  - Live thirst-bar preview for HUD offsets, unsaved-change badges, Apply/Cancel, and confirmation prompt before leaving with unsaved edits.
  - Server-side settings are read-only while connected to a remote server; singleplayer changes apply immediately.
- **Mod Compatibility & Default Item Hydration Support**:
  - Integrations for AppleSkin, Jade, Tough As Nails, Cold Sweat, EnvironmentZ, Vampirism/Supernatural, Create, and Immersive Engineering.
  - Built-in default thirst and quenched values for Farmer's Delight, Farmer's Respite, Brewin' & Chewin', Collector's Reap, Farm & Charm, Brewery, Bakery, Vanilla Delight, Fruits Delight, Delightful, [Let's Do] Beachparty, [Let's Do] HerbalBrews, Hearth & Harvest, and Crock Pot.
- **Localization & Metadata**:
  - Full translations across 9 languages (English, French, Russian, Japanese, Simplified Chinese, Traditional Chinese, Korean, Polish, and Vietnamese) under the `yat.` key namespace.
  - Mod icon and loader/Mod Menu metadata links.
