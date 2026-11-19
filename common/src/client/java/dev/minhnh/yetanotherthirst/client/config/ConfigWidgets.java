package dev.minhnh.yetanotherthirst.client.config;

import dev.minhnh.yetanotherthirst.Lang;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import org.lwjgl.glfw.GLFW;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.IntSupplier;
import java.util.function.Predicate;

/**
 * Control widget factories and reusable vanilla-based controls for {@link ConfigOption} rows.
 * Widgets do not know about loader config specs; they read/write exclusively via {@link ConfigSession}.
 */
public final class ConfigWidgets {

    static final int DEFAULT_CONTROL_WIDTH = 130;
    private static final String LANG = Lang.CONFIG_PREFIX;

    public static final ConfigControlFactory<Boolean> TOGGLE_CONTROL_FACTORY =
            (option, session, onChanged) -> new ToggleSwitch(
                    DEFAULT_CONTROL_WIDTH, 20, Boolean.TRUE.equals(session.value(option)),
                    v -> {
                        session.set(option, v);
                        onChanged.run();
                    });

    public static final ConfigControlFactory<Integer> INTEGER_CONTROL_FACTORY =
            (option, session, onChanged) -> buildNumericControl(
                    option, session, DEFAULT_CONTROL_WIDTH,
                    v -> {
                        session.set(option, (int) Math.round(v));
                        onChanged.run();
                    });

    public static final ConfigControlFactory<Double> DECIMAL_CONTROL_FACTORY =
            (option, session, onChanged) -> buildNumericControl(
                    option, session, DEFAULT_CONTROL_WIDTH,
                    v -> {
                        session.set(option, v);
                        onChanged.run();
                    });

    public static final ConfigControlFactory<String> ENUM_CONTROL_FACTORY =
            (option, session, onChanged) -> CycleButton.<String>builder(option::formatValue)
                    .withValues(option.enumValues())
                    .withInitialValue(session.value(option))
                    .displayOnlyValue()
                    .create(0, 0, DEFAULT_CONTROL_WIDTH, 20, option.title(), (button, v) -> {
                        session.set(option, v);
                        onChanged.run();
                    });

    public static final ConfigControlFactory<String> TEXT_CONTROL_FACTORY =
            (option, session, onChanged) -> new ValidatedField(
                    currentFont(), DEFAULT_CONTROL_WIDTH, 18, option.title(),
                    session.value(option), s -> true,
                    s -> {
                        session.set(option, s);
                        onChanged.run();
                    });

    public static final ConfigControlFactory<Integer> COLOR_CONTROL_FACTORY =
            (option, session, onChanged) -> new ColorField(
                    currentFont(), DEFAULT_CONTROL_WIDTH, 18, option.title(),
                    String.format(Locale.ROOT, "#%06X", session.value(option) & 0xFFFFFF),
                    () -> session.value(option),
                    color -> {
                        session.set(option, color);
                        onChanged.run();
                    });

    public static final ConfigControlFactory<List<?>> LIST_CONTROL_FACTORY =
            (option, session, onChanged) -> Button.builder(
                            Component.translatable(LANG + "list.edit", session.value(option).size()),
                            b -> {
                                Minecraft mc = Minecraft.getInstance();
                                if (mc != null && mc.screen instanceof YatConfigScreen screen) {
                                    screen.openListEditor(option);
                                }
                            })
                    .size(DEFAULT_CONTROL_WIDTH, 20)
                    .build();

    private static final Map<ConfigOption.Type, ConfigControlFactory<?>> REGISTRY;

    static {
        Map<ConfigOption.Type, ConfigControlFactory<?>> map = new EnumMap<>(ConfigOption.Type.class);
        map.put(ConfigOption.Type.BOOL, TOGGLE_CONTROL_FACTORY);
        map.put(ConfigOption.Type.INT, INTEGER_CONTROL_FACTORY);
        map.put(ConfigOption.Type.DECIMAL, DECIMAL_CONTROL_FACTORY);
        map.put(ConfigOption.Type.ENUM, ENUM_CONTROL_FACTORY);
        map.put(ConfigOption.Type.STRING, TEXT_CONTROL_FACTORY);
        map.put(ConfigOption.Type.COLOR, COLOR_CONTROL_FACTORY);
        map.put(ConfigOption.Type.LIST, LIST_CONTROL_FACTORY);
        REGISTRY = Collections.unmodifiableMap(map);
    }

