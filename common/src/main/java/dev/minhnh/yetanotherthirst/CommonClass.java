package dev.minhnh.yetanotherthirst;

import dev.minhnh.yetanotherthirst.platform.Services;

public class CommonClass {

    public static void init() {

        Constants.LOG.info("{} initialized on {} ({})", Constants.MOD_NAME, Services.PLATFORM.getPlatformName(), Services.PLATFORM.getEnvironmentName());
    }
}
