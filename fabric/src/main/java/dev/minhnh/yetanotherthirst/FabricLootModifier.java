package dev.minhnh.yetanotherthirst;

import net.fabricmc.fabric.api.loot.v2.LootTableEvents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootTableReference;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

/**
 * Injects mod loot into vanilla loot tables via the Fabric loot API.
 * Mirrors what ForgeLootModifier does via the global_loot_modifiers JSON on Forge.
 */
public final class FabricLootModifier {

    private FabricLootModifier() {}

    public static void register() {

        LootTableEvents.MODIFY.register((resourceManager, lootManager, id, tableBuilder, source) -> {

            if (!source.isBuiltin()) return;

            String modifiersPath = "loot_modifiers/" + id.getNamespace() + "/" + id.getPath();
            ResourceLocation modTable = Constants.asResource(modifiersPath);

            // Check if there is a matching loot_modifier secondary table for this loot table
            // This mirrors the Forge behavior of add_loot_table global loot modifiers
            tryInject(id, tableBuilder,
                    "minecraft", "chests/simple_dungeon",
                    Constants.asResource("inject/simple_dungeon"));
            tryInject(id, tableBuilder,
                    "minecraft", "chests/village/village_weaponsmith",
                    Constants.asResource("inject/village_weaponsmith"));
            tryInject(id, tableBuilder,
                    "minecraft", "chests/stronghold_corridor",
                    Constants.asResource("inject/stronghold_corridor"));
        });
    }

    private static void tryInject(ResourceLocation id, LootTable.Builder tableBuilder,
                                   String namespace, String path, ResourceLocation injectTable) {

        if (id.getNamespace().equals(namespace) && id.getPath().equals(path)) {
            tableBuilder.withPool(
                    LootPool.lootPool()
                            .setRolls(ConstantValue.exactly(1))
                            .add(LootTableReference.lootTableReference(injectTable)));
        }
    }
}
