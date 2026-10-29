# Yet Another Thirst

<p align="center">
  <img src="https://media.forgecdn.net/attachments/1724/119/yetanotherthirst_banner-png.png" alt="Yet Another Thirst Banner"/>
</p>

<p align="center">
  <a href="https://www.curseforge.com/minecraft/mc-mods/yet-another-thirst/files"><img src="https://cf.way2muchnoise.eu/versions/1563280(c70039).svg" alt="Supported Versions"/></a>
  <a href="https://www.curseforge.com/minecraft/mc-mods/yet-another-thirst"><img src="https://cf.way2muchnoise.eu/1563280.svg" alt="CurseForge Downloads"/></a>
  <a href="https://modrinth.com/mod/yet-another-thirst"><img src="https://img.shields.io/modrinth/dt/HOl2fYpO?logo=modrinth&label=Modrinth" alt="Modrinth Downloads"/></a>
</p>

**Yet Another Thirst** is a multi-loader survival mod introducing a hydration, water purity, and automated filtration/purification system to Minecraft. Rebuilt from the ground up based on [Thirst Was Taken](https://www.curseforge.com/minecraft/mc-mods/thirst-was-taken) (`ghen-git`) and inspired by Dehydration, Tough As Nails, and Cold Sweat.

## Supported Platforms & Versions

| Worktree / Branch | Minecraft Version | Mod Version | Java | Supported Loaders | Config Directory & Format |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `1.20.1` | `1.20.1` | `1.5.3` | Java 17 | Forge (`47.2.30+`), NeoForge (`47.1.99+`), Fabric (`0.15.11+`) | `config/yetanotherthirst/{common,items,compat,client}.toml` (Forge/NeoForge)<br>`config/yetanotherthirst/{common,items,compat,client}.json` (Fabric) |
| `1.21` | `1.21` - `1.21.1` | `1.21-1.5.2` | Java 21 | Forge (`51.0.33+`), NeoForge (`21.0.167+`), Fabric (`0.16.9+`) | `config/yetanotherthirst/{common,items,compat,client}.toml` (Forge/NeoForge)<br>`config/yetanotherthirst/{common,items,compat,client}.json` (Fabric) |

## Configuration Files

All configuration files are stored in `config/yetanotherthirst/` (`.toml` on Forge/NeoForge, `.json` on Fabric). Legacy configs in `config/yet_another_thirst/` are automatically migrated and split into the 4 files below on startup:

| File | Scope | Configured Settings |
| :--- | :--- | :--- |
| `common.toml` / `common.json` | Server / Common | General thirst rules, status effect lists (`suspendThirstEffects`, `pauseDepletionEffects`, `regenThirstEffects`), `Hydration`/`Thirsty` tick rates, dehydration damage, biome/Nether depletion modifiers, water purity chances, world altitude thresholds (`mountainsY`, `cavesY`), and hand drinking |
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
- **Campfire Cooking**: Purifies water containers by `+1` tier.
- **Furnace Smelting**: Purifies water containers by `+2` tiers.
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
- **Capacity & Consumption**: `2,000 mB` tank capacity, `200` ticks boil time, `32 FE/t` energy consumption.
- **Port Configuration**: Configurable inlet, outlet, and electrical connector ports per block face/half.
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

## Mod Compatibility

| Mod | Integration Behavior |
| :--- | :--- |
| **AppleSkin** | Renders thirst and quenched restoration values on item tooltips and HUD overlay |
| **Jade** | Displays water purity on world fluid blocks, cauldrons, fluid tanks, item frames, Filter Frames, and Water Boilers |
| **Create** | Filter Frame block, Engineer's Goggles purity/durability overlays, Open-Ended Pipe & Hose Pulley purity retention, Spout container filling, Splashing filter cleaning, `builders_tea` hydration |
| **Immersive Engineering** | External Heater integration for Water Boiler; Fluid Pump position-based water purity preservation |
| **Cold Sweat** | Scales thirst depletion with body temperature (`50.0` hot / `100.0` burning thresholds, up to `1.75x` modifier) and supports `filled_waterskin` |
| **EnvironmentZ** | Scales dehydration via configurable `playerTemperature` tiers, renders HUD temperature text aligned with the thermometer icon, and extends `/thirst query` |
| **Tough As Nails** | Configurable coexistence (`AUTO_DISABLE_YAT`, `ITEMS_ONLY`, `FORCE_YAT`, `OFF`) plus built-in canteen, juice, and ice cream hydration values |
| **Vampirism / Supernatural** | Automatically suspends thirst depletion and hides the thirst HUD for vampire players; supports `supernatural:blood_bottle` |
| **Corail Tombstone** | Suspends thirst depletion while `ghostly_shape` is active |
| **Food & Beverage Mods** | Pre-configured drink/food hydration values and buff integration (`Nourishment`, `Stuffed`, `Saturated`, `Satiation`, `Sustenance`, `Feast`) for: **Farmer's Delight**, **Farmer's Respite**, **Brewin' & Chewin'**, **Collector's Reap**, **Vanilla Delight**, **Fruits Delight**, **Delightful**, **Hearth & Harvest**, **Crock Pot**, **[Let's Do] Bakery**, **[Let's Do] Brewery**, **[Let's Do] Farm & Charm**, **[Let's Do] Beachparty**, **[Let's Do] HerbalBrews** |

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
