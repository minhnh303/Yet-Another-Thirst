package dev.minhnh.yetanotherthirst;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
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

/**
 * Appends items from a secondary loot table into the primary loot pool.
 * Used to inject custom thirst items into vanilla dungeon chests.
 */
public final class NeoForgeLootModifier extends LootModifier {

    static final DeferredRegister<Codec<? extends IGlobalLootModifier>> SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, Constants.MOD_ID);

    static final RegistryObject<Codec<NeoForgeLootModifier>> SERIALIZER =
            SERIALIZERS.register("add_loot_table", () -> RecordCodecBuilder.create(inst ->
                    codecStart(inst)
                            .and(ResourceLocation.CODEC.fieldOf("lootTable").forGetter(m -> m.lootTable))
                            .apply(inst, NeoForgeLootModifier::new)));

    private final ResourceLocation lootTable;

    private NeoForgeLootModifier(LootItemCondition[] conditions, ResourceLocation lootTable) {
        super(conditions);
        this.lootTable = lootTable;
    }

    @Nonnull
    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        LootTable table = context.getLevel().getServer().getLootData().getLootTable(lootTable);
        table.getRandomItems(context, generatedLoot::add);
        return generatedLoot;
    }

    @Override
    public Codec<? extends IGlobalLootModifier> codec() {
        return SERIALIZER.get();
    }
}