    private ConfigWidgets() {
    }

    public static ConfigControlFactory<?> factoryForType(ConfigOption.Type type) {
        return REGISTRY.get(type);
    }

    @SuppressWarnings("unchecked")
    public static <T> ConfigControlFactory<T> factoryFor(ConfigOption<T> option) {
        if (option.controlFactory() != null) {
            return option.controlFactory();
        }
        return (ConfigControlFactory<T>) REGISTRY.get(option.type);
    }

    public static <T> AbstractWidget createControl(ConfigOption<T> option, ConfigSession session, Runnable onChanged) {
        AbstractWidget widget = factoryFor(option).create(option, session, onChanged);
        boolean editable = !session.locked(option);
        applyEditable(widget, editable);
        return widget;
    }

    public static <T> AbstractWidget createControl(
            ConfigOption<T> option, ConfigSession session, int width, Runnable onChanged) {
        AbstractWidget widget = createControl(option, session, onChanged);
        resizeControl(widget, width);
        return widget;
    }

    static void applyEditable(AbstractWidget widget, boolean editable) {
        widget.active = editable;
        if (widget instanceof EditBox box) {
            box.setEditable(editable);
        } else if (widget instanceof StepperControl stepper) {
            stepper.setEditable(editable);
        }
    }

    static void resizeControl(AbstractWidget widget, int width) {
        if (widget instanceof ColorField colorField) {
            colorField.setTotalWidth(width);
        } else if (widget instanceof StepperControl stepper) {
            stepper.setTotalWidth(width);
        } else {
            widget.setWidth(width);
        }
    }

    static void layoutControl(AbstractWidget widget, int x, int top, int rowHeight, int width) {
        resizeControl(widget, width);
        int y = top + (rowHeight - widget.getHeight()) / 2;
        if (widget instanceof ColorField colorField) {
            colorField.setPosition(x + 18, y);
        } else if (widget instanceof StepperControl stepper) {
            stepper.layout(x, top, rowHeight);
        } else {
            widget.setPosition(x + (widget instanceof EditBox ? 1 : 0), y);
        }
    }

    private static Font currentFont() {
        Minecraft mc = Minecraft.getInstance();
        return mc != null ? mc.font : null;
    }

    private static AbstractWidget buildNumericControl(
            ConfigOption<? extends Number> option,
            ConfigSession session,
            int width,
            DoubleConsumer onValidNumber) {
        double value = session.value(option).doubleValue();
        return switch (option.control()) {
            case SLIDER -> new ValueSlider(option, width, 20, value, onValidNumber);
            case FIELD -> numberField(option, width, value, onValidNumber);
            case STEPPER -> new StepperControl(option, session, width, value, onValidNumber);
        };
    }

    static ValidatedField numberField(
            ConfigOption<?> option, int width, double value, DoubleConsumer onValidNumber) {
        ValidatedField field = new ValidatedField(
                currentFont(), width, 18, option.title(), option.numberText(value),
                numberValidator(option),
                text -> onValidNumber.accept(option.snapToStep(Double.parseDouble(text.trim())))) {
            /** Shows the stored (step-snapped) number once editing ends, so the box matches what is saved. */
            @Override
            protected String normalize(String text) {
                return option.numberText(option.snapToStep(Double.parseDouble(text.trim())));
            }
        };
        field.setFilter(text -> text.chars().allMatch(c -> Character.isDigit(c) || c == '-' || c == '.'));
        return field;
    }

    /** An on/off switch with a sliding knob and the state written beside it. */
    static final class ToggleSwitch extends AbstractButton {
        private static final int TRACK_WIDTH = 24;
        private static final int TRACK_HEIGHT = 12;

        private final Consumer<Boolean> onChange;
        private boolean value;
        private float knob;

