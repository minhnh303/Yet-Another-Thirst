package dev.minhnh.yetanotherthirst.client.config;

import dev.minhnh.yetanotherthirst.Constants;
import dev.minhnh.yetanotherthirst.Lang;
import dev.minhnh.yetanotherthirst.client.config.ConfigWidgets.ResetButton;
import dev.minhnh.yetanotherthirst.platform.Services;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;

import java.util.ArrayList;
import java.util.List;

/** Scrolling list of a config page (or of search results): intro, section headings and option rows. */
final class OptionList extends ContainerObjectSelectionList<OptionList.Row> {

    static final int ROW_HEIGHT = 24;
    private static final int PREVIEW_ROWS = 4;
    private static final String LANG = Lang.CONFIG_PREFIX;

    private final YatConfigScreen screen;
    private final ConfigSession session;
    private final Font font;

    OptionList(YatConfigScreen screen, Minecraft minecraft, ConfigSession session, int width, int height, int y) {
        super(minecraft, width, height, y, ROW_HEIGHT);
        this.screen = screen;
        this.session = session;
        this.font = minecraft.font;
    }

    @Override
    public int getRowWidth() {
        return Math.min(width - 28, 380);
    }

    private int controlWidth() {
        return Mth.clamp(getRowWidth() * 2 / 5, 96, 150);
    }

    /**
     * Options this screen can show truthfully. On a remote server, server-side values live on the server and the
     * local file may differ, so they are left out rather than shown as if they were in effect.
     */
    private boolean shown(ConfigOption<?> option) {
        return session.available(option) && !session.locked(option);
    }

    void showCategory(ConfigCategory category) {
        clearEntries();
        addEntry(new IntroRow(category.title(), category.description()));
        boolean any = false;
        long hidden = category.options().stream().filter(session::available).filter(session::locked).count();
        for (ConfigCategory.Section section : category.sections()) {
            List<ConfigOption<?>> options = section.options().stream().filter(this::shown).toList();
            if (options.isEmpty()) continue;
            any = true;
            addEntry(new SectionRow(section.title(), section.requiredMod()));
            for (ConfigOption<?> option : options) addEntry(new OptionRow(option));
            if (section.hudPreview()) {
                for (int i = 0; i < PREVIEW_ROWS; i++) addEntry(new HudPreviewRow(options, i));
            }
        }
        if (hidden > 0) addEntry(new MessageRow(Component.translatable(LANG + "server_hidden", hidden)));
        else if (!any) addEntry(new MessageRow(Component.translatable(LANG + "empty_page")));
    }

    void showSearch(String query) {
        clearEntries();
        int count = 0;
        List<Row> results = new ArrayList<>();
        for (ConfigCategory category : ConfigOptions.CATEGORIES) {
            List<ConfigOption<?>> matches = category.options().stream()
                    .filter(this::shown).filter(o -> o.matches(query)).toList();
            if (matches.isEmpty()) continue;
            results.add(new SectionRow(category.title(), null));
            for (ConfigOption<?> option : matches) results.add(new OptionRow(option));
            count += matches.size();
        }
        addEntry(new IntroRow(Component.translatable(LANG + "search.results"),
                Component.translatable(LANG + "search.count", count, query)));
        if (count == 0) addEntry(new MessageRow(Component.translatable(LANG + "search.none")));
        results.forEach(this::addEntry);
    }

    /** Rebuilds every option row's controls, e.g. after a page-wide reset. */
    void refreshControls() {
        clearFocus();
        for (Row row : children()) {
            if (row instanceof OptionRow optionRow) optionRow.build();
        }
    }

    /**
     * Drops keyboard focus from the focused row and its control. Vanilla's {@code setFocused(false)} on a list
     * and its entries does nothing, so without this a row's edit box keeps its cursor (and skips its blur
     * handling) when focus moves to a widget outside the list.
     */
    void clearFocus() {
        setFocused(null);
    }

    /** Moving between rows (or out of the list) blurs the previous row's control, which vanilla leaves focused. */
    @Override
    public void setFocused(GuiEventListener listener) {
        Row previous = getFocused();
        if (previous != null && previous != listener) previous.setFocused((GuiEventListener) null);
        super.setFocused(listener);
    }

