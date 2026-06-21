# In-Game Feature Testing Checklist

Use this checklist to verify the functionality of Yet Another Thirst in-game.

---

## 1. Commands & Admin Tools
Verify command registrations and execution results.

- [x] Run `/thirst query <player>`
  - Expect: Prints the targeted player's current thirst and quenched values.
- [x] Run `/thirst set <player> <thirst> <quenched>`
  - Expect: Capped between 0 and 20. Instantly updates the client HUD.
- [x] Run `/thirst enable <player> <true/false>`
  - Expect: When false, freezes the thirst ticks, removes the thirst HUD, and performs a clean sync.

---

## 2. Thirst HUD & Core Mechanics
Verify that HUD elements rendering, peaceful settings, and core sync function correctly.

- [x] HUD Rendering
  - Expect: Thirst droplets overlay matches vanilla food bar style.
  - Offset Config test: Edit `yet_another_thirst/client.toml` to change `hudXOffset` and `hudYOffset`. Verify offsets reflect on reload.
- [x] Overflow Hydration
  - Expect: Drinking when thirst is full converts extra value to quenched hydration (if `extraHydrationConvertsToQuenched` is true).
- [x] Peaceful Mode Depletion
  - Expect: Thirst does not deplete by default in Peaceful unless `thirstDepletesInPeaceful` is set to true.
  - Recovery: If disabled, thirst should slowly regenerate (+1 thirst per tick cycle).
- [x] Persistence
  - Expect: Thirst/quenched values persist across dimensions, player death, and server logout/login.

---

## 3. Water Purity & World Gathering
Test how drinking from different sources impacts player health.

### Purity Level Definition Table
| Purity Level | Name | Color Indicator | Poison / Nausea Chance | Default Restoration |
| :--- | :--- | :--- | :--- | :--- |
| 0 | Dirty | Dark Brown | 30% Poison, 100% Nausea | +6 Thirst, +8 Quenched (Bottle) |
| 1 | Slightly Dirty | Gray-Brown | 10% Poison, 50% Nausea | +6 Thirst, +8 Quenched (Bottle) |
| 2 | Acceptable | Blue-Gray | 0% Poison, 5% Nausea | +6 Thirst, +8 Quenched (Bottle) |
| 3 | Purified | Bright Blue | 0% Poison, 0% Nausea | +6 Thirst, +8 Quenched (Bottle) |

- [x] Water Collection Purity from World
  - Expect: Glass Bottle, Terracotta Bowl, or vanilla Bowl filled from water blocks derives purity from coordinates and flow:
    - Normal ground water (Y 48 to 100): Purity 0 (Dirty).
    - Mountain spring (`y > 100`): Purity 1 (Slightly Dirty).
    - Cave spring (`y < 48`): Purity 1 (Slightly Dirty).
    - Running water (non-source flowing block): Purity level is increased by +1 from coordinate default.
- [x] Wooden Water Bowl filling and drinking
  - Expect: Filling an empty vanilla Bowl from a water source or cauldron produces a purity-aware Wooden Water Bowl. Drinking it restores thirst/quenched and returns an empty Bowl.
- [x] Water Bottle Stacking
  - Expect: Water bottles stack up to 64 (or configured stack limit).
  - Matching tags: Water bottles of the same purity stack together. Bottles with different purity levels do not.
  - Normal potions still stack to 1.
- [x] Cauldron Integration
  - Expect: Cauldron has `purity` block state (0-4).
  - Empty glass bottle / bucket / bowl filled from water cauldron gets the cauldron block's purity level.
  - Filling empty cauldron or adding water from a purity-aware bottle/bucket/bowl correctly stores/blends the purity level into the cauldron block state.
- [x] Dispenser Interaction
  - Expect: Dispensing an empty bucket into water source extracts purity-aware water bucket.
  - Dispensing empty glass bottle into water extracts purity-aware water bottle.

---

## 4. Crafting & Smelting Recipes
Verify custom crafting, smelting, and cooking operations.

- [x] Clay Bowl Crafting
  - Recipe: 3 Clay Balls (shaped like bucket/bowl) -> 4 Clay Bowls.
