package dev.minhnh.yetanotherthirst.client.config;

import dev.minhnh.yetanotherthirst.Lang;
import net.minecraft.client.InputType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.CommonInputs;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;
import org.lwjgl.glfw.GLFW;

import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.Predicate;

/** Controls used by option rows. All of them only report values; the row owns the session write. */
final class ConfigWidgets {

    private static final Component RESET_ICON = Component.literal("↺");
    private static final Component RESET_NARRATION = Component.translatable(Lang.CONFIG_PREFIX + "reset.narration");

    private ConfigWidgets() {
    }

    /** Vanilla button with a 2x scaled, centered {@code ↺} glyph that lights up when active. */
    static final class ResetButton extends Button {

        ResetButton(Tooltip tooltip, Runnable onPress) {
            super(0, 0, 20, 20, Component.empty(), b -> onPress.run(), DEFAULT_NARRATION);
            setTooltip(tooltip);
        }

        /** The visible label is the icon only; narrators read a spoken name instead of an empty message. */
        @Override
        protected MutableComponent createNarrationMessage() {
            return wrapDefaultNarrationMessage(RESET_NARRATION);
        }

        @Override
        protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            super.renderWidget(graphics, mouseX, mouseY, partialTick);
            Font font = Minecraft.getInstance().font;
            int color = !active
                    ? ConfigTheme.DIM
                    : (isHoveredOrFocused() ? ConfigTheme.TEXT : ConfigTheme.MODIFIED);
            float scale = 2.0F;
            var pose = graphics.pose();
            pose.pushPose();
            pose.translate(getX() + getWidth() / 2.0F + 1.0F, getY() + getHeight() / 2.0F - 2.0F, 0.0F);
            pose.scale(scale, scale, 1.0F);
            int textW = font.width(RESET_ICON);
            graphics.drawString(font, RESET_ICON, Math.round(-textW / 2.0F), -4, color, active);
            pose.popPose();
        }
    }

    /** An on/off switch with a sliding knob and the state written beside it. */
    static final class ToggleSwitch extends AbstractButton {
        private static final int TRACK_WIDTH = 24;
        private static final int TRACK_HEIGHT = 12;

        private final Component name;
        private final Consumer<Boolean> onChange;
        private boolean value;
        private float knob;

        ToggleSwitch(Component name, int width, int height, boolean value, Consumer<Boolean> onChange) {
            super(0, 0, width, height, Component.empty());
            this.name = name;
            this.value = value;
            this.knob = value ? 1 : 0;
            this.onChange = onChange;
            updateMessage();
        }

        private void updateMessage() {
            setMessage(Component.translatable(value ? "options.on" : "options.off"));
        }

        @Override
        public void onPress() {
            value = !value;
            updateMessage();
            onChange.accept(value);
        }

        @Override
        protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            Font font = Minecraft.getInstance().font;
            knob += ((value ? 1 : 0) - knob) * 0.35F;
            int x = getX();
            int y = getY();
            graphics.fill(x, y, x + width, y + height, isHoveredOrFocused() ? 0x40000000 : 0x28000000);
            if (isHoveredOrFocused() && active) {
                ConfigTheme.outline(graphics, x, y, width, height, 0x80FFFFFF);
            }

            int trackX = x + width - TRACK_WIDTH - 5;
            int trackY = y + (height - TRACK_HEIGHT) / 2;
            int track = value ? ConfigTheme.SWITCH_ON : ConfigTheme.SWITCH_OFF;
            if (!active) track = (track & 0x00FFFFFF) | 0x80000000;
            graphics.fill(trackX + 1, trackY, trackX + TRACK_WIDTH - 1, trackY + TRACK_HEIGHT, track);
            graphics.fill(trackX, trackY + 1, trackX + TRACK_WIDTH, trackY + TRACK_HEIGHT - 1, track);
            int knobX = trackX + 1 + Math.round(knob * (TRACK_WIDTH - TRACK_HEIGHT));
            graphics.fill(knobX, trackY + 1, knobX + TRACK_HEIGHT - 2, trackY + TRACK_HEIGHT - 1, active ? 0xFFF2F2F2 : 0xFF9A9A9A);

            int color = !active ? ConfigTheme.DIM : value ? ConfigTheme.SUCCESS : ConfigTheme.MUTED;
            graphics.drawString(font, getMessage(), x + 6, y + (height - 8) / 2, color);
        }

        /** Reads "Setting name: On" rather than a bare state the listener cannot place. */
        @Override
        protected MutableComponent createNarrationMessage() {
            return wrapDefaultNarrationMessage(CommonComponents.optionNameValue(name, getMessage()));
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput output) {
            defaultButtonNarrationText(output);
        }
    }

    /** A vanilla slider over [min, max] that snaps to the option's step and labels itself with the option format. */
    static final class ValueSlider extends AbstractSliderButton {
        private final ConfigOption<?> option;
        private final DoubleConsumer onChange;

        ValueSlider(ConfigOption<?> option, int width, int height, double current, DoubleConsumer onChange) {
            super(0, 0, width, height, Component.empty(), toSlider(option, current));
            this.option = option;
            this.onChange = onChange;
            updateMessage();
        }

        private static double toSlider(ConfigOption<?> option, double v) {
            double range = option.max() - option.min();
            return range <= 0 ? 0 : Mth.clamp((v - option.min()) / range, 0, 1);
        }

        double current() {
            double raw = option.min() + value * (option.max() - option.min());
            double snapped = option.min() + Math.round((raw - option.min()) / option.step()) * option.step();
            return Mth.clamp(snapped, option.min(), option.max());
        }

        @Override
        protected void updateMessage() {
            setMessage(option.formatNumber(current()));
        }

        @Override
        protected void applyValue() {
            onChange.accept(current());
        }

        /**
         * Mirrors vanilla's edit mode (on after a click or Tab focus, toggled by Enter/Space), but arrow keys move by
         * one option step: vanilla moves by one pixel, which on short ranges such as purity 0-3 needs many presses
         * before the snapped value changes.
         */
        @Override
        public void setFocused(boolean focused) {
            super.setFocused(focused);
            if (!focused) {
                editing = false;
            } else {
                InputType input = Minecraft.getInstance().getLastInputType();
                editing = input == InputType.MOUSE || input == InputType.KEYBOARD_TAB;
            }
        }

        @Override
        public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
            if (CommonInputs.selected(keyCode)) {
                editing = !editing;
                return super.keyPressed(keyCode, scanCode, modifiers);
            }
            if (editing && (keyCode == GLFW.GLFW_KEY_LEFT || keyCode == GLFW.GLFW_KEY_RIGHT)) {
                double next = current() + (keyCode == GLFW.GLFW_KEY_LEFT ? -option.step() : option.step());
                double clamped = Mth.clamp(next, option.min(), option.max());
                double before = value;
                value = toSlider(option, clamped);
                if (value != before) {
                    updateMessage();
                    applyValue();
                }
                return true;
            }
            return super.keyPressed(keyCode, scanCode, modifiers);
        }

        private boolean editing;

        /** Same wording as vanilla sliders, with the setting name in front of the value. */
        @Override
        protected MutableComponent createNarrationMessage() {
            return Component.translatable("gui.narrate.slider", CommonComponents.optionNameValue(option.title(), getMessage()));
        }
    }

    /**
     * Free-form numeric/text entry. Valid text is reported on every keystroke. Invalid text turns red and reports
     * the value the box held when editing began instead, so a valid prefix typed on the way (e.g. {@code 10000}
     * while typing an out-of-range {@code 100000}) is never what gets saved; on blur the box shows that value
     * again. {@link #keepInvalidOnBlur()} turns both off: the owner then tracks the shown text itself.
     */
    static class ValidatedField extends EditBox {
        private final Predicate<String> validator;
        private final Consumer<String> onValid;
        private String baseline;
        private boolean revertOnBlur = true;

        ValidatedField(Font font, int width, int height, Component name, String initial,
                       Predicate<String> validator, Consumer<String> onValid) {
            super(font, width, height, name);
            this.validator = validator;
            this.onValid = onValid;
            this.baseline = initial;
            setMaxLength(256);
            setValue(initial);
            setResponder(this::onTextChanged);
        }

        protected void onTextChanged(String text) {
            boolean valid = validator.test(text);
            setTextColor(valid ? 0xE0E0E0 : ConfigTheme.ERROR & 0xFFFFFF);
            if (valid) {
                onValid.accept(text);
            } else if (revertOnBlur) {
                onValid.accept(baseline);
            }
        }

        boolean valid() {
            return validator.test(getValue());
        }

        void keepInvalidOnBlur() {
            revertOnBlur = false;
        }

        @Override
        public void setFocused(boolean focused) {
            super.setFocused(focused);
            if (!revertOnBlur) return;
            if (!focused && !valid()) {
                setValue(baseline);
            }
            if (valid()) {
                baseline = getValue();
            }
        }
    }

    static Predicate<String> numberValidator(ConfigOption<?> option) {
        return text -> {
            try {
                double v = option.type == ConfigOption.Type.INT ? Long.parseLong(text.trim()) : Double.parseDouble(text.trim());
                return !Double.isNaN(v) && v >= option.min() && v <= option.max();
            } catch (NumberFormatException e) {
                return false;
            }
        };
    }

    static boolean validColor(String text) {
        String hex = text.trim().toLowerCase(Locale.ROOT);
        if (hex.startsWith("#")) hex = hex.substring(1);
        if (hex.length() != 6) return false;
        for (char c : hex.toCharArray()) {
            if (Character.digit(c, 16) < 0) return false;
        }
        return true;
    }

    static int parseColor(String text) {
        String hex = text.trim();
        if (hex.startsWith("#")) hex = hex.substring(1);
        return Integer.parseInt(hex, 16);
    }
}
