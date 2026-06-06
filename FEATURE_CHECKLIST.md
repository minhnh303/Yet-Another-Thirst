# Feature Implementation Checklist

Scope: current multi-loader rebuild for Minecraft 1.20.1.

## Implemented

- [x] Shared thirst state model
- [x] Default thirst values: thirst `20`, quenched `5`, exhaustion `0`
- [x] Thirst value clamping to `0..20`
- [x] Overflow hydration conversion to quenched
- [x] Player thirst persistence through loader platform helpers
- [x] Server-side player thirst ticking
- [x] Hunger exhaustion tracking into thirst exhaustion
- [x] Quenched depletion before thirst depletion
- [x] Peaceful-mode thirst recovery behavior
- [x] Rain drinking while looking up
- [x] Nausea-based thirst exhaustion
- [x] Fire Resistance dehydration modifier
- [x] Ultra-warm dimension dehydration modifier
- [x] Dehydration damage when thirst reaches `0`
- [x] Vanilla potion thirst restoration
- [x] Vanilla food thirst restoration for configured base foods
- [x] Server-to-client thirst sync packet
- [x] Client-side thirst state sync handling
- [x] Thirst HUD droplet overlay
- [x] HUD texture asset copied under `yet_another_thirst`
- [x] `/thirst query <player>`
- [x] `/thirst set <player> <thirst> <quenched>`
- [x] `/thirst enable <players> <enabled>`
- [x] Forge event adapter
- [x] Forge network adapter
- [x] Forge client HUD adapter
- [x] NeoForge 1.20.1 event adapter
- [x] NeoForge 1.20.1 network adapter
- [x] NeoForge 1.20.1 client HUD adapter
- [x] Template demo mixins removed
- [x] Forge build passes
- [x] NeoForge build passes

## Implemented (continued)

- [x] Sprint prevention when thirsty (configurable threshold, default ≤ 6 / 3 droplets)
- [x] Health regeneration halted while thirst bar is not full (configurable)
- [x] Dedicated dehydration damage type (`yet_another_thirst:dehydrate`) with death messages
- [x] Forge ForgeConfigSpec common config (`yet_another_thirst/common.toml`)
- [x] Forge client config for HUD x/y offsets (`yet_another_thirst/client.toml`)
- [x] Expanded vanilla drink values: Honey Bottle, Milk Bucket
- [x] Expanded vanilla food values: Chorus Fruit, Suspicious Stew, raw/cooked fish, Dried Kelp, Bread, Pumpkin Pie, Cookie

## Implemented (continued)

- [x] Water purity model (`WaterPurity`, `ContainerWithPurity` in common)
- [x] Purity NBT data on items (`Purity` integer tag, 0–3)
- [x] Purity effects (nausea/hunger/poison on dirty/slightly-dirty/acceptable water)
- [x] Purity tooltips (colored purity level shown on water containers)
- [x] Purity-aware water potion, terracotta water bowl, water bucket (NBT-tagged)
- [x] Running-water pickup with purity (glass bottle, terracotta bowl via RightClickItem)
- [x] Hand drinking (shift-right-click water with empty hand, client→server packet)
- [x] Custom clay bowl item (`yet_another_thirst:clay_bowl`)
- [x] Custom terracotta bowl item (`yet_another_thirst:terracotta_bowl`)
- [x] Custom terracotta water bowl item (`yet_another_thirst:terracotta_water_bowl`, drinkable)
- [x] Drinkable custom item behavior (`DrinkableItem` with purity effect + container return)
- [x] Creative tab (`itemGroup.yet_another_thirst`) with all purity variants
- [x] Recipes — clay bowl craft, terracotta bowl smelt/campfire, purity-upgrade smelting for bowl + water bottle
- [x] Loot modifiers — custom items injected into simple_dungeon, abandoned_mineshaft, shipwreck_supply, bastion_other, nether_bridge chests
- [x] Purity config in `yet_another_thirst/common.toml` (all chance values + world thresholds + hand-drink settings)

## Partial

- [ ] Environmental dehydration modifiers beyond ultra-warm dimensions and Fire Resistance
- [ ] Runtime reload of drink values
- [ ] Loader-neutral storage abstraction beyond persistent NBT delegation
- [x] Cauldron purity block state (`BLOCK_PURITY` injected into `LayeredCauldronBlock` via Forge mixin; cauldron interactions replace glass bottle and bucket with purity-aware versions)
- [x] Dispenser purity-aware fill behavior (bucket and glass bottle dispenser behaviors registered in `FMLCommonSetupEvent`)

## Implemented (continued)

- [x] Water bottle stack-size change (`MixinItemStack` injects into `ItemStack.getMaxStackSize`; stack size configurable via `yet_another_thirst/common.toml`)
- [x] Tombstone ghostly-shape compatibility (skip thirst tick while player has `ghostly_shape` effect)
- [x] Vampirism compatibility (skip thirst tick and hide HUD for vampire players via reflection on `Helper.isVampire`)
- [x] Farmers Delight nourishment compatibility (skip exhaustion accumulation while Nourishment effect is active)
- [x] Lets Do Bakery stuffed compatibility (skip exhaustion accumulation while Stuffed effect is active)
- [x] Lets Do Brewery saturated compatibility (skip exhaustion accumulation while Saturated effect is active)

## Not Implemented

- [ ] Quenchness effect
- [ ] AppleSkin compatibility (needs `squeek.appleskin` optional compile dep)
- [ ] Create compatibility (needs `create` optional compile dep + block entity)
- [ ] Tough As Nails compatibility (needs `toughasnails` optional compile dep)
- [ ] Jade compatibility (needs `snownee.jade` optional compile dep)
- [ ] Cold Sweat compatibility (needs `coldsweat` optional compile dep)
- [ ] Supernatural compatibility
- [ ] Unit tests
- [ ] GameTest/runtime test coverage
