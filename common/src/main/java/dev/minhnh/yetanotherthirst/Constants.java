package dev.minhnh.yetanotherthirst;

import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;

public class Constants {

	public static final String MOD_ID = "yet_another_thirst";
	public static final String CONFIG_DIR = "yetanotherthirst";
	public static final String MOD_NAME = "Yet Another Thirst";
	public static final Logger LOG = LoggerFactory.getLogger(MOD_NAME);
	public static final Identifier THIRST_ICONS = asResource("textures/gui/thirst_icons.png");

	@Nonnull
	public static Identifier asResource(@Nonnull String path) {

		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
