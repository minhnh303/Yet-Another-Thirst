package dev.minhnh.yetanotherthirst.screen;

import dev.minhnh.yetanotherthirst.Constants;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModMenuTypes {

    public static final DeferredRegister<MenuType<?>> MENU_TYPES =
            DeferredRegister.create(Registries.MENU, Constants.MOD_ID);

    public static final DeferredHolder<MenuType<?>, MenuType<WaterBoilerMenu>> WATER_BOILER =
            MENU_TYPES.register("water_boiler", () -> IMenuTypeExtension.create(WaterBoilerMenu::new));

    private ModMenuTypes() {}
}
