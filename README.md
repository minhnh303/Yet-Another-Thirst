# Yet Another Thirst

<div align="center">
  <img src="https://media.forgecdn.net/attachments/1724/119/yetanotherthirst_banner-png.png" alt="Yet Another Thirst Banner"/>

  <br/>

  <a href="https://www.curseforge.com/minecraft/mc-mods/yet-another-thirst"><img src="https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy/available/curseforge_64h.png" alt="Available on CurseForge"/></a>
  <a href="https://modrinth.com/mod/yet-another-thirst"><img src="https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy/available/modrinth_64h.png" alt="Available on Modrinth"/></a>

  <br/>

  <a href="https://www.curseforge.com/minecraft/mc-mods/yet-another-thirst/files"><img src="https://cf.way2muchnoise.eu/versions/1563280(c70039).svg" alt="Supported Versions"/></a>
  <a href="https://www.curseforge.com/minecraft/mc-mods/yet-another-thirst"><img src="https://cf.way2muchnoise.eu/1563280.svg" alt="CurseForge Downloads"/></a>
  <a href="https://modrinth.com/mod/yet-another-thirst"><img src="https://img.shields.io/modrinth/dt/HOl2fYpO?logo=modrinth&label=Modrinth" alt="Modrinth Downloads"/></a>
</div>

