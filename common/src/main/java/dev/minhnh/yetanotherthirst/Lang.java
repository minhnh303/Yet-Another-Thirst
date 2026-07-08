package dev.minhnh.yetanotherthirst;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import javax.annotation.Nonnull;

public class Lang {

    private static final String PREFIX = Constants.MOD_ID + ".";
    public static final String PREFIX_SPACE = "    ";

    private static MutableComponent t(String key, @Nonnull Object... args) {
        return Component.translatable(PREFIX + key, args);
    }

    public static final class Tooltip {
        public static MutableComponent goggleFilterTitle() { return t("tooltip.goggles_filter_title"); }
        public static MutableComponent goggleFilterInputPurity(Object amount, Object purity) { return t("tooltip.goggles_filter_input_purity", amount, purity); }
        public static MutableComponent goggleFilterOutputPurity(Object amount, Object purity) { return t("tooltip.goggles_filter_output_purity", amount, purity); }
        public static MutableComponent goggleFilterCore(Object name) { return t("tooltip.goggles_filter_core", name); }
        public static MutableComponent goggleFilterLifespan(Object pct) { return t("tooltip.goggles_filter_lifespan", pct); }
        public static MutableComponent goggleBoilerTitle() { return t("tooltip.goggles_boiler_title"); }
        public static MutableComponent goggleBoilerInput(Object amount, Object capacity, Object purity) { return t("tooltip.goggles_boiler_input", amount, capacity, purity); }
        public static MutableComponent goggleBoilerOutput(Object amount, Object capacity, Object purity) { return t("tooltip.goggles_boiler_output", amount, capacity, purity); }
        public static MutableComponent filterLifespan(Object pct) { return t("tooltip.filter_lifespan", pct); }
        public static MutableComponent filterMaxPurity(Object purity) { return t("tooltip.filter_max_purity", purity); }
    }

    public static final class FilterMaterial {
        public static MutableComponent fabric() { return t("filter_material.fabric"); }
        public static MutableComponent sand() { return t("filter_material.sand"); }
        public static MutableComponent carbon() { return t("filter_material.carbon"); }
        public static MutableComponent fabricClogged() { return t("filter_material.fabric_clogged"); }
        public static MutableComponent sandClogged() { return t("filter_material.sand_clogged"); }
        public static MutableComponent carbonClogged() { return t("filter_material.carbon_clogged"); }
    }

    public static final class Gui {
        public static MutableComponent inputTank() { return t("gui.input_tank"); }
        public static MutableComponent outputTank() { return t("gui.output_tank"); }
        public static MutableComponent waterBoilerTitle() { return t("gui.water_boiler"); }
    }

    public static final class Jade {
        public static MutableComponent waterPurity(Object purity) { return t("jade.water_purity", purity); }
        public static MutableComponent filterInput(Object amount, Object capacity) { return t("jade.filter_input", amount, capacity); }
        public static MutableComponent filterOutput(Object amount, Object capacity) { return t("jade.filter_output", amount, capacity); }
        public static MutableComponent filterCore(Object name) { return t("jade.filter_core", name); }
        public static MutableComponent boilerInput(Object amount, Object capacity) { return t("jade.boiler_input", amount, capacity); }
        public static MutableComponent boilerOutput(Object amount, Object capacity) { return t("jade.boiler_output", amount, capacity); }
        public static MutableComponent waterBoilerInput(Object purity) { return t("jade.water_boiler_input", purity); }
        public static MutableComponent waterBoilerOutput(Object purity) { return t("jade.water_boiler_output", purity); }
    }

    private Lang() {}
}
