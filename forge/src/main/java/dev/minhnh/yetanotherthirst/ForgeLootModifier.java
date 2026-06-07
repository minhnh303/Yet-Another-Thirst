package dev.minhnh.yetanotherthirst;

import com.google.common.base.Suppliers;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Appends items from a secondary loot table into the primary loot pool.
 * Used to inject custom thirst items into vanilla dungeon chests.
 */
public final class ForgeLootModifier extends LootModifier {

    static final DeferredRegister<MapCodec<? extends IGlobalLootModifier>> SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, Constants.MOD_ID);

    private static final Supplier<MapCodec<ForgeLootModifier>> CODEC = Suppliers.memoize(() ->
            RecordCodecBuilder.mapCodec(inst ->
                    codecStart(inst)
                            .and(ResourceLocation.CODEC.fieldOf("lootTable").forGetter(m -> m.lootTable))
                            .apply(inst, ForgeLootModifier::new)));

    static final RegistryObject<MapCodec<? extends IGlobalLootModifier>> SERIALIZER =
            SERIALIZERS.register("add_loot_table", CODEC);

    private final ResourceLocation lootTable;

    private ForgeLootModifier(LootItemCondition[] conditions, ResourceLocation lootTable) {
        super(conditions);
        this.lootTable = lootTable;
    }

    @Nonnull
    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        LootTable table = context.getResolver()
                .get(Registries.LOOT_TABLE, ResourceKey.create(Registries.LOOT_TABLE, lootTable))
                .map(holder -> holder.value())
                .orElse(LootTable.EMPTY);
        Objects.requireNonNull(generatedLoot);
        LootContext subContext = new LootContext.Builder(context)
                .withQueriedLootTableId(lootTable)
                .create(null);
        table.getRandomItems(subContext, generatedLoot::add);
        return generatedLoot;
    }

    @Override
    public MapCodec<? extends IGlobalLootModifier> codec() {
        return SERIALIZER.get();
    }
}