**Yet Another Thirst** is a multi-loader survival mod introducing a hydration, water purity, and automated filtration/purification system to Minecraft. Rebuilt from the ground up based on [Thirst Was Taken](https://www.curseforge.com/minecraft/mc-mods/thirst-was-taken) (`ghen-git`) and inspired by Dehydration, Tough As Nails, and Cold Sweat.

## Supported Platforms & Versions

| Worktree / Branch | Minecraft Version | Mod Version | Java | Supported Loaders | Config Directory & Format |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `1.20.1` | `1.20.1` | `1.6.0` | Java 17 | Forge (`47.2.30+`), NeoForge (`47.1.99+`), Fabric (`0.15.11+`) | `config/yetanotherthirst/{common,items,compat,client}.toml` (Forge/NeoForge)<br>`config/yetanotherthirst/{common,items,compat,client}.json` (Fabric) |
| `1.21` | `1.21` - `1.21.1` | `1.6.0` | Java 21 | Forge (`51.0.33+`), NeoForge (`21.0.167+`), Fabric (`0.16.9+`) | `config/yetanotherthirst/{common,items,compat,client}.toml` (Forge/NeoForge)<br>`config/yetanotherthirst/{common,items,compat,client}.json` (Fabric) |
| `26.2` | `26.2` | `1.6.0` | Java 25 | Forge (`65.1.5+`), NeoForge (`26.2.0.93+`), Fabric (`0.20.1+`) | `config/yetanotherthirst/{common,items,compat,client}.toml` (Forge/NeoForge)<br>`config/yetanotherthirst/{common,items,compat,client}.json` (Fabric) |

## Configuration & In-Game Config GUI

All settings can be edited directly via the built-in **In-Game Config Screen** (open from the Mods list **Config** button on Forge/NeoForge, or via **Mod Menu** on Fabric):
- Categorized sidebar with item icons, cross-page search, per-setting tooltips (description, default, range), and reset controls.
- Interactive list editors (drinks, foods, blacklist, effect rules, EnvironmentZ tiers) with item/effect icons, registry Tab-completion, and live validation.
- Live thirst-bar preview for HUD offsets, unsaved-change indicators, Apply/Cancel, and discard confirmation.
- Server-side settings are read-only while connected to a remote server; singleplayer changes apply immediately.

Configuration files are stored in `config/yetanotherthirst/` (`.toml` on Forge/NeoForge; atomic `.json` writes with `.broken` backup on Fabric). Legacy configs in `config/yet_another_thirst/` are automatically migrated and split into the 4 files below on startup:

| File | Scope | Configured Settings |
| :--- | :--- | :--- |
| `common.toml` / `common.json` | Server / Common | General thirst rules, status effect lists (`suspendThirstEffects`, `pauseDepletionEffects`, `regenThirstEffects`, supporting `!=` amplifier comparisons), `Hydration`/`Thirsty` tick rates, dehydration damage, biome/Nether depletion modifiers, water purity chances, world altitude thresholds (`mountainsY`, `cavesY`), and hand drinking |
| `items.toml` / `items.json` | Server / Common | `waterBottleStackSize`, `drinks` & `foods` hydration lists (supports item IDs and `#tags`), `itemsBlacklist`, Filter Frame parameters (speed, durability, max purity, decay multipliers), and Water Boiler parameters (capacity, boil time, FE/t consumption) |
| `compat.toml` / `compat.json` | Server / Common | AppleSkin tooltip/HUD toggles, `toughAsNailsMode`, Cold Sweat temperature thresholds & modifiers, Vampirism/Supernatural toggles, and EnvironmentZ temperature tiers & HUD thermometer text settings |
| `client.toml` / `client.json` | Client | Thirst HUD position offsets (`hudXOffset`, `hudYOffset`) |

## Core Mechanics

| Feature | Specification |
| :--- | :--- |
| **Thirst & Quenched** | Max Thirst: `20`, Default Quenched (saturation): `5`, Exhaustion threshold: `4.0` |
| **Dehydration Damage** | Damage type `yet_another_thirst:dehydrate` (`1.0` damage per `40` ticks at `0` thirst; no knockback) |
| **Sprint & Regen Penalty** | Sprinting prevented at or below `6` thirst; natural health regeneration halted when dehydrated |
| **Peaceful Mode** | Thirst depletion disabled by default; slowly regenerates until full |
| **Hand Drinking** | Shift + Right-click a water source block with empty hand (`+3` Thirst, `+2` Quenched when enabled) |
| **Rain Drinking** | Look straight up while standing in rain to restore hydration |
| **Climate & Nether** | Hot/dry biomes increase depletion; Nether applies a `3.0x` modifier; Fire Resistance negates heat penalties; Fire Protection armor reduces depletion |

## Water Purity System

| Tier | Name | World Sources | Consumption Effects |
| :--- | :--- | :--- | :--- |
| `0` | **Dirty** | Surface water, swamps, stagnant pools | `100%` Nausea (+30s `Thirsty`), `30%` Poison |
| `1` | **Slightly Dirty** | Underground caves (`Y <= 48`), mountains (`Y >= 100`) | `50%` Nausea (+30s `Thirsty`), `10%` Poison |
| `2` | **Acceptable** | Flowing streams (`+1` purity bonus over source blocks) | `5%` Nausea (+30s `Thirsty`) |
| `3` | **Purified** | Boiled or filtered water | `0%` (Safe) |

## Purification, Containers & Machinery

### Early-Game Containers & Thermal Purification
- **Containers**: `clay_bowl` (craftable from clay balls, found in dungeon/mineshaft/shipwreck/nether bridge/bastion loot), fired into `terracotta_bowl`. Supports `terracotta_water_bowl`, `wooden_water_bowl`, and vanilla `potion` (water bottles stack up to `64`).
- **Campfire Cooking**: Purifies water bottles, terracotta water bowls, and water buckets by `+1` tier.
- **Furnace Smelting**: Purifies water bottles, terracotta water bowls, and water buckets by `+2` tiers.
- **Cauldrons & Dispensers**: Cauldrons track purity using worst-case (minimum) purity when mixing fluids; dispensers preserve source block purity when filling bottles or buckets.

### Modular Filtration (`yet_another_thirst:filter_frame`)
Available on Forge and NeoForge when **Create** is installed (`1,000 mB` internal tank, Wrench & Engineer's Goggles support):

| Filter Core Item | Max Purity Output | Default Durability | Decay Multiplier (Dirty / Slightly Dirty / Acceptable) | Cleaning / Recycling Method |
| :--- | :--- | :--- | :--- | :--- |
| `fabric_filter_core` | Tier `1` (Slightly Dirty) | `10,000` | `4.0x` / `2.0x` / `1.0x` | Wash `clogged_fabric_filter` in a Cauldron, shapeless water bucket recipe, or Create Splashing |
| `sand_filter_core` | Tier `2` (Acceptable) | `50,000` | `4.0x` / `2.0x` / `1.0x` | Wash `clogged_sand_filter` via Create Splashing |
| `carbon_filter_core` | Tier `3` (Purified) | `250,000` | `4.0x` / `2.0x` / `1.0x` | Smelt or campfire-cook `clogged_carbon_filter` |

### Industrial Purification (`yet_another_thirst:water_boiler`)
Available on Forge and NeoForge:
- **Capacity & Consumption**: `2,000 mB` tank capacity, `200` ticks boil time, `32 FE/t` energy consumption, with dynamic in-world fluid rendering and a GUI energy gauge (`FE/t` received vs. required hover tooltip).
- **Port Configuration**: Configurable inlet, outlet, and electrical connector ports per block face/half using a hammer or wrench, with localized action-bar feedback.
- **Immersive Engineering Integration**: Supports heating via IE External Heaters and preserves world water purity with IE Fluid Pumps.

## Status Effects

| Effect | Registry ID | Type | Default Rate (per tick / per level) | Trigger / Source |
| :--- | :--- | :--- | :--- | :--- |
| **Hydration** | `yet_another_thirst:hydration` | Beneficial (`#21B1FF`) | `+1` Thirst, `+1` Quenched | Teas, specialty drinks, stews, or configured effects |
| **Thirsty** | `yet_another_thirst:thirsty` | Harmful (`#B15B2A`) | `+0.005` Exhaustion | Applied for 30s when Nausea triggers from unpurified water |

## Advancements

| Advancement ID | Title | Requirement |
| :--- | :--- | :--- |
| `root` | Stay Hydrated | Craft a Clay Bowl |
| `hand_drinking` | Desperate Measures | Drink water directly from a source block with bare hands |
| `rain_drinking` | Singin' in the Rain | Drink rain by looking straight up during a storm |
| `dirty_water` | Risk Taker | Drink dirty water and suffer the consequences |
| `filter_frame` | Pure Mechanics | Craft a Filter Frame |
| `clogged_filter` | Wear and Tear | Wear out a filter core until it becomes clogged |
| `wash_filter` | Reduce, Reuse, Recycle | Wash a clogged Fabric Filter Core in a cauldron |
| `water_boiler` | Boiling Point | Craft a Water Boiler |
| `purified_water` | Liquid Gold | Consume a drink with Purified quality |
| `dehydration_survival` | Brink of Death | Survive dehydration damage while at 0 thirst |
| `vampire_immunity` | Crimson Thirst | Suspend thirst mechanics by becoming a Vampire |

## Required Mods

| Loader | Required | Optional (Config GUI) |
| :--- | :--- | :--- |
| Fabric | Fabric Loader, <img alt="Fabric API icon" src="https://cdn.modrinth.com/data/P7dR8mSH/icon.png" width="20" height="20" align="absmiddle"> **Fabric API** ([Modrinth](https://modrinth.com/mod/fabric-api) · [CurseForge](https://www.curseforge.com/minecraft/mc-mods/fabric-api)) | <img alt="Mod Menu icon" src="https://cdn.modrinth.com/data/mOgUt4GM/5a20ed1450a0e1e79a1fe04e61bb4e5878bf1d20.png" width="20" height="20" align="absmiddle"> **Mod Menu** ([Modrinth](https://modrinth.com/mod/modmenu) · [CurseForge](https://www.curseforge.com/minecraft/mc-mods/mod-menu)) *(needed to open the config GUI from the Mods list; without it, edit `config/yetanotherthirst/*.json` manually)* |
| NeoForge / Forge | None | None *(config GUI opens from the Mods list via **Config**)* |

## Mod Compatibility

| Mod | Integration Behavior |
| :--- | :--- |
| <img alt="AppleSkin icon" src="https://cdn.modrinth.com/data/EsAfCjCV/icon.png" width="20" height="20" align="absmiddle"> **AppleSkin**<br>([Modrinth](https://modrinth.com/mod/appleskin) · [CurseForge](https://www.curseforge.com/minecraft/mc-mods/appleskin)) | Renders thirst and quenched restoration values on item tooltips and HUD overlay |
| <img alt="Jade icon" src="https://cdn.modrinth.com/data/nvQzSEkH/b04217bc2b7dc524c4d12f81ff42cc1cefb9b0fc_96.webp" width="20" height="20" align="absmiddle"> **Jade**<br>([Modrinth](https://modrinth.com/mod/jade) · [CurseForge](https://www.curseforge.com/minecraft/mc-mods/jade)) | Displays water purity on world fluid blocks, cauldrons, fluid tanks, item frames, Filter Frames, and Water Boilers |
| <img alt="Create icon" src="https://cdn.modrinth.com/data/LNytGWDc/61d716699bcf1ec42ed4926a9e1c7311be6087e2_96.webp" width="20" height="20" align="absmiddle"> **Create**<br>([Modrinth](https://modrinth.com/mod/create) · [CurseForge](https://www.curseforge.com/minecraft/mc-mods/create)) | Filter Frame block, Engineer's Goggles purity/durability overlays, Open-Ended Pipe & Hose Pulley purity retention, Spout container filling, Splashing filter cleaning, `builders_tea` hydration |
| <img alt="Immersive Engineering icon" src="https://cdn.modrinth.com/data/tIm2nV03/icon.png" width="20" height="20" align="absmiddle"> **Immersive Engineering**<br>([Modrinth](https://modrinth.com/mod/immersiveengineering) · [CurseForge](https://www.curseforge.com/minecraft/mc-mods/immersive-engineering)) | External Heater integration for Water Boiler; Fluid Pump position-based water purity preservation |
| <img alt="Cold Sweat icon" src="https://cdn.modrinth.com/data/uXhSmPjd/bf55420556c30d44d2f5cf7b8915705b9214b4ef.png" width="20" height="20" align="absmiddle"> **Cold Sweat**<br>([Modrinth](https://modrinth.com/mod/cold-sweat) · [CurseForge](https://www.curseforge.com/minecraft/mc-mods/cold-sweat)) | Scales thirst depletion with body temperature (`50.0` hot / `100.0` burning thresholds, up to `1.75x` modifier) and supports `filled_waterskin` |
| <img alt="EnvironmentZ icon" src="https://cdn.modrinth.com/data/TggYmc6t/4bab25a93f9e7f06aa22ff3cf44b8b1c829927d4.png" width="20" height="20" align="absmiddle"> **EnvironmentZ**<br>([Modrinth](https://modrinth.com/mod/environmentz) · [CurseForge](https://www.curseforge.com/minecraft/mc-mods/environmentz)) | Scales dehydration via configurable `playerTemperature` tiers, renders HUD temperature text aligned with the thermometer icon, and extends `/thirst query` |
| <img alt="Tough As Nails icon" src="https://cdn.modrinth.com/data/ge1sOdFH/fa385c1d5ed5638282faf31e8fc94884bd60d86e.png" width="20" height="20" align="absmiddle"> **Tough As Nails**<br>([Modrinth](https://modrinth.com/mod/tough-as-nails) · [CurseForge](https://www.curseforge.com/minecraft/mc-mods/tough-as-nails)) | Configurable coexistence (`AUTO_DISABLE_YAT`, `ITEMS_ONLY`, `FORCE_YAT`, `OFF`) plus built-in canteen, juice, and ice cream hydration values |
| <img alt="Vampirism icon" src="https://cdn.modrinth.com/data/jVZ0F1wn/icon.png" width="20" height="20" align="absmiddle"> **Vampirism** ([Modrinth](https://modrinth.com/mod/vampirism) · [CurseForge](https://www.curseforge.com/minecraft/mc-mods/vampirism))<br><img alt="Supernatural icon" src="https://cdn.modrinth.com/data/UXzKPebS/63ba2cf3a91eba83d797904ba8bfa81df089f854.png" width="20" height="20" align="absmiddle"> **Supernatural** ([Modrinth](https://modrinth.com/mod/supernatural) · [CurseForge](https://www.curseforge.com/minecraft/mc-mods/supernatural)) | Automatically suspends thirst depletion and hides the thirst HUD for vampire players; supports `supernatural:blood_bottle` |
| <img alt="Corail Tombstone icon" src="https://cdn.modrinth.com/data/YT5K34AB/b279e8756761d8160f678ad20fb632d0a09b89b4_96.webp" width="20" height="20" align="absmiddle"> **Corail Tombstone**<br>([Modrinth](https://modrinth.com/mod/corail-tombstone) · [CurseForge](https://www.curseforge.com/minecraft/mc-mods/corail-tombstone)) | Suspends thirst depletion while `ghostly_shape` is active |
| **Food & Beverage Mods** | Pre-configured drink/food hydration values and buff integration (`Nourishment`, `Stuffed`, `Saturated`, `Satiation`, `Sustenance`, `Feast`) for:<br>• <img alt="Farmer's Delight icon" src="https://cdn.modrinth.com/data/R2OftAxM/8e7aa38ab94d94bb0a2894a218b69beb49002b34.png" width="20" height="20" align="absmiddle"> **Farmer's Delight** ([Modrinth](https://modrinth.com/mod/farmers-delight) · [CurseForge](https://www.curseforge.com/minecraft/mc-mods/farmers-delight))<br>• <img alt="Farmer's Respite icon" src="https://cdn.modrinth.com/data/fasdaXH5/edba350f7617b0b0d724d6fcd7b432acd8f48de3_96.webp" width="20" height="20" align="absmiddle"> **Farmer's Respite** ([Modrinth](https://modrinth.com/mod/farmers-respite) · [CurseForge](https://www.curseforge.com/minecraft/mc-mods/farmers-respite))<br>• <img alt="Brewin' & Chewin' icon" src="https://cdn.modrinth.com/data/hIu9KJTT/f7c591a80046859d3d45c04ecbbc54d264483d5e.png" width="20" height="20" align="absmiddle"> **Brewin' & Chewin'** ([Modrinth](https://modrinth.com/mod/brewin-and-chewin) · [CurseForge](https://www.curseforge.com/minecraft/mc-mods/brewin-and-chewin))<br>• <img alt="Collector's Reap icon" src="https://cdn.modrinth.com/data/AXXLr36C/6d4c4190cbc77524aa972f4857dc53234cac3b14.png" width="20" height="20" align="absmiddle"> **Collector's Reap** ([Modrinth](https://modrinth.com/mod/collectors-reap) · [CurseForge](https://www.curseforge.com/minecraft/mc-mods/collectors-reap))<br>• <img alt="Vanilla Delight icon" src="https://cdn.modrinth.com/data/q5SZGtGt/f32f581b4fc6f331a60604e348f9492eeee5dc62_96.webp" width="20" height="20" align="absmiddle"> **Vanilla Delight** ([Modrinth](https://modrinth.com/mod/vanilla-delights) · [CurseForge](https://www.curseforge.com/minecraft/mc-mods/vanilla-delight))<br>• <img alt="Fruits Delight icon" src="https://cdn.modrinth.com/data/g6sbyCTu/4ecc5d554f260b876d21c427aa6c2bdf4457fd5c.png" width="20" height="20" align="absmiddle"> **Fruits Delight** ([Modrinth](https://modrinth.com/mod/fruits-delight) · [CurseForge](https://www.curseforge.com/minecraft/mc-mods/fruits-delight))<br>• <img alt="Delightful icon" src="https://cdn.modrinth.com/data/JtSnhtNJ/9a9a702158121ab7a62394c8ff6a9abe9a0e346d.png" width="20" height="20" align="absmiddle"> **Delightful** ([Modrinth](https://modrinth.com/mod/delightful) · [CurseForge](https://www.curseforge.com/minecraft/mc-mods/delightful))<br>• <img alt="Hearth & Harvest icon" src="https://cdn.modrinth.com/data/8EEEXOzj/32ad1244471cbf794f6229476bc80c612bb9bd54.png" width="20" height="20" align="absmiddle"> **Hearth & Harvest** ([Modrinth](https://modrinth.com/mod/hearth-and-harvest) · [CurseForge](https://www.curseforge.com/minecraft/mc-mods/hearth-and-harvest))<br>• <img alt="Crock Pot icon" src="https://media.forgecdn.net/avatars/thumbnails/1031/570/256/256/638554447515017328.png" width="20" height="20" align="absmiddle"> **Crock Pot** ([CurseForge](https://www.curseforge.com/minecraft/mc-mods/crock-pot))<br>• <img alt="&#91;Let's Do&#93; Bakery icon" src="https://cdn.modrinth.com/data/oNB5jhlA/263380c2138f7f28f37639b719cc391334cbb7d5_96.webp" width="20" height="20" align="absmiddle"> **[Let's Do] Bakery** ([Modrinth](https://modrinth.com/mod/lets-do-bakery-farmcharm-compat) · [CurseForge](https://www.curseforge.com/minecraft/mc-mods/lets-do-bakery-farm-charm-compat))<br>• <img alt="&#91;Let's Do&#93; Brewery icon" src="https://cdn.modrinth.com/data/b7NV2plI/f254a05a63fabc3c3648618812301a5ffb37eed9_96.webp" width="20" height="20" align="absmiddle"> **[Let's Do] Brewery** ([Modrinth](https://modrinth.com/mod/lets-do-brewery-farmcharm-compat) · [CurseForge](https://www.curseforge.com/minecraft/mc-mods/lets-do-brewery-farm-charm-compat))<br>• <img alt="&#91;Let's Do&#93; Farm & Charm icon" src="https://cdn.modrinth.com/data/HJetCzWo/7c6c372629b3efa41409621631d60df12963f005_96.webp" width="20" height="20" align="absmiddle"> **[Let's Do] Farm & Charm** ([Modrinth](https://modrinth.com/mod/lets-do-farm-charm) · [CurseForge](https://www.curseforge.com/minecraft/mc-mods/lets-do-farm-charm))<br>• <img alt="&#91;Let's Do&#93; Beachparty icon" src="https://cdn.modrinth.com/data/GyKzAh3l/41b9b45c365ecd55aced04bcd22af93878f766a0_96.webp" width="20" height="20" align="absmiddle"> **[Let's Do] Beachparty** ([Modrinth](https://modrinth.com/mod/lets-do-beachparty) · [CurseForge](https://www.curseforge.com/minecraft/mc-mods/lets-do-beachparty))<br>• <img alt="&#91;Let's Do&#93; HerbalBrews icon" src="https://cdn.modrinth.com/data/Eh11TaTm/cea48ad39e9323e9e0f5354ee1d4c160f46b50be_96.webp" width="20" height="20" align="absmiddle"> **[Let's Do] HerbalBrews** ([Modrinth](https://modrinth.com/mod/lets-do-herbalbrews) · [CurseForge](https://www.curseforge.com/minecraft/mc-mods/lets-do-herbalbrews)) |

### Compatibility Matrix by Mod Loader

#### 📌 Legend
* **✅ Supported:** Fully supported and integrated out of the box.
* **⏳ Pending Port:** Awaiting upstream companion mod update/port to this version.
* **❌ N/A:** Not available or unsupported on this mod loader / version.

#### NeoForge

| Third-Party Mod | 1.20.1 | 1.21 / 1.21.1 | 26.2 | Unsupported Reason |
| :--- | :---: | :---: | :---: | :--- |
| <img alt="AppleSkin icon" src="https://cdn.modrinth.com/data/EsAfCjCV/icon.png" width="20" height="20" align="absmiddle"> **AppleSkin** | ✅ | ✅ | ✅ | — |
| <img alt="Jade icon" src="https://cdn.modrinth.com/data/nvQzSEkH/b04217bc2b7dc524c4d12f81ff42cc1cefb9b0fc_96.webp" width="20" height="20" align="absmiddle"> **Jade** | ✅ | ✅ | ✅ | — |
| <img alt="Create icon" src="https://cdn.modrinth.com/data/LNytGWDc/61d716699bcf1ec42ed4926a9e1c7311be6087e2_96.webp" width="20" height="20" align="absmiddle"> **Create** | ✅ | ✅ | ⏳ | 26.2: Pending Create port to Minecraft 26.2 |
| <img alt="Immersive Engineering icon" src="https://cdn.modrinth.com/data/tIm2nV03/icon.png" width="20" height="20" align="absmiddle"> **Immersive Engineering** | ✅ | ✅ | ⏳ | 26.2: Pending Immersive Engineering port to Minecraft 26.2 |
| <img alt="Cold Sweat icon" src="https://cdn.modrinth.com/data/uXhSmPjd/bf55420556c30d44d2f5cf7b8915705b9214b4ef.png" width="20" height="20" align="absmiddle"> **Cold Sweat** | ✅ | ✅ | ✅ | — |
| <img alt="Tough As Nails icon" src="https://cdn.modrinth.com/data/ge1sOdFH/fa385c1d5ed5638282faf31e8fc94884bd60d86e.png" width="20" height="20" align="absmiddle"> **Tough As Nails** | ✅ | ✅ | ⏳ | 26.2: Pending Tough As Nails port to Minecraft 26.2 |
| <img alt="Vampirism icon" src="https://cdn.modrinth.com/data/jVZ0F1wn/icon.png" width="20" height="20" align="absmiddle"> **Vampirism** | ✅ | ✅ | ⏳ | 26.2: Pending Vampirism port to Minecraft 26.2 |
| <img alt="Supernatural icon" src="https://cdn.modrinth.com/data/UXzKPebS/63ba2cf3a91eba83d797904ba8bfa81df089f854.png" width="20" height="20" align="absmiddle"> **Supernatural** | ✅ | ✅ | ⏳ | 26.2: Pending Supernatural port to Minecraft 26.2 |
| <img alt="Corail Tombstone icon" src="https://cdn.modrinth.com/data/YT5K34AB/b279e8756761d8160f678ad20fb632d0a09b89b4_96.webp" width="20" height="20" align="absmiddle"> **Corail Tombstone** | ✅ | ✅ | ✅ | — |
| <img alt="Farmer's Delight icon" src="https://cdn.modrinth.com/data/R2OftAxM/8e7aa38ab94d94bb0a2894a218b69beb49002b34.png" width="20" height="20" align="absmiddle"> **Farmer's Delight** | ✅ | ✅ | ⏳ | 26.2: Pending Farmer's Delight port to Minecraft 26.2 |
| <img alt="Farmer's Respite icon" src="https://cdn.modrinth.com/data/fasdaXH5/edba350f7617b0b0d724d6fcd7b432acd8f48de3_96.webp" width="20" height="20" align="absmiddle"> **Farmer's Respite** | ✅ | ✅ | ⏳ | 26.2: Pending Farmer's Respite port to Minecraft 26.2 |
| <img alt="Brewin' & Chewin' icon" src="https://cdn.modrinth.com/data/hIu9KJTT/f7c591a80046859d3d45c04ecbbc54d264483d5e.png" width="20" height="20" align="absmiddle"> **Brewin' & Chewin'** | ✅ | ✅ | ⏳ | 26.2: Pending Brewin' & Chewin' port to Minecraft 26.2 |
| <img alt="&#91;Let's Do&#93; Bakery icon" src="https://cdn.modrinth.com/data/oNB5jhlA/263380c2138f7f28f37639b719cc391334cbb7d5_96.webp" width="20" height="20" align="absmiddle"> **[Let's Do] Bakery** | ✅ | ✅ | ⏳ | 26.2: Pending Bakery port to Minecraft 26.2 |
| <img alt="&#91;Let's Do&#93; Brewery icon" src="https://cdn.modrinth.com/data/b7NV2plI/f254a05a63fabc3c3648618812301a5ffb37eed9_96.webp" width="20" height="20" align="absmiddle"> **[Let's Do] Brewery** | ✅ | ✅ | ⏳ | 26.2: Pending Brewery port to Minecraft 26.2 |
| <img alt="&#91;Let's Do&#93; Farm & Charm icon" src="https://cdn.modrinth.com/data/HJetCzWo/7c6c372629b3efa41409621631d60df12963f005_96.webp" width="20" height="20" align="absmiddle"> **[Let's Do] Farm & Charm** | ✅ | ✅ | ⏳ | 26.2: Pending Farm & Charm port to Minecraft 26.2 |
| <img alt="&#91;Let's Do&#93; Beachparty icon" src="https://cdn.modrinth.com/data/GyKzAh3l/41b9b45c365ecd55aced04bcd22af93878f766a0_96.webp" width="20" height="20" align="absmiddle"> **[Let's Do] Beachparty** | ✅ | ✅ | ⏳ | 26.2: Pending Beachparty port to Minecraft 26.2 |
| <img alt="&#91;Let's Do&#93; HerbalBrews icon" src="https://cdn.modrinth.com/data/Eh11TaTm/cea48ad39e9323e9e0f5354ee1d4c160f46b50be_96.webp" width="20" height="20" align="absmiddle"> **[Let's Do] HerbalBrews** | ✅ | ✅ | ⏳ | 26.2: Pending HerbalBrews port to Minecraft 26.2 |
| **Delight Addons** *(Collector's Reap, Fruits Delight, Delightful, Crock Pot, Hearth & Harvest, Vanilla Delight)* | ✅ | ✅ | ⏳ | 26.2: Pending addon ports to Minecraft 26.2 |
| <img alt="EnvironmentZ icon" src="https://cdn.modrinth.com/data/TggYmc6t/4bab25a93f9e7f06aa22ff3cf44b8b1c829927d4.png" width="20" height="20" align="absmiddle"> **EnvironmentZ** | ❌ | ❌ | ❌ | Not supported on NeoForge (Fabric exclusive) |

#### Forge

| Third-Party Mod | 1.20.1 | 1.21 / 1.21.1 | 26.2 | Unsupported Reason |
| :--- | :---: | :---: | :---: | :--- |
| <img alt="AppleSkin icon" src="https://cdn.modrinth.com/data/EsAfCjCV/icon.png" width="20" height="20" align="absmiddle"> **AppleSkin** | ✅ | ❌ | ❌ | 1.21+ & 26.2: Dropped Forge upstream (NeoForge & Fabric only) |
| <img alt="Jade icon" src="https://cdn.modrinth.com/data/nvQzSEkH/b04217bc2b7dc524c4d12f81ff42cc1cefb9b0fc_96.webp" width="20" height="20" align="absmiddle"> **Jade** | ✅ | ❌ | ❌ | 1.21+ & 26.2: Dropped Forge upstream (NeoForge & Fabric only) |
| <img alt="Create icon" src="https://cdn.modrinth.com/data/LNytGWDc/61d716699bcf1ec42ed4926a9e1c7311be6087e2_96.webp" width="20" height="20" align="absmiddle"> **Create** | ✅ | ❌ | ❌ | 1.21+ & 26.2: Create targets NeoForge exclusively |
| <img alt="Immersive Engineering icon" src="https://cdn.modrinth.com/data/tIm2nV03/icon.png" width="20" height="20" align="absmiddle"> **Immersive Engineering** | ✅ | ❌ | ❌ | 1.21+ & 26.2: Transitioned to NeoForge |
| <img alt="Cold Sweat icon" src="https://cdn.modrinth.com/data/uXhSmPjd/bf55420556c30d44d2f5cf7b8915705b9214b4ef.png" width="20" height="20" align="absmiddle"> **Cold Sweat** | ✅ | ❌ | ❌ | 1.21+ & 26.2: Transitioned to NeoForge |
| <img alt="Tough As Nails icon" src="https://cdn.modrinth.com/data/ge1sOdFH/fa385c1d5ed5638282faf31e8fc94884bd60d86e.png" width="20" height="20" align="absmiddle"> **Tough As Nails** | ✅ | ❌ | ❌ | 1.21+ & 26.2: NeoForge & Fabric only |
| <img alt="Vampirism icon" src="https://cdn.modrinth.com/data/jVZ0F1wn/icon.png" width="20" height="20" align="absmiddle"> **Vampirism** | ✅ | ❌ | ❌ | 1.21+ & 26.2: Transitioned to NeoForge |
| <img alt="Supernatural icon" src="https://cdn.modrinth.com/data/UXzKPebS/63ba2cf3a91eba83d797904ba8bfa81df089f854.png" width="20" height="20" align="absmiddle"> **Supernatural** | ✅ | ❌ | ❌ | 1.21+ & 26.2: Transitioned to NeoForge |
| <img alt="Corail Tombstone icon" src="https://cdn.modrinth.com/data/YT5K34AB/b279e8756761d8160f678ad20fb632d0a09b89b4_96.webp" width="20" height="20" align="absmiddle"> **Corail Tombstone** | ✅ | ❌ | ❌ | 1.21+ & 26.2: Transitioned to NeoForge |
| <img alt="Farmer's Delight icon" src="https://cdn.modrinth.com/data/R2OftAxM/8e7aa38ab94d94bb0a2894a218b69beb49002b34.png" width="20" height="20" align="absmiddle"> **Farmer's Delight** | ✅ | ❌ | ❌ | 1.21+ & 26.2: Upstream maintained on NeoForge/Fabric |
| <img alt="Farmer's Respite icon" src="https://cdn.modrinth.com/data/fasdaXH5/edba350f7617b0b0d724d6fcd7b432acd8f48de3_96.webp" width="20" height="20" align="absmiddle"> **Farmer's Respite** | ✅ | ❌ | ❌ | 1.21+ & 26.2: NeoForge only |
| <img alt="Brewin' & Chewin' icon" src="https://cdn.modrinth.com/data/hIu9KJTT/f7c591a80046859d3d45c04ecbbc54d264483d5e.png" width="20" height="20" align="absmiddle"> **Brewin' & Chewin'** | ✅ | ❌ | ❌ | 1.21+ & 26.2: NeoForge only |
| <img alt="&#91;Let's Do&#93; Bakery icon" src="https://cdn.modrinth.com/data/oNB5jhlA/263380c2138f7f28f37639b719cc391334cbb7d5_96.webp" width="20" height="20" align="absmiddle"> **[Let's Do] Bakery** | ✅ | ❌ | ❌ | 1.21+ & 26.2: Architectury NeoForge/Fabric only |
| <img alt="&#91;Let's Do&#93; Brewery icon" src="https://cdn.modrinth.com/data/b7NV2plI/f254a05a63fabc3c3648618812301a5ffb37eed9_96.webp" width="20" height="20" align="absmiddle"> **[Let's Do] Brewery** | ✅ | ❌ | ❌ | 1.21+ & 26.2: Architectury NeoForge/Fabric only |
| <img alt="&#91;Let's Do&#93; Farm & Charm icon" src="https://cdn.modrinth.com/data/HJetCzWo/7c6c372629b3efa41409621631d60df12963f005_96.webp" width="20" height="20" align="absmiddle"> **[Let's Do] Farm & Charm** | ✅ | ❌ | ❌ | 1.21+ & 26.2: Architectury NeoForge/Fabric only |
| <img alt="&#91;Let's Do&#93; Beachparty icon" src="https://cdn.modrinth.com/data/GyKzAh3l/41b9b45c365ecd55aced04bcd22af93878f766a0_96.webp" width="20" height="20" align="absmiddle"> **[Let's Do] Beachparty** | ✅ | ❌ | ❌ | 1.21+ & 26.2: Architectury NeoForge/Fabric only |
| <img alt="&#91;Let's Do&#93; HerbalBrews icon" src="https://cdn.modrinth.com/data/Eh11TaTm/cea48ad39e9323e9e0f5354ee1d4c160f46b50be_96.webp" width="20" height="20" align="absmiddle"> **[Let's Do] HerbalBrews** | ✅ | ❌ | ❌ | 1.21+ & 26.2: Architectury NeoForge/Fabric only |
| **Delight Addons** *(Collector's Reap, Fruits Delight, Delightful, Crock Pot, Hearth & Harvest, Vanilla Delight)* | ✅ | ❌ | ❌ | 1.21+ & 26.2: Upstream maintained on NeoForge/Fabric |
| <img alt="EnvironmentZ icon" src="https://cdn.modrinth.com/data/TggYmc6t/4bab25a93f9e7f06aa22ff3cf44b8b1c829927d4.png" width="20" height="20" align="absmiddle"> **EnvironmentZ** | ❌ | ❌ | ❌ | Not supported on Forge (Fabric exclusive) |

#### Fabric

| Third-Party Mod | 1.20.1 | 1.21 / 1.21.1 | 26.2 | Unsupported Reason |
| :--- | :---: | :---: | :---: | :--- |
| <img alt="AppleSkin icon" src="https://cdn.modrinth.com/data/EsAfCjCV/icon.png" width="20" height="20" align="absmiddle"> **AppleSkin** | ✅ | ✅ | ✅ | — |
| <img alt="Jade icon" src="https://cdn.modrinth.com/data/nvQzSEkH/b04217bc2b7dc524c4d12f81ff42cc1cefb9b0fc_96.webp" width="20" height="20" align="absmiddle"> **Jade** | ✅ | ✅ | ✅ | — |
| <img alt="EnvironmentZ icon" src="https://cdn.modrinth.com/data/TggYmc6t/4bab25a93f9e7f06aa22ff3cf44b8b1c829927d4.png" width="20" height="20" align="absmiddle"> **EnvironmentZ** | ✅ | ✅ | ⏳ | 26.2: Pending EnvironmentZ port to Minecraft 26.2 |
| <img alt="Create Fabric icon" src="https://cdn.modrinth.com/data/Xbc0uyRg/bb29eb217b1fd159579409b06ec7b976afd30b14_96.webp" width="20" height="20" align="absmiddle"> **Create** *(Create: Fabric — [Modrinth](https://modrinth.com/mod/create-fabric) · [CurseForge](https://www.curseforge.com/minecraft/mc-mods/create-fabric))* | ✅ | ❌ | ❌ | 1.21+ & 26.2: Create: Fabric is only maintained up to 1.20.1 |
| <img alt="Immersive Engineering icon" src="https://cdn.modrinth.com/data/tIm2nV03/icon.png" width="20" height="20" align="absmiddle"> **Immersive Engineering** | ❌ | ❌ | ❌ | Not supported on Fabric (Forge/NeoForge exclusive) |
| <img alt="Cold Sweat icon" src="https://cdn.modrinth.com/data/uXhSmPjd/bf55420556c30d44d2f5cf7b8915705b9214b4ef.png" width="20" height="20" align="absmiddle"> **Cold Sweat** | ❌ | ❌ | ❌ | Not supported on Fabric (Forge/NeoForge exclusive) |
| <img alt="Tough As Nails icon" src="https://cdn.modrinth.com/data/ge1sOdFH/fa385c1d5ed5638282faf31e8fc94884bd60d86e.png" width="20" height="20" align="absmiddle"> **Tough As Nails** | ❌ | ✅ | ⏳ | 1.20.1: No official Fabric port; 26.2: Pending upstream port |
| <img alt="Vampirism icon" src="https://cdn.modrinth.com/data/jVZ0F1wn/icon.png" width="20" height="20" align="absmiddle"> **Vampirism** | ❌ | ❌ | ❌ | Not supported on Fabric (Forge/NeoForge exclusive) |
| <img alt="Supernatural icon" src="https://cdn.modrinth.com/data/UXzKPebS/63ba2cf3a91eba83d797904ba8bfa81df089f854.png" width="20" height="20" align="absmiddle"> **Supernatural** | ❌ | ❌ | ❌ | Not supported on Fabric (Forge/NeoForge exclusive) |
| <img alt="Corail Tombstone icon" src="https://cdn.modrinth.com/data/YT5K34AB/b279e8756761d8160f678ad20fb632d0a09b89b4_96.webp" width="20" height="20" align="absmiddle"> **Corail Tombstone** | ❌ | ❌ | ❌ | Not supported on Fabric (Forge/NeoForge exclusive) |
| <img alt="Farmer's Delight Refabricated icon" src="https://cdn.modrinth.com/data/7vxePowz/26e8448993e9bda4dba92b6e7a1a13d9c4333138.png" width="20" height="20" align="absmiddle"> **Farmer's Delight** *(Refabricated — [Modrinth](https://modrinth.com/mod/farmers-delight-refabricated) · [CurseForge](https://www.curseforge.com/minecraft/mc-mods/farmers-delight-refabricated))* | ✅ | ✅ | ⏳ | 26.2: Pending Farmer's Delight port to Minecraft 26.2 |
| <img alt="Farmer's Respite icon" src="https://cdn.modrinth.com/data/fasdaXH5/edba350f7617b0b0d724d6fcd7b432acd8f48de3_96.webp" width="20" height="20" align="absmiddle"> **Farmer's Respite** | ✅ | ❌ | ❌ | 1.21+ & 26.2: Unofficial 1.21 port is NeoForge exclusive |
| <img alt="Brewin' & Chewin' icon" src="https://cdn.modrinth.com/data/hIu9KJTT/f7c591a80046859d3d45c04ecbbc54d264483d5e.png" width="20" height="20" align="absmiddle"> **Brewin' & Chewin'** | ✅ | ❌ | ❌ | 1.21+ & 26.2: NeoForge exclusive |
| <img alt="&#91;Let's Do&#93; Bakery icon" src="https://cdn.modrinth.com/data/oNB5jhlA/263380c2138f7f28f37639b719cc391334cbb7d5_96.webp" width="20" height="20" align="absmiddle"> **[Let's Do] Bakery** | ✅ | ✅ | ⏳ | 26.2: Pending Bakery port to Minecraft 26.2 |
| <img alt="&#91;Let's Do&#93; Brewery icon" src="https://cdn.modrinth.com/data/b7NV2plI/f254a05a63fabc3c3648618812301a5ffb37eed9_96.webp" width="20" height="20" align="absmiddle"> **[Let's Do] Brewery** | ✅ | ✅ | ⏳ | 26.2: Pending Brewery port to Minecraft 26.2 |
| <img alt="&#91;Let's Do&#93; Farm & Charm icon" src="https://cdn.modrinth.com/data/HJetCzWo/7c6c372629b3efa41409621631d60df12963f005_96.webp" width="20" height="20" align="absmiddle"> **[Let's Do] Farm & Charm** | ✅ | ✅ | ⏳ | 26.2: Pending Farm & Charm port to Minecraft 26.2 |
| <img alt="&#91;Let's Do&#93; Beachparty icon" src="https://cdn.modrinth.com/data/GyKzAh3l/41b9b45c365ecd55aced04bcd22af93878f766a0_96.webp" width="20" height="20" align="absmiddle"> **[Let's Do] Beachparty** | ✅ | ✅ | ⏳ | 26.2: Pending Beachparty port to Minecraft 26.2 |
| <img alt="&#91;Let's Do&#93; HerbalBrews icon" src="https://cdn.modrinth.com/data/Eh11TaTm/cea48ad39e9323e9e0f5354ee1d4c160f46b50be_96.webp" width="20" height="20" align="absmiddle"> **[Let's Do] HerbalBrews** | ✅ | ✅ | ⏳ | 26.2: Pending HerbalBrews port to Minecraft 26.2 |
| **Delight Addons** *(Collector's Reap, Fruits Delight, Delightful, Crock Pot, Hearth & Harvest, Vanilla Delight)* | ✅ | ✅ | ⏳ | 26.2: Pending addon ports to Minecraft 26.2 |

## Commands

Requires Operator permission (Level 2):
- `/thirst query <player>`: Displays current thirst, quenched saturation, and active EnvironmentZ temperature/modifier data.
- `/thirst set <player> <thirst> <quenched>`: Sets exact thirst (`0-20`) and quenched (`0-20`) values.
- `/thirst enable <player> <true|false>`: Enables or disables thirst mechanics for the target player (persists across death and respawn).

## Developer API

Entrypoint: `dev.minhnh.yetanotherthirst.api.YetAnotherThirstAPI`

```java
// Query and modify player state (auto-synced to client)
ThirstState state = YetAnotherThirstAPI.getThirstState(player);
int thirst = YetAnotherThirstAPI.getThirst(player);
int quenched = YetAnotherThirstAPI.getQuenched(player);
YetAnotherThirstAPI.setThirst(player, 20);
YetAnotherThirstAPI.setQuenched(player, 10);
YetAnotherThirstAPI.addExhaustion(player, 1.5F);
YetAnotherThirstAPI.drink(player, 6, 8);
YetAnotherThirstAPI.setThirstEnabled(player, false);

// Register custom drinks and foods (without overriding user configs)
YetAnotherThirstAPI.registerDrink(item, 8, 12);
YetAnotherThirstAPI.registerFood(item, 4, 5);

// Inspect and manipulate container water purity (0 = Dirty, 1 = Slightly Dirty, 2 = Acceptable, 3 = Purified)
if (YetAnotherThirstAPI.isWaterFilledContainer(stack)) {
    int purity = YetAnotherThirstAPI.getPurity(stack);
    YetAnotherThirstAPI.setPurity(stack, 3);
    YetAnotherThirstAPI.givePurityEffects(player, purity);
}
```

## Localization

Built-in translations for 9 languages: English (`en_us`), French (`fr_fr`), Japanese (`ja_jp`), Korean (`ko_kr`), Polish (`pl_pl`), Russian (`ru_ru`), Simplified Chinese (`zh_cn`), Traditional Chinese (`zh_tw`), and Vietnamese (`vi_vn`).

## License & Modpack Policy

- **License**: [MIT License](LICENSE)
- **Modpacks**: Free to include in any public or private modpack with credit to **MinhNH**.
- **Wiki**: [GitHub Wiki](https://github.com/minhnh303/Yet-Another-Thirst/wiki)
