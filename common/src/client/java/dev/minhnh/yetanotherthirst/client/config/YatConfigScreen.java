package dev.minhnh.yetanotherthirst.client.config;

import dev.minhnh.yetanotherthirst.Lang;
import dev.minhnh.yetanotherthirst.platform.Services;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * In-game editor for Yet Another Thirst's config, opened from the mod list (Forge/NeoForge) or Mod Menu
 * (Fabric). A sidebar of pages with item icons, a search box that looks through every page, and a list of
 * rows built from vanilla widgets. Edits stay in a {@link ConfigSession} until Apply/Done, so Cancel
 * really cancels; leaving with unsaved edits asks first.
 */
public final class YatConfigScreen extends Screen {

    private static final String LANG = Lang.CONFIG_PREFIX;
    private static final int HEADER_HEIGHT = 36;
    private static final int FOOTER_HEIGHT = 36;
    private static final int BANNER_HEIGHT = 14;
    private static final int TAB_HEIGHT = 22;
    private static final int MIN_TAB_HEIGHT = 18;
    private static final int COMPACT_BELOW = 400;

    private final Screen parent;
    private final ConfigSession session = new ConfigSession();

    private ConfigCategory selected = ConfigOptions.CATEGORIES.get(0);
    private String query = "";
    private double scroll;
    private int lastSaveFailed;
    private boolean shownInvalid;
    private List<ConfigOption<?>> cachedVisibleOptions = List.of();

    private OptionList list;
    private EditBox search;
    private Button resetPage;
    private Button apply;
    private Button done;
    private int sidebarWidth;
    private int tabHeight = TAB_HEIGHT;
    private final List<CategoryTab> tabs = new ArrayList<>();
    private int tabsTop;
    private int tabsBottom;
    private int tabScroll;
    private boolean compact;

    public YatConfigScreen(Screen parent) {
        super(Component.translatable(LANG + "title"));
        this.parent = parent;
    }

    private int bannerHeight() {
        return session.remote() ? BANNER_HEIGHT : 0;
    }