        ToggleSwitch(int width, int height, boolean value, Consumer<Boolean> onChange) {
            super(0, 0, width, height, Component.empty());
            this.value = value;
            this.knob = value ? 1 : 0;
            this.onChange = onChange;
            updateMessage();
        }

        boolean value() {
            return value;
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
            graphics.fill(knobX, trackY + 1, knobX + TRACK_HEIGHT - 2, trackY + TRACK_HEIGHT - 1,
                    active ? 0xFFF2F2F2 : 0xFF9A9A9A);

            int color = !active ? ConfigTheme.DIM : value ? ConfigTheme.SUCCESS : ConfigTheme.MUTED;
            graphics.drawString(font, getMessage(), x + 6, y + (height - 8) / 2, color);
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
         * Arrow keys move one step (Shift: ten). Vanilla moves 1/(width-8) of the range, which for small ranges
         * such as 0-3 often rounds back to the same step.
         */
        @Override
        public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
            double before = value;
            double stepped = current();
            boolean handled = super.keyPressed(keyCode, scanCode, modifiers);
            boolean arrow = keyCode == GLFW.GLFW_KEY_LEFT || keyCode == GLFW.GLFW_KEY_RIGHT;
            if (handled && arrow && value != before) {
                double direction = keyCode == GLFW.GLFW_KEY_LEFT ? -1 : 1;
                double amount = option.step() * (Screen.hasShiftDown() ? 10 : 1) * direction;
                double next = Mth.clamp(stepped + amount, option.min(), option.max());
                value = toSlider(option, next);
                applyValue();
                updateMessage();
            }
            return handled;
        }
    }

    /**
     * Free-form numeric/text entry. Valid text is reported as it is typed. Text that fails {@code validator}
     * turns red and reports the baseline (the text accepted when the box last lost focus) instead, so a valid
     * prefix such as {@code 10000} of an out-of-range {@code 100000} is never kept. Losing focus with invalid
     * text restores the baseline.
     */
    static class ValidatedField extends EditBox {
        private final Predicate<String> validator;
        private final Consumer<String> onValid;
        private String baseline;
        private String reported;

        ValidatedField(Font font, int width, int height, Component name, String initial,
                       Predicate<String> validator, Consumer<String> onValid) {
            super(font, 0, 0, width, height, name);
            this.validator = validator;
            this.onValid = onValid;
            this.baseline = initial;
            this.reported = initial;
            setMaxLength(256);
            setValue(initial);
            setResponder(this::onTextChanged);
        }

        protected void onTextChanged(String text) {
            boolean valid = validator.test(text);
            setTextColor(valid ? 0xE0E0E0 : ConfigTheme.ERROR & 0xFFFFFF);
            String next = valid ? text : baseline;
            if (valid || !next.equals(reported)) {
                reported = next;
                onValid.accept(next);
            }
        }

        boolean valid() {
            return validator.test(getValue());
        }

        /** Canonical form of valid text, applied when the box loses focus. */
        protected String normalize(String text) {
            return text;
        }

        @Override
        public void setFocused(boolean focused) {
            super.setFocused(focused);
            if (focused) return;
            if (valid()) {
                String normalized = normalize(getValue());
                if (!normalized.equals(getValue())) {
                    setValue(normalized);
                }
                baseline = getValue();
            } else {
                setValue(baseline);
            }
        }
    }

    /** Hex color input box with a 16x16 color swatch rendered immediately to its left. */
    static final class ColorField extends ValidatedField {
        private final IntSupplier colorSupplier;

        ColorField(Font font, int totalWidth, int height, Component name, String initial,
                   IntSupplier colorSupplier, Consumer<Integer> onValidColor) {
            super(font, Math.max(20, totalWidth - 18), height, name, initial,
                    ConfigWidgets::validColor, s -> onValidColor.accept(parseColor(s)));
            this.colorSupplier = colorSupplier;
        }

        void setTotalWidth(int totalWidth) {
            setWidth(Math.max(20, totalWidth - 18));
        }

