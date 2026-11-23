# Yet Another Thirst

![Yet Another Thirst Banner](https://media.forgecdn.net/attachments/1724/119/yetanotherthirst_banner-png.png)

**Yet Another Thirst** is a highly polished survival mod that introduces a realistic hydration and water purification system to Minecraft. Built with modpack balance, visual excellence, and modern design in mind, it adds an immersive layer of survival without being tedious.

Whether you're exploring scorching deserts, dodging dehydration in the Nether, or brewing specialized teas for late-game hydration, this mod integrates seamlessly into your gameplay. The project utilizes a modern multi-loader structure to support **Forge**, **NeoForge**, and **Fabric** across **Minecraft 1.20.1, 1.21/1.21.1, and 26.2**.

_Note: This project is rebuilt based on the classic [Thirst Was Taken](https://www.curseforge.com/minecraft/mc-mods/thirst-was-taken) mod (originally created by **ghen-git**). Special thanks to them for the inspiration and codebase reference!_

> **Note:** If you can't find the build for your specific Minecraft version or mod loader, please use the **Comments** tab on this page or open an issue on **GitHub** to suggest it. I will try to review and implement it as soon as possible.

***

## 💧 Core Survival Mechanics

### Hydration & Exhaustion System

*   **Thirst HUD Overlay:** Adds a sleek, custom thirst bar (blue droplets) right above your food bar.
*   **Exhaustion & Saturation:** Your thirst drains dynamically based on your physical actions. Sprinting, jumping, and fighting will make you thirsty much faster.
*   **Sprint Prevention:** When your thirst bar drops too low, you will no longer be able to sprint, urging you to find water.
*   **Halted Regeneration:** Natural health regeneration can be paused if you are dehydrated, making water a top priority during battles.
*   **Dehydration Damage:** Running completely out of water triggers custom dehydration damage (`yet_another_thirst:dehydrate`) with unique death messages.

### Alternative Drinking Methods

*   **Drink by Hand:** No bottle? No problem. Hold Shift and right-click a water block with empty hands to drink directly from the source.
*   **Rain Drinking:** Look straight up at the sky during rainstorms to slowly quench your thirst.

***

### 💧 Thirst HUD & Overlay Preview

![A screenshot showing the blue Thirst droplets HUD overlay positioned next to the hunger bar in-game.](https://media.forgecdn.net/attachments/1724/35/thirst-hud-overlay-preview-png.png)

***

## 🧪 Water Purity & Purification

Not all water in the world is safe to drink. The mod features a comprehensive **Water Purity** system with four distinct tiers:

| Purity Level |Name           |Source &amp; Description                            |Effects When Consumed   |
| ------------ |-------------- |--------------------------------------------------- |----------------------- |
| <strong>Tier 0</strong> |<strong>Dirty</strong> |Surface water, swamps, and stagnant pools.          |100% Nausea, 30% Poison |
| <strong>Tier 1</strong> |<strong>Slightly Dirty</strong> |Underground cave springs and high mountain streams. |50% Nausea, 10% Poison  |
| <strong>Tier 2</strong> |<strong>Acceptable</strong> |Partially filtered or natural clean water.          |5% Nausea               |
| <strong>Tier 3</strong> |<strong>Purified</strong> |Processed, boiled, and completely safe water.       |None (100% Safe)        |

### Environmental Purity

The purity of harvested water is determined by where you collect it. Mountain springs (high altitude) and cave springs (deep underground) yield cleaner water. Flowing water (running streams) receives a purity bonus over stagnant source blocks!

### How to Purify Water

Use heat to sanitize your water containers. You can boil water bottles, terracotta water bowls, or water buckets:

*   **Campfire:** Increases the purity level by **1 step** (e.g., Dirty -> Slightly Dirty).
*   **Furnace:** Purifies water by **2 steps** (e.g., Dirty -> Acceptable).

***

### 🔥 Boiling and Purifying Water

![A screenshot showing water bottles and terracotta water bowls placed on top of a lit Campfire, being boiled.](https://media.forgecdn.net/attachments/1724/37/water-purification-campfire-png.png)

![A screenshot showing the Furnace UI open with a water bottle or terracotta water bowl in the input slot, actively being smelted.](https://media.forgecdn.net/attachments/1724/109/water-purification-furnace-png.png)

***

## 🏺 Custom Early-Game Containers

To help you survive your first night, the mod introduces new crafting options:

*   **Clay Bowls:** Craftable early-game using clay balls.
*   **Terracotta Bowls:** Fire your clay bowls in a furnace or on a campfire.
*   **Terracotta & Wooden Water Bowls:** Fill them up with water. They track purity just like glass bottles, allowing you to drink or boil them for safe hydration before you have iron or glass!

***

### 🏺 Crafting & Harvesting Containers

![A screenshot of the Crafting Table UI showing the recipe to craft a Clay Bowl from clay balls.](https://media.forgecdn.net/attachments/1724/28/crafting-clay-bowl-png.png)

![A screenshot of the Furnace UI showing a Clay Bowl hovered in the input slot, with its item tooltip visible, before being fired into a Terracotta Bowl.](https://media.forgecdn.net/attachments/1724/38/water-purification-furnace-input-png.png)

![A screenshot of the Furnace UI showing the resulting Terracotta Bowl hovered in the output slot, with its item tooltip visible, after smelting is complete.](https://media.forgecdn.net/attachments/1724/39/water-purification-furnace-output-png.png)

![A screenshot showing 4 item tooltips side by side for each purity tier — Dirty, Slightly Dirty, Acceptable, and Purified — each displaying its purity label and colored indicator in the tooltip UI.](https://media.forgecdn.net/attachments/1724/36/tooltip-purity-all-tiers-png.png)

***

## ⚙️ Cauldron & Dispenser Automation

*   **Cauldron Purity Tracking:** Cauldrons track water purity using worst-case (minimum) purity when mixing fluids, and can also be used to wash clogged Fabric Filter Cores.
*   **Dispenser Support:** Dispensers can automate your water processing by filling bottles and buckets, respecting the source block's purity level automatically.

***

## 🏭 Modular Filtration & Industrial Purification

For mid- to late-game automation, Yet Another Thirst provides dedicated filtration and boiling machinery:

### Filter Frame & Filter Cores _(Forge & NeoForge with Create)_

*   **Modular Filtration:** Install a filter core inside a **Filter Frame** (`1,000 mB` internal tank, Wrench & Engineer's Goggles support) to automatically purify water piped through Create fluid networks:
    *   **Fabric Filter Core:** Purifies up to **Tier 1 (Slightly Dirty)**; wash clogged cores in a cauldron, with a water bucket, or via Create Splashing.
    *   **Sand Filter Core:** Purifies up to **Tier 2 (Acceptable)**; clean clogged cores via Create Splashing.
    *   **Carbon Filter Core:** Purifies up to **Tier 3 (Purified)**; regenerate clogged cores in a furnace or on a campfire.

### Water Boiler _(Forge & NeoForge)_

*   **Industrial Boiling:** Heats and purifies water in bulk (`2,000 mB` tank, `200` ticks boil time, `32 FE/t`) with dynamic in-world fluid rendering and a custom GUI featuring an energy gauge (`FE/t` hover tooltip), temperature, and fluid indicators.
*   **Configurable Ports:** Use a hammer or wrench to configure inlet, outlet, and energy ports per face with localized action-bar feedback.
*   **Immersive Engineering Support:** Supports heating via IE External Heaters and preserves world water purity with IE Fluid Pumps.

***

## ☀ Biome Climate & Temperature Modifiers

*   **Climate Depletion:** Thirst depletion is heavily dependent on the biome you are in. Hot, dry biomes (like Deserts, Savannas, or Badlands) cause rapid dehydration. Cold or humid biomes reduce depletion.
*   **Nether Dehydration:** Being in the Nether makes you dehydrate 3x faster.
*   **Heat Protection:** Having the _Fire Resistance_ effect active completely stops thirst depletion from heat. Armor with _Fire Protection_ enchantments also reduces dehydration rates.

***

## 📦 Required Mods

| Loader | Required | Optional (Config GUI) |
| :--- | :--- | :--- |
| Fabric | Fabric Loader, <img alt="Fabric API icon" src="https://cdn.modrinth.com/data/P7dR8mSH/icon.png" width="20" height="20" align="absmiddle"> **Fabric API** ([CurseForge](https://www.curseforge.com/minecraft/mc-mods/fabric-api) · [Modrinth](https://modrinth.com/mod/fabric-api)) | <img alt="Mod Menu icon" src="https://cdn.modrinth.com/data/mOgUt4GM/5a20ed1450a0e1e79a1fe04e61bb4e5878bf1d20.png" width="20" height="20" align="absmiddle"> **Mod Menu** ([CurseForge](https://www.curseforge.com/minecraft/mc-mods/mod-menu) · [Modrinth](https://modrinth.com/mod/modmenu)) (needed to open the config GUI from the Mods list; without it, edit `config/yetanotherthirst/*.json` manually) |
| NeoForge / Forge | None | None (config GUI opens from the Mods list via **Config**) |

## 🔌 Rich Mod Compatibility & Integrations

Yet Another Thirst is designed to be the ultimate modpack-friendly hydration mod. It features built-in compatibility with:

*   <img alt="AppleSkin icon" src="https://cdn.modrinth.com/data/EsAfCjCV/icon.png" width="20" height="20" align="absmiddle"> **AppleSkin** ([CurseForge](https://www.curseforge.com/minecraft/mc-mods/appleskin) · [Modrinth](https://modrinth.com/mod/appleskin)): Visually previews how much thirst and hydration saturation a drink will restore directly on your HUD and item tooltips when holding it.
*   <img alt="Jade icon" src="https://cdn.modrinth.com/data/nvQzSEkH/b04217bc2b7dc524c4d12f81ff42cc1cefb9b0fc_96.webp" width="20" height="20" align="absmiddle"> **Jade** ([CurseForge](https://www.curseforge.com/minecraft/mc-mods/jade) · [Modrinth](https://modrinth.com/mod/jade)): Adds an in-world tooltip when looking at water blocks, cauldrons, fluid tanks, item frames, Filter Frames, and Water Boilers, showing their current purity level before you harvest.
*   <img alt="Create icon" src="https://cdn.modrinth.com/data/LNytGWDc/61d716699bcf1ec42ed4926a9e1c7311be6087e2_96.webp" width="20" height="20" align="absmiddle"> **Create** ([CurseForge](https://www.curseforge.com/minecraft/mc-mods/create) · [Modrinth](https://modrinth.com/mod/create)): Filter Frame block, Engineer's Goggles purity/durability overlays, Open-Ended Pipe & Hose Pulley purity retention, Spout container filling, Splashing filter cleaning, and Builder's Tea hydration.
*   <img alt="Immersive Engineering icon" src="https://cdn.modrinth.com/data/tIm2nV03/icon.png" width="20" height="20" align="absmiddle"> **Immersive Engineering** ([CurseForge](https://www.curseforge.com/minecraft/mc-mods/immersive-engineering) · [Modrinth](https://modrinth.com/mod/immersiveengineering)): External Heater integration for the Water Boiler and position-based water purity preservation with IE Fluid Pumps.
*   <img alt="Cold Sweat icon" src="https://cdn.modrinth.com/data/uXhSmPjd/bf55420556c30d44d2f5cf7b8915705b9214b4ef.png" width="20" height="20" align="absmiddle"> **Cold Sweat** ([CurseForge](https://www.curseforge.com/minecraft/mc-mods/cold-sweat) · [Modrinth](https://modrinth.com/mod/cold-sweat)): Seamlessly links with core body and ambient temperature, scaling thirst depletion dynamically and supporting filled waterskins.
*   <img alt="EnvironmentZ icon" src="https://cdn.modrinth.com/data/TggYmc6t/4bab25a93f9e7f06aa22ff3cf44b8b1c829927d4.png" width="20" height="20" align="absmiddle"> **EnvironmentZ (Fabric)** ([CurseForge](https://www.curseforge.com/minecraft/mc-mods/environmentz) · [Modrinth](https://modrinth.com/mod/environmentz)): Scales dehydration via configurable temperature tiers, displays HUD temperature text aligned with the thermometer icon, and extends `/thirst query`.
*   <img alt="Farmer's Delight icon" src="https://cdn.modrinth.com/data/R2OftAxM/8e7aa38ab94d94bb0a2894a218b69beb49002b34.png" width="20" height="20" align="absmiddle"> **Farmer's Delight & Beverage Mods:**
    *   Water bottles stack up to **64** to fit better in food-focused packs.
    *   Consuming stews, meals, teas, or alcoholic beverages from mods like [Farmer's Delight](https://www.curseforge.com/minecraft/mc-mods/farmers-delight), [Farmer's Respite](https://www.curseforge.com/minecraft/mc-mods/farmers-respite), [Brewin' & Chewin'](https://www.curseforge.com/minecraft/mc-mods/brewin-and-chewin), [Collector's Reap](https://www.curseforge.com/minecraft/mc-mods/collectors-reap), [Vanilla Delight](https://www.curseforge.com/minecraft/mc-mods/vanilla-delight), [Fruits Delight](https://www.curseforge.com/minecraft/mc-mods/fruits-delight), [Delightful](https://www.curseforge.com/minecraft/mc-mods/delightful), [Hearth & Harvest](https://www.curseforge.com/minecraft/mc-mods/hearth-and-harvest), and [Crock Pot](https://www.curseforge.com/minecraft/mc-mods/crock-pot) provides long-lasting hydration.
    *   Active food buffs (like Nourishment) pause thirst depletion and slowly regenerate hydration.
*   <img alt="Let's Do Farm & Charm icon" src="https://cdn.modrinth.com/data/HJetCzWo/7c6c372629b3efa41409621631d60df12963f005_96.webp" width="20" height="20" align="absmiddle"> **Let's Do Mods** ([Bakery](https://www.curseforge.com/minecraft/mc-mods/lets-do-bakery-farm-charm-compat), [Brewery](https://www.curseforge.com/minecraft/mc-mods/lets-do-brewery-farm-charm-compat), [Farm & Charm](https://www.curseforge.com/minecraft/mc-mods/lets-do-farm-charm), [Beachparty](https://www.curseforge.com/minecraft/mc-mods/lets-do-beachparty), [HerbalBrews](https://www.curseforge.com/minecraft/mc-mods/lets-do-herbalbrews)): Native support for specialized teas, meals, and drinks. Food buffs (like Stuffed, Saturated, Satiation, Sustenance, or Feast) automatically pause thirst drain or restore hydration.
*   <img alt="Vampirism icon" src="https://cdn.modrinth.com/data/jVZ0F1wn/icon.png" width="20" height="20" align="absmiddle"> **Vampirism** ([CurseForge](https://www.curseforge.com/minecraft/mc-mods/vampirism) · [Modrinth](https://modrinth.com/mod/vampirism)) & <img alt="Supernatural icon" src="https://cdn.modrinth.com/data/UXzKPebS/63ba2cf3a91eba83d797904ba8bfa81df089f854.png" width="20" height="20" align="absmiddle"> **Supernatural** ([CurseForge](https://www.curseforge.com/minecraft/mc-mods/supernatural) · [Modrinth](https://modrinth.com/mod/supernatural)): Vampire players do not need water! The thirst mechanic is paused and the HUD is completely hidden if you become a vampire.
*   <img alt="Corail Tombstone icon" src="https://cdn.modrinth.com/data/YT5K34AB/b279e8756761d8160f678ad20fb632d0a09b89b4_96.webp" width="20" height="20" align="absmiddle"> **Corail Tombstone** ([CurseForge](https://www.curseforge.com/minecraft/mc-mods/corail-tombstone) · [Modrinth](https://modrinth.com/mod/corail-tombstone)): The _Ghostly Shape_ effect suspends thirst depletion.
*   <img alt="Tough As Nails icon" src="https://cdn.modrinth.com/data/ge1sOdFH/fa385c1d5ed5638282faf31e8fc94884bd60d86e.png" width="20" height="20" align="absmiddle"> **Tough As Nails** ([CurseForge](https://www.curseforge.com/minecraft/mc-mods/tough-as-nails) · [Modrinth](https://modrinth.com/mod/tough-as-nails)): Configurable coexistence mode (can auto-disable YAT if Tough As Nails is active).

***

### 🍲 AppleSkin & Jade Integrations

![A screenshot showing Jade's in-world tooltip overlay displaying the purity level (e.g. "Purity: Purified") when the player looks at a water block or cauldron.](https://media.forgecdn.net/attachments/1724/33/jade-water-purity-tooltip-png.png)

![A screenshot showing AppleSkin's HUD preview while the player holds a drink, displaying how much thirst and quenched saturation it will restore.](https://media.forgecdn.net/attachments/1724/27/appleskin-thirst-preview-png.png)

***

## 🛠 Admin & Server Commands

Manage player thirst easily with commands (requires operator permission):

*   `/thirst query <player>` - Check a player's exact thirst and saturation levels.
*   `/thirst set <player> <thirst> <quenched>` - Set specific thirst and saturation levels.
*   `/thirst enable <player> <true/false>` - Enable or disable thirst depletion for specific players.

***

## ⚙️ Fully Configurable & In-Game Config GUI

Edit every setting directly inside Minecraft using the built-in **In-Game Config Screen** — open it from the **Config** button in the Mods list on **Forge/NeoForge**, or via **Mod Menu** on **Fabric**:

*   **Sidebar & Search:** Categorized pages with item icons, instant cross-page search, detailed tooltips (description, default, range), and one-click resets.
*   **Interactive List Editors:** Configure drinks, foods, blacklists, status effect rules (supporting `!=` amplifier comparisons), and EnvironmentZ tiers with item/effect icons, registry Tab-completion, and live validation.
*   **Live HUD Preview:** Reposition the thirst bar with a live preview of HUD offsets.
*   **Multiplayer Safe:** Server-side settings are read-only while connected to a remote server; singleplayer changes apply immediately.

Prefer editing files directly? Configs are split cleanly in `config/yetanotherthirst/{common,items,compat,client}.toml` (Forge/NeoForge) or `.json` (Fabric).

### Out-of-the-Box Mod Integration

The default configuration ships **pre-tuned** for all supported companion mods so you get a smooth experience without any manual setup:

| Mod | Default Behavior |
| :--- | :--- |
| <img alt="AppleSkin icon" src="https://cdn.modrinth.com/data/EsAfCjCV/icon.png" width="20" height="20" align="absmiddle"> <strong>AppleSkin</strong> ([CurseForge](https://www.curseforge.com/minecraft/mc-mods/appleskin) · [Modrinth](https://modrinth.com/mod/appleskin)) | Thirst &amp; quenched preview enabled immediately after installation. |
| <img alt="Jade icon" src="https://cdn.modrinth.com/data/nvQzSEkH/b04217bc2b7dc524c4d12f81ff42cc1cefb9b0fc_96.webp" width="20" height="20" align="absmiddle"> <strong>Jade</strong> ([CurseForge](https://www.curseforge.com/minecraft/mc-mods/jade) · [Modrinth](https://modrinth.com/mod/jade)) | Water purity tooltips active on water blocks, cauldrons, tanks, Filter Frames, and Water Boilers out of the box. |
| <img alt="Create icon" src="https://cdn.modrinth.com/data/LNytGWDc/61d716699bcf1ec42ed4926a9e1c7311be6087e2_96.webp" width="20" height="20" align="absmiddle"> <strong>Create</strong> ([CurseForge](https://www.curseforge.com/minecraft/mc-mods/create) · [Modrinth](https://modrinth.com/mod/create)) | Filter Frame, Goggles overlays, pipe/pulley purity retention, Spout filling, and Splashing enabled automatically. |
| <img alt="Immersive Engineering icon" src="https://cdn.modrinth.com/data/tIm2nV03/icon.png" width="20" height="20" align="absmiddle"> <strong>Immersive Engineering</strong> ([CurseForge](https://www.curseforge.com/minecraft/mc-mods/immersive-engineering) · [Modrinth](https://modrinth.com/mod/immersiveengineering)) | External Heater support for the Water Boiler and Fluid Pump purity preservation active automatically. |
| <img alt="Cold Sweat icon" src="https://cdn.modrinth.com/data/uXhSmPjd/bf55420556c30d44d2f5cf7b8915705b9214b4ef.png" width="20" height="20" align="absmiddle"> <strong>Cold Sweat</strong> ([CurseForge](https://www.curseforge.com/minecraft/mc-mods/cold-sweat) · [Modrinth](https://modrinth.com/mod/cold-sweat)) | Body-temperature-based thirst scaling enabled with sensible thresholds. |
| <img alt="EnvironmentZ icon" src="https://cdn.modrinth.com/data/TggYmc6t/4bab25a93f9e7f06aa22ff3cf44b8b1c829927d4.png" width="20" height="20" align="absmiddle"> <strong>EnvironmentZ</strong> ([CurseForge](https://www.curseforge.com/minecraft/mc-mods/environmentz) · [Modrinth](https://modrinth.com/mod/environmentz)) | Temperature tiers scale thirst depletion and render HUD temperature text out of the box (Fabric only). |
| <img alt="Farmer's Delight icon" src="https://cdn.modrinth.com/data/R2OftAxM/8e7aa38ab94d94bb0a2894a218b69beb49002b34.png" width="20" height="20" align="absmiddle"> <strong>Farmer's Delight &amp; Addons</strong> ([CurseForge](https://www.curseforge.com/minecraft/mc-mods/farmers-delight) · [Modrinth](https://modrinth.com/mod/farmers-delight)) | Water bottles stack to 64; stews, teas, juices, and meals from Farmer's Delight, Respite, Brewin' &amp; Chewin', Collector's Reap, Vanilla/Fruits Delight, Delightful, Hearth &amp; Harvest, and Crock Pot grant hydration by default. |
| <img alt="Let's Do Farm &amp; Charm icon" src="https://cdn.modrinth.com/data/HJetCzWo/7c6c372629b3efa41409621631d60df12963f005_96.webp" width="20" height="20" align="absmiddle"> <strong>Let's Do</strong> ([Bakery](https://www.curseforge.com/minecraft/mc-mods/lets-do-bakery-farm-charm-compat), [Brewery](https://www.curseforge.com/minecraft/mc-mods/lets-do-brewery-farm-charm-compat), [Farm &amp; Charm](https://www.curseforge.com/minecraft/mc-mods/lets-do-farm-charm), [Beachparty](https://www.curseforge.com/minecraft/mc-mods/lets-do-beachparty), [HerbalBrews](https://www.curseforge.com/minecraft/mc-mods/lets-do-herbalbrews)) | Pre-configured drink/food hydration values; Stuffed / Saturated / Satiation / Sustenance / Feast buffs auto-pause thirst drain. |
| <img alt="Vampirism icon" src="https://cdn.modrinth.com/data/jVZ0F1wn/icon.png" width="20" height="20" align="absmiddle"> <strong>Vampirism</strong> ([CurseForge](https://www.curseforge.com/minecraft/mc-mods/vampirism) · [Modrinth](https://modrinth.com/mod/vampirism)) / <img alt="Supernatural icon" src="https://cdn.modrinth.com/data/UXzKPebS/63ba2cf3a91eba83d797904ba8bfa81df089f854.png" width="20" height="20" align="absmiddle"> <strong>Supernatural</strong> ([CurseForge](https://www.curseforge.com/minecraft/mc-mods/supernatural) · [Modrinth](https://modrinth.com/mod/supernatural)) | Thirst mechanic and HUD automatically hidden for vampire players. |
| <img alt="Corail Tombstone icon" src="https://cdn.modrinth.com/data/YT5K34AB/b279e8756761d8160f678ad20fb632d0a09b89b4_96.webp" width="20" height="20" align="absmiddle"> <strong>Corail Tombstone</strong> ([CurseForge](https://www.curseforge.com/minecraft/mc-mods/corail-tombstone) · [Modrinth](https://modrinth.com/mod/corail-tombstone)) | <em>Ghostly Shape</em> effect pre-mapped to suspend thirst depletion. |
| <img alt="Tough As Nails icon" src="https://cdn.modrinth.com/data/ge1sOdFH/fa385c1d5ed5638282faf31e8fc94884bd60d86e.png" width="20" height="20" align="absmiddle"> <strong>Tough As Nails</strong> ([CurseForge](https://www.curseforge.com/minecraft/mc-mods/tough-as-nails) · [Modrinth](https://modrinth.com/mod/tough-as-nails)) | Coexistence mode pre-configured; YAT auto-disables if TAN is detected. |

> Simply drop the relevant mods into your `mods/` folder — no extra config editing required.

***

### 📊 Supported Integrations Matrix by Mod Loader

Detailed compatibility and integration breakdown across mod loaders and Minecraft versions:

#### 📌 Legend
* **✅ Supported:** Fully supported and integrated out of the box.
* **⏳ Pending Port:** Awaiting upstream companion mod update/port to this version.
* **❌ N/A:** Not available or unsupported on this mod loader / version.

#### 🔹 NeoForge

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
| <img alt="Let's Do Bakery icon" src="https://cdn.modrinth.com/data/oNB5jhlA/263380c2138f7f28f37639b719cc391334cbb7d5_96.webp" width="20" height="20" align="absmiddle"> **[Let's Do] Bakery** | ✅ | ✅ | ⏳ | 26.2: Pending Bakery port to Minecraft 26.2 |
| <img alt="Let's Do Brewery icon" src="https://cdn.modrinth.com/data/b7NV2plI/f254a05a63fabc3c3648618812301a5ffb37eed9_96.webp" width="20" height="20" align="absmiddle"> **[Let's Do] Brewery** | ✅ | ✅ | ⏳ | 26.2: Pending Brewery port to Minecraft 26.2 |
| <img alt="Let's Do Farm & Charm icon" src="https://cdn.modrinth.com/data/HJetCzWo/7c6c372629b3efa41409621631d60df12963f005_96.webp" width="20" height="20" align="absmiddle"> **[Let's Do] Farm & Charm** | ✅ | ✅ | ⏳ | 26.2: Pending Farm & Charm port to Minecraft 26.2 |
| <img alt="Let's Do Beachparty icon" src="https://cdn.modrinth.com/data/GyKzAh3l/41b9b45c365ecd55aced04bcd22af93878f766a0_96.webp" width="20" height="20" align="absmiddle"> **[Let's Do] Beachparty** | ✅ | ✅ | ⏳ | 26.2: Pending Beachparty port to Minecraft 26.2 |
| <img alt="Let's Do HerbalBrews icon" src="https://cdn.modrinth.com/data/Eh11TaTm/cea48ad39e9323e9e0f5354ee1d4c160f46b50be_96.webp" width="20" height="20" align="absmiddle"> **[Let's Do] HerbalBrews** | ✅ | ✅ | ⏳ | 26.2: Pending HerbalBrews port to Minecraft 26.2 |
| **Delight Addons** *([Collector's Reap](https://www.curseforge.com/minecraft/mc-mods/collectors-reap), [Fruits Delight](https://www.curseforge.com/minecraft/mc-mods/fruits-delight), [Delightful](https://www.curseforge.com/minecraft/mc-mods/delightful), [Crock Pot](https://www.curseforge.com/minecraft/mc-mods/crock-pot), [Hearth & Harvest](https://www.curseforge.com/minecraft/mc-mods/hearth-and-harvest), [Vanilla Delight](https://www.curseforge.com/minecraft/mc-mods/vanilla-delight))* | ✅ | ✅ | ⏳ | 26.2: Pending addon ports to Minecraft 26.2 |
| <img alt="EnvironmentZ icon" src="https://cdn.modrinth.com/data/TggYmc6t/4bab25a93f9e7f06aa22ff3cf44b8b1c829927d4.png" width="20" height="20" align="absmiddle"> **EnvironmentZ** | ❌ | ❌ | ❌ | Not supported on NeoForge (Fabric exclusive) |

#### 🔹 Forge

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
| <img alt="Let's Do Bakery icon" src="https://cdn.modrinth.com/data/oNB5jhlA/263380c2138f7f28f37639b719cc391334cbb7d5_96.webp" width="20" height="20" align="absmiddle"> **[Let's Do] Bakery** | ✅ | ❌ | ❌ | 1.21+ & 26.2: Architectury NeoForge/Fabric only |
| <img alt="Let's Do Brewery icon" src="https://cdn.modrinth.com/data/b7NV2plI/f254a05a63fabc3c3648618812301a5ffb37eed9_96.webp" width="20" height="20" align="absmiddle"> **[Let's Do] Brewery** | ✅ | ❌ | ❌ | 1.21+ & 26.2: Architectury NeoForge/Fabric only |
| <img alt="Let's Do Farm & Charm icon" src="https://cdn.modrinth.com/data/HJetCzWo/7c6c372629b3efa41409621631d60df12963f005_96.webp" width="20" height="20" align="absmiddle"> **[Let's Do] Farm & Charm** | ✅ | ❌ | ❌ | 1.21+ & 26.2: Architectury NeoForge/Fabric only |
| <img alt="Let's Do Beachparty icon" src="https://cdn.modrinth.com/data/GyKzAh3l/41b9b45c365ecd55aced04bcd22af93878f766a0_96.webp" width="20" height="20" align="absmiddle"> **[Let's Do] Beachparty** | ✅ | ❌ | ❌ | 1.21+ & 26.2: Architectury NeoForge/Fabric only |
| <img alt="Let's Do HerbalBrews icon" src="https://cdn.modrinth.com/data/Eh11TaTm/cea48ad39e9323e9e0f5354ee1d4c160f46b50be_96.webp" width="20" height="20" align="absmiddle"> **[Let's Do] HerbalBrews** | ✅ | ❌ | ❌ | 1.21+ & 26.2: Architectury NeoForge/Fabric only |
| **Delight Addons** *([Collector's Reap](https://www.curseforge.com/minecraft/mc-mods/collectors-reap), [Fruits Delight](https://www.curseforge.com/minecraft/mc-mods/fruits-delight), [Delightful](https://www.curseforge.com/minecraft/mc-mods/delightful), [Crock Pot](https://www.curseforge.com/minecraft/mc-mods/crock-pot), [Hearth & Harvest](https://www.curseforge.com/minecraft/mc-mods/hearth-and-harvest), [Vanilla Delight](https://www.curseforge.com/minecraft/mc-mods/vanilla-delight))* | ✅ | ❌ | ❌ | 1.21+ & 26.2: Upstream maintained on NeoForge/Fabric |
| <img alt="EnvironmentZ icon" src="https://cdn.modrinth.com/data/TggYmc6t/4bab25a93f9e7f06aa22ff3cf44b8b1c829927d4.png" width="20" height="20" align="absmiddle"> **EnvironmentZ** | ❌ | ❌ | ❌ | Not supported on Forge (Fabric exclusive) |

#### 🔹 Fabric

| Third-Party Mod | 1.20.1 | 1.21 / 1.21.1 | 26.2 | Unsupported Reason |
| :--- | :---: | :---: | :---: | :--- |
| <img alt="AppleSkin icon" src="https://cdn.modrinth.com/data/EsAfCjCV/icon.png" width="20" height="20" align="absmiddle"> **AppleSkin** | ✅ | ✅ | ✅ | — |
| <img alt="Jade icon" src="https://cdn.modrinth.com/data/nvQzSEkH/b04217bc2b7dc524c4d12f81ff42cc1cefb9b0fc_96.webp" width="20" height="20" align="absmiddle"> **Jade** | ✅ | ✅ | ✅ | — |
| <img alt="EnvironmentZ icon" src="https://cdn.modrinth.com/data/TggYmc6t/4bab25a93f9e7f06aa22ff3cf44b8b1c829927d4.png" width="20" height="20" align="absmiddle"> **EnvironmentZ** | ✅ | ✅ | ⏳ | 26.2: Pending EnvironmentZ port to Minecraft 26.2 |
| <img alt="Create Fabric icon" src="https://cdn.modrinth.com/data/Xbc0uyRg/bb29eb217b1fd159579409b06ec7b976afd30b14_96.webp" width="20" height="20" align="absmiddle"> **Create** *(Create: Fabric — [CurseForge](https://www.curseforge.com/minecraft/mc-mods/create-fabric) · [Modrinth](https://modrinth.com/mod/create-fabric))* | ✅ | ❌ | ❌ | 1.21+ & 26.2: Create: Fabric is only maintained up to 1.20.1 |
| <img alt="Immersive Engineering icon" src="https://cdn.modrinth.com/data/tIm2nV03/icon.png" width="20" height="20" align="absmiddle"> **Immersive Engineering** | ❌ | ❌ | ❌ | Not supported on Fabric (Forge/NeoForge exclusive) |
| <img alt="Cold Sweat icon" src="https://cdn.modrinth.com/data/uXhSmPjd/bf55420556c30d44d2f5cf7b8915705b9214b4ef.png" width="20" height="20" align="absmiddle"> **Cold Sweat** | ❌ | ❌ | ❌ | Not supported on Fabric (Forge/NeoForge exclusive) |
| <img alt="Tough As Nails icon" src="https://cdn.modrinth.com/data/ge1sOdFH/fa385c1d5ed5638282faf31e8fc94884bd60d86e.png" width="20" height="20" align="absmiddle"> **Tough As Nails** | ❌ | ✅ | ⏳ | 1.20.1: No official Fabric port; 26.2: Pending upstream port |
| <img alt="Vampirism icon" src="https://cdn.modrinth.com/data/jVZ0F1wn/icon.png" width="20" height="20" align="absmiddle"> **Vampirism** | ❌ | ❌ | ❌ | Not supported on Fabric (Forge/NeoForge exclusive) |
| <img alt="Supernatural icon" src="https://cdn.modrinth.com/data/UXzKPebS/63ba2cf3a91eba83d797904ba8bfa81df089f854.png" width="20" height="20" align="absmiddle"> **Supernatural** | ❌ | ❌ | ❌ | Not supported on Fabric (Forge/NeoForge exclusive) |
| <img alt="Corail Tombstone icon" src="https://cdn.modrinth.com/data/YT5K34AB/b279e8756761d8160f678ad20fb632d0a09b89b4_96.webp" width="20" height="20" align="absmiddle"> **Corail Tombstone** | ❌ | ❌ | ❌ | Not supported on Fabric (Forge/NeoForge exclusive) |
| <img alt="Farmer's Delight Refabricated icon" src="https://cdn.modrinth.com/data/7vxePowz/26e8448993e9bda4dba92b6e7a1a13d9c4333138.png" width="20" height="20" align="absmiddle"> **Farmer's Delight** *(Refabricated — [CurseForge](https://www.curseforge.com/minecraft/mc-mods/farmers-delight-refabricated) · [Modrinth](https://modrinth.com/mod/farmers-delight-refabricated))* | ✅ | ✅ | ⏳ | 26.2: Pending Farmer's Delight port to Minecraft 26.2 |
| <img alt="Farmer's Respite icon" src="https://cdn.modrinth.com/data/fasdaXH5/edba350f7617b0b0d724d6fcd7b432acd8f48de3_96.webp" width="20" height="20" align="absmiddle"> **Farmer's Respite** | ✅ | ❌ | ❌ | 1.21+ & 26.2: Unofficial 1.21 port is NeoForge exclusive |
| <img alt="Brewin' & Chewin' icon" src="https://cdn.modrinth.com/data/hIu9KJTT/f7c591a80046859d3d45c04ecbbc54d264483d5e.png" width="20" height="20" align="absmiddle"> **Brewin' & Chewin'** | ✅ | ❌ | ❌ | 1.21+ & 26.2: NeoForge exclusive |
| <img alt="Let's Do Bakery icon" src="https://cdn.modrinth.com/data/oNB5jhlA/263380c2138f7f28f37639b719cc391334cbb7d5_96.webp" width="20" height="20" align="absmiddle"> **[Let's Do] Bakery** | ✅ | ✅ | ⏳ | 26.2: Pending Bakery port to Minecraft 26.2 |
| <img alt="Let's Do Brewery icon" src="https://cdn.modrinth.com/data/b7NV2plI/f254a05a63fabc3c3648618812301a5ffb37eed9_96.webp" width="20" height="20" align="absmiddle"> **[Let's Do] Brewery** | ✅ | ✅ | ⏳ | 26.2: Pending Brewery port to Minecraft 26.2 |
| <img alt="Let's Do Farm & Charm icon" src="https://cdn.modrinth.com/data/HJetCzWo/7c6c372629b3efa41409621631d60df12963f005_96.webp" width="20" height="20" align="absmiddle"> **[Let's Do] Farm & Charm** | ✅ | ✅ | ⏳ | 26.2: Pending Farm & Charm port to Minecraft 26.2 |
| <img alt="Let's Do Beachparty icon" src="https://cdn.modrinth.com/data/GyKzAh3l/41b9b45c365ecd55aced04bcd22af93878f766a0_96.webp" width="20" height="20" align="absmiddle"> **[Let's Do] Beachparty** | ✅ | ✅ | ⏳ | 26.2: Pending Beachparty port to Minecraft 26.2 |
| <img alt="Let's Do HerbalBrews icon" src="https://cdn.modrinth.com/data/Eh11TaTm/cea48ad39e9323e9e0f5354ee1d4c160f46b50be_96.webp" width="20" height="20" align="absmiddle"> **[Let's Do] HerbalBrews** | ✅ | ✅ | ⏳ | 26.2: Pending HerbalBrews port to Minecraft 26.2 |
| **Delight Addons** *([Collector's Reap](https://www.curseforge.com/minecraft/mc-mods/collectors-reap), [Fruits Delight](https://www.curseforge.com/minecraft/mc-mods/fruits-delight), [Delightful](https://www.curseforge.com/minecraft/mc-mods/delightful), [Crock Pot](https://www.curseforge.com/minecraft/mc-mods/crock-pot), [Hearth & Harvest](https://www.curseforge.com/minecraft/mc-mods/hearth-and-harvest), [Vanilla Delight](https://www.curseforge.com/minecraft/mc-mods/vanilla-delight))* | ✅ | ✅ | ⏳ | 26.2: Pending addon ports to Minecraft 26.2 |

***

## 🗺️ Loot & Exploration

Survival requires searching for resources. You don't have to craft everything:

*   **Dungeon Loot:** Clay Bowls and Terracotta Bowls can be found while exploring the world.
*   **Chest Spawn Locations:** Injected natively into chest loot tables for **Simple Dungeons, Abandoned Mineshafts, Shipwrecks, Nether Bridges,** and **Bastions**.

***

## 🌟 Status Effects

Yet Another Thirst introduces custom status effects to manage hydration:

*   **Hydration** (Beneficial) — `yet_another_thirst:hydration`
    *   **Color:** `#21B1FF` (Blue)
    *   **Source / Trigger:** Drinking premium teas, beverages, or stews (via compatibility or configuration).
    *   **Mechanic:** Restores thirst and quenched saturation.
    *   **Rate (per level):** +1 thirst & quenched per tick (+20 per second at level I).
*   **Thirsty** (Harmful) — `yet_another_thirst:thirsty`
    *   **Color:** `#B15B2A` (Brown)
    *   **Source / Trigger:** Drinking unpurified water (applied for 30 seconds when nausea triggers).
    *   **Mechanic:** Adds exhaustion.
    *   **Rate (per level):** +0.005 exhaustion per tick (+0.1/s at level I; drains 1 point per 40s at rest).

***

### ✨ Hydration Effect Preview

![A screenshot of the player's inventory/status screen showing the active Hydration effect icon with its blue color and timer.](https://media.forgecdn.net/attachments/1724/29/effect-hydration-inventory-png.png)

![A screenshot showing the player in-world under the Hydration status effect with blue particle effects surrounding them and the thirst bar visibly restoring.](https://media.forgecdn.net/attachments/1724/30/effect-hydration-particles-png.png)

*   **Hydration Effect:** When active, your thirst and quenched levels are continuously restored, allowing you to sustain activities without depleting your thirst bar.

***

### 🤢 Thirsty Effect Preview

![A screenshot of the player's inventory/status screen showing the active Thirsty effect icon with its brown color and timer.](https://media.forgecdn.net/attachments/1724/31/effect-thirsty-inventory-png.png)

![A screenshot showing the player in-world under the Thirsty status effect with brown particle swirls surrounding them and the thirst bar visibly draining faster.](https://media.forgecdn.net/attachments/1724/32/effect-thirsty-particles-png.png)

*   **Thirsty Effect:** Increases your exhaustion rate, causing your thirst to drain much faster. Usually contracted from drinking unpurified water.

***

## 🚀 Near-Future Plans

*   **Water Storage Expansion:** Add new items and containers specifically designed for storing and transporting clean water.
*   **Sophisticated Storage & Backpacks Integration:** Support dynamic fluid storage upgrades inside backpacks and chests, allowing direct drinking and automatic refilling.

***

## 📦 Modpack Policy

*   **100% Free to Use:** You are fully allowed to include Yet Another Thirst in any public or private modpack.
*   **No Permission Needed:** There is no need to ask or register. All I ask is that you kindly credit me (MinhNH) as the creator in your modpack description.

***

## ❓ Frequently Asked Questions (FAQ)

### Can I play this mod on Peaceful difficulty?

Yes! By default, thirst does not deplete in Peaceful mode. If your thirst is low when switching to Peaceful, it will slowly regenerate (+1 thirst per tick cycle) until full. You can toggle this behavior in the config.

### How is this different from Thirst Was Taken?

Yet Another Thirst is a complete, modern rewrite targeting **Minecraft 1.20.1, 1.21/1.21.1, and 26.2**. It provides multi-loader support for **Forge**, **NeoForge**, and **Fabric**, a built-in in-game config GUI, modular filtration and industrial water boiling machinery, 11 progression advancements, and expanded mod integrations.

### Is it compatible with other thirst mods?

Yes! If you have **Tough As Nails** installed, Yet Another Thirst includes a configurable coexistence mode that can automatically disable YAT to prevent conflict, allowing modpacks to switch between systems easily.

***

## ⚙️ Installation & Requirements

1.  Make sure you are using **Minecraft 1.20.1, 1.21/1.21.1, or 26.2**.
2.  Install the latest version of **Forge**, **NeoForge**, or **Fabric** _(Fabric also requires **Fabric API**, plus **Mod Menu** if you want the in-game config button)_.
3.  Download the matching **Yet Another Thirst** jar file.
4.  Place the jar in your Minecraft instance's `mods` folder.
5.  (Optional) Install **AppleSkin** and **Jade** for the best visual experience.