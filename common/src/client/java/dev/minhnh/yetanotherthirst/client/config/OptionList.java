package dev.minhnh.yetanotherthirst.client.config;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import dev.minhnh.yetanotherthirst.Constants;
import dev.minhnh.yetanotherthirst.Lang;
import dev.minhnh.yetanotherthirst.platform.Services;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

/** Scrolling list of a config page (or of search results): intro, section headings, option rows, and live HUD preview. */
final class OptionList extends ContainerObjectSelectionList<OptionList.Row> {

    static final int ROW_HEIGHT = 24;
    private static final int PREVIEW_ROWS = 4;
    private static final String LANG = Lang.CONFIG_PREFIX;

    private final YatConfigScreen screen;
    private final ConfigSession session;
    private final Font font;

    OptionList(YatConfigScreen screen, Minecraft minecraft, ConfigSession session,
               int width, int screenHeight, int y0, int y1) {
        super(minecraft, width, screenHeight, y0, y1, ROW_HEIGHT);
        this.screen = screen;
        this.session = session;
        this.font = minecraft.font;
        setRenderBackground(false);
        setRenderTopAndBottom(false);
        setRenderSelection(false);
    }

    @Override
    public int getRowWidth() {
        return Math.min(width - 28, 380);
    }

    @Override
    protected int getScrollbarPosition() {
        return getRowLeft() + getRowWidth() + 6;
    }

    private int controlWidth() {
        return Mth.clamp(getRowWidth() * 2 / 5, 96, 150);
    }

    void showCategory(ConfigCategory category) {
        List<Row> entries = new ArrayList<>();
        entries.add(new IntroRow(category.title(), category.description()));
        boolean any = false;
        for (ConfigCategory.Section section : category.sections()) {
            List<ConfigOption<?>> options = section.options().stream().filter(session::available).toList();
            if (options.isEmpty()) continue;
            any = true;
            entries.add(new SectionRow(section.title(), section.requiredMod()));
            for (ConfigOption<?> option : options) {
                entries.add(new OptionRow(option));
            }
            if (section.hudPreview()) {
                HudPreviewRow preview = new HudPreviewRow();
                entries.add(preview);
                for (int i = 1; i < PREVIEW_ROWS; i++) {
                    entries.add(new PreviewPartRow(preview, i));
                }
            }
        }
        if (!any) {
            entries.add(new MessageRow(Component.translatable(LANG + "empty_page")));
        }
        replaceEntries(entries);
    }

    void showSearch(String query) {
        List<Row> entries = new ArrayList<>();
        int count = 0;
        List<Row> results = new ArrayList<>();
        for (ConfigCategory category : ConfigOptions.CATEGORIES) {
            List<ConfigOption<?>> matches = category.options().stream()
                    .filter(session::available)
                    .filter(o -> o.matches(query))
                    .toList();
            if (matches.isEmpty()) continue;
            results.add(new SectionRow(category.title(), null));
            for (ConfigOption<?> option : matches) {
                results.add(new OptionRow(option));
            }
            count += matches.size();
        }
        entries.add(new IntroRow(
                Component.translatable(LANG + "search.results"),
                Component.translatable(LANG + "search.count", count, query)));
        if (count == 0) {
            entries.add(new MessageRow(Component.translatable(LANG + "search.none")));
        }
        entries.addAll(results);
        replaceEntries(entries);
    }

    static List<ConfigOption<?>> searchOptions(ConfigSession session, String query) {
        List<ConfigOption<?>> matches = new ArrayList<>();
        for (ConfigCategory category : ConfigOptions.CATEGORIES) {
            for (ConfigOption<?> option : category.options()) {
                if (session.available(option) && option.matches(query)) {
                    matches.add(option);
                }
            }
        }
        return matches;
    }

    /** Whether a visible text control holds text that fails validation; saving is blocked while true. */
    boolean hasInvalidField() {
        for (Row row : children()) {
            if (row instanceof OptionRow optionRow && !optionRow.controlValid()) {
                return true;
            }
        }
        return false;
    }

    /** Losing focus to a screen widget also clears the focused row, so its text field blurs and validates. */
    @Override
    public void setFocused(boolean focused) {
        if (!focused) {
            setFocused((GuiEventListener) null);
        }
    }

