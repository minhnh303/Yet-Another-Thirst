package dev.minhnh.yetanotherthirst;

import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.NestedLootTable;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

/**
 * Injects mod loot into vanilla loot tables via the Fabric loot API.
 * Mirrors what ForgeLootModifier does via the global_loot_modifiers JSON on Forge.
 */
public final class FabricLootModifier {

    private FabricLootModifier() {}

    public static void register() {

        LootTableEvents.MODIFY.register((key, tableBuilder, source, registries) -> {

            if (!source.isBuiltin()) return;
            Identifier id = key.identifier();

            tryInject(id, tableBuilder,
                    "minecraft", "chests/simple_dungeon",
                    Constants.asResource("chests/simple_dungeon"));
            tryInject(id, tableBuilder,
                    "minecraft", "chests/abandoned_mineshaft",
                    Constants.asResource("chests/abandoned_mineshaft"));
            tryInject(id, tableBuilder,
                    "minecraft", "chests/shipwreck_supply",
                    Constants.asResource("chests/shipwreck_supply"));
            tryInject(id, tableBuilder,
                    "minecraft", "chests/bastion_other",
                    Constants.asResource("chests/bastion_other"));
            tryInject(id, tableBuilder,
                    "minecraft", "chests/nether_bridge",
                    Constants.asResource("chests/nether_bridge"));
        });
    }

    private static void tryInject(Identifier id, LootTable.Builder tableBuilder,
                                   String namespace, String path, Identifier injectTable) {

        if (id.getNamespace().equals(namespace) && id.getPath().equals(path)) {
            tableBuilder.withPool(
                    LootPool.lootPool()
                            .setRolls(ConstantValue.exactly(1))
                            .add(NestedLootTable.lootTableReference(ResourceKey.create(Registries.LOOT_TABLE, injectTable))));
        }
    }
}
