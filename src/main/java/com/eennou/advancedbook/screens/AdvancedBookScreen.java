package com.eennou.advancedbook.screens;

import com.eennou.advancedbook.AdvancedBook;
import com.eennou.advancedbook.networking.ChangePageBookC2SPacket;
import com.eennou.advancedbook.networking.EditBookC2SPacket;
import com.eennou.advancedbook.networking.EditPagesBookC2SPacket;
import com.eennou.advancedbook.networking.UpdateBookmarksC2SPacket;
import com.eennou.advancedbook.screens.bookelements.*;
import com.eennou.advancedbook.screens.components.AdvancedPageButton;
import com.eennou.advancedbook.utils.Bookmark;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.inventory.PageButton;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FastColor;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.*;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class AdvancedBookScreen extends AbstractGraphicsScreen {
    protected int currentPage;
    protected int pagesCount;
    protected Map<Integer, List<BookElement>> pages;
    private Button keepOpenButton;
    protected PageButton forwardButton;
    protected PageButton backButton;
    protected Button bookmarkButton;
    protected final boolean playTurnSound;
    private List<Bookmark> bookmarks;
    protected Button addPageButton;
    protected Button deletePageButton;

    public AdvancedBookScreen(ItemStack itemstack) {
        super(itemstack);
        this.playTurnSound = true;

    }

    @Override
    protected void init() {
        this.graphicsWidth = 132;
        this.graphicsHeight = 165;
        this.graphicsX = (this.width - this.graphicsWidth) / 2;
        this.graphicsY = 8;
        this.guiScale = 1;
        this.pagesCount = Math.max(1, this.itemstack.getOrCreateTag().getList("pages", ListTag.TAG_LIST).size());
        this.bookmarks = new ArrayList<>();
        this.pages = new HashMap<>();
        if (!this.itemstack.getOrCreateTag().contains("pages")) {
            ListTag tag = new ListTag();
            tag.add(new ListTag());
            this.itemstack.getOrCreateTag().put("pages", tag);
        }
        this.currentPage = Math.min(this.itemstack.getOrCreateTag().getInt("openedPage"), this.pagesCount - 1);
        this.createPageControlButtons();
        this.bookmarkButton = this.addRenderableWidget(new ImageButton(this.graphicsX + this.graphicsWidth / this.guiScale + 26, 8, 20, 20, 476, 182, 20, BOOK_LOCATION, 512, 256, (idk) -> {
            this.switchBookmark(this.currentPage, this.bookmarks.stream().max(Comparator.comparingInt(x -> x.position)).map(x -> x.position).orElse(-1) + 1, Color.HSBtoRGB(this.hueSlider.getValue(), this.sbSliders.getSaturation(), this.sbSliders.getBrightness()));
        }));
        super.init();
        this.loadBookmarks();
        this.updatePageAddButtons();
        this.bookmarkButton.visible = !signed;
        this.addPageButton.visible = !signed;
        this.deletePageButton.visible = !signed;
    }

    protected void loadInitialPage() {
        this.loadPage(this.currentPage);
        this.updateOpenedPage(this.currentPage);
    }

    private void loadBookmarks() {
        for (Tag bookmarkTag : this.itemstack.getOrCreateTag().getList("bookmarks", Tag.TAG_COMPOUND)) {
            CompoundTag bookmarkCompound = (CompoundTag) bookmarkTag;
            if (bookmarkCompound.getInt("page") >= this.pagesCount) {
                this.removeBookmark(bookmarkCompound.getInt("page"));
                continue;
            }
            this.bookmarks.add(new Bookmark(bookmarkCompound.getInt("page"), bookmarkCompound.getInt("position"), bookmarkCompound.getInt("color")));
        }
    }

    @Override
    protected void specialColorChange() {
        this.bookmarks.get(-2 - selectedElement).color = (Color.HSBtoRGB(this.hueSlider.getValue(), this.sbSliders.getSaturation(), this.sbSliders.getBrightness()));
    }

    private void addBookmark(int page, int position, int color) {
        this.bookmarks.add(new Bookmark(page, position, color));
    }

    private void switchBookmark(int page, int position, int color) {
        if (this.bookmarks.stream().anyMatch(x -> x.page == page)) {
            this.changeSelectedElement(-1);
            this.removeBookmark(page);
        } else {
            this.addBookmark(page, position, color);
        }
    }

    private void removeBookmark(int page) {
        this.bookmarks.remove(this.bookmarks.stream().filter(x -> x.page == page).findFirst().orElse(null));
    }

    private void saveBookmarks() {
        ListTag bookmarksTag = new ListTag();
        for (Bookmark bookmark : this.bookmarks) {
            bookmarksTag.add(bookmark.toCompound());
        }
        this.itemstack.getOrCreateTag().put("bookmarks", bookmarksTag);
        AdvancedBook.PacketHandler.sendToServer(new UpdateBookmarksC2SPacket(this.bookmarks));
    }

    @Override
    protected void save() {
        AdvancedBook.PacketHandler.sendToServer(new EditBookC2SPacket(this.currentPage, this.getCurrentPage()));
        this.saveBookmarks();
    }

    @Override
    protected void createMenuControls() {
        super.createMenuControls();
        this.doneButton = this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, (p_289629_) -> {
            this.save();
            this.closeBook();
            this.onClose();
        }).bounds(this.width / 2 - 100, 196, 98, 20).build());
        this.signButton = this.addRenderableWidget(Button.builder(Component.translatable("book.signButton"), (p_289629_) -> {
            this.updateSigning(true);
        }).bounds(this.width / 2 - 100, 221, 200, 20).build());
        this.keepOpenButton = this.addRenderableWidget(Button.builder(Component.translatable("gui.advancedbook.keepOpen"), (p_289629_) -> {
            this.save();
            this.onClose();
        }).bounds(this.width / 2 + 2, 196, 98, 20).build());
        this.addPageButton = this.addRenderableWidget(Button.builder(net.minecraft.network.chat.Component.translatable("gui.advancedbook.addPage"), (idk) -> {
            this.itemstack.getOrCreateTag().getList("pages", ListTag.TAG_LIST).add(this.currentPage + 1, new ListTag());
            this.pages.put(this.currentPage + 1, new ArrayList<>());
            this.pagesCount += 1;
            AdvancedBook.PacketHandler.sendToServer(new EditPagesBookC2SPacket(this.currentPage + 1, true));
            this.loadPage(this.currentPage + 1);
            this.reindexBookmarks(this.currentPage + 1, true);
            this.saveBookmarks();
            this.updatePageAddButtons();
            this.updateButtonVisibility();
        }).bounds(this.graphicsX - 105, 58, 70, 20).build());
        this.deletePageButton = this.addRenderableWidget(Button.builder(Component.translatable("gui.advancedbook.deletePage"), (idk) -> {
            this.itemstack.getOrCreateTag().getList("pages", ListTag.TAG_LIST).remove(this.currentPage);
            this.pages.remove(this.currentPage);
            this.pagesCount -= 1;
            AdvancedBook.PacketHandler.sendToServer(new EditPagesBookC2SPacket(this.currentPage, false));
            this.currentPage = Math.min(this.currentPage, this.pagesCount - 1);
            this.loadPage(this.currentPage);
            this.reindexBookmarks(this.currentPage + 1, false);
            this.saveBookmarks();
            this.updatePageAddButtons();
            this.updateButtonVisibility();
        }).bounds(this.graphicsX - 105, 83, 70, 20).build());
    }

    protected void createPageControlButtons() {
        int i = (this.width - 192) / 2;
        this.forwardButton = this.addRenderableWidget(new AdvancedPageButton(i + 116, 159, true, (p_98297_) -> {
            this.pageForward();
        }, this.playTurnSound));
        this.backButton = this.addRenderableWidget(new AdvancedPageButton(i + 43, 159, false, (p_98287_) -> {
            this.pageBack();
        }, this.playTurnSound));
        this.updateButtonVisibility();
    }

    private void reindexBookmarks(int fromPage, boolean action) {
        for (int i = 0; i < this.bookmarks.size(); i++) {
            Bookmark bookmark = this.bookmarks.get(i);
            if (bookmark.page == fromPage && !action) {
                this.bookmarks.remove(i);
                continue;
            }
            if (bookmark.page >= fromPage) {
                bookmark.page += action ? 1 : -1;
            }
        }
    }

    private void updatePageAddButtons() {
        this.addPageButton.active = this.pagesCount < 256;
        this.deletePageButton.active = this.pagesCount > 1;
    }

    @Override
    protected void updateSigning(boolean isSigning) {
        super.updateSigning(isSigning);
        this.keepOpenButton.visible = !isSigning;
        this.addPageButton.visible = !isSigning;
        this.deletePageButton.visible = !isSigning;
        this.bookmarkButton.visible = !isSigning;
    }

    @Override
    protected List<BookElement> getCurrentPage() {
        return this.pages.get(this.currentPage);
    }

    @Override
    protected BookElement getCurrentElement() {
        return this.getCurrentPage().get(this.selectedElement);
    }

    protected void loadPage(int index) {
        if (!this.itemstack.hasTag()) {
            return;
        }
        if (this.pages.containsKey(index)) {
            return;
        }
        this.pages.put(index, new ArrayList<>());

        ListTag tag = this.itemstack.getTag().getList("pages", ListTag.TAG_LIST);
        ListTag pageTag = (ListTag) tag.get(index);
        for (Tag elementTag : pageTag) {
            this.pages.get(index).add(switch (((CompoundTag) elementTag).getByte("type")) {
                case 1 -> new RectangleElement((CompoundTag) elementTag);
                case 2 -> new StringElement((CompoundTag) elementTag);
                case 3 -> new ItemElement((CompoundTag) elementTag);
                default ->
                        throw new IllegalStateException("Unexpected value: " + ((CompoundTag) elementTag).getByte("type"));
            });
        }
        this.changeSelectedElement(-1);
        this.updateButtonVisibility();
    }

    protected void updatePage() {
        CompoundTag tag = this.itemstack.getOrCreateTag();
        ListTag pagesTag = tag.getList("pages", ListTag.TAG_LIST);
        ListTag pageTag = new ListTag();
        for (BookElement element : this.getCurrentPage()) {
            pageTag.add(element.toCompound());
        }
        pagesTag.set(this.currentPage, pageTag);
        tag.put("pages", pagesTag);
    }

    protected void updateOpenedPage(int index) {
        CompoundTag tag = this.itemstack.getOrCreateTag();
        tag.putInt("openedPage", index);
        AdvancedBook.PacketHandler.sendToServer(new ChangePageBookC2SPacket(this.currentPage));
        this.changeSelectedElement(-1);
        this.updateButtonVisibility();
    }
    @Override
    protected void closeBook() {
        CompoundTag tag = this.itemstack.getOrCreateTag();
        tag.remove("openedPage");
        AdvancedBook.PacketHandler.sendToServer(new ChangePageBookC2SPacket(-1));
    }

    protected void pageBack() {
        if (!this.signed)
            AdvancedBook.PacketHandler.sendToServer(new EditBookC2SPacket(this.currentPage, this.getCurrentPage()));
        updatePage();
        if (this.currentPage > 0) {
            --this.currentPage;
        }
        loadPage(this.currentPage);
        updateOpenedPage(this.currentPage);
    }

    protected void pageForward() {
        if (!this.signed)
            AdvancedBook.PacketHandler.sendToServer(new EditBookC2SPacket(this.currentPage, this.getCurrentPage()));
        updatePage();
        if (this.currentPage < this.pagesCount - 1) {
            ++this.currentPage;
        }
        loadPage(this.currentPage);
        updateOpenedPage(this.currentPage);
    }

    protected void updateButtonVisibility() {
        this.forwardButton.visible = this.currentPage < this.pagesCount - 1;
        this.backButton.visible = this.currentPage > 0;
    }

    @Override
    public void resize(Minecraft minecraft, int width, int height) {
        super.resize(minecraft, width, height);
        this.graphicsX = (width - this.graphicsWidth) / 2;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        guiGraphics.pose().pushPose();
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(this.graphicsX, this.graphicsY, 0);
        guiGraphics.blit(BOOK_LOCATION, -7, -8, 0, 0, 146, 182, 512, 256);

        GL11.glEnable(GL11.GL_STENCIL_TEST);
        Minecraft.getInstance().getMainRenderTarget().enableStencil();
        RenderSystem.stencilFunc(GL11.GL_ALWAYS, 1, 0xFF);
        RenderSystem.stencilMask(0xFF);
        RenderSystem.clearStencil(0);
        GL11.glClear(GL11.GL_STENCIL_BUFFER_BIT);
        RenderSystem.stencilOp(GL11.GL_REPLACE, GL11.GL_REPLACE, GL11.GL_REPLACE);
        // Draw stencil

        RenderSystem.colorMask(false, false, false, false);
        guiGraphics.blit(BOOK_LOCATION, 0, 0, 148, 1, 132, 165, 512, 256);
        RenderSystem.colorMask(true, true, true, true);

        RenderSystem.stencilFunc(GL11.GL_NOTEQUAL, 0, 0xFF);
        RenderSystem.stencilMask(0x00);
        // Draw elements cropped to stencil

        if (!isSigning) {
            renderBookElements(guiGraphics);
        }

        GL11.glDisable(GL11.GL_STENCIL_TEST);
        guiGraphics.pose().translate(0, 0, 100);

        // Draw overlay texture
        RenderSystem.enableBlend();
        guiGraphics.blit(BOOK_LOCATION, 0, 0, 148, 1, 132, 165, 512, 256);
        this.afterBookElementsRender(guiGraphics);

        this.renderSelected(guiGraphics);
        RenderSystem.disableBlend();
        guiGraphics.pose().popPose();

        if (this.isSigning) {
            renderSigning(guiGraphics);
        }

        guiGraphics.pose().translate(0, 0, 50 * this.getCurrentPage().size() + 200);
        if (this.isAddElementsChanged) {
            this.isAddElementsChanged = false;
            changeAddElementsVisibility(this.isAddElementsOpened);
        }
        super.renderWidgets(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.pose().translate(0, 0, 100);
        this.renderItemSearch(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.pose().popPose();
    }

    protected void renderBookmark(GuiGraphics guiGraphics, int y, float red, float green, float blue, boolean isSelected) {
        guiGraphics.setColor(red * 0.7F + 0.2F, green * 0.7F + 0.2F, blue * 0.7F + 0.2F, 1.0F);
        guiGraphics.blit(BOOK_LOCATION, this.graphicsWidth / this.guiScale - 13, y * 15, 460, isSelected ? 0 : 16, 34, 16, 512, 256);
        guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    @Override
    protected void renderSelectedSpecial(GuiGraphics guiGraphics) {
        guiGraphics.blitNineSlicedSized(BOOK_LOCATION, this.graphicsWidth / this.guiScale - 5, this.bookmarks.get(-2 - selectedElement).position * 15, 27, 15, 8, 8, 32, 32, 0, 218, 512, 256);
    }

    @Override
    protected void afterBookElementsRender(GuiGraphics guiGraphics) {
        GL11.glEnable(GL11.GL_STENCIL_TEST);

        RenderSystem.stencilFunc(GL11.GL_ALWAYS, 1, 0xFF);
        RenderSystem.stencilMask(0xFF);
        RenderSystem.clearStencil(0);
        GL11.glClear(GL11.GL_STENCIL_BUFFER_BIT);
        GL11.glColorMask(false, false, false, false);
        guiGraphics.blit(BOOK_LOCATION, this.graphicsWidth / this.guiScale - 13, 0, 281, 1, 12, 165, 512, 256);
        RenderSystem.stencilFunc(GL11.GL_EQUAL, 0, 0xFF);
        RenderSystem.stencilMask(0x00);
        GL11.glColorMask(true, true, true, true);
        Bookmark currentBookmark = null;
        for (Bookmark bookmark : this.bookmarks) {
            if (bookmark.page == this.currentPage && !this.isSigning) {
                currentBookmark = bookmark;
            } else {
                this.renderBookmark(guiGraphics, bookmark.position, FastColor.ARGB32.red(bookmark.color) / 255.0F, FastColor.ARGB32.green(bookmark.color) / 255.0F, FastColor.ARGB32.blue(bookmark.color) / 255.0F, false);
            }
        }
        GL11.glDisable(GL11.GL_STENCIL_TEST);
        if (currentBookmark != null) {
            this.renderBookmark(guiGraphics, currentBookmark.position, FastColor.ARGB32.red(currentBookmark.color) / 255.0F, FastColor.ARGB32.green(currentBookmark.color) / 255.0F, FastColor.ARGB32.blue(currentBookmark.color) / 255.0F, true);
        }
    }

    @Override
    protected boolean mouseReleaseSpecial(double mouseX, double mouseY, int buttons) { // TODO: fix that horrible thing
        if (this.width / 2 + 62 <= mouseX && this.width / 2 + 58 + 27 > mouseX && !this.isSigning) {
            int j = 0;
            for (Bookmark bookmark : bookmarks) {
                if (bookmark.position * 15 + 8 <= mouseY && bookmark.position * 15 + 8 + 15 > mouseY && -2 - j != selectedElement) {
                    this.setFocused(null);
                    this.currentPage = bookmark.page;
                    loadPage(bookmark.page);
                    updateOpenedPage(bookmark.page);
                    if (!this.signed) {
                        this.updatePageAddButtons();
                        changeSelectedElement(-2 - j);
                        int color = bookmark.color;
                        float[] hsb = Color.RGBtoHSB((color >> 16) & 0xff, (color >> 8) & 0xff, color & 0xff, null);
                        this.hueSlider.setValue(1 - hsb[0]);
                        this.sbSliders.setSaturation(hsb[1]);
                        this.sbSliders.setBrightness(1 - hsb[2]);
                        this.sbSliders.setColor(Color.HSBtoRGB(hsb[0], 1, 1));
                    }
                    return true;
                }
                j++;
            }
        }
        return false;
    }

    @Override
    protected boolean mouseDraggedSpecial(double mouseX, double mouseY, int buttons) {
        if (selectedElement < -1 && this.width / 2 + 50 <= mouseX && this.width / 2 + 85 > mouseX) {
            int position = Math.max(0, Math.min((int) ((mouseY - 8) / 15), 10));
            if (this.bookmarks.stream().noneMatch(x -> x.position == position)) {
                this.bookmarks.get(-2 - this.selectedElement).position = position;
            }
            return true;
        }
        return false;
    }
}