    /** True while any shown edit box holds text that is not a valid value. */
    boolean hasInvalidField() {
        for (Row row : children()) {
            if (row instanceof OptionRow optionRow) {
                for (AbstractWidget control : optionRow.controls) {
                    if (control instanceof ConfigWidgets.ValidatedField field && !field.valid()) return true;
                }
            }
        }
        return false;
    }

    /** Refreshes only the row bound to {@code option}, avoiding a full list rebuild. */
    void refreshOption(ConfigOption<?> option) {
        for (Row row : children()) {
            if (row instanceof OptionRow optionRow && optionRow.option == option) {
                optionRow.build();
                break;
            }
        }
    }

    private void tooltip(GuiGraphicsExtractor graphics, List<Component> lines, int mouseX, int mouseY) {
        List<FormattedCharSequence> out = new ArrayList<>();
        for (Component line : lines) {
            if (line.getString().isEmpty()) out.add(FormattedCharSequence.EMPTY);
            else out.addAll(font.split(line, 260));
        }
        graphics.setTooltipForNextFrame(font, out, mouseX, mouseY);
    }

    /** Base row; also used as an empty spacer under multi-row content. */
    static class Row extends ContainerObjectSelectionList.Entry<Row> {
        /**
         * Vanilla refocuses the clicked child on every click, blurring it first even when it already has focus.
         * Skipping the same-child case keeps edit boxes from treating an in-box click as leaving the field.
         */
        @Override
        public void setFocused(GuiEventListener listener) {
            if (listener != null && listener == getFocused()) return;
            super.setFocused(listener);
        }

        @Override
        public void extractContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean hovering, float partialTick) {
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return List.of();
        }

