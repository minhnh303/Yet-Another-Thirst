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
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;
import net.minecraftforge.common.loot.LootTableIdCondition;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Appends items from a secondary loot table into the primary loot pool.
 * Used to inject custom thirst items into vanilla dungeon chests.
 */
public final class ForgeLootModifier extends LootModifier {

    static final DeferredRegister<MapCodec<? extends IGlobalLootModifier>> SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, Constants.MOD_ID);

    static final RegistryObject<MapCodec<ForgeLootModifier>> SERIALIZER =
            SERIALIZERS.register("add_loot_table", () -> RecordCodecBuilder.mapCodec(inst ->
                    codecStart(inst)
                            .and(Identifier.CODEC.fieldOf("lootTable").forGetter(m -> m.lootTable))
                            .apply(inst, ForgeLootModifier::new)));

    static final DeferredRegister<MapCodec<? extends LootItemCondition>> LOOT_CONDITION_TYPES =
            DeferredRegister.create(Registries.LOOT_CONDITION_TYPE, "neoforge");

    static final RegistryObject<MapCodec<NeoForgeLootTableIdCondition>> NEOFORGE_LOOT_TABLE_ID =
            LOOT_CONDITION_TYPES.register("loot_table_id", () -> NeoForgeLootTableIdCondition.CODEC);

    public record NeoForgeLootTableIdCondition(Identifier id) implements LootItemCondition {
        public static final MapCodec<NeoForgeLootTableIdCondition> CODEC = RecordCodecBuilder.mapCodec(b -> b.group(
                Identifier.CODEC.fieldOf("loot_table_id").forGetter(NeoForgeLootTableIdCondition::id)
        ).apply(b, NeoForgeLootTableIdCondition::new));

        @Override
        public MapCodec<NeoForgeLootTableIdCondition> codec() {
            return CODEC;
        }

        @Override
        public boolean test(LootContext ctx) {
            return new LootTableIdCondition(this.id).test(ctx);
        }
    }

    private final Identifier lootTable;

    private ForgeLootModifier(LootItemCondition[] conditions, Identifier lootTable) {
        super(conditions);
        this.lootTable = lootTable;
    }

    @Override
    protected ObjectArrayList<ItemStack> doApply(LootTable table, ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        LootTable extraTable = context.getLevel().getServer().reloadableRegistries().getLootTable(ResourceKey.create(Registries.LOOT_TABLE, this.lootTable));
        extraTable.getRandomItems(context, generatedLoot::add);
        return generatedLoot;
    }

    @Override
    public MapCodec<? extends IGlobalLootModifier> codec() {
        return SERIALIZER.get();
    }
}
