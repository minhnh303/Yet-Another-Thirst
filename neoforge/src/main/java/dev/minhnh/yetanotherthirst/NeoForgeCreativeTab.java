package dev.minhnh.yetanotherthirst;

import dev.minhnh.yetanotherthirst.core.item.ModItems;
import dev.minhnh.yetanotherthirst.core.purity.WaterPurity;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public final class NeoForgeCreativeTab {

    static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Constants.MOD_ID);

    static final RegistryObject<CreativeModeTab> THIRST_TAB = TABS.register("thirst",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.yet_another_thirst"))
                    .icon(() -> new ItemStack(ModItems.TERRACOTTA_WATER_BOWL.get()))
                    .displayItems((params, output) -> {
                        if (NeoForgeItems.FILTER_FRAME != null) {
                            output.accept(new ItemStack(NeoForgeItems.FILTER_FRAME.get()));
                        }
                        if (NeoForgeItems.WATER_BOILER != null) {
                            output.accept(new ItemStack(NeoForgeItems.WATER_BOILER.get()));
                        }
                        if (ModItems.FABRIC_FILTER_CORE.get() != Items.AIR) {
                            output.accept(new ItemStack(ModItems.FABRIC_FILTER_CORE.get()));
                            output.accept(new ItemStack(ModItems.SAND_FILTER_CORE.get()));
                            output.accept(new ItemStack(ModItems.CARBON_FILTER_CORE.get()));
                            output.accept(new ItemStack(ModItems.CLOGGED_FABRIC_FILTER.get()));
                            output.accept(new ItemStack(ModItems.CLOGGED_SAND_FILTER.get()));
                            output.accept(new ItemStack(ModItems.CLOGGED_CARBON_FILTER.get()));
                        }
                        output.accept(new ItemStack(ModItems.CLAY_BOWL.get()));
                        output.accept(new ItemStack(ModItems.TERRACOTTA_BOWL.get()));
                        for (int p = WaterPurity.MIN_PURITY; p <= WaterPurity.MAX_PURITY; p++) {
                            output.accept(WaterPurity.addPurity(new ItemStack(ModItems.TERRACOTTA_WATER_BOWL.get()), p));
                        }
                        for (int p = WaterPurity.MIN_PURITY; p <= WaterPurity.MAX_PURITY; p++) {
                            output.accept(WaterPurity.addPurity(new ItemStack(ModItems.WOODEN_WATER_BOWL.get()), p));
                        }
                        for (int p = WaterPurity.MIN_PURITY; p <= WaterPurity.MAX_PURITY; p++) {
                            output.accept(WaterPurity.addPurity(
                                     PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.WATER), p));
                        }
                        for (int p = WaterPurity.MIN_PURITY; p <= WaterPurity.MAX_PURITY; p++) {
                            output.accept(WaterPurity.addPurity(new ItemStack(Items.WATER_BUCKET), p));
                        }
                    })
                    .build());

    private NeoForgeCreativeTab() {}
}
