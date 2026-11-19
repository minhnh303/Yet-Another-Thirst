package dev.minhnh.yetanotherthirst.client.config;

import dev.minhnh.yetanotherthirst.Lang;
import dev.minhnh.yetanotherthirst.client.config.ConfigWidgets.ValidatedField;
import dev.minhnh.yetanotherthirst.client.config.ListSchema.Column;
import dev.minhnh.yetanotherthirst.client.config.ListSchema.Kind;
import dev.minhnh.yetanotherthirst.core.thirst.ThirstConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Shared schema-driven list editor for item values, item/effect id lists, and temperature tiers.
 * Caches registry completions and resolved item/effect visuals for the screen's lifecycle.
 */
final class ListEditorScreen<T> extends Screen {

    private static final String LANG = Lang.CONFIG_PREFIX;
    private static final int HEADER_HEIGHT = 50;
    private static final int FOOTER_HEIGHT = 36;
    private static final int ICON_WIDTH = 20;
    private static final int REMOVE_WIDTH = 20;

    private record ResolvedVisual(ItemStack itemStack, MobEffect effect, Component tooltip) {
    }

    private final Screen parent;
    private final ConfigOption<List<T>> option;
    private final ListSchema<T> schema;
    private final List<T> defaultValue;
    private final Consumer<List<T>> onDone;
    private final ListEditorModel<T> model;
    /** Per-screen icon/name cache; bounded because every typed prefix becomes a key. */
    private static final int VISUAL_CACHE_LIMIT = 512;
    private final Map<String, ResolvedVisual> visualCache = new HashMap<>();

    private EditorList editor;
    private EditBox filter;
    private Button done;
    private String filterText = "";
    private List<String> completions;
    private boolean focusFirstRow;

    @SuppressWarnings("unchecked")
    ListEditorScreen(
            Screen parent,
            ConfigOption<List<T>> option,
            List<T> current,
            List<T> defaultValue,
            Consumer<List<T>> onDone) {
        super(Component.translatable(LANG + "list.title", option.title()));
        this.parent = parent;
        this.option = option;
        this.schema = (ListSchema<T>) option.schema();
        this.defaultValue = defaultValue;
        this.onDone = onDone;
        this.model = new ListEditorModel<>(schema, current);
    }