        @Override
        public List<? extends NarratableEntry> narratables() {
            return List.of();
        }
    }

    /** Page title and one-line description. */
    final class IntroRow extends Row {
        private final Component title;
        private final Component description;

        IntroRow(Component title, Component description) {
            this.title = title.copy().withStyle(ChatFormatting.BOLD);
            this.description = description;
        }

        @Override
        public void extractContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean hovering, float partialTick) {
            int left = getContentX();
            int top = getContentY();
            int width = getContentWidth();
            graphics.text(font, title, left, top + 1, ConfigTheme.TEXT);
            boolean cut = ConfigTheme.clipped(graphics, font, description, left, top + 12, width, ConfigTheme.MUTED);
            if (cut && hovering) tooltip(graphics, List.of(description), mouseX, mouseY);
        }
    }

    /** Section heading with a rule, and an installed/missing badge for compatibility sections. */
    final class SectionRow extends Row {
        private final Component title;
        private final String modId;
        private final boolean installed;

        SectionRow(Component title, String modId) {
            this.title = title;
            this.modId = modId;
            this.installed = modId == null || Services.PLATFORM.isModLoaded(modId);
        }

        @Override
        public void extractContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean hovering, float partialTick) {
            int left = getContentX();
            int top = getContentY();
            int width = getContentWidth();
            int height = getContentHeight();
            int y = top + height - 10;
            graphics.text(font, title, left, y, ConfigTheme.ACCENT);
            int right = left + width;
            if (modId != null) {
                Component badge = Component.translatable(LANG + (installed ? "mod.installed" : "mod.missing"));
                int badgeWidth = font.width(badge) + 8;
                int badgeX = right - badgeWidth;
                int color = installed ? ConfigTheme.SUCCESS : ConfigTheme.DIM;
                ConfigTheme.outline(graphics, badgeX, y - 3, badgeWidth, 14, (color & 0x00FFFFFF) | 0x90000000);
                graphics.text(font, badge, badgeX + 4, y, color);
                right = badgeX - 6;
            }
            int lineX = left + font.width(title) + 6;
            if (lineX < right) graphics.fill(lineX, y + 4, right, y + 5, ConfigTheme.LINE);
        }
    }

    final class MessageRow extends Row {
        private final Component message;

        MessageRow(Component message) {
            this.message = message;
        }

        @Override
        public void extractContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean hovering, float partialTick) {
            int left = getContentX();
            int top = getContentY();
            int width = getContentWidth();
            int height = getContentHeight();
            graphics.centeredText(font, message, left + width / 2, top + (height - 8) / 2, ConfigTheme.MUTED);
        }
    }

    /** One setting: label (+ hint line), its control and a reset-to-default button. */
    final class OptionRow extends Row {
        private final ConfigOption<?> option;
        private final List<AbstractWidget> controls = new ArrayList<>();
        private final List<AbstractWidget> children = new ArrayList<>();
        private final boolean modMissing;
        /** Kept across {@link #build()} so focus stays on a live widget after it rebuilds the row from its own click. */
        private final Button reset;
        private Component cachedHint;
        private List<Component> cachedTooltip;

        OptionRow(ConfigOption<?> option) {
            this.option = option;
            this.modMissing = option.requiredMod() != null && !Services.PLATFORM.isModLoaded(option.requiredMod());
            this.reset = new ResetButton(null, () -> {
                // Blur first: a focused edit box reports its own value on blur and must not undo the reset.
                setFocused(null);
                session.reset(option);
                build();
            });
            build();
        }

        private boolean editable() {
            return !session.locked(option);
        }

        void build() {
            controls.clear();
            ConfigControlFactory.Context context = new ConfigControlFactory.Context(
                    font, screen, session, option, controlWidth(), this::change);
            controls.addAll(ConfigControlFactory.forType(option.type).create(context));
            for (AbstractWidget control : controls) {
                control.active = editable();
                if (control instanceof EditBox box) box.setEditable(editable());
            }
            reset.setTooltip(Tooltip.create(Component.translatable(LANG + "reset.tooltip",
                    option.formatValue(session.defaultValue(option)))));
            if (getFocused() != reset) setFocused(null);
            children.clear();
            children.addAll(controls);
            children.add(reset);
            cachedHint = computeHint();
            cachedTooltip = option.tooltip(session.defaultValue(option), session.locked(option), modMissing);
        }

        private void change(Object value) {
            session.set(option, value);
            cachedHint = computeHint();
        }

        private Component computeHint() {
            if (session.locked(option)) return Component.translatable(LANG + "hint.locked");
            if (modMissing) return Component.translatable(LANG + "hint.mod_missing");
            Object value = session.value(option);
            if (option.type == ConfigOption.Type.LIST) {
                List<?> list = (List<?>) value;
                if (list.isEmpty()) return Component.translatable(LANG + "list.empty");
                List<String> heads = new ArrayList<>();
                for (Object entry : list) {
                    Object head = entry instanceof List<?> row ? row.get(0) : entry;
                    heads.add(ListSchema.text(option.schema().columns().get(0), head));
                    if (heads.size() == 3) break;
                }
                return Component.literal(String.join(", ", heads) + (list.size() > 3 ? ", …" : ""));
            }
            if ((option.type == ConfigOption.Type.INT || option.type == ConfigOption.Type.DECIMAL)
                    && option.control() != ConfigOption.Control.SLIDER && option.format != ConfigOption.Format.PLAIN) {
                return option.formatValue(value);
            }
            return null;
        }

        @Override
        public void extractContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean hovering, float partialTick) {
            int left = getContentX();
            int top = getContentY();
            int width = getContentWidth();
            int height = getContentHeight();
            if (hovering) graphics.fill(left - 4, top - 2, left + width + 2, top + height + 2, ConfigTheme.ROW_HOVER);

            int resetX = left + width - 20;
            int controlX = resetX - 4 - controlWidth();
            int labelWidth = controlX - left - 6;
            int labelColor = (!editable() || modMissing) ? ConfigTheme.MUTED : ConfigTheme.TEXT;

            int labelY = cachedHint == null ? top + (height - 8) / 2 : top + 1;
            ConfigTheme.clipped(graphics, font, option.title(), left, labelY, labelWidth, labelColor);
            if (cachedHint != null) {
                ConfigTheme.clipped(graphics, font, cachedHint, left, top + 11, labelWidth, ConfigTheme.DIM);
            }

            layoutControls(controlX, top, height);
            if (option.type == ConfigOption.Type.COLOR) {
                int color = (Integer) session.value(option);
                graphics.fill(controlX, top + 2, controlX + 16, top + 18, 0xFF000000);
                graphics.fill(controlX + 1, top + 3, controlX + 15, top + 17, 0xFF000000 | color);
            }
            for (AbstractWidget control : controls) control.extractRenderState(graphics, mouseX, mouseY, partialTick);

            reset.active = editable() && !session.isDefault(option);
            reset.setPosition(resetX, top + (height - reset.getHeight()) / 2);
            reset.extractRenderState(graphics, mouseX, mouseY, partialTick);

            if (mouseX >= left && mouseX < controlX - 2 && mouseY >= top && mouseY < top + height
                    && OptionList.this.isMouseOver(mouseX, mouseY)) {
                tooltip(graphics, cachedTooltip, mouseX, mouseY);
            }
        }

        private void layoutControls(int x, int top, int height) {
            int cursor = option.type == ConfigOption.Type.COLOR ? x + 18 : x;
            for (AbstractWidget control : controls) {
                int y = top + (height - control.getHeight()) / 2;
                control.setPosition(cursor + (control instanceof EditBox ? 1 : 0), y);
                cursor += control.getWidth() + 2;
            }
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return children;
        }

        @Override
        public List<? extends NarratableEntry> narratables() {
            return children;
        }
    }

    /**
     * One of {@link #PREVIEW_ROWS} slices of the live HUD/AppleSkin preview backed by pending session values.
     * The list only renders rows inside its viewport, so each slice draws the whole box clipped to its own row:
     * the visible part stays drawn whichever rows are scrolled out.
     */
    final class HudPreviewRow extends Row {
        private final int slice;
        private final ConfigOption<?> xOffset;
        private final ConfigOption<?> yOffset;
        private final ConfigOption<?> appleSkinTooltip;
        private final ConfigOption<?> appleSkinHudPreview;

        HudPreviewRow(List<ConfigOption<?>> options, int slice) {
            this.slice = slice;
            this.xOffset = options.stream().filter(o -> o.key.equals("hudXOffset")).findFirst().orElse(null);
            this.yOffset = options.stream().filter(o -> o.key.equals("hudYOffset")).findFirst().orElse(null);
            this.appleSkinTooltip = ConfigOptions.byKey("appleSkinThirstTooltip");
            this.appleSkinHudPreview = ConfigOptions.byKey("appleSkinThirstHudPreview");
        }

        private int offset(ConfigOption<?> option) {
            return option == null ? 0 : ((Number) session.value(option)).intValue();
        }

        private boolean enabled(ConfigOption<?> option) {
            return option != null && session.available(option) && Boolean.TRUE.equals(session.value(option));
        }

        @Override
        public void extractContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean hovering, float partialTick) {
            int left = getContentX();
            int top = getContentY();
            int width = getContentWidth();
            int sliceTop = top - 2;
            graphics.enableScissor(left - 1, sliceTop, left + width + 1, sliceTop + ROW_HEIGHT);
            renderBox(graphics, left, top - slice * ROW_HEIGHT, width);
            graphics.disableScissor();
        }

        private void renderBox(GuiGraphicsExtractor graphics, int left, int top, int width) {
            int boxHeight = PREVIEW_ROWS * ROW_HEIGHT - 4;
            int right = left + width;
            int bottom = top + boxHeight;
            graphics.fill(left, top, right, bottom, 0x70000000);
            ConfigTheme.outline(graphics, left, top, width, boxHeight, ConfigTheme.LINE);

            graphics.enableScissor(left + 1, top + 1, right - 1, bottom - 1);
            int tooltipWidth = width / 2;
            int split = left + tooltipWidth;
            graphics.fill(split, top, split + 1, bottom, ConfigTheme.LINE);
            renderAppleSkinPreview(graphics, left, top, tooltipWidth, boxHeight);
            renderHudPreview(graphics, split + 1, top, right - split - 1, boxHeight);
            graphics.disableScissor();
        }

        private void renderAppleSkinPreview(GuiGraphicsExtractor graphics, int left, int top, int width, int height) {
            graphics.text(font, Component.translatable(LANG + "preview.appleskin"), left + 5, top + 5, ConfigTheme.ACCENT);
            if (!enabled(appleSkinTooltip)) {
                graphics.text(font, Component.translatable(LANG + "preview.disabled"), left + 5,
                        top + height / 2, ConfigTheme.DIM);
                return;
            }
            int x = left + 6;
            int thirstY = top + 27;
            for (int i = 0; i < 3; i++) {
                graphics.blit(RenderPipelines.GUI_TEXTURED, Constants.THIRST_ICONS, x + i * 9, thirstY, 18.0F, 0.0F, 9, 9, 36, 25);
            }
            int quenchedY = thirstY + 13;
            for (int i = 0; i < 2; i++) {
                graphics.blit(RenderPipelines.GUI_TEXTURED, Constants.THIRST_ICONS, x + i * 8, quenchedY, 0.0F, 18.0F, 7, 7, 36, 25);
            }
            ConfigTheme.clipped(graphics, font, appleSkinTooltip.title(), left + 5, top + height - 13,
                    width - 10, ConfigTheme.MUTED);
        }

        private void renderHudPreview(GuiGraphicsExtractor graphics, int left, int top, int width, int height) {
            graphics.text(font, Component.translatable(LANG + "preview.hud"), left + 5, top + 5, ConfigTheme.ACCENT);
            int thirstRight = left + width / 2 + 41 + Mth.clamp(offset(xOffset), -24, 24);
            int thirstTop = top + 31 + Mth.clamp(offset(yOffset), -12, 12);
            int exhaustionWidth = (int) (((Util.getMillis() % 4000L) / 4000.0) * 81);

            for (int i = 0; i < 10; i++) {
                int x = thirstRight - i * 8 - 9;
                int visibleLeft = Math.max(x, thirstRight - exhaustionWidth);
                if (visibleLeft < x + 9) {
                    int sourceX = visibleLeft - x;
                    graphics.blit(RenderPipelines.GUI_TEXTURED, Constants.THIRST_ICONS, visibleLeft, thirstTop,
                            (float) (27 + sourceX), 9.0F, x + 9 - visibleLeft, 9, 36, 25, 0xC0FFFFFF);
                }
            }

            int thirst = 12;
            int quenched = 6;
            for (int i = 0; i < 10; i++) {
                int x = thirstRight - i * 8 - 9;
                int level = i * 2 + 1;
                if (level > thirst) {
                    graphics.blit(RenderPipelines.GUI_TEXTURED, Constants.THIRST_ICONS, x, thirstTop, 0.0F, 0.0F, 9, 9, 36, 25);
                }
                if (level < thirst) graphics.blit(RenderPipelines.GUI_TEXTURED, Constants.THIRST_ICONS, x, thirstTop, 18.0F, 0.0F, 9, 9, 36, 25);
                else if (level == thirst) graphics.blit(RenderPipelines.GUI_TEXTURED, Constants.THIRST_ICONS, x, thirstTop, 9.0F, 0.0F, 9, 9, 36, 25);
                if (quenched >= i * 2 + 2) graphics.blit(RenderPipelines.GUI_TEXTURED, Constants.THIRST_ICONS, x, thirstTop, 0.0F, 9.0F, 9, 9, 36, 25);
                else if (quenched > i * 2) graphics.blit(RenderPipelines.GUI_TEXTURED, Constants.THIRST_ICONS, x, thirstTop, 9.0F, 9.0F, 9, 9, 36, 25);
            }

            if (enabled(appleSkinHudPreview)) {
                float wave = 0.5F + 0.5F * (float) Math.sin(Util.getMillis() / 300.0);
                float eased = wave * wave * (3.0F - 2.0F * wave);
                renderHeldItemFade(graphics, thirstRight, thirstTop, 0.18F + 0.62F * eased);
            }

            Component offsets = Component.translatable(LANG + "preview.offset", offset(xOffset), offset(yOffset));
            ConfigTheme.clipped(graphics, font, offsets, left + 5, top + height - 13, width - 10, ConfigTheme.MUTED);
        }

        private void renderHeldItemFade(GuiGraphicsExtractor graphics, int thirstRight, int thirstTop, float alpha) {
            int color = (Mth.clamp((int) (alpha * 255.0F), 0, 255) << 24) | 0x00FFFFFF;
            for (int i = 6; i < 9; i++) {
                int x = thirstRight - i * 8 - 9;
                graphics.blit(RenderPipelines.GUI_TEXTURED, Constants.THIRST_ICONS, x, thirstTop,
                        i == 8 ? 9.0F : 18.0F, 0.0F, 9, 9, 36, 25, color);
            }
            for (int i = 3; i < 5; i++) {
                int x = thirstRight - i * 8 - 9;
                graphics.blit(RenderPipelines.GUI_TEXTURED, Constants.THIRST_ICONS, x, thirstTop,
                        0.0F, 9.0F, 9, 9, 36, 25, color);
            }
        }
    }
}

