package dev.minhnh.yetanotherthirst.compat;

import dev.minhnh.yetanotherthirst.Constants;
import dev.minhnh.yetanotherthirst.core.purity.WaterPurity;
import dev.minhnh.yetanotherthirst.core.thirst.ThirstConfig;
import dev.minhnh.yetanotherthirst.core.thirst.ThirstValue;
import dev.minhnh.yetanotherthirst.core.thirst.ThirstValues;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;
import snownee.jade.api.config.IPluginConfig;

@WailaPlugin("jade")
public final class JadeCompatPlugin implements IWailaPlugin {

    private static final ResourceLocation WATER_PURITY = Constants.asResource("water_purity");

    @Override
    public void registerClient(IWailaClientRegistration registration) {

        registration.addConfig(WATER_PURITY, true);
        registration.registerBlockComponent(WaterPurityProvider.INSTANCE, Block.class);
        registration.registerEntityComponent(ItemFrameWaterContainerProvider.INSTANCE, ItemFrame.class);
    }

    private enum WaterPurityProvider implements IBlockComponentProvider {
        INSTANCE;

        @Override
        public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {

            if (!config.get(WATER_PURITY)) {
                return;
            }
            if (!WaterPurity.isWaterDisplayTarget(accessor.getLevel(), accessor.getPosition())) {
                return;
            }

            int purity = WaterPurity.getDisplayPurity(accessor.getLevel(), accessor.getPosition());
            tooltip.add(Component.translatable("yet_another_thirst.jade.water_purity",
                    WaterPurity.purityComponent(purity)));

            if (ThirstConfig.CAN_DRINK_BY_HAND) {
                tooltip.add(new ThirstElement(new ThirstValue(
                        ThirstConfig.HAND_DRINKING_THIRST,
                        ThirstConfig.HAND_DRINKING_QUENCHED
                )));
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
            tooltip.add(Component.translatable("yet_another_thirst.jade.water_purity",
                    WaterPurity.purityComponent(purity)));
            ThirstValues.get(stack).ifPresent(value -> tooltip.add(new ThirstElement(value)));
        }

        @Override
        public ResourceLocation getUid() {

            return WATER_PURITY;
        }
    }
}
