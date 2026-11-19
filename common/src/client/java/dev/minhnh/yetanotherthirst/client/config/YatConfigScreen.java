package dev.minhnh.yetanotherthirst.client.config;

import dev.minhnh.yetanotherthirst.Lang;
import dev.minhnh.yetanotherthirst.platform.Services;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Locale;

/**
 * In-game config editor for Yet Another Thirst (1.20.1), using a sidebar of categories,
 * a global search box, a vanilla {@link OptionList}, and transactional {@link ConfigSession} persistence.
 */
public final class YatConfigScreen extends Screen {

    private static final String LANG = Lang.CONFIG_PREFIX;
    private static final int HEADER_HEIGHT = 36;
    private static final int FOOTER_HEIGHT = 36;
    private static final int BANNER_HEIGHT = 14;
    private static final int TAB_HEIGHT = 22;
    private static final int COMPACT_BELOW = 400;

    private final Screen parent;
    private final ConfigSession session;

    private ConfigCategory selected = ConfigOptions.CATEGORIES.get(0);
    private String query = "";
    private double scroll;
    /** Available, unlocked options on the current page or search result; recomputed with the rows. */
    private List<ConfigOption<?>> visibleEditable = List.of();

    private OptionList list;
    private EditBox search;
    private Button resetPage;
    private Button apply;
    private Button done;
    private int sidebarWidth;
    private int tabHeight = TAB_HEIGHT;
    private boolean compact;

    public YatConfigScreen(Screen parent) {
        this(parent, new ConfigSession());
    }

    YatConfigScreen(Screen parent, ConfigSession session) {
        super(Component.translatable(LANG + "title"));
        this.parent = parent;
        this.session = session;
    }

    private int bannerHeight() {
        return session.remote() ? BANNER_HEIGHT : 0;
    }

