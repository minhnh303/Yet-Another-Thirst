package dev.minhnh.yetanotherthirst.client.config;

import dev.minhnh.yetanotherthirst.Lang;
import dev.minhnh.yetanotherthirst.client.config.ConfigWidgets.ValidatedField;
import dev.minhnh.yetanotherthirst.client.config.ListSchema.Column;
import dev.minhnh.yetanotherthirst.client.config.ListSchema.Kind;
import dev.minhnh.yetanotherthirst.core.thirst.ThirstConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Editor for list options: item values, item/effect id lists and temperature tiers. Each entry is a row of
 * edit boxes validated as typed, with an item or effect icon for id columns and Tab-completion from the
 * registries. Rows hold the text shown in each box, valid or not; the result is handed back only on Done, and
 * only when every non-blank row is valid. Leaving with edits asks first.
 */
final class ListEditorScreen extends Screen {

    private static final String LANG = Lang.CONFIG_PREFIX;
    private static final int HEADER_HEIGHT = 50;
    private static final int FOOTER_HEIGHT = 36;
    private static final int ICON_WIDTH = 20;
    private static final int REMOVE_WIDTH = 20;

    private final Screen parent;
    private final ConfigOption<?> option;
    private final ListSchema<?> schema;
    private final Object defaultValue;
    private static final int ICON_CACHE_LIMIT = 256;

    private final Consumer<List<Object>> onDone;
    private final List<RowData> rows = new ArrayList<>();
    /** Widgets per row, created when a row is first shown and kept across filter changes. */
    private final Map<RowData, EditorRow> rowWidgets = new HashMap<>();
    private final Map<String, ResolvedIcon> iconCache = new HashMap<>();
    private final Map<Kind, Completions> completionCache = new EnumMap<>(Kind.class);
    private final List<?> initial;

    private EditorList editor;
    private EditBox filter;
    private Button done;
    private String filterText = "";
    private int invalidRows;
    private int shownInvalid = -1;
    private Runnable pendingFocus;

    /**
     * What the id column resolves to: one item, a tag (kept as its holder set, so a large tag costs no stacks
     * until a member is shown), an effect, or nothing.
     */
    private record ResolvedIcon(ItemStack item, HolderSet<Item> tag, Holder.Reference<MobEffect> effect, Component staticName) {
        static final ResolvedIcon EMPTY = new ResolvedIcon(null, null, null, null);
    }

    /**
     * Registry ids sorted for prefix lookup, plus the same ids sorted by their path (the part after the
     * namespace) so typing {@code apple} finds {@code minecraft:apple} without a full scan.
     */
    private record Completions(List<String> ids, List<String> paths, List<String> idsByPath) {
        static Completions of(Stream<String> source) {
            List<String> ids = source.sorted().toList();
            List<String> byPath = ids.stream()
                    .sorted(Comparator.comparing(ListEditorScreen::pathOf).thenComparing(Comparator.naturalOrder()))
                    .toList();
            return new Completions(ids, byPath.stream().map(ListEditorScreen::pathOf).toList(), byPath);
        }
    }

    private static String pathOf(String id) {
        return id.substring(id.indexOf(':') + 1);
    }

    /** Text of one entry's cells and their validity, updated per keystroke so no check rescans the list. */
    private final class RowData {
        final String[] cells;
        private final boolean[] valid;

        RowData(String[] cells) {
            this.cells = cells;
            this.valid = new boolean[cells.length];
            for (int i = 0; i < cells.length; i++) valid[i] = ListSchema.valid(schema.columns().get(i), cells[i]);
        }

        /** A row with an empty id is ignored on Done rather than reported as invalid. */
        boolean blank() {
            return !schema.columns().get(0).numeric() && cells[0].trim().isEmpty();
        }

        boolean invalid() {
            return firstInvalidCell() >= 0;
        }

        int firstInvalidCell() {
            if (blank()) return -1;
            for (int i = 0; i < valid.length; i++) {
                if (!valid[i]) return i;
            }
            return -1;
        }

