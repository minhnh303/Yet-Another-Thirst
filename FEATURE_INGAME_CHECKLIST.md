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