        @Override
        public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            int swatchX = getX() - 18;
            int swatchY = getY() - 1;
            int color = colorSupplier.getAsInt() & 0xFFFFFF;
            graphics.fill(swatchX, swatchY, swatchX + 16, swatchY + 16, 0xFF000000);
            graphics.fill(swatchX + 1, swatchY + 1, swatchX + 15, swatchY + 15, 0xFF000000 | color);
            super.renderWidget(graphics, mouseX, mouseY, partialTick);
        }
    }

    /** Composite stepper control: [−] [ValidatedField] [+]. */
    static final class StepperControl extends AbstractWidget {
        private final ConfigOption<? extends Number> option;
        private final ConfigSession session;
        private final Button minus;
        private final ValidatedField field;
        private final Button plus;

        StepperControl(
                ConfigOption<? extends Number> option,
                ConfigSession session,
                int width,
                double value,
                DoubleConsumer onValidNumber) {
            super(0, 0, width, 20, option.title());
            this.option = option;
            this.session = session;
            this.field = numberField(option, Math.max(20, width - 36), value, onValidNumber);
            this.minus = Button.builder(Component.literal("−"), b -> step(-1))
                    .tooltip(Tooltip.create(Component.translatable(LANG + "stepper.tooltip")))
                    .size(16, 20)
                    .build();
            this.plus = Button.builder(Component.literal("+"), b -> step(1))
                    .tooltip(Tooltip.create(Component.translatable(LANG + "stepper.tooltip")))
                    .size(16, 20)
                    .build();
        }

        private void step(int direction) {
            double amount = option.step() * (Screen.hasShiftDown() ? 10 : 1) * direction;
            double current = session.value(option).doubleValue();
            double next = Mth.clamp(current + amount, option.min(), option.max());
            field.setValue(option.numberText(next));
        }

        boolean valid() {
            return field.valid();
        }

        void setEditable(boolean editable) {
            this.active = editable;
            this.minus.active = editable;
            this.plus.active = editable;
            this.field.active = editable;
            this.field.setEditable(editable);
        }

        void setTotalWidth(int totalWidth) {
            setWidth(totalWidth);
            field.setWidth(Math.max(20, totalWidth - 36));
        }

        void layout(int x, int top, int rowHeight) {
            setPosition(x, top + (rowHeight - height) / 2);
            int buttonY = top + (rowHeight - 20) / 2;
            int fieldY = top + (rowHeight - 18) / 2;
            minus.setPosition(x, buttonY);
            field.setPosition(x + 19, fieldY);
            plus.setPosition(x + width - 16, buttonY);
        }

        @Override
        protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            minus.render(graphics, mouseX, mouseY, partialTick);
            field.render(graphics, mouseX, mouseY, partialTick);
            plus.render(graphics, mouseX, mouseY, partialTick);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (!active || !visible) return false;
            if (minus.mouseClicked(mouseX, mouseY, button)) {
                field.setFocused(false);
                return true;
            }
            if (plus.mouseClicked(mouseX, mouseY, button)) {
                field.setFocused(false);
                return true;
            }
            if (field.mouseClicked(mouseX, mouseY, button)) {
                field.setFocused(true);
                setFocused(true);
                return true;
            }
            return false;
        }

        @Override
        public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
            return field.isFocused() && field.keyPressed(keyCode, scanCode, modifiers);
        }

        @Override
        public boolean charTyped(char codePoint, int modifiers) {
            return field.isFocused() && field.charTyped(codePoint, modifiers);
        }

        @Override
        public void setFocused(boolean focused) {
            super.setFocused(focused);
            field.setFocused(focused);
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput output) {
            field.updateNarration(output);
        }
    }

    static Predicate<String> numberValidator(ConfigOption<?> option) {
        return text -> {
            if (text == null) return false;
            try {
                double v = option.type == ConfigOption.Type.INT
                        ? Long.parseLong(text.trim())
                        : Double.parseDouble(text.trim());
                return !Double.isNaN(v) && Double.isFinite(v) && v >= option.min() && v <= option.max();
            } catch (NumberFormatException e) {
                return false;
            }
        };
    }

    static boolean validColor(String text) {
        if (text == null) return false;
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