        void setCell(int index, String text) {
            boolean before = invalid();
            cells[index] = text;
            valid[index] = ListSchema.valid(schema.columns().get(index), text);
            boolean after = invalid();
            if (before != after) invalidRows += after ? 1 : -1;
        }

        boolean matches(String query) {
            return query.isEmpty() || String.join(" ", cells).toLowerCase(Locale.ROOT).contains(query);
        }
    }

    ListEditorScreen(Screen parent, ConfigOption<?> option, Object current, Object defaultValue, Consumer<List<Object>> onDone) {
        super(Component.translatable(LANG + "list.title", option.title()));
        this.parent = parent;
        this.option = option;
        this.schema = option.schema();
        this.defaultValue = defaultValue;
        this.onDone = onDone;
        this.initial = schema.canonical(current);
        load(current);
    }

    private void load(Object value) {
        rows.clear();
        rowWidgets.clear();
        for (Object entry : schema.canonical(value)) {
            List<?> cells = schema.flat() ? List.of(entry) : (List<?>) entry;
            String[] row = new String[schema.columns().size()];
            for (int i = 0; i < row.length; i++) row[i] = ListSchema.text(schema.columns().get(i), cells.get(i));
            rows.add(new RowData(row));
        }
        invalidRows = (int) rows.stream().filter(RowData::invalid).count();
    }

