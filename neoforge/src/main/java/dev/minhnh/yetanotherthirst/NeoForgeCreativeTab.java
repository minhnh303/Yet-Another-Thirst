package dev.minhnh.yetanotherthirst;

import dev.minhnh.yetanotherthirst.core.block.ModBlocks;
import dev.minhnh.yetanotherthirst.core.item.ModItems;
import dev.minhnh.yetanotherthirst.core.purity.WaterPurity;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class NeoForgeCreativeTab {

    static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Constants.MOD_ID);

    static final DeferredHolder<CreativeModeTab, CreativeModeTab> THIRST_TAB = TABS.register("thirst",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.yet_another_thirst"))
                    .icon(() -> new ItemStack(ModItems.TERRACOTTA_WATER_BOWL.get()))
                    .displayItems((params, output) -> {
                        output.accept(new ItemStack(ModItems.CLAY_BOWL.get()));
                        output.accept(new ItemStack(ModItems.TERRACOTTA_BOWL.get()));
                        for (int p = WaterPurity.MIN_PURITY; p <= WaterPurity.MAX_PURITY; p++) {
                            output.accept(WaterPurity.addPurity(new ItemStack(ModItems.TERRACOTTA_WATER_BOWL.get()), p));
                        }
                        for (int p = WaterPurity.MIN_PURITY; p <= WaterPurity.MAX_PURITY; p++) {
                            output.accept(WaterPurity.addPurity(new ItemStack(ModItems.WOODEN_WATER_BOWL.get()), p));
                        }
                        for (int p = WaterPurity.MIN_PURITY; p <= WaterPurity.MAX_PURITY; p++) {
                            output.accept(WaterPurity.addPurity(WaterPurity.waterPotion(), p));
                        }
                        for (int p = WaterPurity.MIN_PURITY; p <= WaterPurity.MAX_PURITY; p++) {
                            output.accept(WaterPurity.addPurity(new ItemStack(Items.WATER_BUCKET), p));
                        }
                        // Filter Frame items (only when Create is loaded)
                        if (ModList.get().isLoaded("create")) {
                            if (ModItems.FABRIC_FILTER_CORE.get() != Items.AIR)
                                output.accept(new ItemStack(ModItems.FABRIC_FILTER_CORE.get()));
                            if (ModItems.SAND_FILTER_CORE.get() != Items.AIR)
                                output.accept(new ItemStack(ModItems.SAND_FILTER_CORE.get()));
                            if (ModItems.CARBON_FILTER_CORE.get() != Items.AIR)
                                output.accept(new ItemStack(ModItems.CARBON_FILTER_CORE.get()));
                            if (ModItems.CLOGGED_FABRIC_FILTER.get() != Items.AIR)
                                output.accept(new ItemStack(ModItems.CLOGGED_FABRIC_FILTER.get()));
                            if (ModItems.CLOGGED_SAND_FILTER.get() != Items.AIR)
                                output.accept(new ItemStack(ModItems.CLOGGED_SAND_FILTER.get()));
                            if (ModItems.CLOGGED_CARBON_FILTER.get() != Items.AIR)
                                output.accept(new ItemStack(ModItems.CLOGGED_CARBON_FILTER.get()));
                            if (ModBlocks.FILTER_FRAME != null)
                                output.accept(new ItemStack(ModBlocks.FILTER_FRAME.get()));
                        }
                        // Water Boiler (only when Immersive Engineering is loaded)
                        if (ModList.get().isLoaded("immersiveengineering") && ModBlocks.WATER_BOILER != null) {
                            output.accept(new ItemStack(ModBlocks.WATER_BOILER.get()));
                        }
                    })
                    .build());

    private NeoForgeCreativeTab() {}
}
