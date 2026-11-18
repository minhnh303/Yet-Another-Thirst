package dev.minhnh.yetanotherthirst;

import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import javax.annotation.Nonnull;

public class Lang {

    /** Short namespace for mod-owned translation keys; registry-derived keys keep the full mod id. */
    public static final String PREFIX = "yat.";
    public static final String CONFIG_PREFIX = PREFIX + "config.";
    public static final String PREFIX_SPACE = "    ";

    private static MutableComponent t(String key, @Nonnull Object... args) {
        return Component.translatable(PREFIX + key, args);
    }

    /** Translation key for a purity level (0 dirty .. 3 purified). */
    public static String purityKey(int purity) {
        return PREFIX + "purity." + switch (purity) {
            case 0 -> "dirty";
            case 1 -> "slightly_dirty";
            case 2 -> "acceptable";
            default -> "purified";
        };
    }

    public static final class Tooltip {
        public static MutableComponent goggleFilterTitle() { return t("tooltip.goggles.title"); }
        public static MutableComponent goggleFilterInputPurity(Object amount, Object purity) { return t("tooltip.goggles.input_purity", amount, purity); }
        public static MutableComponent goggleFilterInputEmpty() { return t("tooltip.goggles.input_empty"); }
        public static MutableComponent goggleFilterOutputPurity(Object amount, Object purity) { return t("tooltip.goggles.output_purity", amount, purity); }
        public static MutableComponent goggleFilterOutputEmptyPurity(Object purity) { return t("tooltip.goggles.output_empty_purity", purity); }
        public static MutableComponent goggleFilterOutputEmpty() { return t("tooltip.goggles.output_empty"); }
        public static MutableComponent filterCoreStatus(Object name, Object pct) { return t("tooltip.filter.core_status", name, pct); }
        public static MutableComponent filterCoreStatusNoDurability(Object name) { return t("tooltip.filter.core_status_no_durability", name); }
        public static MutableComponent filterEmpty() { return t("tooltip.filter.empty"); }
        public static MutableComponent filterLifespan(Object pct) { return t("tooltip.filter.lifespan", pct); }
        public static MutableComponent filterMaxPurity(Object purity) { return t("tooltip.filter.max_purity", purity); }
    }

    public static final class Jade {
        public static MutableComponent waterBoilerInput(Object purity) { return t("jade.water_boiler_input", purity); }
        public static MutableComponent waterBoilerOutput(Object purity) { return t("jade.water_boiler_output", purity); }
        public static MutableComponent filterCoreClogged() { return t("jade.filter_core_clogged"); }
        public static MutableComponent filterCoreDurability(Object pct) { return t("jade.filter_core_durability", pct); }
        public static MutableComponent filterCoreEmpty() { return t("jade.filter_core_empty"); }
        public static MutableComponent filterInput(Object purity) { return t("jade.filter_input", purity); }
        public static MutableComponent filterOutput(Object purity) { return t("jade.filter_output", purity); }
        public static MutableComponent waterPurity(Object purity) { return t("jade.water_purity", purity); }
    }

    public static final class Gui {
        public static MutableComponent inputTank() { return t("gui.input_tank"); }
        public static MutableComponent outputTank() { return t("gui.output_tank"); }
        public static MutableComponent energy() { return t("gui.energy"); }
        public static MutableComponent energyRate(Object received, Object required) { return t("gui.energy_rate", received, required); }
    }

    /** Action-bar messages shown when a hammer reconfigures a Water Boiler side. */
    public static final class WaterBoiler {
        public static MutableComponent inletSet(Direction face, boolean upper) { return t("tooltip.water_boiler.inlet_set", direction(face), half(upper)); }
        public static MutableComponent outletSet(Direction face, boolean upper) { return t("tooltip.water_boiler.outlet_set", direction(face), half(upper)); }
        public static MutableComponent portSet(Direction face, boolean upper) { return t("tooltip.water_boiler.port_set", direction(face), half(upper)); }
        public static MutableComponent portRemoved(Direction face, boolean upper) { return t("tooltip.water_boiler.port_removed", direction(face), half(upper)); }
    }

    public static MutableComponent direction(Direction direction) { return t("direction." + direction.getName()); }

    public static MutableComponent half(boolean upper) { return t(upper ? "block_half.upper" : "block_half.lower"); }

    public static final class FilterMaterial {
        public static MutableComponent fabric() { return t("filter_material.fabric"); }
        public static MutableComponent sand() { return t("filter_material.sand"); }
        public static MutableComponent carbon() { return t("filter_material.carbon"); }
        public static MutableComponent fabricClogged() { return t("filter_material.fabric_clogged"); }
        public static MutableComponent sandClogged() { return t("filter_material.sand_clogged"); }
        public static MutableComponent carbonClogged() { return t("filter_material.carbon_clogged"); }
    }
}