- [x] Terracotta Bowl Smelting
  - Recipe: Smelt Clay Bowl in furnace or campfire -> 1 Terracotta Bowl.
- [x] Water Bottle Purification
  - Expect: Smelting in a furnace or cooking on a campfire.
    - Dirty (Purity 0) or Slightly Dirty (Purity 1) Water Bottle -> Acceptable (Purity 2) Water Bottle.
    - Acceptable (Purity 2) or default Water Bottle -> Purified (Purity 3) Water Bottle.
- [x] Terracotta Water Bowl Purification
  - Expect: Smelting in a furnace or cooking on a campfire.
    - Dirty (Purity 0) or Slightly Dirty (Purity 1) Terracotta Water Bowl -> Acceptable (Purity 2) Terracotta Water Bowl.
    - Acceptable (Purity 2) Terracotta Water Bowl -> Purified (Purity 3) Terracotta Water Bowl.
- [x] Water Bucket Purification
  - Expect: Smelting in a furnace or cooking on a campfire.
    - Dirty (Purity 0) or Slightly Dirty (Purity 1) Water Bucket -> Acceptable (Purity 2) Water Bucket.
    - Acceptable (Purity 2) Water Bucket -> Purified (Purity 3) Water Bucket.
- [ ] Filter Core Crafting
  - Fabric Filter Core: shaped recipe (wool + string arrangement).
  - Sand Filter Core: shaped recipe (sand layers).
  - Carbon Filter Core: shaped recipe (charcoal/carbon arrangement).
- [ ] Clogged Fabric Filter Washing
  - Recipe: Shapeless — Clogged Fabric Filter Core + Water Bucket -> Fabric Filter Core (clean).
  - Expect: Returns a fresh Fabric Filter Core with full lifespan.
- [ ] Clogged Carbon Filter Recycling
  - Recipe: Smelt Clogged Carbon Filter Core in furnace or campfire -> Carbon Filter Core (clean).
- [ ] Filter Frame Crafting (Create loaded)
  - Recipe: Shaped — copper casings + fluid tanks -> Filter Frame block.

---

## 5. Dehydration & Depletion Modifiers
Test the negative effects of run-out thirst, movement block, and environmental modifiers.

- [x] Dehydration Damage
  - Expect: Thirst at 0 inflicts damage over time using `yet_another_thirst:dehydrate` damage source.
  - Difficulty Scaling: Configurable damage amount, tick interval, and minimum health limits per difficulty:
    - Damage: `dehydrationDamage` (default 1.0)
    - Interval: `damageIntervalTicks` (default 40)
    - Easy/Peaceful limit: `dehydrationDamageEasyLimit` (default 10.0)
    - Normal limit: `dehydrationDamageNormalLimit` (default 0.0)
    - Hard limit: `dehydrationDamageHardLimit` (default 0.0)
- [x] Sprint Prevention
  - Expect: Blocking sprints when thirst <= `sprintThreshold` (default 6).
- [x] Health Regeneration Halt
  - Expect: Natural health regeneration is paused while thirst is not full (if `dehydrationHaltsHealthRegen` is true).
- [x] Dimension Modifier
  - Expect: Thirst depletes 3x faster in the Nether (or configured `netherThirstDepletionModifier`).
- [x] Biome Temperature & Humidity Modifiers
  - Expect: Hot/dry biomes increase depletion; cold/rainy biomes decrease depletion.
- [x] Active Dehydration Mitigations
  - Expect: Fire Resistance effect reduces depletion to 0.
  - Fire Protection armor enchantments reduce thirst depletion rate.

---

## 6. Drinking Alternatives
Test alternative methods for satisfying thirst.

- [x] Rain Drinking
  - Expect: Stand under rain looking straight up (pitch <= -80 degrees). Restores thirst and quenched.
- [x] Hand Drinking
  - Config: Enable `canDrinkByHand` in `yet_another_thirst/common.toml`.
  - Expect: Shift-right-click a water block with an empty hand. Restores thirst and quenched.

---

## 7. Mod Compatibilities
Verify integration triggers for optional third-party mods.

- [x] Tombstone (Dynamic Config)
  - Expect: Depletion is suspended while player has the `ghostly_shape` effect active (via `suspendThirstEffects`).