    @Override
    protected void init() {
        double previousScroll = editor == null ? 0 : editor.scrollAmount();
        shownInvalid = -1;
        pendingFocus = null;
        editor = new EditorList(minecraft, width, height - HEADER_HEIGHT - FOOTER_HEIGHT, HEADER_HEIGHT);
        // Row widgets are sized for the old list width.
        rowWidgets.clear();
        addRenderableWidget(editor);

        int filterWidth = Math.min(160, width / 3);
        filter = new EditBox(font, width - filterWidth - 10, 8, filterWidth, 18, Component.translatable(LANG + "list.filter"));
        filter.setHint(Component.translatable(LANG + "list.filter").withStyle(ChatFormatting.DARK_GRAY));
        filter.setValue(filterText);
        filter.setResponder(text -> {
            filterText = text.trim().toLowerCase(Locale.ROOT);
            editor.rebuild();
        });
        addRenderableWidget(filter);

        int y = height - FOOTER_HEIGHT + (FOOTER_HEIGHT - 20) / 2;
        addRenderableWidget(Button.builder(Component.translatable(LANG + "list.add"), b -> addRow())
                .bounds(10, y, 80, 20).build());
        addRenderableWidget(Button.builder(Component.translatable(LANG + "list.reset"), b -> {
                    load(defaultValue);
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
        editor.setScrollAmount(previousScroll);
    }

    private void addRow() {
        String[] row = new String[schema.columns().size()];
        List<Object> blank = schema.blankRow();
        for (int i = 0; i < row.length; i++) row[i] = ListSchema.text(schema.columns().get(i), blank.get(i));
        RowData data = new RowData(row);
        rows.add(0, data);
        if (data.invalid()) invalidRows++;
        if (!filterText.isEmpty()) {
            filter.setValue("");
        } else {
            editor.rebuild();
        }
        editor.setScrollAmount(0);
        if (!editor.children().isEmpty()) {
            focus(editor.children().get(0), 0);
        }
    }

    /**
     * Focuses a cell once the current input event is handled. Called from button presses, and a click handler
     * focuses the clicked button after {@code onPress} returns, which would take the focus straight back.
     */
    private void focus(EditorRow row, int cell) {
        pendingFocus = () -> {
            setFocused(editor);
            editor.setFocused(row);
            row.focusField(cell);
        };
    }

    /** Applies a focus request made while handling the event, and drops focus left on a row that was removed. */
    private void afterInput() {
        if (editor.getFocused() != null && !editor.children().contains(editor.getFocused())) {
            editor.setFocused(null);
        }
        if (pendingFocus != null) {
            Runnable focus = pendingFocus;
            pendingFocus = null;
            focus.run();
        }
    }

    /** Focus leaving the list also leaves the focused row's field, so it stops showing a cursor. */
    @Override
    public void setFocused(GuiEventListener listener) {
        if (editor != null && listener != editor && getFocused() == editor) {
            editor.setFocused(null);
        }
        super.setFocused(listener);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        boolean handled = super.mouseClicked(event, doubleClick);
        afterInput();
        return handled;
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        boolean handled = super.keyPressed(event);
        afterInput();
        return handled;
    }

    /** Non-blank rows converted to backend cells. */
    private List<Object> result() {
        List<Object> result = new ArrayList<>();
        for (RowData data : rows) {
            if (data.blank()) continue;
            String[] row = data.cells;
            if (schema.flat()) {
                result.add(row[0].trim());
            } else {
                List<Object> cells = new ArrayList<>();
                for (int i = 0; i < row.length; i++) cells.add(ListSchema.cell(schema.columns().get(i), row[i].trim()));
                result.add(cells);
            }
        }
        return result;
    }

    private void finish() {
        if (invalidRows > 0) {
            revealFirstInvalid();
            return;
        }
        onDone.accept(result());
        minecraft.gui.setScreen(parent);
    }

    /** Scrolls to and focuses the first invalid cell, clearing the filter when it hides that row. */
    private void revealFirstInvalid() {
        RowData target = rows.stream().filter(RowData::invalid).findFirst().orElse(null);
        if (target == null) return;
        if (!target.matches(filterText)) filter.setValue("");
        for (EditorRow row : editor.children()) {
            if (row.data == target) {
                editor.reveal(row);
                focus(row, target.firstInvalidCell());
                return;
            }
        }
    }

    private boolean dirty() {
        return invalidRows > 0 || !schema.canonical(result()).equals(initial);
    }

    @Override
    public void onClose() {
        if (!dirty()) {
            minecraft.gui.setScreen(parent);
            return;
        }
        minecraft.gui.setScreen(new ConfirmScreen(
                discard -> minecraft.gui.setScreen(discard ? parent : this),
                Component.translatable(LANG + "list.discard.title"),
                Component.translatable(LANG + "list.discard.message"),
                Component.translatable(LANG + "unsaved.discard"),
                Component.translatable(LANG + "unsaved.back")));
    }

    @Override
    public void tick() {
        super.tick();
        if (invalidRows != shownInvalid) {
            shownInvalid = invalidRows;
            done.setTooltip(invalidRows > 0
                    ? Tooltip.create(Component.translatable(LANG + "list.done.invalid", invalidRows))
                    : null);
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        graphics.text(font, title.copy().withStyle(ChatFormatting.BOLD), 10, 8, ConfigTheme.TEXT);
        ConfigTheme.clipped(graphics, font, option.description(), 10, 20, filter.getX() - 20, ConfigTheme.MUTED);

        int left = editor.getRowLeft();
        int x = left + (schema.columns().get(0).numeric() ? 0 : ICON_WIDTH);
        int[] widths = editor.columnWidths();
        for (int i = 0; i < widths.length; i++) {
            graphics.text(font, Component.translatable(LANG + "column." + schema.columns().get(i).label()),
                    x + 2, HEADER_HEIGHT - 11, ConfigTheme.ACCENT);
            x += widths[i] + 4;
        }

        int invalid = invalidRows;
        Component status = Component.translatable(LANG + "list.status", rows.size(), invalid);
        int statusX = 182;
        int statusRight = width - 18 - done.getWidth() * 2;
        if (statusRight - statusX > 40) {
            ConfigTheme.clipped(graphics, font, status, statusX, height - FOOTER_HEIGHT + 14, statusRight - statusX,
                    invalid > 0 ? ConfigTheme.ERROR : ConfigTheme.DIM);
        }
    }

    /** Registry ids offered for Tab-completion in the first column, indexed once per column kind. */
    private Completions completions() {
        Kind kind = schema.columns().get(0).kind();
        return completionCache.computeIfAbsent(kind, k -> Completions.of(switch (k) {
            case ITEM -> Stream.concat(
                    BuiltInRegistries.ITEM.keySet().stream().map(Identifier::toString),
                    BuiltInRegistries.ITEM.getTags().map(tag -> "#" + tag.key().location()));
            case EFFECT -> BuiltInRegistries.MOB_EFFECT.keySet().stream().map(Identifier::toString);
            default -> Stream.empty();
        }));
    }

    /**
     * First completion for {@code typed}: a full-id prefix match, else (for a bare name) a match on the path
     * after the namespace. Null when nothing matches or the text already is a complete id.
     */
    private String complete(String typed) {
        String text = typed.trim().toLowerCase(Locale.ROOT);
        if (text.isEmpty()) return null;
        Completions index = completions();
        int found = Collections.binarySearch(index.ids(), text);
        if (found >= 0) return null;
        int at = -found - 1;
        if (at < index.ids().size() && index.ids().get(at).startsWith(text)) return index.ids().get(at);
        if (text.startsWith("#") || text.indexOf(':') >= 0) return null;
        int pathFound = Collections.binarySearch(index.paths(), text);
        int pathAt = pathFound >= 0 ? pathFound : -pathFound - 1;
        if (pathAt < index.paths().size() && index.paths().get(pathAt).startsWith(text)) return index.idsByPath().get(pathAt);
        return null;
    }

    private ResolvedIcon resolveIcon(String id) {
        if (id.isEmpty()) {
            return ResolvedIcon.EMPTY;
        }
        if (schema.columns().get(0).kind() == Kind.ITEM) {
            if (id.startsWith("#")) {
                Identifier location = Identifier.tryParse(id.substring(1));
                if (location == null) return ResolvedIcon.EMPTY;
                Optional<HolderSet.Named<Item>> tag = BuiltInRegistries.ITEM.get(TagKey.create(Registries.ITEM, location));
                if (tag.isEmpty() || tag.get().size() == 0) {
                    // Item tags are only bound once a world is joined; from the main menu a tag cannot be checked.
                    if (minecraft.level == null) {
                        return new ResolvedIcon(ConfigTheme.displayStack(Items.NAME_TAG), null, null,
                                Component.translatable(LANG + "list.tag_unchecked", id).withStyle(ChatFormatting.GRAY));
                    }
                    return ResolvedIcon.EMPTY;
                }
                return new ResolvedIcon(null, tag.get(), null, Component.translatable(LANG + "list.tag", id));
            }
            Identifier location = Identifier.tryParse(id);
            if (location == null) return ResolvedIcon.EMPTY;
            Item item = BuiltInRegistries.ITEM.getOptional(location).orElse(Items.AIR);
            if (item == Items.AIR) return ResolvedIcon.EMPTY;
            ItemStack stack = ConfigTheme.displayStack(item);
            return new ResolvedIcon(stack, null, null, stack.getHoverName());
        }
        Optional<Holder.Reference<MobEffect>> effect = effectIcon(id);
        if (effect.isPresent()) {
            return new ResolvedIcon(null, null, effect.get(), effect.get().value().getDisplayName());
        }
        return ResolvedIcon.EMPTY;
    }

    private final class EditorList extends ContainerObjectSelectionList<EditorRow> {

        EditorList(Minecraft minecraft, int width, int height, int y) {
            super(minecraft, width, height, y, 24);
        }

        @Override
        public int getRowWidth() {
            return Math.min(width - 40, 440);
        }

        /** Widths of the edit boxes; numeric columns are narrow and the id column takes the rest. */
        int[] columnWidths() {
            List<Column> columns = schema.columns();
            int[] widths = new int[columns.size()];
            boolean hasId = !columns.get(0).numeric();
            // Entries lay out inside getContentWidth(), which is the row width minus 4.
            int available = getRowWidth() - 4 - REMOVE_WIDTH - 4 -(hasId ? ICON_WIDTH : 0) - 4 * (columns.size() - 1);
            int numeric = hasId ? 46 : available / columns.size();
            int idWidth = available;
            for (int i = 0; i < columns.size(); i++) {
                if (columns.get(i).numeric()) {
                    widths[i] = numeric;
                    idWidth -= numeric;
                }
            }
            for (int i = 0; i < columns.size(); i++) {
                if (!columns.get(i).numeric()) widths[i] = idWidth;
            }
            return widths;
        }

        /** Shows the rows matching the filter, reusing each row's widgets so filtering allocates nothing per row. */
        void rebuild() {
            double scroll = scrollAmount();
            EditorRow focused = getFocused();
            if (focused != null) focused.setFocused((GuiEventListener) null);
            setFocused(null);
            clearEntries();
            for (RowData row : rows) {
                if (row.matches(filterText)) addEntry(rowWidgets.computeIfAbsent(row, EditorRow::new));
            }
            setScrollAmount(scroll);
        }

        /** Moving between rows blurs the previous row's field, which vanilla leaves focused. */
        @Override
        public void setFocused(GuiEventListener listener) {
            EditorRow previous = getFocused();
            if (previous != null && previous != listener) previous.setFocused((GuiEventListener) null);
            super.setFocused(listener);
        }

        void remove(RowData row) {
            if (rows.remove(row) && row.invalid()) invalidRows--;
            rowWidgets.remove(row);
            rebuild();
        }

        void reveal(EditorRow row) {
            centerScrollOn(row);
        }
    }

    private final class EditorRow extends ContainerObjectSelectionList.Entry<EditorRow> {
        private final RowData data;
        private final String[] row;
        private final List<EditBox> fields = new ArrayList<>();
        private final List<GuiEventListener> children = new ArrayList<>();
        private final Button remove;
        private HolderSet<Item> shownTag;
        private int shownTagIndex = -1;
        private ItemStack shownTagStack = ItemStack.EMPTY;

        /** Skips vanilla's blur-then-refocus of an already focused child so in-box clicks keep its state. */
        @Override
        public void setFocused(GuiEventListener listener) {
            if (listener != null && listener == getFocused()) return;
            super.setFocused(listener);
        }

        EditorRow(RowData data) {
            this.data = data;
            this.row = data.cells;
            int[] widths = editor.columnWidths();
            for (int i = 0; i < row.length; i++) {
                Column column = schema.columns().get(i);
                int index = i;
                ValidatedField field = i == 0 && !column.numeric()
                        ? new IdField(font, widths[i], data, index)
                        : new CellField(font, widths[i], Component.translatable(LANG + "column." + column.label()),
                        data, index, text -> ListSchema.valid(column, text));
                if (column.numeric()) {
                    field.setFilter(text -> text.chars().allMatch(c -> Character.isDigit(c) || c == '-' || c == '.'));
                }
                fields.add(field);
            }
            remove = Button.builder(Component.literal("✕").withStyle(ChatFormatting.RED), b -> editor.remove(data))
                    .tooltip(Tooltip.create(Component.translatable(LANG + "list.remove")))
                    .size(REMOVE_WIDTH, 20).build();
            children.addAll(fields);
            children.add(remove);
        }

        void focusField(int index) {
            EditBox field = fields.get(Mth.clamp(index, 0, fields.size() - 1));
            setFocused(field);
            field.setFocused(true);
        }

        @Override
        public void extractContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean hovering, float partialTick) {
            int left = getContentX();
            int top = getContentY();
            int width = getContentWidth();
            int height = getContentHeight();
            if (hovering) graphics.fill(left - 4, top - 2, left + width + 2, top + height + 2, ConfigTheme.ROW_HOVER);
            int x = left;
            if (!schema.columns().get(0).numeric()) {
                renderIcon(graphics, x + 1, top + 2, mouseX, mouseY);
                x += ICON_WIDTH;
            }
            for (EditBox field : fields) {
                field.setPosition(x + 1, top + 1);
                field.extractRenderState(graphics, mouseX, mouseY, partialTick);
                x += field.getWidth() + 4;
            }
            remove.setPosition(left + width - REMOVE_WIDTH, top);
            remove.extractRenderState(graphics, mouseX, mouseY, partialTick);
        }

        /** The tag member shown this second; only that one stack exists, rebuilt when the shown member changes. */
        private ItemStack tagStack(HolderSet<Item> tag) {
            int index = (int) (Util.getMillis() / 1000 % tag.size());
            if (tag != shownTag || index != shownTagIndex) {
                shownTag = tag;
                shownTagIndex = index;
                shownTagStack = ConfigTheme.displayStack(tag.get(index).value());
            }
            return shownTagStack;
        }

        private void renderIcon(GuiGraphicsExtractor graphics, int x, int y, int mouseX, int mouseY) {
            String id = row[0].trim();
            // Keyed by typed text, so partial ids accumulate while typing; dropping them is cheaper than tracking use.
            if (iconCache.size() > ICON_CACHE_LIMIT && !iconCache.containsKey(id)) iconCache.clear();
            ResolvedIcon resolved = iconCache.computeIfAbsent(id, ListEditorScreen.this::resolveIcon);
            Component name = null;
            if (resolved.item() != null) {
                graphics.fakeItem(resolved.item(), x, y);
                name = resolved.staticName();
            } else if (resolved.tag() != null) {
                ItemStack stack = tagStack(resolved.tag());
                graphics.fakeItem(stack, x, y);
                name = resolved.staticName().copy().append(" · ").append(stack.getHoverName());
            } else if (resolved.effect() != null) {
                graphics.blitSprite(RenderPipelines.GUI_TEXTURED, Hud.getMobEffectSprite(resolved.effect()), x - 1, y - 1, 18, 18);
                name = resolved.staticName();
            } else if (!id.isEmpty()) {
                graphics.fill(x, y, x + 16, y + 16, 0x40FF0000);
                graphics.centeredText(font, "?", x + 8, y + 4, ConfigTheme.ERROR);
                name = Component.translatable(LANG + "list.unknown").withStyle(ChatFormatting.GOLD);
            }
            if (name != null && mouseX >= x && mouseX < x + 16 && mouseY >= y && mouseY < y + 16) {
                graphics.setTooltipForNextFrame(font, name, mouseX, mouseY);
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

    private static Optional<Holder.Reference<MobEffect>> effectIcon(String rule) {
        ThirstConfig.EffectCondition condition = ThirstConfig.EffectCondition.parse(rule);
        if (condition == null) return Optional.empty();
        return BuiltInRegistries.MOB_EFFECT.get(ResourceKey.create(Registries.MOB_EFFECT, condition.effectId));
    }

    /** Edit box bound to one row cell: every keystroke is stored, so invalid text stays visible and blocks Done. */
    private class CellField extends ValidatedField {
        private final RowData row;
        private final int index;

        CellField(Font font, int width, Component name, RowData row, int index, Predicate<String> validator) {
            super(font, width, 18, name, row.cells[index], validator, text -> {
            });
            this.row = row;
            this.index = index;
            keepInvalidOnBlur();
        }

        @Override
        protected void onTextChanged(String text) {
            row.setCell(index, text);
            super.onTextChanged(text);
        }
    }

    /** First-column box with registry Tab-completion shown as ghost text. */
    private final class IdField extends CellField {
        private String completion;

        IdField(Font font, int width, RowData row, int index) {
            super(font, width, Component.translatable(LANG + "column." + schema.columns().get(0).label()),
                    row, index, text -> ListSchema.valid(schema.columns().get(0), text));
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
        public boolean keyPressed(KeyEvent event) {
            if (event.key() == GLFW.GLFW_KEY_TAB && completion != null && isFocused()) {
                setValue(completion);
                moveCursorToEnd(false);
                return true;
            }
            return super.keyPressed(event);
        }

        @Override
        public void setFocused(boolean focused) {
            super.setFocused(focused);
            if (!focused) setSuggestion(null);
        }
    }
}

