package dev.minhnh.yetanotherthirst.compat;

import dev.minhnh.yetanotherthirst.Constants;
import dev.minhnh.yetanotherthirst.core.purity.WaterPurity;
import dev.minhnh.yetanotherthirst.core.thirst.ThirstConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
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
                tooltip.add(Component.translatable("yet_another_thirst.tooltip.thirst_value",
                        ThirstConfig.HAND_DRINKING_THIRST,
                        ThirstConfig.HAND_DRINKING_QUENCHED).withStyle(ChatFormatting.AQUA));
            }
        }

        @Override
        public ResourceLocation getUid() {

            return WATER_PURITY;
        }
    }
}