- [x] Vampirism (Loader-neutral)
  - Expect: If player is a vampire, thirst depletion is paused, and thirst HUD is hidden.
- [x] Farmer's Delight (Dynamic Config)
  - Expect: Depletion is paused and thirst regenerates when the `nourishment` effect is active (via `pauseDepletionEffects` and `regenThirstEffects`).
- [x] Let's Do Bakery (Dynamic Config)
  - Expect: Depletion is paused when the `stuffed` effect is active (via `pauseDepletionEffects`).
- [x] Let's Do Brewery (Dynamic Config)
  - Expect: Depletion is paused when the `saturated` effect is active (via `pauseDepletionEffects`).
- [x] Let's Do Farm & Charm (Dynamic Config)
  - Expect: Depletion is paused when `satiation`, `sustenance`, or `feast` effects are active (via `pauseDepletionEffects`), and thirst regenerates when `sustenance` is active (via `regenThirstEffects`).
- [ ] AppleSkin
  - Expect: Hovering over any drinkable item shows a thirst tooltip preview (droplet icons) indicating thirst/quenched restoration.
  - Expect: Holding a drinkable item shows a HUD overlay preview of the thirst change on the thirst bar.
  - Expect: When AppleSkin thirst tooltips are disabled in its config, text-based thirst tooltips are shown instead.
- [ ] Jade
  - Expect: Looking at a water source block or cauldron shows purity level in the Jade tooltip (e.g. "Purity: Dirty").
  - Expect: Looking at any fluid handler containing water shows purity prepended to the fluid name.
  - Expect: Looking at a Filter Frame shows filter core durability %, input/output purity, and clogged status.
  - Expect: Thirst icon renders inside item-frame Jade tooltip for thirst-restoring items.
- [ ] Tough As Nails
  - Expect: With `toughAsNailsDisablesYAT` enabled (default), YAT thirst depletion is suspended when TAN is loaded (TAN manages thirst instead).
  - Expect: Toggling the config off allows both mods to run simultaneously.
- [ ] Cold Sweat
  - Expect: Player body temperature above `coldSweatBurningBodyTemperature` (default 100) increases thirst depletion.
  - Expect: Depletion modifier scales up to `coldSweatMaxDehydrationModifier` (default 1.75x) at peak temperature.
  - Expect: YAT environment-based modifier (biome temp/humidity) is replaced by Cold Sweat's body temperature modifier when Cold Sweat is loaded.
- [ ] Supernatural
  - Expect: Supernatural vampire players have thirst depletion fully suspended and the thirst HUD hidden.
- [ ] Create
  - Expect: Wrench right-clicking the Filter Frame toggles input/output flow direction.
  - Expect: Engineer's Goggles overlay on Filter Frame shows: input/output tank capacities, fluid purity levels, and filter core durability %.
  - Expect: Engineer's Goggles overlay on a Create Fluid Tank shows stored water purity level.
  - Expect: Water drained from the world via Open-Ended Pipes + mechanical pump preserves position-based purity.
  - Expect: Filling/emptying containers via Spout preserves water purity on the item.
  - Expect: Create Splashing recipe washes Clogged Fabric Filter Core -> Fabric Filter Core.
  - Expect: Create Splashing recipe washes Clogged Sand Filter Core -> Sand Filter Core.
- [ ] Immersive Engineering
  - Expect: Water blocks harvested by IE Fluid Pumps carry the position-based purity level (matching world-gather purity rules).
  - Expect: IE External Heater attached to Water Boiler powers it without fuel.

---

## 8. Loot Table Chest Injections
Verify that custom bowls spawn as dungeon loot.

- [x] Chest Types
  - Simple Dungeon (`minecraft:chests/simple_dungeon`)
  - Abandoned Mineshaft (`minecraft:chests/abandoned_mineshaft`)
  - Shipwreck Supply (`minecraft:chests/shipwreck_supply`)
  - Bastion Other (`minecraft:chests/bastion_other`)
  - Nether Bridge (`minecraft:chests/nether_bridge`)

---

## 9. Custom Mob Effects
Verify active effects and API functions.