    @Override
    protected void init() {
        if (list != null) {
            scroll = list.getScrollAmount();
        }
        compact = width < COMPACT_BELOW;
        sidebarWidth = compact ? 28 : computeSidebarWidth();

        int listTop = HEADER_HEIGHT + bannerHeight();
        int listBottom = height - FOOTER_HEIGHT;
        list = new OptionList(this, minecraft, session, width - sidebarWidth, height, listTop, listBottom);
        list.setLeftPos(sidebarWidth);
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
        tabHeight = Mth.clamp((listBottom - listTop - 8) / categories, 18, TAB_HEIGHT);
        for (int i = 0; i < categories; i++) {
            ConfigCategory category = ConfigOptions.CATEGORIES.get(i);
            CategoryTab tab = new CategoryTab(category, 4, listTop + 4 + i * tabHeight, sidebarWidth - 8);
            if (compact) {
                tab.setTooltip(Tooltip.create(category.title()));
            }
            addRenderableWidget(tab);
        }

        int buttonY = listBottom + (FOOTER_HEIGHT - 20) / 2;
        resetPage = addRenderableWidget(Button.builder(Component.translatable(LANG + "reset_page"), b -> resetPage())
                .tooltip(Tooltip.create(Component.translatable(LANG + "reset_page.tooltip")))
                .bounds(10, buttonY, 96, 20).build());
        int buttonWidth = Mth.clamp((width - 140) / 3, 60, 90);
        int right = width - 10;
        done = addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, b -> saveAndClose())
                .bounds(right - buttonWidth, buttonY, buttonWidth, 20).build());
        apply = addRenderableWidget(Button.builder(Component.translatable(LANG + "apply"), b -> applyChanges())
                .tooltip(Tooltip.create(Component.translatable(LANG + "apply.tooltip")))
                .bounds(right - buttonWidth * 2 - 4, buttonY, buttonWidth, 20).build());
        addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, b -> closeTo(parent, true))
                .bounds(right - buttonWidth * 3 - 8, buttonY, buttonWidth, 20).build());

        rebuildRows();
        list.setScrollAmount(scroll);
        refreshFooterState();
    }

    private int computeSidebarWidth() {
        int widest = 0;
        for (ConfigCategory category : ConfigOptions.CATEGORIES) {
            widest = Math.max(widest, font.width(category.title()));
        }
        return Mth.clamp(widest + 44, 100, 150);
    }

    private void rebuildRows() {
        List<ConfigOption<?>> options;
        if (query.isEmpty()) {
            list.showCategory(selected);
            options = selected.options();
        } else {
            list.showSearch(query);
            options = OptionList.searchOptions(session, query);
        }
        visibleEditable = options.stream().filter(session::available).filter(o -> !session.locked(o)).toList();
    }

    private void onSearch(String value) {
        String next = value.trim().toLowerCase(Locale.ROOT);
        if (next.equals(query)) return;
        query = next;
        scroll = 0;
        rebuildRows();
        list.setScrollAmount(0);
        refreshFooterState();
    }

    private void select(ConfigCategory category) {
        if (selected == category && query.isEmpty()) return;
        selected = category;
        scroll = 0;
        if (!query.isEmpty()) {
            search.setValue("");
        } else {
            rebuildRows();
            list.setScrollAmount(0);
            refreshFooterState();
        }
    }

    private void resetPage() {
        for (ConfigOption<?> option : visibleEditable) {
            session.reset(option);
        }
        list.refreshControls();
        refreshFooterState();
    }

    private void applyChanges() {
        if (list.hasInvalidField()) return;
        trySave();
        list.refreshControls();
        refreshFooterState();
    }

    private void saveAndClose() {
        if (list.hasInvalidField()) {
            minecraft.setScreen(this);
            return;
        }
        if (trySave()) {
            closeTo(parent, false);
        } else {
            minecraft.setScreen(this);
        }
    }

    /** Saves pending edits; on failure shows a toast and leaves the edits pending. */
    private boolean trySave() {
        if (session.save()) return true;
        SystemToast.addOrUpdate(minecraft.getToasts(), SystemToast.SystemToastIds.PACK_LOAD_FAILURE,
                Component.translatable(LANG + "save_failed"), Component.translatable(LANG + "save_failed.desc"));
        return false;
    }

    private void closeTo(Screen target, boolean discard) {
        if (discard) {
            session.discard();
        }
        minecraft.setScreen(target);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    void openListEditor(ConfigOption<?> rawOption) {
        scroll = list.getScrollAmount();
        ConfigOption<List<Object>> option = (ConfigOption<List<Object>>) rawOption;
        List<Object> current = session.value(option);
        List<Object> defaultVal = session.defaultValue(option);
        minecraft.setScreen(new ListEditorScreen(this, option, current, defaultVal,
                result -> session.set(option, (List<Object>) result)));
    }

    @Override
    public void tick() {
        super.tick();
        updateSaveButtons();
    }

    /** Save buttons stay disabled while a visible field holds invalid text, which is not part of the session. */
    private void updateSaveButtons() {
        boolean invalid = list != null && list.hasInvalidField();
        if (apply != null) {
            apply.active = session.pendingCount() > 0 && !invalid;
        }
        if (done != null) {
            done.active = !invalid;
        }
    }

    void refreshFooterState() {
        int pending = session.pendingCount();
        updateSaveButtons();
        if (done != null) {
            done.setMessage(pending > 0
                    ? Component.translatable(LANG + "save", pending)
                    : CommonComponents.GUI_DONE);
        }
        if (resetPage != null) {
            resetPage.active = false;
            for (ConfigOption<?> option : visibleEditable) {
                if (!session.isDefault(option)) {
                    resetPage.active = true;
                    break;
                }
            }
        }
    }

    @Override
    public void onClose() {
        if (session.pendingCount() == 0) {
            closeTo(parent, false);
            return;
        }
        scroll = list.getScrollAmount();
        minecraft.setScreen(new UnsavedChangesScreen(session.pendingCount()));
    }

    @Override
    public void renderBackground(GuiGraphics graphics) {
        super.renderBackground(graphics);
        int listTop = HEADER_HEIGHT + bannerHeight();
        int footerTop = height - FOOTER_HEIGHT;
        graphics.fill(sidebarWidth, listTop, width, footerTop, ConfigTheme.LIST_BG);
        graphics.fill(0, listTop, sidebarWidth, footerTop, ConfigTheme.SIDEBAR);
        graphics.fill(sidebarWidth - 1, listTop, sidebarWidth, footerTop, ConfigTheme.LINE);
        graphics.fill(0, listTop - 1, width, listTop, ConfigTheme.LINE);
        graphics.fill(0, footerTop, width, footerTop + 1, ConfigTheme.LINE);
        if (session.remote()) {
            graphics.fill(0, HEADER_HEIGHT, width, listTop, ConfigTheme.BANNER);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderChrome(graphics);
    }

    private void renderChrome(GuiGraphics graphics) {
        ConfigTheme.droplet(graphics, 10, (HEADER_HEIGHT - 18) / 2, 2);
        graphics.drawString(font, title.copy().withStyle(ChatFormatting.BOLD), 36, 8, ConfigTheme.TEXT);
        Component subtitle = Component.translatable(LANG + "subtitle." + scope(), Services.PLATFORM.getPlatformName());
        if (36 + font.width(subtitle) < search.getX() - 6) {
            graphics.drawString(font, subtitle, 36, 20, ConfigTheme.MUTED);
        }

        int footerTop = height - FOOTER_HEIGHT;
        if (session.remote()) {
            graphics.drawCenteredString(font, Component.translatable(LANG + "banner.remote"),
                    width / 2, HEADER_HEIGHT + 3, ConfigTheme.MODIFIED);
        }

        int pending = session.pendingCount();
        Component status = pending > 0
                ? Component.translatable(LANG + "status.pending", pending)
                : Component.translatable(LANG + "status.saved");
        int statusX = resetPage.getX() + resetPage.getWidth() + 8;
        int statusRight = width - 10 - (done.getWidth() + 4) * 3;
        if (statusRight - statusX > 40) {
            ConfigTheme.clipped(graphics, font, status, statusX, footerTop + (FOOTER_HEIGHT - 8) / 2,
                    statusRight - statusX - 4, pending > 0 ? ConfigTheme.MODIFIED : ConfigTheme.DIM);
        }
    }

    private String scope() {
        if (minecraft.level == null) return "menu";
        return session.remote() ? "remote" : "world";
    }

    /** A sidebar entry: page icon, name, and a badge with that page's unsaved-change count. */
    private final class CategoryTab extends AbstractButton {
        private final ConfigCategory category;
        private final ItemStack icon;

        CategoryTab(ConfigCategory category, int x, int y, int width) {
            super(x, y, width, tabHeight - 2, category.title());
            this.category = category;
            this.icon = category.icon().get();
        }

        @Override
        public void onPress() {
            select(category);
        }

        @Override
        protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
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
            graphics.renderFakeItem(icon, iconX, y + (height - 16) / 2);

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
                graphics.drawString(font, count, badgeX + 3, badgeY + 1, 0xFF3A2A00, false);
            }
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput output) {
            defaultButtonNarrationText(output);
        }
    }

    /** Asks what to do with unsaved edits: save, discard, or go back to editing. */
    private final class UnsavedChangesScreen extends ConfirmScreen {
        UnsavedChangesScreen(int pending) {
            super(ignored -> YatConfigScreen.this.minecraft.setScreen(YatConfigScreen.this),
                    Component.translatable(LANG + "unsaved.title"),
                    Component.translatable(LANG + "unsaved.message", pending));
        }

        @Override
        protected void addButtons(int y) {
            int x = width / 2 - 155;
            addExitButton(Button.builder(Component.translatable(LANG + "unsaved.save"), b -> saveAndClose())
                    .bounds(x, y, 100, 20).build());
            addExitButton(Button.builder(Component.translatable(LANG + "unsaved.discard"), b -> closeTo(parent, true))
                    .bounds(x + 105, y, 100, 20).build());
            addExitButton(Button.builder(Component.translatable(LANG + "unsaved.back"), b -> minecraft.setScreen(YatConfigScreen.this))
                    .bounds(x + 210, y, 100, 20).build());
        }

        @Override
        public boolean shouldCloseOnEsc() {
            return true;
        }

        @Override
        public void onClose() {
            minecraft.setScreen(YatConfigScreen.this);
        }
    }
}
