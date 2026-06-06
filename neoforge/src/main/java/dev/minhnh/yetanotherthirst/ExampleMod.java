package dev.minhnh.yetanotherthirst;

import net.minecraftforge.fml.common.Mod;

@Mod(Constants.MOD_ID)
public class ExampleMod {
    
    public ExampleMod() {

        NeoForgeNetwork.register();
        CommonClass.init();
        
    }
}
