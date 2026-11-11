package dev.minhnh.yetanotherthirst.compat;

import dev.minhnh.yetanotherthirst.Constants;
import dev.minhnh.yetanotherthirst.Lang;
import dev.minhnh.yetanotherthirst.core.purity.WaterPurity;
import dev.minhnh.yetanotherthirst.core.thirst.ThirstConfig;
import dev.minhnh.yetanotherthirst.core.thirst.ThirstValue;
import dev.minhnh.yetanotherthirst.core.thirst.ThirstValues;
import net.minecraft.nbt.CompoundTag;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.view.ViewGroup;

import java.util.List;

@WailaPlugin("jade")
public final class JadeCompatPlugin implements IWailaPlugin {

    private static final ResourceLocation WATER_PURITY = Constants.asResource("water_purity");

    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerBlockDataProvider(BlockEntityDataProvider.INSTANCE, BlockEntity.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.addConfig(WATER_PURITY, true);
        registration.registerBlockComponent(WaterPurityProvider.INSTANCE, Block.class);
        registration.registerEntityComponent(ItemFrameWaterContainerProvider.INSTANCE, ItemFrame.class);
    }

    private static int getTankPurity(CompoundTag tank, int defaultPurity) {
        if (tank == null || tank.isEmpty()) {
            return defaultPurity;
        }
        if (tank.contains("Tag")) {
            CompoundTag tag = tank.getCompound("Tag");
            if (tag.contains("Purity")) {
                return tag.getInt("Purity");
            }
        }
        if (tank.contains("Fluid")) {
            CompoundTag fluid = tank.getCompound("Fluid");
            if (fluid.contains("tag")) {
                CompoundTag t = fluid.getCompound("tag");
                if (t.contains("Purity")) {
                    return t.getInt("Purity");
                }
            }
            if (fluid.contains("Tag")) {
                CompoundTag t = fluid.getCompound("Tag");
                if (t.contains("Purity")) {
                    return t.getInt("Purity");
                }
            }
        }
        if (tank.contains("Purity")) {
            return tank.getInt("Purity");
        }
        return defaultPurity;
    }

    private static int getTankAmount(CompoundTag tank) {
        if (tank == null || tank.isEmpty()) {
            return 0;
        }
        if (tank.contains("Amount")) {
            return tank.getInt("Amount");
        }
        if (tank.contains("Fluid")) {
            CompoundTag fluid = tank.getCompound("Fluid");
            if (fluid.contains("Amount")) {
                return fluid.getInt("Amount");
            }
        }
        return 0;
    }

    private enum BlockEntityDataProvider implements IServerDataProvider<BlockAccessor> {
        INSTANCE;

        @Override
        public void appendServerData(CompoundTag data, BlockAccessor accessor) {
            try {
                BlockEntity blockEntity = accessor.getBlockEntity();
                if (blockEntity == null) {
                    return;
                }
                String beName = blockEntity.getClass().getSimpleName();
                if ("WaterBoilerBlockEntity".equals(beName) || "FilterFrameBlockEntity".equals(beName)) {
                    BlockEntity masterEntity = blockEntity;
                    net.minecraft.world.level.block.state.BlockState state = blockEntity.getBlockState();
                    if (state.hasProperty(net.minecraft.world.level.block.state.properties.BlockStateProperties.DOUBLE_BLOCK_HALF)) {
                        if (state.getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.DOUBLE_BLOCK_HALF) == net.minecraft.world.level.block.state.properties.DoubleBlockHalf.UPPER) {
                            BlockEntity below = blockEntity.getLevel().getBlockEntity(blockEntity.getBlockPos().below());
                            if (below != null) {
                                masterEntity = below;
                            }
                        }
                    }
                    
                    CompoundTag beTag = masterEntity.saveWithoutMetadata();
                    if (beTag.contains("InputTank")) {
                        data.put("InputTank", beTag.getCompound("InputTank"));
                    }
                    if (beTag.contains("OutputTank")) {
                        data.put("OutputTank", beTag.getCompound("OutputTank"));
                    }
                    if (beTag.contains("Inventory")) {
                        data.put("Inventory", beTag.getCompound("Inventory"));
                    }
                }
            } catch (Throwable ignored) {
            }
        }