    @Override
    protected void init() {
        double scroll = editor == null ? 0 : editor.getScrollAmount();
        editor = new EditorList(minecraft, width, height, HEADER_HEIGHT, height - FOOTER_HEIGHT);
        addRenderableWidget(editor);

        int filterWidth = Math.min(160, width / 3);
        filter = new EditBox(font, width - filterWidth - 10, 8, filterWidth, 18,
                Component.translatable(LANG + "list.filter"));
        filter.setHint(Component.translatable(LANG + "list.filter").withStyle(ChatFormatting.DARK_GRAY));
        filter.setValue(filterText);
        filter.setResponder(text -> {
            String next = text.trim().toLowerCase(Locale.ROOT);
            if (!next.equals(filterText)) {
                filterText = next;
                editor.rebuild();
            }
        });
        addRenderableWidget(filter);

        int y = height - FOOTER_HEIGHT + (FOOTER_HEIGHT - 20) / 2;
        addRenderableWidget(Button.builder(Component.translatable(LANG + "list.add"), b -> addRow())
                .bounds(10, y, 80, 20).build());
        addRenderableWidget(Button.builder(Component.translatable(LANG + "list.reset"), b -> {
                    model.load(defaultValue);
                    updateDoneState();
                    editor.rebuild();
                })
                .tooltip(Tooltip.create(Component.translatable(LANG + "list.reset.tooltip")))
                .bounds(94, y, 80, 20).build());
        int buttonWidth = Mth.clamp((width - 200) / 2, 60, 90);
        done = addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, b -> finish())
                .bounds(width - 10 - buttonWidth, y, buttonWidth, 20).build());
        addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, b -> onClose())
                .bounds(width - 14 - buttonWidth * 2, y, buttonWidth, 20).build());

        editor.rebuild();
        editor.setScrollAmount(scroll);
        updateDoneState();
    }

    private void addRow() {
        model.addBlank();
        updateDoneState();
        if (!filterText.isEmpty()) {
            filter.setValue("");
        } else {
            editor.rebuild();
        }
        editor.setScrollAmount(0);
        focusFirstRow = true;
    }

    /**
     * Focuses the new row's first field. Runs after the Add button's click has finished, because the screen
     * moves focus to the clicked button once {@code onPress} returns.
     */
    private void applyPendingFocus() {
        if (!focusFirstRow) return;
        focusFirstRow = false;
        if (!editor.children().isEmpty()) {
            EditorRow first = editor.children().get(0);
            setFocused(editor);
            editor.setFocused(first);
            first.focusFirstField();
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        boolean handled = super.mouseClicked(mouseX, mouseY, button);
        applyPendingFocus();
        return handled;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        boolean handled = super.keyPressed(keyCode, scanCode, modifiers);
        applyPendingFocus();
        return handled;
    }

    private void updateDoneState() {
        if (done != null) {
            done.active = model.invalidCount() == 0;
        }
    }

    private void finish() {
        if (model.invalidCount() > 0) return;
        onDone.accept(model.result());
        minecraft.setScreen(parent);
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parent);
    }

    @Override
    public void renderBackground(GuiGraphics graphics) {
        super.renderBackground(graphics);
        int footerTop = height - FOOTER_HEIGHT;
        graphics.fill(0, HEADER_HEIGHT, width, footerTop, ConfigTheme.LIST_BG);
        graphics.fill(0, HEADER_HEIGHT - 1, width, HEADER_HEIGHT, ConfigTheme.LINE);
        graphics.fill(0, footerTop, width, footerTop + 1, ConfigTheme.LINE);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        graphics.drawString(font, title.copy().withStyle(ChatFormatting.BOLD), 10, 8, ConfigTheme.TEXT);
        ConfigTheme.clipped(graphics, font, option.description(), 10, 20, filter.getX() - 20, ConfigTheme.MUTED);

        int left = editor.getRowLeft();
        int x = left + (schema.columns().get(0).numeric() ? 0 : ICON_WIDTH);
        int[] widths = editor.columnWidths();
        for (int i = 0; i < widths.length; i++) {
            graphics.drawString(font, Component.translatable(LANG + "column." + schema.columns().get(i).label()),
                    x + 2, HEADER_HEIGHT - 11, ConfigTheme.ACCENT);
            x += widths[i] + 4;
        }

        Component status = Component.translatable(LANG + "list.status", model.rows().size(), model.invalidCount());
        int statusX = 182;
        int statusRight = width - 18 - done.getWidth() * 2;
        if (statusRight - statusX > 40) {
            ConfigTheme.clipped(graphics, font, status, statusX, height - FOOTER_HEIGHT + 14, statusRight - statusX,
                    model.invalidCount() > 0 ? ConfigTheme.ERROR : ConfigTheme.DIM);
        }
    }

    /** Lazily initialized and sorted registry IDs for first-column Tab-completion. */
    private List<String> completions() {
        if (completions == null) {
            Kind kind = schema.columns().get(0).kind();
            Stream<String> ids = switch (kind) {
                case ITEM -> Stream.concat(
                        BuiltInRegistries.ITEM.keySet().stream().map(ResourceLocation::toString),
                        BuiltInRegistries.ITEM.getTagNames().map(tag -> "#" + tag.location()));
                case EFFECT -> BuiltInRegistries.MOB_EFFECT.keySet().stream().map(ResourceLocation::toString);
                default -> Stream.empty();
            };
            completions = ids.sorted().toList();
        }
        return completions;
    }

    private String complete(String typed) {
        String text = typed.trim().toLowerCase(Locale.ROOT);
        if (text.isEmpty()) return null;
        String byPath = null;
        for (String id : completions()) {
            if (id.equals(text)) return null;
            if (id.startsWith(text)) return id;
            if (byPath == null) {
                int colon = id.indexOf(':');
                String path = id.substring(colon + 1);
                if (path.startsWith(text) && (id.startsWith("#") == text.startsWith("#") || !text.startsWith("#"))) {
                    byPath = id;
                }
            }
        }
        return byPath;
    }

    private ResolvedVisual resolveVisual(String rawId) {
        String id = rawId == null ? "" : rawId.trim();
        if (id.isEmpty()) {
            return new ResolvedVisual(ItemStack.EMPTY, null, null);
        }
        if (visualCache.size() >= VISUAL_CACHE_LIMIT && !visualCache.containsKey(id)) {
            visualCache.clear();
        }
        return visualCache.computeIfAbsent(id, key -> {
            if (schema.columns().get(0).kind() == Kind.ITEM) {
                if (key.startsWith("#") && !tagsLoaded()) {
                    return new ResolvedVisual(ItemStack.EMPTY, null,
                            Component.translatable(LANG + "list.tag_unloaded", key).withStyle(ChatFormatting.GRAY));
                }
                ItemStack stack = ListSchema.resolveItemIcon(key);
                if (!stack.isEmpty()) {
                    Component name = key.startsWith("#")
                            ? Component.translatable(LANG + "list.tag", key).append(" · ").append(stack.getHoverName())
                            : stack.getHoverName();
                    return new ResolvedVisual(stack, null, name);
                }
            } else {
                Optional<MobEffect> effect = resolveEffect(key);
                if (effect.isPresent()) {
                    return new ResolvedVisual(ItemStack.EMPTY, effect.get(), effect.get().getDisplayName());
                }
            }
            return new ResolvedVisual(
                    ItemStack.EMPTY,
                    null,
                    Component.translatable(LANG + "list.unknown").withStyle(ChatFormatting.GOLD));
        });
    }

    /** Item tags are bound only after joining a world; on the title screen every tag would look unknown. */
    private static boolean tagsLoaded() {
        return BuiltInRegistries.ITEM.getTagNames().findAny().isPresent();
    }

    private static Optional<MobEffect> resolveEffect(String rule) {
        ThirstConfig.EffectCondition condition = ThirstConfig.EffectCondition.parse(rule);
        if (condition == null) return Optional.empty();
        return BuiltInRegistries.MOB_EFFECT.getOptional(condition.effectId);
    }

    private final class EditorList extends ContainerObjectSelectionList<EditorRow> {

        EditorList(Minecraft minecraft, int width, int screenHeight, int y0, int y1) {
            super(minecraft, width, screenHeight, y0, y1, 24);
            setRenderBackground(false);
            setRenderTopAndBottom(false);
            setRenderSelection(false);
        }

        @Override
        public int getRowWidth() {
            return Math.min(width - 40, 440);
        }

        @Override
        protected int getScrollbarPosition() {
            return getRowLeft() + getRowWidth() + 6;
        }

        /** Losing focus to a screen widget also clears the focused row, so its text field blurs. */
        @Override
        public void setFocused(boolean focused) {
            if (!focused) {
                setFocused((GuiEventListener) null);
            }
        }

        /** Drops focus left on a row that a click handler removed (the remove button rebuilds the list). */
        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            boolean handled = super.mouseClicked(mouseX, mouseY, button);
            GuiEventListener focused = getFocused();
            if (focused != null && !children().contains(focused)) {
                setFocused((GuiEventListener) null);
            }
            return handled;
        }

        int[] columnWidths() {
            List<Column> columns = schema.columns();
            int[] widths = new int[columns.size()];
            boolean hasId = !columns.get(0).numeric();
            int available = getRowWidth() - REMOVE_WIDTH - 4 - (hasId ? ICON_WIDTH : 0) - 4 * (columns.size() - 1);
            int numeric = hasId ? 46 : available / columns.size();
            int idWidth = available;
            for (int i = 0; i < columns.size(); i++) {
                if (columns.get(i).numeric()) {
                    widths[i] = numeric;
                    idWidth -= numeric;
                }
            }
            for (int i = 0; i < columns.size(); i++) {
                if (!columns.get(i).numeric()) {
                    widths[i] = idWidth;
                }
            }
            return widths;
        }

        void rebuild() {
            double scroll = getScrollAmount();
            setFocused(null);
            List<EditorRow> filtered = new ArrayList<>();
            for (String[] row : model.rows()) {
                if (filterText.isEmpty() || matchesFilter(row, filterText)) {
                    filtered.add(new EditorRow(row));
                }
            }
            replaceEntries(filtered);
            setScrollAmount(scroll);
        }

        private boolean matchesFilter(String[] row, String query) {
            for (String cell : row) {
                if (cell != null && cell.toLowerCase(Locale.ROOT).contains(query)) {
                    return true;
                }
            }
            return false;
        }

        void remove(String[] row) {
            model.remove(row);
            updateDoneState();
            rebuild();
        }
    }

    private final class EditorRow extends ContainerObjectSelectionList.Entry<EditorRow> {
        private final String[] row;
        private final List<EditBox> fields = new ArrayList<>();
        private final List<GuiEventListener> children = new ArrayList<>();
        private final Button remove;

        EditorRow(String[] row) {
            this.row = row;
            int[] widths = editor.columnWidths();
            for (int i = 0; i < row.length; i++) {
                Column column = schema.columns().get(i);
                int index = i;
                Predicate<String> store = text -> {
                    boolean ok = model.set(row, index, text);
                    updateDoneState();
                    return ok;
                };
                ValidatedField field = i == 0 && !column.numeric()
                        ? new IdField(font, widths[i], row[i], store)
                        : new ValidatedField(
                        font, widths[i], 18,
                        Component.translatable(LANG + "column." + column.label()),
                        row[i],
                        store,
                        text -> { });
                if (column.numeric()) {
                    field.setFilter(text -> text.chars().allMatch(c -> Character.isDigit(c) || c == '-' || c == '.'));
                }
                fields.add(field);
            }
            remove = Button.builder(Component.literal("✕").withStyle(ChatFormatting.RED), b -> editor.remove(row))
                    .tooltip(Tooltip.create(Component.translatable(LANG + "list.remove")))
                    .size(REMOVE_WIDTH, 20).build();
            children.addAll(fields);
            children.add(remove);
        }

        /** Clears the focused field when the row itself loses focus (the 1.20.1 default does nothing). */
        @Override
        public void setFocused(boolean focused) {
            if (!focused) {
                setFocused((GuiEventListener) null);
            }
        }

        void focusFirstField() {
            EditBox first = fields.get(0);
            setFocused(first);
            first.setFocused(true);
        }

        @Override
        public void render(GuiGraphics graphics, int index, int top, int left, int width, int height,
                           int mouseX, int mouseY, boolean hovering, float partialTick) {
            if (hovering) {
                graphics.fill(left - 4, top - 2, left + width + 2, top + height + 2, ConfigTheme.ROW_HOVER);
            }
            int x = left;
            if (!schema.columns().get(0).numeric()) {
                renderIcon(graphics, x + 1, top + 2, mouseX, mouseY);
                x += ICON_WIDTH;
            }
            for (EditBox field : fields) {
                field.setPosition(x + 1, top + 1);
                field.render(graphics, mouseX, mouseY, partialTick);
                x += field.getWidth() + 4;
            }
            remove.setPosition(left + width - REMOVE_WIDTH, top);
            remove.render(graphics, mouseX, mouseY, partialTick);
        }

        private void renderIcon(GuiGraphics graphics, int x, int y, int mouseX, int mouseY) {
            ResolvedVisual visual = resolveVisual(row[0]);
            if (!visual.itemStack().isEmpty()) {
                graphics.renderFakeItem(visual.itemStack(), x, y);
            } else if (visual.effect() != null) {
                graphics.blit(x - 1, y - 1, 0, 18, 18, minecraft.getMobEffectTextures().get(visual.effect()));
            } else if (visual.tooltip() != null) {
                graphics.fill(x, y, x + 16, y + 16, 0x40FF0000);
                graphics.drawCenteredString(font, "?", x + 8, y + 4, ConfigTheme.ERROR);
            }
            if (visual.tooltip() != null && mouseX >= x && mouseX < x + 16 && mouseY >= y && mouseY < y + 16) {
                setTooltipForNextRenderPass(visual.tooltip());
            }
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return children;
        }

        @Override
        public List<? extends NarratableEntry> narratables() {
            List<NarratableEntry> out = new ArrayList<>(fields);
            out.add(remove);
            return out;
        }
    }

    /** First-column box with registry Tab-completion shown as ghost text. */
    private final class IdField extends ValidatedField {
        private String completion;

        IdField(Font font, int width, String initial, Predicate<String> store) {
            super(font, width, 18, Component.translatable(LANG + "column." + schema.columns().get(0).label()),
                    initial, store, text -> { });
        }

        @Override
        protected void onTextChanged(String text) {
            super.onTextChanged(text);
            String id = text.trim();
            String match = id.contains(" ") ? null : complete(id);
            completion = match;
            if (match == null) {
                setSuggestion(null);
            } else if (match.startsWith(id.toLowerCase(Locale.ROOT))) {
                setSuggestion(match.substring(id.length()));
            } else {
                setSuggestion("  ⇥ " + match);
            }
        }

        @Override
        public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
            if (keyCode == GLFW.GLFW_KEY_TAB && completion != null && isFocused()) {
                setValue(completion);
                moveCursorToEnd();
                return true;
            }
            return super.keyPressed(keyCode, scanCode, modifiers);
        }

        @Override
        public void setFocused(boolean focused) {
            super.setFocused(focused);
            if (!focused) {
                setSuggestion(null);
            }
        }
    }
}

