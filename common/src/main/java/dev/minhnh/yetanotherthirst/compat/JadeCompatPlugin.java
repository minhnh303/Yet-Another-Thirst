package dev.minhnh.yetanotherthirst.compat;

import dev.minhnh.yetanotherthirst.Constants;
import dev.minhnh.yetanotherthirst.Lang;
import dev.minhnh.yetanotherthirst.core.block.IWaterBoiler;
import dev.minhnh.yetanotherthirst.core.purity.WaterPurity;
import dev.minhnh.yetanotherthirst.core.thirst.ThirstConfig;
import dev.minhnh.yetanotherthirst.core.thirst.ThirstValue;
import dev.minhnh.yetanotherthirst.core.thirst.ThirstValues;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.fluid.JadeFluidObject;
import snownee.jade.api.view.ViewGroup;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@WailaPlugin("jade")
public final class JadeCompatPlugin implements IWailaPlugin {

    private static final ResourceLocation WATER_PURITY = Constants.asResource("water_purity");
    private static final ResourceLocation ITEM_FRAME_WATER_PURITY = Constants.asResource("item_frame_water_purity");
    private static final ResourceLocation WATER_BOILER_PURITY = Constants.asResource("water_boiler_purity");

    @Override
    public void registerClient(IWailaClientRegistration registration) {

        registration.registerBlockComponent(WaterPurityProvider.INSTANCE, Block.class);
        registration.registerBlockComponent(WaterBoilerProvider.INSTANCE, Block.class);
        registration.registerEntityComponent(ItemFrameWaterContainerProvider.INSTANCE, ItemFrame.class);
    }

    private enum WaterPurityProvider implements IBlockComponentProvider {
        INSTANCE;

        @Override
        public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
            if (!config.get(WATER_PURITY)) return;

            if (WaterPurity.isWaterDisplayTarget(accessor.getLevel(), accessor.getPosition())) {
                int purity = WaterPurity.getDisplayPurity(accessor.getLevel(), accessor.getPosition());
                tooltip.add(Component.translatable("yet_another_thirst.jade.water_purity",
                        WaterPurity.purityComponent(purity)));
                if (ThirstConfig.CAN_DRINK_BY_HAND) {
                    tooltip.add(new ThirstElement(new ThirstValue(
                            ThirstConfig.HAND_DRINKING_THIRST,
                            ThirstConfig.HAND_DRINKING_QUENCHED)));
                }
                return;
            }

            // Fluid containers (IE tanks, etc.) — purity stored in Jade's JadeFluidStorage via DataComponentPatch
            CompoundTag serverData = accessor.getServerData();
            if (!serverData.contains("JadeFluidStorage")) return;
            try {
                List<ViewGroup<CompoundTag>> groups = ViewGroup.readList(serverData, "JadeFluidStorage", Function.identity());
                outer:
                for (ViewGroup<CompoundTag> group : groups) {
                    for (CompoundTag viewTag : group.views) {
                        if (!viewTag.contains("fluid")) continue;
                        Optional<JadeFluidObject> parsed = JadeFluidObject.CODEC
                                .parse(NbtOps.INSTANCE, viewTag.get("fluid")).result();
                        if (parsed.isEmpty()) continue;
                        JadeFluidObject jadeFluid = parsed.get();
                        if (!jadeFluid.getType().defaultFluidState().is(FluidTags.WATER)) continue;
                        DataComponentPatch components = jadeFluid.getComponents();
                        Optional<? extends CustomData> customDataOpt = components.get(DataComponents.CUSTOM_DATA);
                        if (customDataOpt == null || customDataOpt.isEmpty()) continue;
                        CompoundTag nbt = customDataOpt.get().copyTag();
                        if (!nbt.contains("Purity")) continue;
                        tooltip.add(Component.translatable("yet_another_thirst.jade.water_purity",
                                WaterPurity.purityComponent(nbt.getInt("Purity"))));
                        break outer;
                    }
                }
            } catch (Throwable ignored) {}
        }

        @Override
        public ResourceLocation getUid() {

            return WATER_PURITY;
        }
    }

    private enum WaterBoilerProvider implements IBlockComponentProvider {
        INSTANCE;

        @Override
        public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
            if (!config.get(WATER_BOILER_PURITY)) return;

            BlockEntity be = accessor.getBlockEntity();
            if (!(be instanceof IWaterBoiler boiler)) return;

            if (boiler.isUpper()) {
                boiler = boiler.getLowerEntity(accessor.getLevel());
            }
            if (boiler == null) return;

            tooltip.add(Lang.Jade.waterBoilerInput(WaterPurity.purityComponent(boiler.getInputPurity())));
            tooltip.add(Lang.Jade.waterBoilerOutput(WaterPurity.purityComponent(boiler.getOutputPurity())));
        }

        @Override
        public ResourceLocation getUid() {
            return WATER_BOILER_PURITY;
        }
    }

    private enum ItemFrameWaterContainerProvider implements IEntityComponentProvider {
        INSTANCE;

        @Override
        public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {

            if (!config.get(ITEM_FRAME_WATER_PURITY)) {
                return;
            }
            ItemStack stack = ((ItemFrame) accessor.getEntity()).getItem();
            if (!WaterPurity.hasPurity(stack)) {
                return;
            }

            int purity = WaterPurity.getPurity(stack);
            if (purity < WaterPurity.MIN_PURITY || purity > WaterPurity.MAX_PURITY) {
                return;
            }
            tooltip.add(Component.translatable("yet_another_thirst.jade.water_purity",
                    WaterPurity.purityComponent(purity)));
            ThirstValues.get(stack).ifPresent(value -> tooltip.add(new ThirstElement(value)));
        }

        @Override
        public ResourceLocation getUid() {

            return ITEM_FRAME_WATER_PURITY;
        }
    }
}