        @Override
        public ResourceLocation getUid() {
            return WATER_PURITY;
        }
    }

    private enum WaterPurityProvider implements IBlockComponentProvider {
        INSTANCE;

        @Override
        public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
            try {
                if (!config.get(WATER_PURITY)) {
                    return;
                }

                String blockName = accessor.getBlockState().getBlock().getClass().getSimpleName();
                if ("WaterBoilerBlock".equals(blockName)) {
                    CompoundTag serverData = accessor.getServerData();
                    if (serverData.contains("InputTank")) {
                        CompoundTag inputTank = serverData.getCompound("InputTank");
                        int p = getTankPurity(inputTank, ThirstConfig.DEFAULT_PURITY);
                        tooltip.add(Lang.Jade.waterBoilerInput(WaterPurity.purityComponent(p)));
                    }
                    if (serverData.contains("OutputTank")) {
                        CompoundTag outputTank = serverData.getCompound("OutputTank");
                        int p = getTankPurity(outputTank, 3);
                        tooltip.add(Lang.Jade.waterBoilerOutput(WaterPurity.purityComponent(p)));
                    }
                    return;
                }

                if ("FilterFrameBlock".equals(blockName)) {
                    CompoundTag serverData = accessor.getServerData();
                    CompoundTag inv = serverData.getCompound("Inventory");
                    CompoundTag filterCoreItem = null;
                    
                    if (inv.contains("Item")) {
                        filterCoreItem = inv.getCompound("Item");
                    } else if (inv.contains("Items")) {
                        net.minecraft.nbt.ListTag items = inv.getList("Items", net.minecraft.nbt.Tag.TAG_COMPOUND);
                        if (!items.isEmpty()) {
                            filterCoreItem = items.getCompound(0);
                        }
                    }

                    if (filterCoreItem != null && !filterCoreItem.isEmpty()) {
                        String id = filterCoreItem.getString("id");
                        boolean clogged = id.contains("clogged");
                        if (clogged) {
                            tooltip.add(Lang.Jade.filterCoreClogged()
                                    .withStyle(net.minecraft.ChatFormatting.RED));
                        } else {
                            double maxLifespan = 0;
                            if (id.contains("carbon")) maxLifespan = ThirstConfig.CARBON_FILTER_DURABILITY;
                            else if (id.contains("sand")) maxLifespan = ThirstConfig.SAND_FILTER_DURABILITY;
                            else maxLifespan = ThirstConfig.FABRIC_FILTER_DURABILITY;
                            CompoundTag nbt = filterCoreItem.getCompound("tag");
                            double remaining = nbt.contains("Lifespan") ? nbt.getDouble("Lifespan") : maxLifespan;
                            int pct = (int) Math.round(remaining / maxLifespan * 100);
                            net.minecraft.ChatFormatting color = pct > 50 ? net.minecraft.ChatFormatting.GREEN
                                    : pct > 20 ? net.minecraft.ChatFormatting.YELLOW : net.minecraft.ChatFormatting.RED;
                            tooltip.add(Lang.Jade.filterCoreDurability(pct)
                                    .withStyle(color));
                        }
                    } else {
                        tooltip.add(Lang.Jade.filterCoreEmpty()
                                .withStyle(net.minecraft.ChatFormatting.GRAY));
                    }

                    if (serverData.contains("InputTank")) {
                        CompoundTag inputTank = serverData.getCompound("InputTank");
                        if (getTankAmount(inputTank) > 0) {
                            tooltip.add(Lang.Jade.filterInput(WaterPurity.purityComponent(getTankPurity(inputTank, 0))));
                        }
                    }
                    if (serverData.contains("OutputTank")) {
                        CompoundTag outputTank = serverData.getCompound("OutputTank");
                        if (getTankAmount(outputTank) > 0) {
                            tooltip.add(Lang.Jade.filterOutput(WaterPurity.purityComponent(getTankPurity(outputTank, 0))));
                        }
                    }
                    return;
                }

                int purity = -1;
                boolean isWater = false;
                boolean isWaterTarget = false;

                if (WaterPurity.isWaterDisplayTarget(accessor.getLevel(), accessor.getPosition())) {
                    isWater = true;
                    isWaterTarget = true;
                    purity = WaterPurity.getDisplayPurity(accessor.getLevel(), accessor.getPosition());
                } else if (accessor.getServerData().contains("JadeFluidStorage")) {
                    try {
                        List<ViewGroup<CompoundTag>> groups = ViewGroup.readList(
                                accessor.getServerData(),
                                "JadeFluidStorage",
                                java.util.function.Function.identity()
                        );
                        for (ViewGroup<CompoundTag> group : groups) {
                            for (CompoundTag viewTag : group.views) {
                                String fluidId = viewTag.getString("fluid");
                                if ("minecraft:water".equals(fluidId)) {
                                    isWater = true;
                                    int p = ThirstConfig.DEFAULT_PURITY;
                                    if (viewTag.contains("tag")) {
                                        CompoundTag tag = viewTag.getCompound("tag");
                                        if (tag.contains("Purity")) {
                                            p = tag.getInt("Purity");
                                        }
                                    }
                                    purity = p;
                                    break;
                                }
                            }
                            if (isWater) break;
                        }
                    } catch (Throwable ignored) {
                    }
                }

                if (!isWater) {
                    return;
                }

                tooltip.add(Lang.Jade.waterPurity(WaterPurity.purityComponent(purity)));

                if (isWaterTarget && ThirstConfig.CAN_DRINK_BY_HAND) {
                    tooltip.add(new ThirstElement(new ThirstValue(
                            ThirstConfig.HAND_DRINKING_THIRST,
                            ThirstConfig.HAND_DRINKING_QUENCHED
                    )));
                }
            } catch (Throwable ignored) {
            }
        }

        @Override
        public ResourceLocation getUid() {
            return WATER_PURITY;
        }
    }

    private enum ItemFrameWaterContainerProvider implements IEntityComponentProvider {
        INSTANCE;

        @Override
        public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
            try {
                if (!config.get(WATER_PURITY)) {
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
                tooltip.add(Lang.Jade.waterPurity(WaterPurity.purityComponent(purity)));
                ThirstValues.get(stack).ifPresent(value -> {
                    try {
                        tooltip.add(new ThirstElement(value));
                    } catch (Throwable ignored) {
                    }
                });
            } catch (Throwable ignored) {
            }
        }

        @Override
        public ResourceLocation getUid() {
            return WATER_PURITY;
        }
    }
}
