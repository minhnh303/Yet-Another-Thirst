# Feature Implementation Checklist

Scope: current multi-loader rebuild for Minecraft 1.20.1.

## Implemented

### Core Thirst Mechanics & HUD
- [x] Shared thirst state model
- [x] Default thirst values: thirst `20`, quenched `5`, exhaustion `0`
- [x] Thirst value clamping to `0..20`
- [x] Overflow hydration conversion to quenched
- [x] Server-side player thirst ticking
- [x] Hunger exhaustion tracking into thirst exhaustion
- [x] Quenched depletion before thirst depletion
- [x] Peaceful-mode thirst recovery behavior
- [x] Thirst HUD droplet overlay
- [x] HUD texture asset copied under `yet_another_thirst`
- [x] Server-to-client thirst sync packet
- [x] Client-side thirst state sync handling
- [x] Player thirst persistence through loader platform helpers

### Dehydration & Depletion Modifiers
- [x] Nausea-based thirst exhaustion
- [x] Fire Resistance dehydration modifier
- [x] Ultra-warm dimension dehydration modifier
- [x] Dehydration damage when thirst reaches `0`
- [x] Sprint prevention when thirsty (configurable threshold, default ≤ 6 / 3 droplets)
- [x] Health regeneration halted while thirst bar is not full (configurable)
- [x] Dedicated dehydration damage type (`yet_another_thirst:dehydrate`) with death messages
- [x] Environmental dehydration modifiers beyond ultra-warm dimensions and Fire Resistance (biome temperature/precipitation + Fire Protection)

### Food & Drink Restoration
- [x] Vanilla potion thirst restoration
- [x] Vanilla food thirst restoration for configured base foods
- [x] Expanded vanilla drink values: Honey Bottle, Milk Bucket
- [x] Expanded vanilla food values: Chorus Fruit, Suspicious Stew, raw/cooked fish, Dried Kelp, Bread, Pumpkin Pie, Cookie

### Water Purity & Gathering
- [x] Water purity model (`WaterPurity`, `ContainerWithPurity` in common)
- [x] Purity NBT data on items (`Purity` integer tag, 0–3)
- [x] Purity effects (nausea/hunger/poison on dirty/slightly-dirty/acceptable water)
- [x] Purity tooltips (colored purity level shown on water containers)
- [x] Purity-aware water potion, terracotta water bowl, water bucket (NBT-tagged)
- [x] Running-water pickup with purity (glass bottle, terracotta bowl via RightClickItem)
- [x] Hand drinking (shift-right-click water with empty hand, client→server packet)
- [x] Rain drinking while looking up

### Custom Items, Crafting, and Loot
- [x] Custom clay bowl item (`yet_another_thirst:clay_bowl`)
- [x] Custom terracotta bowl item (`yet_another_thirst:terracotta_bowl`)
- [x] Custom terracotta water bowl item (`yet_another_thirst:terracotta_water_bowl`, drinkable)
- [x] Custom wooden water bowl item (`yet_another_thirst:wooden_water_bowl`, drinkable)
- [x] Drinkable custom item behavior (`DrinkableItem` with purity effect + container return)
- [x] Creative tab (`itemGroup.yet_another_thirst`) with all purity variants
- [x] Recipes — clay bowl craft, terracotta bowl smelt/campfire, purity-upgrade smelting and campfire cooking for water bottle, terracotta water bowl, and water bucket
- [x] Loot modifiers — custom items injected into simple_dungeon, abandoned_mineshaft, shipwreck_supply, bastion_other, nether_bridge chests
- [x] Water bottle stack-size change (`MixinItemStack` injects into `ItemStack.getMaxStackSize`; stack size configurable via `yet_another_thirst/common.toml`; mixin moved to `common` so NeoForge also benefits)

### Commands & Admin Tools
- [x] `/thirst query <player>`
- [x] `/thirst set <player> <thirst> <quenched>`
- [x] `/thirst enable <players> <enabled>`

### Configurations
- [x] Forge ForgeConfigSpec common config (`yet_another_thirst/common.toml`)
- [x] Forge client config for HUD x/y offsets (`yet_another_thirst/client.toml`)
- [x] Purity config in `yet_another_thirst/common.toml` (all chance values + world thresholds + running-water purity + hand-drink settings)
- [x] Runtime reload of Forge drink/food thirst values from `yet_another_thirst/common.toml`
- [x] Forge item-value config supports item IDs, item tags, blacklist, and optional-mod default entries

### Platform & Loader Implementations
- [x] Forge event adapter
- [x] Forge network adapter
- [x] Forge client HUD adapter
- [x] NeoForge 1.20.1 event adapter
- [x] NeoForge 1.20.1 network adapter
- [x] NeoForge 1.20.1 client HUD adapter
- [x] Template demo mixins removed
- [x] Forge build passes
- [x] NeoForge build passes
- [x] Unified developer API class (`YetAnotherThirstAPI`) and default helper methods in `IPlatformHelper` for easy integration

### Custom Mob Effects
- [x] Hydration and Thirsty effects (`yet_another_thirst:hydration`, `yet_another_thirst:thirsty`)

### Third-Party Mod Compatibility
- [x] Dynamic effect-based compatibility config (configurable lists for suspending thirst, pausing depletion, or regenerating thirst via `suspendThirstEffects`, `pauseDepletionEffects`, `regenThirstEffects` with custom amplifier conditions)
- [x] Refactored Tombstone ghostly-shape, Farmers Delight nourishment, Let's Do Bakery stuffed, and Let's Do Brewery saturated compatibility to utilize the new dynamic effects config system
- [x] Vampirism compatibility (skip thirst tick and hide HUD for vampire players via loader-neutral reflection checking `Helper.isVampire`)
- [x] Let's Do Farm & Charm compatibility (satiation, sustenance, and feast effects mapped to dynamic pause/regen configuration)
- [x] AppleSkin compatibility (compile-only AppleSkin API dependency, thirst tooltips, held-item HUD preview)
- [x] Jade compatibility (compile-only Jade dependency, water and cauldron purity component)
- [x] Tough As Nails compatibility (configurable coexistence mode with default YAT auto-disable)
- [x] Cold Sweat compatibility (body-temperature dehydration modifier with YAT environment modifier replacement)
- [x] Supernatural compatibility (vampire detection suspends YAT thirst and hides HUD)

## Partial

- [ ] Loader-neutral storage abstraction beyond persistent NBT delegation
- [x] Cauldron purity block state (`BLOCK_PURITY` injected into `LayeredCauldronBlock` via Forge mixin; cauldron interactions replace glass bottle and bucket with purity-aware versions)
- [x] Dispenser purity-aware fill behavior (bucket and glass bottle dispenser behaviors registered in `FMLCommonSetupEvent`)

## Not Implemented

- [ ] Unit tests
- [ ] GameTest/runtime test coverage