    @Override
    protected void init() {
        compact = width < COMPACT_BELOW;
        sidebarWidth = compact ? 28 : sidebarWidth();

        int listTop = HEADER_HEIGHT + bannerHeight();
        int listWidth = width - sidebarWidth;
        int listHeight = height - listTop - FOOTER_HEIGHT;
        boolean reuseList = list != null && list.getWidth() == listWidth && list.getHeight() == listHeight && list.getY() == listTop;
        if (!reuseList) {
            if (list != null) scroll = list.scrollAmount();
            list = new OptionList(this, minecraft, session, listWidth, listHeight, listTop);
            list.setX(sidebarWidth);
        }
        addRenderableWidget(list);

        int searchWidth = Math.min(170, width / 3);
        String previous = search == null ? "" : search.getValue();
        search = new EditBox(font, width - searchWidth - 10, (HEADER_HEIGHT - 18) / 2, searchWidth, 18,
                Component.translatable(LANG + "search"));
        search.setHint(Component.translatable(LANG + "search").withStyle(ChatFormatting.DARK_GRAY));
        search.setMaxLength(64);
        search.setValue(previous);
        search.setResponder(this::onSearch);
        addRenderableWidget(search);

        int categories = ConfigOptions.CATEGORIES.size();
        tabsTop = listTop + 4;
        tabsBottom = height - FOOTER_HEIGHT - 4;
        tabHeight = Mth.clamp((tabsBottom - tabsTop) / categories, MIN_TAB_HEIGHT, TAB_HEIGHT);
        tabs.clear();
        for (int i = 0; i < categories; i++) {
            ConfigCategory category = ConfigOptions.CATEGORIES.get(i);
            CategoryTab tab = new CategoryTab(category, 4, tabsTop + i * tabHeight, sidebarWidth - 8);
            if (compact) tab.setTooltip(Tooltip.create(category.title()));
            // Input only: tabs are drawn clipped to the sidebar in extractRenderState().
            addWidget(tab);
            tabs.add(tab);
        }
        scrollTabs(0);

        int buttonY = height - FOOTER_HEIGHT + (FOOTER_HEIGHT - 20) / 2;
        resetPage = addRenderableWidget(Button.builder(Component.translatable(LANG + "reset_page"), b -> resetPage())
                .bounds(10, buttonY, 96, 20).build());
        updateResetLabel();
        int buttonWidth = Mth.clamp((width - 140) / 3, 60, 90);
        int right = width - 10;
        done = addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, b -> saveAndClose())
                .bounds(right - buttonWidth, buttonY, buttonWidth, 20).build());
        apply = addRenderableWidget(Button.builder(Component.translatable(LANG + "apply"), b -> applyChanges())
                .tooltip(Tooltip.create(Component.translatable(LANG + "apply.tooltip")))
                .bounds(right - buttonWidth * 2 - 4, buttonY, buttonWidth, 20).build());
        addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, b -> closeTo(parent, true))
                .bounds(right - buttonWidth * 3 - 8, buttonY, buttonWidth, 20).build());
        shownInvalid = false;

        if (!reuseList) {
            rebuildRows();
        }
        list.setScrollAmount(scroll);
    }

    private int maxTabScroll() {
        return Math.max(0, tabs.size() * tabHeight - (tabsBottom - tabsTop));
    }

    /**
     * Moves the sidebar by {@code delta} pixels, keeping it in range. Tabs outside the viewport stay visible so
     * Tab navigation can reach (and reveal) them; drawing is clipped and clicks are limited to the sidebar.
     */
    private void scrollTabs(int delta) {
        tabScroll = Mth.clamp(tabScroll + delta, 0, maxTabScroll());
        for (int i = 0; i < tabs.size(); i++) {
            tabs.get(i).setY(tabsTop + i * tabHeight - tabScroll);
        }
    }

    /** Scrolls just enough to show the whole tab, e.g. when it gains keyboard focus. */
    private void revealTab(CategoryTab tab) {
        if (tab.getY() < tabsTop) scrollTabs(tab.getY() - tabsTop);
        else if (tab.getY() + tab.getHeight() > tabsBottom) scrollTabs(tab.getY() + tab.getHeight() - tabsBottom);
    }

    private boolean overTabs(double mouseX, double mouseY) {
        return mouseX < sidebarWidth && mouseY >= tabsTop && mouseY < tabsBottom;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (overTabs(mouseX, mouseY) && maxTabScroll() > 0) {
            scrollTabs((int) Math.round(-scrollY * tabHeight));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    private void renderTabs(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        graphics.enableScissor(0, tabsTop, sidebarWidth, tabsBottom);
        for (CategoryTab tab : tabs) tab.extractRenderState(graphics, mouseX, mouseY, partialTick);
        graphics.disableScissor();
        int max = maxTabScroll();
        if (max > 0) {
            int track = tabsBottom - tabsTop;
            int thumb = Math.max(12, track * track / (track + max));
            int thumbY = tabsTop + (track - thumb) * tabScroll / max;
            graphics.fill(sidebarWidth - 3, tabsTop, sidebarWidth - 1, tabsBottom, ConfigTheme.LINE);
            graphics.fill(sidebarWidth - 3, thumbY, sidebarWidth - 1, thumbY + thumb, ConfigTheme.MUTED);
        }
    }

    private int sidebarWidth() {
        int widest = 0;
        for (ConfigCategory category : ConfigOptions.CATEGORIES) {
            widest = Math.max(widest, font.width(category.title()));
        }
        return Mth.clamp(widest + 44, 100, 150);
    }

    private void rebuildRows() {
        cachedVisibleOptions = computeVisibleOptions();
        if (query.isEmpty()) list.showCategory(selected);
        else list.showSearch(query);
        updateResetLabel();
    }

    /** While searching, the reset button acts on the results across every page, so it says so. */
    private void updateResetLabel() {
        String key = query.isEmpty() ? "reset_page" : "reset_results";
        resetPage.setMessage(Component.translatable(LANG + key));
        resetPage.setTooltip(Tooltip.create(Component.translatable(LANG + key + ".tooltip")));
    }

    private void onSearch(String value) {
        String next = value.trim().toLowerCase(Locale.ROOT);
        if (next.equals(query)) return;
        query = next;
        scroll = 0;
        rebuildRows();
        list.setScrollAmount(0);
    }

    private void select(ConfigCategory category) {
        selected = category;
        scroll = 0;
        if (!query.isEmpty()) {
            search.setValue("");
        } else {
            rebuildRows();
        }
        list.setScrollAmount(0);
    }

    private List<ConfigOption<?>> computeVisibleOptions() {
        List<ConfigOption<?>> options = query.isEmpty()
                ? selected.options()
                : ConfigOptions.CATEGORIES.stream().flatMap(c -> c.options().stream()).filter(o -> o.matches(query)).toList();
        return options.stream().filter(session::available).filter(o -> !session.locked(o)).toList();
    }

    private void resetPage() {
        list.clearFocus();
        for (ConfigOption<?> option : cachedVisibleOptions) session.reset(option);
        list.refreshControls();
    }

    /**
     * Saves and returns true when every edit reached disk; otherwise the failure shows in the footer. Edit boxes
     * are blurred first, so one still holding invalid text falls back to its last committed value on screen too.
     */
    private boolean save() {
        list.clearFocus();
        lastSaveFailed = session.save().failed();
        list.refreshControls();
        return lastSaveFailed == 0;
    }

    private void applyChanges() {
        save();
    }

    private void saveAndClose() {
        if (save()) {
            closeTo(parent, false);
        } else if (minecraft.gui.screen() != this) {
            minecraft.gui.setScreen(this);
        }
    }

    private void closeTo(Screen target, boolean discard) {
        if (discard) session.discard();
        minecraft.gui.setScreen(target);
    }

    void openListEditor(ConfigOption<?> option) {
        scroll = list.scrollAmount();
        minecraft.gui.setScreen(new ListEditorScreen(this, option, session.value(option), session.defaultValue(option),
                result -> {
                    session.set(option, result);
                    list.refreshOption(option);
                }));
    }

    @Override
    public void onClose() {
        if (session.pendingCount() == 0) {
            closeTo(parent, false);
            return;
        }
        scroll = list.scrollAmount();
        minecraft.gui.setScreen(new UnsavedChangesScreen(session.pendingCount()));
    }

    /** Focus leaving the option list must also leave its row and edit box (see {@link OptionList#clearFocus()}). */
    @Override
    public void setFocused(GuiEventListener listener) {
        if (list != null && listener != list && getFocused() == list) {
            list.clearFocus();
        }
        super.setFocused(listener);
    }

    @Override
    public void tick() {
        super.tick();
        int pending = session.pendingCount();
        boolean invalid = list.hasInvalidField();
        apply.active = pending > 0 && !invalid;
        done.active = !invalid;
        if (invalid != shownInvalid) {
            shownInvalid = invalid;
            Tooltip blocked = invalid ? Tooltip.create(Component.translatable(LANG + "invalid_field")) : null;
            done.setTooltip(blocked);
            apply.setTooltip(blocked != null ? blocked : Tooltip.create(Component.translatable(LANG + "apply.tooltip")));
        }
        done.setMessage(pending > 0
                ? Component.translatable(LANG + "save", pending)
                : CommonComponents.GUI_DONE);
        resetPage.active = cachedVisibleOptions.stream().anyMatch(o -> !session.isDefault(o));
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        int listTop = HEADER_HEIGHT + bannerHeight();
        int footerTop = height - FOOTER_HEIGHT;
        graphics.fill(0, listTop, sidebarWidth, footerTop, ConfigTheme.SIDEBAR);
        graphics.fill(sidebarWidth - 1, listTop, sidebarWidth, footerTop, ConfigTheme.LINE);
        boolean inWorld = minecraft.level != null;
        graphics.blit(RenderPipelines.GUI_TEXTURED, inWorld ? INWORLD_HEADER_SEPARATOR : HEADER_SEPARATOR,
                0, listTop - 2, 0.0F, 0.0F, sidebarWidth, 2, 32, 2);
        graphics.blit(RenderPipelines.GUI_TEXTURED, inWorld ? INWORLD_FOOTER_SEPARATOR : FOOTER_SEPARATOR,
                0, footerTop, 0.0F, 0.0F, sidebarWidth, 2, 32, 2);
        if (session.remote()) {
            graphics.fill(0, HEADER_HEIGHT, width, listTop, ConfigTheme.BANNER);
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        renderTabs(graphics, mouseX, mouseY, partialTick);
        renderChrome(graphics);
    }

    private void renderChrome(GuiGraphicsExtractor graphics) {
        ConfigTheme.droplet(graphics, 10, (HEADER_HEIGHT - 18) / 2, 2);
        graphics.text(font, title.copy().withStyle(ChatFormatting.BOLD), 36, 8, ConfigTheme.TEXT);
        Component subtitle = Component.translatable(LANG + "subtitle." + scope(), Services.PLATFORM.getPlatformName());
        if (36 + font.width(subtitle) < search.getX() - 6) {
            graphics.text(font, subtitle, 36, 20, ConfigTheme.MUTED);
        }

        int footerTop = height - FOOTER_HEIGHT;
        if (session.remote()) {
            graphics.centeredText(font, Component.translatable(LANG + "banner.remote"),
                    width / 2, HEADER_HEIGHT + 3, ConfigTheme.MODIFIED);
        }

        int pending = session.pendingCount();
        if (lastSaveFailed > pending) lastSaveFailed = pending;
        Component status;
        int statusColor;
        if (lastSaveFailed > 0) {
            status = Component.translatable(LANG + "status.save_failed", lastSaveFailed);
            statusColor = ConfigTheme.ERROR;
        } else if (pending > 0) {
            status = Component.translatable(LANG + "status.pending", pending);
            statusColor = ConfigTheme.MODIFIED;
        } else {
            status = Component.translatable(LANG + "status.saved");
            statusColor = ConfigTheme.DIM;
        }
        int statusX = resetPage.getX() + resetPage.getWidth() + 8;
        int statusRight = width - 10 - (done.getWidth() + 4) * 3;
        if (statusRight - statusX > 40) {
            ConfigTheme.clipped(graphics, font, status, statusX, footerTop + (FOOTER_HEIGHT - 8) / 2,
                    statusRight - statusX - 4, statusColor);
        }
    }

    private String scope() {
        if (minecraft.level == null) return "menu";
        return session.remote() ? "remote" : "world";
    }

    /** A sidebar entry: page icon, name and a badge with that page's unsaved-change count. */
    private final class CategoryTab extends AbstractButton {
        private final ConfigCategory category;
        private final ItemStack icon;

        CategoryTab(ConfigCategory category, int x, int y, int width) {
            super(x, y, width, tabHeight - 2, category.title());
            this.category = category;
            this.icon = category.icon().get();
        }

        @Override
        public void onPress(InputWithModifiers input) {
            select(category);
        }

        /** Parts of a tab scrolled under the header or footer must not take clicks meant for those. */
        @Override
        public boolean isMouseOver(double mouseX, double mouseY) {
            return overTabs(mouseX, mouseY) && super.isMouseOver(mouseX, mouseY);
        }

        @Override
        public void setFocused(boolean focused) {
            super.setFocused(focused);
            if (focused) revealTab(this);
        }

        @Override
        protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
            int x = getX();
            int y = getY();
            boolean current = query.isEmpty() && selected == category;
            if (current) {
                graphics.fill(x, y, x + width, y + height, ConfigTheme.SELECTED);
                graphics.fill(x, y, x + 2, y + height, ConfigTheme.ACCENT);
            } else if (isHoveredOrFocused()) {
                graphics.fill(x, y, x + width, y + height, ConfigTheme.ROW_HOVER);
            }
            int iconX = compact ? x + (width - 16) / 2 : x + 5;
            graphics.fakeItem(icon, iconX, y + (height - 16) / 2);

            int pending = session.pendingCount(category);
            if (!compact) {
                int textRight = x + width - 4 - (pending > 0 ? font.width(String.valueOf(pending)) + 8 : 0);
                ConfigTheme.clipped(graphics, font, category.title(), x + 25, y + (height - 8) / 2,
                        textRight - x - 25, current || isHoveredOrFocused() ? ConfigTheme.TEXT : ConfigTheme.MUTED);
            }
            if (pending > 0) {
                String count = String.valueOf(pending);
                int badgeWidth = font.width(count) + 5;
                int badgeX = compact ? x + width - badgeWidth : x + width - badgeWidth - 3;
                int badgeY = compact ? y : y + (height - 10) / 2;
                graphics.fill(badgeX, badgeY, badgeX + badgeWidth, badgeY + 10, ConfigTheme.MODIFIED);
                graphics.text(font, count, badgeX + 3, badgeY + 1, 0xFF3A2A00, false);
            }
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput output) {
            defaultButtonNarrationText(output);
        }
    }

    private void reopen() {
        minecraft.gui.setScreen(this);
    }

    /**
     * Asks what to do with unsaved edits: save, discard or go back to editing. ConfirmScreen answers Esc through
     * its callback (not {@link #onClose()}), so the callback goes back to editing.
     */
    private final class UnsavedChangesScreen extends ConfirmScreen {
        UnsavedChangesScreen(int pending) {
            super(ignored -> YatConfigScreen.this.reopen(),
                    Component.translatable(LANG + "unsaved.title"),
                    Component.translatable(LANG + "unsaved.message", pending));
        }

        @Override
        protected void addButtons(LinearLayout buttonLayout) {
            buttonLayout.addChild(Button.builder(Component.translatable(LANG + "unsaved.save"), b -> saveAndClose())
                    .width(100).build());
            buttonLayout.addChild(Button.builder(Component.translatable(LANG + "unsaved.discard"), b -> closeTo(parent, true))
                    .width(100).build());
            buttonLayout.addChild(Button.builder(Component.translatable(LANG + "unsaved.back"), b -> minecraft.gui.setScreen(YatConfigScreen.this))
                    .width(100).build());
        }

        @Override
        public boolean shouldCloseOnEsc() {
            return true;
        }

        @Override
        public void onClose() {
            minecraft.gui.setScreen(YatConfigScreen.this);
        }
    }
}