- [x] Hydration Effect (`yet_another_thirst:hydration`)
  - Expect: Grants beneficial thirst and quenched values over time based on effect level/amplifier (e.g. +1 thirst and quenched per tick at level 1).
- [x] Thirsty Effect (`yet_another_thirst:thirsty`)
  - Expect: Speeds up thirst depletion by adding thirst exhaustion per tick (amplifier-scaled). Applied when receiving nausea from drinking dirty water.

---

## 10. Filter Frame & Water Boiler
Verify block machines, fluid filtering, and boiling mechanics.

### Filter Frame (`yet_another_thirst:filter_frame`)
- [ ] Placement & GUI
  - Expect: Place Filter Frame, right-click to open GUI with input/output fluid slots and filter core slot.
- [ ] Fabric Filter Core (Tier 1)
  - Expect: Insert Fabric Filter Core. Filter upgrades input water up to Purity 1 (Slightly Dirty). Lifespan shown as damage bar.
  - Decay: Dirty water (Purity 0) drains lifespan 4x faster; Slightly Dirty 2x faster; Acceptable 1x.
- [ ] Sand Filter Core (Tier 2)
  - Expect: Insert Sand Filter Core. Upgrades water up to Purity 2 (Acceptable). Higher durability than Fabric.
- [ ] Carbon Filter Core (Tier 3)
  - Expect: Insert Carbon Filter Core. Upgrades water up to Purity 3 (Purified). Highest durability.
- [ ] Filter Core Lifespan Damage Bar
  - Expect: Filter Core item in inventory shows a colored durability bar (green → yellow → red) as lifespan depletes.
  - Tooltip shows remaining lifespan % and max purity capability.
- [ ] Filter Core Clogging
  - Expect: When lifespan reaches 0, core is replaced by the matching Clogged variant (e.g. Clogged Fabric Filter Core). Filtering stops until replaced.
- [ ] Cauldron Washing (Clogged Fabric)
  - Expect: Right-click a water cauldron with a Clogged Fabric Filter Core -> returns Fabric Filter Core (full lifespan). Cauldron loses one water level.
- [ ] Jade Overlay (Filter Frame)
  - Expect: Jade tooltip on Filter Frame shows filter core material, durability %, input purity, output purity, and clogged warning in red if clogged.

### Water Boiler (`yet_another_thirst:water_boiler`)
- [ ] Placement & GUI
  - Expect: Place Water Boiler, right-click to open GUI with input/output fluid tanks and fuel slot.
- [ ] Boiling Purification
  - Expect: Water in input tank is heated over time and output as higher-purity water. Requires solid fuel (or IE External Heater).
  - Dirty (Purity 0) or Slightly Dirty (Purity 1) -> Acceptable (Purity 2).
  - Acceptable (Purity 2) -> Purified (Purity 3).
- [ ] IE External Heater Integration
  - Expect: Attaching an IE External Heater to the Water Boiler powers it without consuming fuel items.

---

## 11. Advancements
Verify each advancement triggers correctly in-game.

| Advancement | Title | Trigger |
| :--- | :--- | :--- |
| `root` | Stay Hydrated | Craft a Clay Bowl |
| `hand_drinking` | Desperate Measures | Drink from a water source block with bare hands |
| `rain_drinking` | Singin' in the Rain | Drink rain looking straight up during a storm |
| `dirty_water` | Risk Taker | Drink Dirty (Purity 0) water and receive poison/nausea |
| `filter_frame` | Pure Mechanics | Craft a Filter Frame block |
| `clogged_filter` | Wear and Tear | A filter core reaches 0 lifespan and becomes clogged |
| `wash_filter` | Reduce, Reuse, Recycle | Wash a Clogged Fabric Filter Core in a cauldron |
| `water_boiler` | Boiling Point | Craft a Water Boiler block |
| `purified_water` | Liquid Gold | Consume a Purified (Purity 3) drink |
| `dehydration_survival` | Brink of Death | Survive a dehydration damage tick at 0 thirst |
| `vampire_immunity` | Crimson Thirst | Become a Vampire (Vampirism/Supernatural) to suspend thirst |

- [ ] All advancements above trigger exactly once at the correct moment.
- [ ] Advancements appear in the correct tab in the Advancements screen under the YAT tree.
