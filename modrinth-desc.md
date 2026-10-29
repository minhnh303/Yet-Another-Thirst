# Yet Another Thirst

![Yet Another Thirst Banner](https://cdn.modrinth.com/data/HOl2fYpO/images/914b048341d608fd718fb455ff7ac033b4c6dfef.png)

**Yet Another Thirst** is a highly polished survival mod that introduces a realistic hydration and water purification system to Minecraft. Built with modpack balance, visual excellence, and modern design in mind, it adds an immersive layer of survival without being tedious.

Whether you're exploring scorching deserts, dodging dehydration in the Nether, or brewing specialized teas for late-game hydration, this mod integrates seamlessly into your gameplay. The project utilizes a modern multi-loader structure to support both **Forge** and **NeoForge**.

*Note: This project is rebuilt based on the classic [Thirst Was Taken](https://www.curseforge.com/minecraft/mc-mods/thirst-was-taken) mod (originally created by **ghen-git**). Special thanks to them for the inspiration and codebase reference!*

---

## 💧 Core Survival Mechanics

### Hydration & Exhaustion System
* **Thirst HUD Overlay:** Adds a sleek, custom thirst bar (blue droplets) right above your food bar.
* **Exhaustion & Saturation:** Your thirst drains dynamically based on your physical actions. Sprinting, jumping, and fighting will make you thirsty much faster.
* **Sprint Prevention:** When your thirst bar drops too low, you will no longer be able to sprint, urging you to find water.
* **Halted Regeneration:** Natural health regeneration can be paused if you are dehydrated, making water a top priority during battles.
* **Dehydration Damage:** Running completely out of water triggers custom dehydration damage (`yet_another_thirst:dehydrate`) with unique death messages.

### Alternative Drinking Methods
* **Drink by Hand:** No bottle? No problem. Hold Shift and right-click a water block with empty hands to drink directly from the source.
* **Rain Drinking:** Look straight up at the sky during rainstorms to slowly quench your thirst.

---

### 💧 Thirst HUD & Overlay Preview
![A screenshot showing the blue Thirst droplets HUD overlay positioned next to the hunger bar in-game.](https://cdn.modrinth.com/data/HOl2fYpO/images/78724ad206b78dc90204a54c1a77cf4b760e23d0.png)

---

## 🧪 Water Purity & Purification

Not all water in the world is safe to drink. The mod features a comprehensive **Water Purity** system with four distinct tiers:

| Purity Level | Name | Source & Description | Effects When Consumed |
| :--- | :--- | :--- | :--- |
| **Tier 0** | **Dirty** | Surface water, swamps, and stagnant pools. | 100% Nausea, 30% Poison |
| **Tier 1** | **Slightly Dirty** | Underground cave springs and high mountain streams. | 50% Nausea, 10% Poison |
| **Tier 2** | **Acceptable** | Partially filtered or natural clean water. | 5% Nausea |
| **Tier 3** | **Purified** | Processed, boiled, and completely safe water. | None (100% Safe) |

### Environmental Purity
The purity of harvested water is determined by where you collect it. Mountain springs (high altitude) and cave springs (deep underground) yield cleaner water. Flowing water (running streams) receives a purity bonus over stagnant source blocks!

### How to Purify Water
Use heat to sanitize your water containers. You can boil water bottles or terracotta water bowls:
* **Campfire:** Increases the purity level by **1 step** (e.g., Dirty -> Slightly Dirty).
* **Furnace:** Purifies water by **2 steps** (e.g., Dirty -> Acceptable).

---

### 🔥 Boiling and Purifying Water
![A screenshot showing water bottles and terracotta water bowls placed on top of a lit Campfire, being boiled.](https://cdn.modrinth.com/data/HOl2fYpO/images/537bc459861f2bfbeb5dfcf8e0549d4d094d3ae6.png)

![A screenshot showing the Furnace UI open with a water bottle or terracotta water bowl in the input slot, actively being smelted.](https://cdn.modrinth.com/data/HOl2fYpO/images/0b308e950805ffdb193d33f440f95675608937ce.png)

---

## 🏺 Custom Early-Game Containers

To help you survive your first night, the mod introduces new crafting options:
* **Clay Bowls:** Craftable early-game using clay balls.
* **Terracotta Bowls:** Fire your clay bowls in a furnace or on a campfire.
* **Terracotta & Wooden Water Bowls:** Fill them up with water. They track purity just like glass bottles, allowing you to drink or boil them for safe hydration before you have iron or glass!

---

### 🏺 Crafting & Harvesting Containers
![A screenshot of the Crafting Table UI showing the recipe to craft a Clay Bowl from clay balls.](https://cdn.modrinth.com/data/HOl2fYpO/images/3f6f41d2a0b42adadac25259320af46706fafd20.png)

![A screenshot of the Furnace UI showing a Clay Bowl hovered in the input slot, with its item tooltip visible, before being fired into a Terracotta Bowl.](https://cdn.modrinth.com/data/HOl2fYpO/images/275a03c0d5c31f9cd699bc3bdd8fcbee42ee1bde.png)

![A screenshot of the Furnace UI showing the resulting Terracotta Bowl hovered in the output slot, with its item tooltip visible, after smelting is complete.](https://cdn.modrinth.com/data/HOl2fYpO/images/63560d72bf3b39135fa808794800d49c569e2f09.png)

![A screenshot showing 4 item tooltips side by side for each purity tier — Dirty, Slightly Dirty, Acceptable, and Purified — each displaying its purity label and colored indicator in the tooltip UI.](https://cdn.modrinth.com/data/HOl2fYpO/images/84bc7242c175f68614cb65555e0d60e822247d15.png)

---

## ⚙️ Cauldron & Dispenser Automation

* **Cauldron Purity Blending:** Cauldrons dynamically track and average out the purity of the water inside. If you mix purified water with dirty water, the cauldron's water purity shifts dynamically!
* **Dispenser Support:** Dispensers can automate your water processing by filling bottles and buckets, respecting the source block's purity level automatically.

---

## ☀ Biome Climate & Temperature Modifiers

* **Climate Depletion:** Thirst depletion is heavily dependent on the biome you are in. Hot, dry biomes (like Deserts, Savannas, or Badlands) cause rapid dehydration. Cold or humid biomes reduce depletion.
* **Nether Dehydration:** Being in the Nether makes you dehydrate 3x faster.
* **Heat Protection:** Having the *Fire Resistance* effect active completely stops thirst depletion from heat. Armor with *Fire Protection* enchantments also reduces dehydration rates.

---

## 🔌 Rich Mod Compatibility & Integrations

Yet Another Thirst is designed to be the ultimate modpack-friendly hydration mod. It features built-in compatibility with:

* **AppleSkin:** Visually previews how much thirst and hydration saturation a drink will restore directly on your HUD when holding it.
* **Jade:** Adds an in-world tooltip when looking at water blocks or cauldrons, showing their current purity level before you harvest.
* **Cold Sweat:** Seamlessly links with core body and ambient temperature. Your thirst rate adapts dynamically based on how hot or cold your body is.
* **Farmer's Delight & Beverage Mods:** 
  * Water bottles stack up to **64** to fit better in food-focused packs.
  * Consuming stews, meals, teas, or alcoholic beverages from mods like *Farmer's Delight*, *Respite*, and *Brewin' & Chewin'* provides long-lasting hydration.
  * Active food buffs (like Nourishment) pause thirst depletion and slowly regenerate hydration.
* **Let's Do Mods (Bakery, Brewery, Farm & Charm):** Native support for specialized teas, meals, and drinks. Food buffs (like Stuffed, Saturated, Satiation, Sustenance, or Feast) automatically pause thirst drain or restore hydration.
* **Vampirism & Supernatural:** Vampire players do not need water! The thirst mechanic is paused and the HUD is completely hidden if you become a vampire.
* **Tombstone:** The *Ghostly Shape* effect suspends thirst depletion.
* **Tough As Nails:** Configurable coexistence mode (can auto-disable YAT if Tough As Nails is active).

---

### 🍲 AppleSkin & Jade Integrations
![A screenshot showing Jade's in-world tooltip overlay displaying the purity level (e.g. "Purity: Purified") when the player looks at a water block or cauldron.](https://cdn.modrinth.com/data/HOl2fYpO/images/ef885b1cab437ec06d7b54d33a0ebddb8ae9dadc.png)

![A screenshot showing AppleSkin's HUD preview while the player holds a drink, displaying how much thirst and quenched saturation it will restore.](https://cdn.modrinth.com/data/HOl2fYpO/images/5db29445f19ac22c3149fffe768d2ccd5490a2a9.png)

---

## 🛠 Admin & Server Commands

Manage player thirst easily with commands (requires operator permission):
* `/thirst query <player>` - Check a player's exact thirst and saturation levels.
* `/thirst set <player> <thirst> <quenched>` - Set specific thirst and saturation levels.
* `/thirst enable <player> <true/false>` - Enable or disable thirst depletion for specific players.

---

## ⚙️ Fully Configurable

Almost every aspect of Yet Another Thirst can be customized in the `config/yet_another_thirst/common.toml` config file:
* Adjust exhaustion rates, damage intervals, and sprint thresholds.
* Change purity levels of world-harvested water.
* Customize which foods/drinks restore thirst and by how much.
* Add or remove status effects that pause or regenerate thirst.
* Reposition the HUD overlay to fit your custom UI layout.

### Out-of-the-Box Mod Integration

The default configuration ships **pre-tuned** for all supported companion mods so you get a smooth experience without any manual setup:

| Mod | Default Behavior |
| :--- | :--- |
| **AppleSkin** | Thirst & quenched preview enabled immediately after installation. |
| **Jade** | Water purity tooltips active on all water blocks and cauldrons out of the box. |
| **Cold Sweat** | Body-temperature-based thirst scaling enabled with sensible thresholds. |
| **Farmer's Delight** | Water bottles stack to 64; stews/meals grant hydration by default. |
| **Respite / Brewin' & Chewin'** | Teas and brews restore thirst without any extra configuration. |
| **Let's Do (Bakery, Brewery, Farm & Charm)** | Stuffed / Saturated / Satiation / Sustenance / Feast buffs auto-pause thirst drain. |
| **Vampirism** | Thirst mechanic and HUD automatically hidden for vampire players. |
| **Tombstone** | *Ghostly Shape* effect pre-mapped to suspend thirst depletion. |
| **Tough As Nails** | Coexistence mode pre-configured; YAT auto-disables if TAN is detected. |

> Simply drop the relevant mods into your `mods/` folder — no extra config editing required.

---

## 🗺️ Loot & Exploration

Survival requires searching for resources. You don't have to craft everything:
* **Dungeon Loot:** Clay Bowls and Terracotta Bowls can be found while exploring the world.
* **Chest Spawn Locations:** Injected natively into chest loot tables for **Simple Dungeons, Abandoned Mineshafts, Shipwrecks, Nether Bridges,** and **Bastions**.

---

## 🌟 Status Effects

Yet Another Thirst introduces custom status effects to manage hydration:

| Effect | Type | Registry ID | Color | Source / Trigger | Mechanic | Rate (per level) |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **Hydration** | Beneficial | `yet_another_thirst:hydration` | `#21B1FF` (Blue) | Drinking premium teas, beverages, or stews (via compatibility or configuration) | Restores thirst and quenched saturation | +1 thirst & quenched per tick (+20 per second at level I) |
| **Thirsty** | Harmful | `yet_another_thirst:thirsty` | `#B15B2A` (Brown) | Drinking unpurified water (applied for 30 seconds when nausea triggers) | Adds exhaustion | +0.005 exhaustion per tick (+0.1/s at level I; drains 1 point per 40s at rest) |

---

### ✨ Hydration Effect Preview
![A screenshot of the player's inventory/status screen showing the active Hydration effect icon with its blue color and timer.](https://cdn.modrinth.com/data/HOl2fYpO/images/678bc5d4a62e82f54eb411f368949d2d8eaf176e.png)

![A screenshot showing the player in-world under the Hydration status effect with blue particle effects surrounding them and the thirst bar visibly restoring.](https://cdn.modrinth.com/data/HOl2fYpO/images/c5d83f2ca9e3a19e7e8acf2e68382aeabae72362.png)
* **Hydration Effect:** When active, your thirst and quenched levels are continuously restored, allowing you to sustain activities without depleting your thirst bar.

---

### 🤢 Thirsty Effect Preview
![A screenshot of the player's inventory/status screen showing the active Thirsty effect icon with its brown color and timer.](https://cdn.modrinth.com/data/HOl2fYpO/images/3fcee6ffee51fa4ea48c270d33be5f2ccc04316a.png)

![A screenshot showing the player in-world under the Thirsty status effect with brown particle swirls surrounding them and the thirst bar visibly draining faster.](https://cdn.modrinth.com/data/HOl2fYpO/images/10cfd5806f43699e4cf50b931f501ca85ba95426.png)
* **Thirsty Effect:** Increases your exhaustion rate, causing your thirst to drain much faster. Usually contracted from drinking unpurified water.

---

## 🚀 Near-Future Plans

* **Create Mod Integration:** Add blocks and items to support automated water filtration using kinetic machinery.
* **Immersive Engineering Integration:** Add blocks and items to support large-scale water cooking, purification, and storage compatible with industrial themes.
* **Water Storage Expansion:** Add new items and containers specifically designed for storing and transporting clean water.
* **Sophisticated Storage & Backpacks Integration:** Support dynamic fluid storage upgrades inside backpacks and chests, allowing direct drinking and automatic refilling.

---

## 📦 Modpack Policy

* **100% Free to Use:** You are fully allowed to include Yet Another Thirst in any public or private modpack.
* **No Permission Needed:** There is no need to ask or register. All I ask is that you kindly credit me (MinhNH) as the creator in your modpack description.

---

## ❓ Frequently Asked Questions (FAQ)

### Can I play this mod on Peaceful difficulty?
Yes! By default, thirst does not deplete in Peaceful mode. If your thirst is low when switching to Peaceful, it will slowly regenerate (+1 thirst per tick cycle) until full. You can toggle this behavior in the config.

### How is this different from Thirst Was Taken?
Yet Another Thirst is a complete, modern rewrite targeting Minecraft 1.20.1+. It provides multi-loader support for both **Forge** and **NeoForge**, improved stability, more reliable mod integrations (via a dynamic status effect configuration), and fixes common issues with modern rendering pipelines.

### Is it compatible with other thirst mods?
Yes! If you have **Tough As Nails** installed, Yet Another Thirst includes a configurable coexistence mode that can automatically disable YAT to prevent conflict, allowing modpacks to switch between systems easily.

---

## ⚙️ Installation & Requirements

1. Make sure you are using **Minecraft 1.20.1+**.
2. Install the latest version of either **Forge** or **NeoForge**.
3. Download the matching **Yet Another Thirst** jar file.
4. Place the jar in your Minecraft instance's `mods` folder.
5. (Optional) Install **AppleSkin** and **Jade** for the best visual experience.