    /** Drops focus left on a row that a click handler removed or replaced. */
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        boolean handled = super.mouseClicked(mouseX, mouseY, button);
        GuiEventListener focused = getFocused();
        if (focused != null && !children().contains(focused)) {
            setFocused((GuiEventListener) null);
        }
        return handled;
    }

    /** Rebuilds every option row's control, e.g. after a page-wide reset or returning from a list editor. */
    void refreshControls() {
        setFocused(null);
        for (Row row : children()) {
            if (row instanceof OptionRow optionRow) {
                optionRow.build();
            }
        }
    }

    private List<FormattedCharSequence> splitTooltip(List<Component> lines) {
        List<FormattedCharSequence> out = new ArrayList<>(lines.size());
        for (Component line : lines) {
            if (line.getString().isEmpty()) {
                out.add(FormattedCharSequence.EMPTY);
            } else {
                out.addAll(font.split(line, 260));
            }
        }
        return List.copyOf(out);
    }

    /** Base row; also used as an empty spacer under multi-row content. */
    static class Row extends ContainerObjectSelectionList.Entry<Row> {
        @Override
        public void render(GuiGraphics graphics, int index, int top, int left, int width, int height,
                           int mouseX, int mouseY, boolean hovering, float partialTick) {
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return List.of();
        }

        @Override
        public List<? extends NarratableEntry> narratables() {
            return List.of();
        }

        /** Clears the focused child when the row itself loses focus (the 1.20.1 default does nothing). */
        @Override
        public void setFocused(boolean focused) {
            if (!focused) {
                setFocused((GuiEventListener) null);
            }
        }
    }

    /** Page title and one-line description. */
    final class IntroRow extends Row {
        private final Component title;
        private final Component description;
        private final List<FormattedCharSequence> descriptionTooltip;

        IntroRow(Component title, Component description) {
            this.title = title.copy().withStyle(ChatFormatting.BOLD);
            this.description = description;
            this.descriptionTooltip = splitTooltip(List.of(description));
        }

        @Override
        public void render(GuiGraphics graphics, int index, int top, int left, int width, int height,
                           int mouseX, int mouseY, boolean hovering, float partialTick) {
            graphics.drawString(font, title, left, top + 1, ConfigTheme.TEXT);
            boolean cut = ConfigTheme.clipped(graphics, font, description, left, top + 12, width, ConfigTheme.MUTED);
            if (cut && hovering) {
                screen.setTooltipForNextRenderPass(descriptionTooltip);
            }
        }
    }

    /** Section heading with a rule, and an installed/missing badge for compatibility sections. */
    final class SectionRow extends Row {
        private final Component title;
        private final String modId;
        private final boolean installed;
        private final Component badge;

        SectionRow(Component title, String modId) {
            this.title = title;
            this.modId = modId;
            this.installed = modId == null || Services.PLATFORM.isModLoaded(modId);
            this.badge = modId != null
                    ? Component.translatable(LANG + (installed ? "mod.installed" : "mod.missing"))
                    : null;
        }

        @Override
        public void render(GuiGraphics graphics, int index, int top, int left, int width, int height,
                           int mouseX, int mouseY, boolean hovering, float partialTick) {
            int y = top + height - 10;
            graphics.drawString(font, title, left, y, ConfigTheme.ACCENT);
            int right = left + width;
            if (badge != null) {
                int badgeWidth = font.width(badge) + 8;
                int badgeX = right - badgeWidth;
                int color = installed ? ConfigTheme.SUCCESS : ConfigTheme.DIM;
                ConfigTheme.outline(graphics, badgeX, y - 3, badgeWidth, 14, (color & 0x00FFFFFF) | 0x90000000);
                graphics.drawString(font, badge, badgeX + 4, y, color);
                right = badgeX - 6;
            }
            int lineX = left + font.width(title) + 6;
            if (lineX < right) {
                graphics.fill(lineX, y + 4, right, y + 5, ConfigTheme.LINE);
            }
        }
    }

    final class MessageRow extends Row {
        private final Component message;

        MessageRow(Component message) {
            this.message = message;
        }

        @Override
        public void render(GuiGraphics graphics, int index, int top, int left, int width, int height,
                           int mouseX, int mouseY, boolean hovering, float partialTick) {
            graphics.drawCenteredString(font, message, left + width / 2, top + (height - 8) / 2, ConfigTheme.MUTED);
        }
    }

    /** One setting: label (+ hint line), its control from {@link ConfigWidgets}, and a reset-to-default button. */
    final class OptionRow extends Row {
        private final ConfigOption<?> option;
        private final Component title;
        private final boolean modMissing;
        private final List<FormattedCharSequence> cachedTooltip;
        private final List<AbstractWidget> children = new ArrayList<>(2);
        private AbstractWidget control;
        private final Button reset;
        private Component cachedHint;

        OptionRow(ConfigOption<?> option) {
            this.option = option;
            this.title = option.title();
            this.modMissing = option.requiredMod() != null && !Services.PLATFORM.isModLoaded(option.requiredMod());
            this.cachedTooltip = splitTooltip(
                    option.tooltip(session.defaultValue(option), session.locked(option), modMissing));
            this.reset = Button.builder(Component.literal("↺"), b -> {
                        session.reset(option);
                        build();
                        screen.refreshFooterState();
                    })
                    .tooltip(Tooltip.create(Component.translatable(LANG + "reset.tooltip",
                            option.formatValue(session.defaultValue(option)))))
                    .size(20, 20)
                    .build();
            build();
        }

        private boolean editable() {
            return !session.locked(option);
        }

        /** Recreates the control from session state; the reset button is kept so it can stay focused. */
        void build() {
            if (getFocused() == control) {
                setFocused((GuiEventListener) null);
            }
            int w = controlWidth();
            control = ConfigWidgets.createControl(option, session, w, this::onValueChanged);
            children.clear();
            children.add(control);
            children.add(reset);
            updateState();
        }

        /** Row-local state only; the footer is refreshed once by whoever rebuilt the rows. */
        private void updateState() {
            this.cachedHint = computeHint();
            this.reset.active = editable() && !session.isDefault(option);
        }

        boolean controlValid() {
            if (control instanceof ConfigWidgets.ValidatedField field) return field.valid();
            if (control instanceof ConfigWidgets.StepperControl stepper) return stepper.valid();
            return true;
        }

        private void onValueChanged() {
            updateState();
            screen.refreshFooterState();
        }

        private Component computeHint() {
            if (session.locked(option)) return Component.translatable(LANG + "hint.locked");
            if (modMissing) return Component.translatable(LANG + "hint.mod_missing");
            Object value = session.value(option);
            if (option.type == ConfigOption.Type.LIST) {
                List<?> list = (List<?>) value;
                if (list.isEmpty()) return Component.translatable(LANG + "list.empty");
                List<String> heads = new ArrayList<>(3);
                for (Object entry : list) {
                    heads.add(option.schema().headText(entry));
                    if (heads.size() == 3) break;
                }
                return Component.literal(String.join(", ", heads) + (list.size() > 3 ? ", …" : ""));
            }
            if ((option.type == ConfigOption.Type.INT || option.type == ConfigOption.Type.DECIMAL)
                    && option.control() != ConfigOption.Control.SLIDER
                    && option.format() != ConfigOption.Format.PLAIN) {
                return option.formatValue(value);
            }
            return null;
        }

        @Override
        public void render(GuiGraphics graphics, int index, int top, int left, int width, int height,
                           int mouseX, int mouseY, boolean hovering, float partialTick) {
            boolean modified = session.modified(option);
            if (hovering) {
                graphics.fill(left - 4, top - 2, left + width + 2, top + height + 2, ConfigTheme.ROW_HOVER);
            }
            if (modified) {
                graphics.fill(left - 4, top - 2, left - 2, top + height + 2, ConfigTheme.MODIFIED);
            }

            int w = controlWidth();
            int resetX = left + width - 20;
            int controlX = resetX - 4 - w;
            int labelWidth = controlX - left - 6;
            int labelColor = modified ? ConfigTheme.MODIFIED
                    : (!editable() || modMissing) ? ConfigTheme.MUTED : ConfigTheme.TEXT;

            int labelY = cachedHint == null ? top + (height - 8) / 2 : top + 1;
            ConfigTheme.clipped(graphics, font, title, left, labelY, labelWidth, labelColor);
            if (cachedHint != null) {
                ConfigTheme.clipped(graphics, font, cachedHint, left, top + 11, labelWidth, ConfigTheme.DIM);
            }

            ConfigWidgets.layoutControl(control, controlX, top, height, w);
            control.render(graphics, mouseX, mouseY, partialTick);

            reset.setPosition(resetX, top + (height - 20) / 2);
            reset.render(graphics, mouseX, mouseY, partialTick);

            if (hovering && mouseX >= left && mouseX < controlX - 2) {
                screen.setTooltipForNextRenderPass(cachedTooltip);
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

    /** Live HUD/AppleSkin preview backed by pending session values. Spans {@link #PREVIEW_ROWS} rows. */
    final class HudPreviewRow extends Row {
        private static final Component APPLESKIN_TITLE = Component.translatable(LANG + "preview.appleskin");
        private static final Component HUD_TITLE = Component.translatable(LANG + "preview.hud");
        private static final Component DISABLED_TEXT = Component.translatable(LANG + "preview.disabled");

        private int lastXOffset = Integer.MIN_VALUE;
        private int lastYOffset = Integer.MIN_VALUE;
        private Component cachedOffsetText = Component.empty();

        private boolean enabled(ConfigOption<Boolean> option) {
            return session.available(option) && Boolean.TRUE.equals(session.value(option));
        }

        @Override
        public void render(GuiGraphics graphics, int index, int top, int left, int width, int height,
                           int mouseX, int mouseY, boolean hovering, float partialTick) {
            renderBox(graphics, top, left, width);
        }

        /** Draws the whole preview box with its top at {@code top}; the list scissor clips what is off screen. */
        void renderBox(GuiGraphics graphics, int top, int left, int width) {
            int boxHeight = PREVIEW_ROWS * ROW_HEIGHT - 4;
            int right = left + width;
            int bottom = top + boxHeight;
            graphics.fill(left, top, right, bottom, 0x70000000);
            ConfigTheme.outline(graphics, left, top, width, boxHeight, ConfigTheme.LINE);

            graphics.enableScissor(left + 1, top + 1, right - 1, bottom - 1);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            int tooltipWidth = width / 2;
            int split = left + tooltipWidth;
            graphics.fill(split, top, split + 1, bottom, ConfigTheme.LINE);
            renderAppleSkinPreview(graphics, left, top, tooltipWidth, boxHeight);
            renderHudPreview(graphics, split + 1, top, right - split - 1, boxHeight);
            RenderSystem.disableBlend();
            graphics.disableScissor();
        }

        private void renderAppleSkinPreview(GuiGraphics graphics, int left, int top, int width, int height) {
            graphics.drawString(font, APPLESKIN_TITLE, left + 5, top + 5, ConfigTheme.ACCENT);
            if (!enabled(CompatOptions.APPLE_SKIN_THIRST_TOOLTIP)) {
                graphics.drawString(font, DISABLED_TEXT, left + 5, top + height / 2, ConfigTheme.DIM);
                return;
            }
            int x = left + 6;
            int thirstY = top + 27;
            for (int i = 0; i < 3; i++) {
                graphics.blit(Constants.THIRST_ICONS, x + i * 9, thirstY, 18, 0, 9, 9, 36, 25);
            }
            int quenchedY = thirstY + 13;
            for (int i = 0; i < 2; i++) {
                graphics.blit(Constants.THIRST_ICONS, x + i * 8, quenchedY, 0, 18, 7, 7, 36, 25);
            }
            ConfigTheme.clipped(graphics, font, CompatOptions.APPLE_SKIN_THIRST_TOOLTIP.title(),
                    left + 5, top + height - 13, width - 10, ConfigTheme.MUTED);
        }

        private void renderHudPreview(GuiGraphics graphics, int left, int top, int width, int height) {
            graphics.drawString(font, HUD_TITLE, left + 5, top + 5, ConfigTheme.ACCENT);
            int xOff = session.value(ClientOptions.HUD_X_OFFSET);
            int yOff = session.value(ClientOptions.HUD_Y_OFFSET);
            int thirstRight = left + width / 2 + 41 + Mth.clamp(xOff, -24, 24);
            int thirstTop = top + 31 + Mth.clamp(yOff, -12, 12);
            int exhaustionWidth = (int) (((Util.getMillis() % 4000L) / 4000.0) * 81);

            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            graphics.setColor(1, 1, 1, 0.75F);
            for (int i = 0; i < 10; i++) {
                int x = thirstRight - i * 8 - 9;
                int visibleLeft = Math.max(x, thirstRight - exhaustionWidth);
                if (visibleLeft < x + 9) {
                    int sourceX = visibleLeft - x;
                    graphics.blit(Constants.THIRST_ICONS, visibleLeft, thirstTop, 27 + sourceX, 9,
                            x + 9 - visibleLeft, 9, 36, 25);
                }
            }
            graphics.setColor(1, 1, 1, 1);

            int thirst = 12;
            int quenched = 6;
            for (int i = 0; i < 10; i++) {
                int x = thirstRight - i * 8 - 9;
                int level = i * 2 + 1;
                if (level > thirst) {
                    graphics.blit(Constants.THIRST_ICONS, x, thirstTop, 0, 0, 9, 9, 36, 25);
                }
                if (level < thirst) graphics.blit(Constants.THIRST_ICONS, x, thirstTop, 18, 0, 9, 9, 36, 25);
                else if (level == thirst) graphics.blit(Constants.THIRST_ICONS, x, thirstTop, 9, 0, 9, 9, 36, 25);
                if (quenched >= i * 2 + 2) graphics.blit(Constants.THIRST_ICONS, x, thirstTop, 0, 9, 9, 9, 36, 25);
                else if (quenched > i * 2) graphics.blit(Constants.THIRST_ICONS, x, thirstTop, 9, 9, 9, 9, 36, 25);
            }

            if (enabled(CompatOptions.APPLE_SKIN_THIRST_HUD_PREVIEW)) {
                float wave = 0.5F + 0.5F * (float) Math.sin(Util.getMillis() / 300.0);
                float eased = wave * wave * (3.0F - 2.0F * wave);
                renderHeldItemFade(graphics, thirstRight, thirstTop, 0.18F + 0.62F * eased);
            }

            if (xOff != lastXOffset || yOff != lastYOffset) {
                lastXOffset = xOff;
                lastYOffset = yOff;
                cachedOffsetText = Component.translatable(LANG + "preview.offset", xOff, yOff);
            }
            ConfigTheme.clipped(graphics, font, cachedOffsetText, left + 5, top + height - 13,
                    width - 10, ConfigTheme.MUTED);
        }

        private void renderHeldItemFade(GuiGraphics graphics, int thirstRight, int thirstTop, float alpha) {
            Matrix4f matrix = graphics.pose().last().pose();

            RenderSystem.setShaderTexture(0, Constants.THIRST_ICONS);
            RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            BufferBuilder buffer = Tesselator.getInstance().getBuilder();
            buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);

            for (int i = 6; i < 9; i++) {
                int x = thirstRight - i * 8 - 9;
                addTexturedQuad(buffer, matrix, x, thirstTop, i == 8 ? 9 : 18, 0, alpha);
            }
            for (int i = 3; i < 5; i++) {
                int x = thirstRight - i * 8 - 9;
                addTexturedQuad(buffer, matrix, x, thirstTop, 0, 9, alpha);
            }

            BufferUploader.drawWithShader(buffer.end());
            RenderSystem.disableBlend();
        }

        private void addTexturedQuad(BufferBuilder buffer, Matrix4f matrix,
                                     int x, int y, int u, int v, float alpha) {
            float minU = u / 36.0F;
            float maxU = (u + 9) / 36.0F;
            float minV = v / 25.0F;
            float maxV = (v + 9) / 25.0F;
            buffer.vertex(matrix, x, y, 0).uv(minU, minV).color(1, 1, 1, alpha).endVertex();
            buffer.vertex(matrix, x, y + 9, 0).uv(minU, maxV).color(1, 1, 1, alpha).endVertex();
            buffer.vertex(matrix, x + 9, y + 9, 0).uv(maxU, maxV).color(1, 1, 1, alpha).endVertex();
            buffer.vertex(matrix, x + 9, y, 0).uv(maxU, minV).color(1, 1, 1, alpha).endVertex();
        }
    }

    /**
     * Spacer under the HUD preview. The list only renders rows whose own bounds are on screen, so once the
     * rows above have scrolled out, the first visible part draws the preview at its original position.
     */
    final class PreviewPartRow extends Row {
        private final HudPreviewRow preview;
        private final int offset;

        PreviewPartRow(HudPreviewRow preview, int offset) {
            this.preview = preview;
            this.offset = offset;
        }

        @Override
        public void render(GuiGraphics graphics, int index, int top, int left, int width, int height,
                           int mouseX, int mouseY, boolean hovering, float partialTick) {
            if (top < y0) {
                preview.renderBox(graphics, top - offset * ROW_HEIGHT, left, width);
            }
        }
    }
}
