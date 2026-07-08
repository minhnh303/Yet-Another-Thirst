package dev.minhnh.yetanotherthirst;

import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
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

        LootTableEvents.MODIFY.register((id, tableBuilder, source, registries) -> {

            if (!source.isBuiltin()) return;

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

    private static void tryInject(ResourceKey<LootTable> id, LootTable.Builder tableBuilder,
                                   String namespace, String path, ResourceLocation injectLoc) {

        ResourceLocation idLoc = id.location();
        if (idLoc.getNamespace().equals(namespace) && idLoc.getPath().equals(path)) {
            ResourceKey<LootTable> injectKey = ResourceKey.create(id.registryKey(), injectLoc);
            tableBuilder.withPool(
                    LootPool.lootPool()
                            .setRolls(ConstantValue.exactly(1))
                            .add(NestedLootTable.lootTableReference(injectKey)));
        }
    }
}
