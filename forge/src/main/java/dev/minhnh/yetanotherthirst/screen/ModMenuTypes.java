package dev.minhnh.yetanotherthirst.screen;

import dev.minhnh.yetanotherthirst.Constants;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENU_TYPES =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, Constants.MOD_ID);

    public static final RegistryObject<MenuType<WaterBoilerMenu>> WATER_BOILER = net.minecraftforge.fml.ModList.get().isLoaded("immersiveengineering") ?
            MENU_TYPES.register("water_boiler", () -> IForgeMenuType.create(WaterBoilerMenu::new)) : null;

    private ModMenuTypes() {}
}
