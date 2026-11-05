package dev.minhnh.yetanotherthirst;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/**
 * Appends items from a secondary loot table into the primary loot pool.
 * Used to inject custom thirst items into vanilla dungeon chests.
 */
public final class NeoForgeLootModifier extends LootModifier {

    static final DeferredRegister<MapCodec<? extends IGlobalLootModifier>> SERIALIZERS =
            DeferredRegister.create(NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, Constants.MOD_ID);

    static final DeferredHolder<MapCodec<? extends IGlobalLootModifier>, MapCodec<NeoForgeLootModifier>> SERIALIZER =
            SERIALIZERS.register("add_loot_table", () -> RecordCodecBuilder.mapCodec(inst ->
                    codecStart(inst)
                            .and(Identifier.CODEC.fieldOf("lootTable").forGetter(m -> m.lootTable))
                            .apply(inst, NeoForgeLootModifier::new)));

    private final Identifier lootTable;

    public NeoForgeLootModifier(LootItemCondition[] conditions, int priority, Identifier lootTable) {
        super(conditions, priority);
        this.lootTable = lootTable;
    }

    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        LootTable table = context.getLevel().getServer().reloadableRegistries().getLootTable(ResourceKey.create(Registries.LOOT_TABLE, lootTable));
        table.getRandomItems(context, generatedLoot::add);
        return generatedLoot;
    }

    @Override
    public MapCodec<? extends IGlobalLootModifier> codec() {
        return SERIALIZER.get();
    }
}
