package dev.minhnh.yetanotherthirst.client.config;

import dev.minhnh.yetanotherthirst.Lang;
import dev.minhnh.yetanotherthirst.client.config.ConfigOption.Type;
import dev.minhnh.yetanotherthirst.client.config.ConfigWidgets.ToggleSwitch;
import dev.minhnh.yetanotherthirst.client.config.ConfigWidgets.ValidatedField;
import dev.minhnh.yetanotherthirst.client.config.ConfigWidgets.ValueSlider;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Registry mapping each {@link ConfigOption.Type} to the factory that creates its row controls.
 * Adding a new option type only requires registering a factory here without modifying {@link OptionList}.
 */
@FunctionalInterface
interface ConfigControlFactory {

    String LANG = Lang.CONFIG_PREFIX;
    /** Built-in factories are present from class init, so {@link #register} only adds or overrides. */
    Map<Type, ConfigControlFactory> REGISTRY = defaults();

    record Context(
            Font font,
            YatConfigScreen screen,
            ConfigSession session,
            ConfigOption<?> option,
            int controlWidth,
            Consumer<Object> onChange
    ) {
        Object value() {
            return session.value(option);
        }
    }

    List<AbstractWidget> create(Context context);

    static void register(Type type, ConfigControlFactory factory) {
        REGISTRY.put(Objects.requireNonNull(type), Objects.requireNonNull(factory));
    }

    static ConfigControlFactory forType(Type type) {
        ConfigControlFactory factory = REGISTRY.get(type);
        if (factory == null) {
            throw new IllegalStateException("No ConfigControlFactory registered for " + type);
        }
        return factory;
    }

    private static Map<Type, ConfigControlFactory> defaults() {
        Map<Type, ConfigControlFactory> map = new EnumMap<>(Type.class);
        map.put(Type.BOOL, ctx -> List.of(
                new ToggleSwitch(ctx.option().title(), ctx.controlWidth(), 20, (Boolean) ctx.value(), ctx.onChange()::accept)));

        ConfigControlFactory numericFactory = ctx -> buildNumericControls(ctx, ((Number) ctx.value()).doubleValue());
        map.put(Type.INT, numericFactory);
        map.put(Type.DECIMAL, numericFactory);

        map.put(Type.ENUM, ctx -> List.of(
                CycleButton.<String>builder(ctx.option()::formatValue)
                        .withValues(ctx.option().enumValues())
                        .withInitialValue((String) ctx.value())
                        .displayOnlyValue()
                        .create(0, 0, ctx.controlWidth(), 20, ctx.option().title(),
                                (button, v) -> ctx.onChange().accept(v))));

        map.put(Type.STRING, ctx -> List.of(
                new ValidatedField(ctx.font(), ctx.controlWidth(), 18, ctx.option().title(),
                        (String) ctx.value(), s -> true, ctx.onChange()::accept)));

        map.put(Type.COLOR, ctx -> List.of(
                new ValidatedField(ctx.font(), ctx.controlWidth() - 18, 18, ctx.option().title(),
                        String.format(Locale.ROOT, "#%06X", (Integer) ctx.value()),
                        ConfigWidgets::validColor,
                        s -> ctx.onChange().accept(ConfigWidgets.parseColor(s)))));

        map.put(Type.LIST, ctx -> List.of(
                Button.builder(
                                Component.translatable(LANG + "list.edit", ((List<?>) ctx.value()).size()),
                                b -> ctx.screen().openListEditor(ctx.option()))
                        .size(ctx.controlWidth(), 20).build()));
        return map;
    }

    private static List<AbstractWidget> buildNumericControls(Context ctx, double value) {
        ConfigOption<?> option = ctx.option();
        int w = ctx.controlWidth();
        return switch (option.control()) {
            case SLIDER -> List.of(new ValueSlider(option, w, 20, value, ctx.onChange()::accept));
            case FIELD -> List.of(numberField(ctx, w, value));
            case STEPPER -> {
                ValidatedField field = numberField(ctx, w - 36, value);
                Button minus = Button.builder(Component.literal("-"), b -> step(ctx, field, -1))
                        .tooltip(Tooltip.create(Component.translatable(LANG + "stepper.tooltip")))
                        .size(16, 20).build();
                Button plus = Button.builder(Component.literal("+"), b -> step(ctx, field, 1))
                        .tooltip(Tooltip.create(Component.translatable(LANG + "stepper.tooltip")))
                        .size(16, 20).build();
                yield List.of(minus, field, plus);
            }
        };
    }

    private static ValidatedField numberField(Context ctx, int width, double value) {
        ConfigOption<?> option = ctx.option();
        ValidatedField field = new ValidatedField(ctx.font(), width, 18, option.title(),
                option.numberText(value), ConfigWidgets.numberValidator(option), ctx.onChange()::accept);
        field.setFilter(text -> text.chars().allMatch(c -> Character.isDigit(c) || c == '-' || c == '.'));
        return field;
    }

    private static void step(Context ctx, ValidatedField field, int direction) {
        ConfigOption<?> option = ctx.option();
        double amount = option.step() * (Screen.hasShiftDown() ? 10 : 1) * direction;
        double current = ((Number) ctx.session().value(option)).doubleValue();
        double next = Mth.clamp(current + amount, option.min(), option.max());
        field.setValue(option.numberText(next));
    }
}
