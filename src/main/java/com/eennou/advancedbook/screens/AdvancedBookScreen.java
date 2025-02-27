package com.eennou.advancedbook.screens;

import com.eennou.advancedbook.AdvancedBook;
import com.eennou.advancedbook.networking.*;
import com.eennou.advancedbook.screens.bookelements.*;
import com.eennou.advancedbook.screens.components.DropRightButton;
import com.eennou.advancedbook.screens.components.ElementAddButton;
import com.eennou.advancedbook.screens.components.HueSlider;
import com.eennou.advancedbook.screens.components.SBSliders;
import com.eennou.advancedbook.utils.Bookmark;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.GameNarrator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.PageButton;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistries;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Stream;

// Sorry for that code, I promise, I can do better! But... I'm tired

@OnlyIn(Dist.CLIENT)
public class AdvancedBookScreen extends Screen {
    public static final ResourceLocation BOOK_LOCATION = new ResourceLocation(AdvancedBook.MODID, "textures/gui/book.png");
    public static final ResourceLocation ITEM_SEARCH = new ResourceLocation(AdvancedBook.MODID,"textures/gui/item_search.png");
    protected int currentPage;
    protected int pagesCount;
    protected Map<Integer, List<BookElement>> pages;
    protected Button copyButton;
    protected Button cutButton;
    protected Button pasteButton;
    protected Button addButton;
    protected Button addPageButton;
    protected Button deletePageButton;
    protected List<ElementAddButton> addElementButtons;
    protected List<ImageButton> alignElementButtons;
    protected boolean isAddElementsOpened = false;
    protected boolean isAddElementsChanged = true;
    protected PageButton forwardButton;
    protected PageButton backButton;
    protected Button bookmarkButton;
    protected Button elementFGButton;
    protected Button elementBGButton;
    protected Button elementDeleteButton;
    protected EditBox editBoxElement;
    protected HueSlider hueSlider;
    protected SBSliders sbSliders;
    protected final boolean playTurnSound;
    protected BookElement clipboard;
    protected int selectedElement = -1;
    protected final ItemStack itemstack;
    protected boolean signed;
    protected boolean isSigning;
    private Button doneButton;
    private Button keepOpenButton;
    private Button signButton;
    private Button confirmSignButton;
    private Button cancelSignButton;
    private EditBox titleEditBox;
    private List<Bookmark> bookmarks;
    private EditBox searchBox;
    private boolean showItemSearch;
    private Button openSearchButton;

    public AdvancedBookScreen(ItemStack itemstack) {
        super(GameNarrator.NO_TITLE);
        this.itemstack = itemstack;
        this.bookmarks = new ArrayList<>();
        this.signed = this.itemstack.getOrCreateTag().contains("author");
        this.isSigning = false;
        this.playTurnSound = true;
        this.showItemSearch = false;
        this.searchResults = getItems("");
    }

    protected void init() {
        super.init();
        this.itemstack.getOrCreateTag().getAsString();
        this.createPageControlButtons();
        this.titleEditBox = this.addRenderableWidget(new EditBox(this.font, this.width / 2 - 50, 70, 100, 20, Component.literal("")));
        this.titleEditBox.setMaxLength(32);
        this.createMenuControls();
        this.createElementEditControls();
        this.searchBox = this.addWidget(new EditBox(this.font, this.editBoxElement.getX() - 195 - 5 + 82, this.editBoxElement.getY() - 66 + 10 + 6, 80, 9, Component.translatable("itemGroup.search")));
        this.searchBox.setBordered(false);
        this.searchBox.setResponder((search) -> this.searchResults = getItems(search));
        if (!this.itemstack.getOrCreateTag().contains("pages")) {
            ListTag tag = new ListTag();
            tag.add(new ListTag());
            this.itemstack.getOrCreateTag().put("pages", tag);
        }
        this.pagesCount = this.itemstack.getOrCreateTag().getList("pages", ListTag.TAG_LIST).size();
        this.pages = new HashMap<>();
        this.currentPage = Math.min(this.itemstack.getOrCreateTag().getInt("openedPage"), this.pagesCount - 1);
        loadPage(this.currentPage);
        updateOpenedPage(this.currentPage);
        loadBookmarks();
        this.updatePageAddButtons();
        changeAddElementsVisibility(false);
        this.updateSigning(false);
        this.copyButton.visible = !signed;
        this.cutButton.visible = !signed;
        this.pasteButton.visible = !signed;
        this.addButton.visible = !signed;
        this.signButton.visible = !signed;
        this.addPageButton.visible = !signed;
        this.deletePageButton.visible = !signed;
        this.bookmarkButton.visible = !signed;
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

    private void createElementEditControls() {
        int i = (this.width - 192) / 2;
        this.hueSlider = this.addRenderableWidget(new HueSlider(i + 186 + 78, 33, 0, (slider) -> {
            this.sbSliders.setColor(Color.HSBtoRGB(slider.getValue(), 1, 1));
            if (selectedElement > -1) {
                BookElement element = this.getCurrentElement();
                if (element instanceof ColorableBookElement) {
                    ((ColorableBookElement) element).setColor(Color.HSBtoRGB(slider.getValue(), this.sbSliders.getSaturation(), this.sbSliders.getBrightness()));
                }
            } else if (selectedElement < -1) {
                this.bookmarks.get(-2 - selectedElement).color = (Color.HSBtoRGB(slider.getValue(), this.sbSliders.getSaturation(), this.sbSliders.getBrightness()));
            }
        }));
        this.sbSliders = this.addRenderableWidget(new SBSliders(i + 186, 33, 0, 0, (sliders) -> {
            if (selectedElement > -1) {
                BookElement element = this.getCurrentElement();
                if (element instanceof ColorableBookElement) {
                    ((ColorableBookElement) element).setColor(Color.HSBtoRGB(this.hueSlider.getValue(), sliders.getSaturation(), sliders.getBrightness()));
                }
            } else if (selectedElement < -1) {
                this.bookmarks.get(-2 - selectedElement).color = (Color.HSBtoRGB(this.hueSlider.getValue(), sliders.getSaturation(), sliders.getBrightness()));
            }
        }));
        this.bookmarkButton = this.addRenderableWidget(new ImageButton(i + 186, 8, 20, 20, 476, 182, 20, BOOK_LOCATION, 512, 256, (idk) -> {
            this.switchBookmark(this.currentPage, this.bookmarks.stream().max(Comparator.comparingInt(x -> x.position)).map(x -> x.position).orElse(-1) + 1, Color.HSBtoRGB(this.hueSlider.getValue(), this.sbSliders.getSaturation(), this.sbSliders.getBrightness()));
        }));
        this.elementFGButton = this.addRenderableWidget(new ImageButton(i + 186 + 25, 8, 20, 20, 296, 182, 20, BOOK_LOCATION, 512, 256, (idk) -> {
            if (this.selectedElement > -1 && this.selectedElement < getCurrentPage().size() - 1) {
                Collections.swap(getCurrentPage(), this.selectedElement, this.selectedElement + 1);
                changeSelectedElement(this.selectedElement + 1);
            }
        }));
        this.elementFGButton.setTooltip(Tooltip.create(Component.translatable("gui.advancedbook.moveToFG")));
        this.elementBGButton = this.addRenderableWidget(new ImageButton(i + 186 + 45, 8, 20, 20, 316, 182, 20, BOOK_LOCATION, 512, 256, (idk) -> {
            if (this.selectedElement > 0) {
                Collections.swap(getCurrentPage(), this.selectedElement, this.selectedElement - 1);
                changeSelectedElement(this.selectedElement - 1);
            }
        }));
        this.elementBGButton.setTooltip(Tooltip.create(Component.translatable("gui.advancedbook.moveToBG")));
        this.elementDeleteButton = this.addRenderableWidget(new ImageButton(i + 186 + 65, 8, 20, 20, 336, 182, 20, BOOK_LOCATION, 512, 256, (idk) -> {
            if (this.selectedElement > -1) {
                getCurrentPage().remove(this.selectedElement);
                changeSelectedElement(-1);
            }
        }));
        this.elementDeleteButton.setTooltip(Tooltip.create(Component.translatable("gui.advancedbook.elementDelete")));

        this.editBoxElement = this.addRenderableWidget(new EditBox(this.font, i + 186 + 1, 116, 123, 18, Component.literal("Text")));
        this.editBoxElement.setMaxLength(320);
        this.openSearchButton = this.addRenderableWidget(new ImageButton(i + 186 + 125, 115, 20, 20, 492, 122, 20, BOOK_LOCATION, 512, 256, (idk) -> {
            this.showItemSearch ^= true;
            updateItemSearch();
        }));

        this.alignElementButtons = new ArrayList<>();
        for (int button = 0; button < 3; button++) {
            int finalButton = button;
            this.alignElementButtons.add(this.addRenderableWidget(new ImageButton(i + 186 + button * 20, 140, 20, 20, 356 + button * 20, 182, 20, BOOK_LOCATION, 512, 256, (idk) -> {
                if (this.selectedElement != -1) {
                    BookElement element = this.getCurrentElement();
                    if (element instanceof StringElement) {
                        ((StringElement) element).setHAlign(finalButton * 0.5F);
                    }
                }
            })));
            this.alignElementButtons.get(button).setTooltip(Tooltip.create(Component.translatable("gui.advancedbook.hAlign")));
        }
        for (int button = 0; button < 3; button++) {
            int finalButton = button;
            this.alignElementButtons.add(this.addRenderableWidget(new ImageButton(i + 186 + 65 + button * 20, 140, 20, 20, 416 + button * 20, 182, 20, BOOK_LOCATION, 512, 256, (idk) -> {
                if (this.selectedElement != -1) {
                    BookElement element = this.getCurrentElement();
                    if (element instanceof StringElement) {
                        ((StringElement) element).setVAlign(finalButton * 0.5F);
                    }
                }
            })));
            this.alignElementButtons.get(3 + button).setTooltip(Tooltip.create(Component.translatable("gui.advancedbook.vAlign")));
        }
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
    public boolean shouldCloseOnEsc() {
        AdvancedBook.PacketHandler.sendToServer(new EditBookC2SPacket(this.currentPage, this.getCurrentPage()));
        closeBook();
        saveBookmarks();
        return super.shouldCloseOnEsc();
    }

    protected void createMenuControls() {
        this.doneButton = this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, (p_289629_) -> {
            AdvancedBook.PacketHandler.sendToServer(new EditBookC2SPacket(this.currentPage, this.getCurrentPage()));
            closeBook();
            saveBookmarks();
            this.onClose();
        }).bounds(this.width / 2 - 100, 196, 98, 20).build());
        this.keepOpenButton = this.addRenderableWidget(Button.builder(Component.translatable("gui.advancedbook.keepOpen"), (p_289629_) -> {
            AdvancedBook.PacketHandler.sendToServer(new EditBookC2SPacket(this.currentPage, this.getCurrentPage()));
            updateOpenedPage(this.currentPage);
            saveBookmarks();
            this.onClose();
        }).bounds(this.width / 2 + 2, 196, 98, 20).build());
        this.signButton = this.addRenderableWidget(Button.builder(Component.translatable("book.signButton"), (p_289629_) -> {
            this.updateSigning(true);
        }).bounds(this.width / 2 - 100, 221, 200, 20).build());

        this.confirmSignButton = this.addRenderableWidget(Button.builder(Component.translatable("book.finalizeButton"), (p_289629_) -> {
            AdvancedBook.PacketHandler.sendToServer(new EditBookC2SPacket(this.currentPage, this.getCurrentPage()));
            saveBookmarks();
            AdvancedBook.PacketHandler.sendToServer(new SignBookC2SPacket(this.titleEditBox.getValue()));
            closeBook();
            this.onClose();
        }).bounds(this.width / 2 - 100, 196, 98, 20).build());
        this.cancelSignButton = this.addRenderableWidget(Button.builder(Component.translatable("gui.cancel"), (p_289629_) -> {
            this.updateSigning(false);
        }).bounds(this.width / 2 + 2, 196, 98, 20).build());

        this.copyButton = this.addRenderableWidget(new ImageButton(this.width / 2 - 170, 8, 20, 20, 56, 182, 20, BOOK_LOCATION, 512, 256, (idk) -> {
            this.clipboard = this.getCurrentElement();
            this.pasteButton.active = true;
        }));
        this.copyButton.setTooltip(Tooltip.create(Component.translatable("gui.advancedbook.copy")));
        this.cutButton = this.addRenderableWidget(new ImageButton(this.width / 2 - 145, 8, 20, 20, 76, 182, 20, BOOK_LOCATION, 512, 256, (idk) -> {
            this.clipboard = this.getCurrentElement();
            getCurrentPage().remove(this.selectedElement);
            changeSelectedElement(-1);
            this.pasteButton.active = true;
        }));
        this.cutButton.setTooltip(Tooltip.create(Component.translatable("gui.advancedbook.cut")));
        this.pasteButton = this.addRenderableWidget(new ImageButton(this.width / 2 - 120, 8, 20, 20, 96, 182, 20, BOOK_LOCATION, 512, 256, (idk) -> {
            if (this.clipboard != null) {
                getCurrentPage().add(this.clipboard.clone());
                changeSelectedElement(getCurrentPage().size() - 1);
            }
        }));
        this.pasteButton.setTooltip(Tooltip.create(Component.translatable("gui.advancedbook.paste")));
        this.pasteButton.active = false;

        this.addButton = this.addRenderableWidget(new DropRightButton(this.font, Component.translatable("gui.advancedbook.add"), this.width / 2 - 170, 33, 70, 20, 116, 182, 20, BOOK_LOCATION, 512, 256, (isOpened) -> {
            this.isAddElementsOpened = isOpened;
            this.isAddElementsChanged = true;
        }));
        int dropdownX = this.width / 2 - 160 + 60;
        int dropdownY = 33;
        this.addElementButtons = new ArrayList<>();
        this.addElementButtons.add(createElementButton(Component.translatable("gui.advancedbook.rectangleElement"), dropdownX, dropdownY, 492, 40, (idk) -> {
            getCurrentPage().add(new RectangleElement(0, 0, 100, 50, Color.HSBtoRGB(this.hueSlider.getValue(), this.sbSliders.getSaturation(), this.sbSliders.getBrightness())));
            changeSelectedElement(this.getCurrentPage().size() - 1);
        }));
        dropdownY += 20;
        this.addElementButtons.add(createElementButton(Component.translatable("gui.advancedbook.stringElement"), dropdownX, dropdownY, 492, 60, (idk) -> {
            getCurrentPage().add(new StringElement(0, 0, 60, 20, Color.HSBtoRGB(this.hueSlider.getValue(), this.sbSliders.getSaturation(), this.sbSliders.getBrightness()), Component.translatable("gui.advancedbook.defaultText")));
            changeSelectedElement(this.getCurrentPage().size() - 1);
        }));
        dropdownY += 20;
        this.addElementButtons.add(createElementButton(Component.translatable("gui.advancedbook.itemElement"), dropdownX, dropdownY, 492, 80, (idk) -> {
            getCurrentPage().add(new ItemElement(0, 0, 32, 32, "minecraft:iron_ingot"));
            changeSelectedElement(this.getCurrentPage().size() - 1);
        }));
        this.addPageButton = this.addRenderableWidget(Button.builder(Component.translatable("gui.advancedbook.addPage"), (idk) -> {
            this.itemstack.getOrCreateTag().getList("pages", ListTag.TAG_LIST).add(this.currentPage + 1, new ListTag());
            this.pages.put(this.currentPage + 1, new ArrayList<>());
            this.pagesCount += 1;
            AdvancedBook.PacketHandler.sendToServer(new EditPagesBookC2SPacket(this.currentPage + 1, true));
            this.loadPage(this.currentPage + 1);
            this.reindexBookmarks(this.currentPage + 1, true);
            this.saveBookmarks();
            this.updatePageAddButtons();
            this.updateButtonVisibility();
        }).bounds(this.width / 2 - 170, 58, 70, 20).build());
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
        }).bounds(this.width / 2 - 170, 83, 70, 20).build());
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

    private void updateSigning(boolean isSigning) {
        this.isSigning = isSigning;
        this.confirmSignButton.visible = isSigning;
        this.cancelSignButton.visible = isSigning;
        this.titleEditBox.visible = isSigning;
        this.doneButton.visible = !isSigning;
        this.keepOpenButton.visible = !isSigning;
        this.signButton.visible = !isSigning;
        this.copyButton.visible = !isSigning;
        this.cutButton.visible = !isSigning;
        this.pasteButton.visible = !isSigning;
        this.addButton.visible = !isSigning;
        this.addPageButton.visible = !isSigning;
        this.deletePageButton.visible = !isSigning;
        this.bookmarkButton.visible = !isSigning;
        changeSelectedElement(-1);
    }

    private List<BookElement> getCurrentPage() {
        return this.pages.get(this.currentPage);
    }
    private BookElement getCurrentElement() {
        return this.getCurrentPage().get(this.selectedElement);
    }

    private void changeAddElementsVisibility(boolean visible) {
        for (ElementAddButton button : this.addElementButtons) {
            button.visible = visible;
            if (!visible)
                button.setFocused(false);
        }
    }

    protected ElementAddButton createElementButton(Component text, int x, int y, int iconX, int iconY, Button.OnPress action) {
        return this.addRenderableWidget(new ElementAddButton(this.font, text, x, y, 110, 20, iconX, iconY, 20, 20, 186, 182, 20, BOOK_LOCATION, 512, 256, action));
    }

    @Override
    protected void changeFocus(ComponentPath componentPath) {
        super.changeFocus(componentPath);
        changeAddElementsVisibility(this.addElementButtons.stream().anyMatch(ElementAddButton::isFocused) || this.addButton.isFocused());
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
    private List<Item> searchResults;

    private List<Item> getItems(String search) {
        final String searchLS = search.toLowerCase().strip();
        return ForgeRegistries.ITEMS.getValues().stream().filter(
            item -> item.getDefaultInstance().getHoverName().getString().toLowerCase().contains(searchLS)
            || Objects.requireNonNull(ForgeRegistries.ITEMS.getKey(item)).toString().contains(searchLS)
        ).sorted(Comparator.comparing(a -> a.getDefaultInstance().getHoverName().getString())).sorted(Comparator.comparingInt(a -> {
            int x = a.getDefaultInstance().getHoverName().getString().toLowerCase().indexOf(searchLS);
            return (x != -1) ? x : 99;
        })).toList();
    }
    protected void renderItemSearch(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (!this.showItemSearch)
            return;
        guiGraphics.blit(ITEM_SEARCH, this.editBoxElement.getX() - 195 - 5, this.editBoxElement.getY() - 66 + 10, 0, 0, 202, 132);
        this.searchBox.render(guiGraphics, mouseX, mouseY, partialTick);
        int i = 0;
        int startX = this.editBoxElement.getX() - 195 - 5 + 9;
        int startY = this.editBoxElement.getY() - 66 + 10 + 18;
        for (Item item : this.searchResults) {
            int x = startX + i % 10 * 18;
            int y = startY + i / 10 * 18;
            guiGraphics.renderFakeItem(item.getDefaultInstance(), x, y);
            if (x <= mouseX && mouseX < x + 18 && y <= mouseY && mouseY < y + 18) {
                guiGraphics.pose().pushPose();
                guiGraphics.pose().translate(0, 0, 200);
                guiGraphics.fill(x, y, x + 18, y + 18, 0x55FFFFFF);
                guiGraphics.pose().popPose();
                guiGraphics.renderTooltip(this.font, item.getDefaultInstance().getHoverName(), mouseX, mouseY);
            }
            i++;
            if (i >= 60)
                break;
        }
    }
    private void updateItemSearch() {
        this.searchBox.setVisible(this.showItemSearch);
    }

    private void loadPage(int index) {
        if (!this.itemstack.hasTag()) {
            return;
        }
        if (this.pages.containsKey(index)) {
            return;
        } else {
            this.pages.put(index, new ArrayList<>());
        }
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
        changeSelectedElement(-1);
        this.updateButtonVisibility();
    }

    private void updateBookPage() {
        CompoundTag tag = this.itemstack.getOrCreateTag();
        ListTag pagesTag = tag.getList("pages", ListTag.TAG_LIST);
        ListTag pageTag = new ListTag();
        for (BookElement element : this.getCurrentPage()) {
            pageTag.add(element.toCompound());
        }
        pagesTag.set(this.currentPage, pageTag);
        tag.put("pages", pagesTag);
    }

    private void updateOpenedPage(int index) {
        CompoundTag tag = this.itemstack.getOrCreateTag();
        tag.putInt("openedPage", index);
        AdvancedBook.PacketHandler.sendToServer(new ChangePageBookC2SPacket(this.currentPage));
        changeSelectedElement(-1);
        this.updateButtonVisibility();
    }

    private void closeBook() {
        CompoundTag tag = this.itemstack.getOrCreateTag();
        tag.remove("openedPage");
        AdvancedBook.PacketHandler.sendToServer(new ChangePageBookC2SPacket(-1));
    }

    private int getNumPages() {
        return this.pagesCount;
    }

    protected void pageBack() {
        if (!this.signed)
            AdvancedBook.PacketHandler.sendToServer(new EditBookC2SPacket(this.currentPage, this.getCurrentPage()));
        updateBookPage();
        if (this.currentPage > 0) {
            --this.currentPage;
        }
        loadPage(this.currentPage);
        updateOpenedPage(this.currentPage);
    }

    protected void pageForward() {
        if (!this.signed)
            AdvancedBook.PacketHandler.sendToServer(new EditBookC2SPacket(this.currentPage, this.getCurrentPage()));
        updateBookPage();
        if (this.currentPage < this.getNumPages() - 1) {
            ++this.currentPage;
        }
        loadPage(this.currentPage);
        updateOpenedPage(this.currentPage);
    }

    private void updateButtonVisibility() {
        this.forwardButton.visible = this.currentPage < this.getNumPages() - 1;
        this.backButton.visible = this.currentPage > 0;
    }

    private void changeSelectedElement(int index) {
        boolean elementSelected = index > -1;
        boolean bookmarkSelected = index < -1;
        boolean colorableElement = elementSelected && this.getCurrentPage().get(index) instanceof ColorableBookElement;
        this.hueSlider.visible = (elementSelected && colorableElement) || bookmarkSelected;
        this.sbSliders.visible = (elementSelected && colorableElement) || bookmarkSelected;
        this.copyButton.active = elementSelected;
        this.cutButton.active = elementSelected;
        this.editBoxElement.visible = elementSelected;
        this.openSearchButton.visible = false;
        for (Button button : this.alignElementButtons) {
            button.visible = false;
        }
        if (elementSelected) {
            BookElement element = getCurrentPage().get(index);
            if (element instanceof StringElement) {
                this.editBoxElement.setResponder((text) -> {
                    ((StringElement) element).setText(Component.literal(text));
                });
                this.editBoxElement.setValue(((StringElement) element).getText().getString());
                for (Button button : this.alignElementButtons) {
                    button.visible = true;
                }
            } else if (element instanceof ItemElement) {
                this.editBoxElement.setResponder((text) -> {
                    ((ItemElement) element).setItem(text);
                });
                this.editBoxElement.setValue(((ItemElement) element).getItem());
                this.openSearchButton.visible = true;
            } else {
                this.editBoxElement.visible = false;
            }
        }
        this.elementFGButton.visible = elementSelected;
        this.elementBGButton.visible = elementSelected;
        this.elementDeleteButton.visible = elementSelected;
        this.elementFGButton.active = index < getCurrentPage().size() - 1;
        this.elementBGButton.active = index != 0;
        this.selectedElement = index;
    }

    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        int i = (this.width - 148) / 2;
        guiGraphics.blit(BOOK_LOCATION, i, 0, 0, 0, 148, 182, 512, 256);
        guiGraphics.blit(BOOK_LOCATION, i, 0, 148, 0, 148, 182, 512, 256);
//        guiGraphics.enableScissor(i, 0, 148, 182);
        GL11.glEnable(GL11.GL_STENCIL_TEST);
        Minecraft.getInstance().getMainRenderTarget().enableStencil();
//        GL11.glAlphaFunc(GL11.GL_GREATER, 0);
        RenderSystem.stencilFunc(GL11.GL_ALWAYS, 1, 0xFF);
        RenderSystem.stencilMask(0xFF);
        RenderSystem.clearStencil(0);
        RenderSystem.stencilOp(GL11.GL_REPLACE, GL11.GL_REPLACE, GL11.GL_REPLACE);
        GL11.glColorMask(false, false, false, false);
        guiGraphics.blit(BOOK_LOCATION, i, 0, 148 * 2, 0, 148, 182, 512, 256);

        RenderSystem.stencilFunc(GL11.GL_NOTEQUAL, 0, 0xFF);
        RenderSystem.stencilMask(0x00);
        GL11.glColorMask(true, true, true, true);
        guiGraphics.pose().pushPose();
        if (!isSigning) {
            for (BookElement element : pages.get(this.currentPage)) {
                guiGraphics.pose().translate(0, 0, 50);
                element.render(guiGraphics, i + 7, 8);
            }
        }
        guiGraphics.pose().translate(0, 0, 100);
        GL11.glDisable(GL11.GL_STENCIL_TEST);
        RenderSystem.enableBlend();
        guiGraphics.blit(BOOK_LOCATION, i, 0, 148 * 2, 0, 148, 182, 512, 256);
        RenderSystem.disableBlend();
        GL11.glEnable(GL11.GL_STENCIL_TEST);

        RenderSystem.stencilFunc(GL11.GL_ALWAYS, 1, 0xFF);
        RenderSystem.stencilMask(0xFF);
        RenderSystem.clearStencil(0);
        GL11.glClear(GL11.GL_STENCIL_BUFFER_BIT);
        GL11.glColorMask(false, false, false, false);
        guiGraphics.blit(BOOK_LOCATION, i + 125, 0, 444, 0, 12, 182, 512, 256);
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
        if (this.isSigning) {
            Component title = Component.translatable("book.editTitle");
            guiGraphics.drawString(this.font, title, (this.width - this.font.width(title)) / 2, 50, 0xFF111111, false);
            Component text = Component.translatable("book.byAuthor", Minecraft.getInstance().player.getName().getString());
            guiGraphics.drawString(this.font, text, (this.width - this.font.width(text)) / 2, 100, 0xFF111111, false);
        }
        RenderSystem.enableBlend();
//        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        if (selectedElement > -1) {
            this.getCurrentElement().renderSelection(guiGraphics, i + 7, 8);
        } else if (selectedElement < -1) {
            guiGraphics.blitNineSlicedSized(BOOK_LOCATION, this.width / 2 + 50, this.bookmarks.get(-2 - selectedElement).position * 15 + 8, 35, 15, 8, 8, 32, 32, 0, 218, 512, 256);
        }
        RenderSystem.disableBlend();
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        RenderSystem.defaultBlendFunc();
        if (this.isAddElementsChanged) {
            this.isAddElementsChanged = false;
            changeAddElementsVisibility(this.isAddElementsOpened);
        }
        guiGraphics.pose().translate(0, 0, 100);
        this.renderItemSearch(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.pose().popPose();
    }

    private void renderBookmark(GuiGraphics guiGraphics, int y, float red, float green, float blue, boolean isSelected) {
        guiGraphics.setColor(red * 0.7F + 0.2F, green * 0.7F + 0.2F, blue * 0.7F + 0.2F, 1.0F);
        guiGraphics.blit(BOOK_LOCATION, this.width / 2 + 50, 8 + y * 15, 460, isSelected ? 0 : 16, 34, 16, 512, 256);
        guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private int mouseDragOffsetX = 0;
    private int mouseDragOffsetY = 0;
    private boolean isDragging = false;
    private int cornerSelected = -1;

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int buttons) {
        if (super.mouseClicked(mouseX, mouseY, buttons)) {
            return true;
        }

        return false;
    }

    @Override
    public boolean keyPressed(int key, int p_96553_, int modifiers) {
        if (super.keyPressed(key, p_96553_, modifiers)) {
            return true;
        }
        if (this.selectedElement <= -1) {
            return false;
        }
        AdvancedBook.LOGGER.debug("Key: {}|{}|{}", key, p_96553_, modifiers);
        boolean ctrl = (modifiers & 0b10) == 0b10;
        boolean shift = (modifiers & 0b1) == 0b1;
        int step = ctrl ? 5 : 1;
        if (shift) {
            switch (key) {
                case 262: // RIGHT
                    this.getCurrentElement().x = getSnap(1) - this.getCurrentElement().width;
                    break;
                case 263: // LEFT
                    this.getCurrentElement().x = getSnap(3);
                    break;
                case 264: // DOWN
                    this.getCurrentElement().y = getSnap(2) - this.getCurrentElement().height;
                    break;
                case 265: // UP
                    this.getCurrentElement().y = getSnap(0);
                    break;
            }
        } else {
            switch (key) {
                case 262: // RIGHT
                    this.getCurrentElement().x += step;
                    break;
                case 263: // LEFT
                    this.getCurrentElement().x -= step;
                    break;
                case 264: // DOWN
                    this.getCurrentElement().y += step;
                    break;
                case 265: // UP
                    this.getCurrentElement().y -= step;
                    break;
            }
        }
        if (262 <= key && key <= 265) {
            this.setFocused(null);
        }
        return true;
    }

    private int getSnap(int direction) {
        int idk = switch (direction) {
            case 0 -> // UP
                this.getCurrentElement().y;
            case 1 -> // RIGHT
                this.getCurrentElement().x + this.getCurrentElement().width;
            case 2 -> // DOWN
                this.getCurrentElement().y + this.getCurrentElement().height;
            case 3 -> // LEFT
                this.getCurrentElement().x;
            default -> throw new IllegalStateException("Unexpected direction: " + direction);
        };
        int resultSnap = switch (direction) {
            case 1 -> 132;
            case 2 -> 165;
            default -> 0;
        };
        for (BookElement element : this.getCurrentPage()) {
            resultSnap = switch (direction) {
                case 0 -> { // SNAPPING UP NEAREST
                    int bottomPos = element.y + element.height;
                    yield (idk > bottomPos && resultSnap < bottomPos) ? bottomPos : resultSnap;
                }
                case 1 -> { // SNAPPING RIGHT NEAREST
                    int leftPos = element.x;
                    yield (idk < leftPos && resultSnap > leftPos) ? leftPos : resultSnap;
                }
                case 2 -> { // SNAPPING DOWN NEAREST
                    int upPos = element.y;
                    yield (idk < upPos && resultSnap > upPos) ? upPos : resultSnap;
                }
                case 3 -> { // SNAPPING LEFT NEAREST
                    int rightPos = element.x + element.width;
                    yield (idk > rightPos && resultSnap < rightPos) ? rightPos : resultSnap;
                }
                default -> 0;
            };
        }
        return resultSnap;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int buttons) {
        if (this.showItemSearch) {
            if (this.openSearchButton.isHovered()) {
                return true;
            }
            int startX = this.editBoxElement.getX() - 195 - 5;
            int startY = this.editBoxElement.getY() - 66 + 10;
            if (!(startX <= mouseX && mouseX < startX + 195 && startY <= mouseY && mouseY < startY + 132)) {
                this.showItemSearch = false;
            } else {
                int gridStartX = this.editBoxElement.getX() - 195 - 5 + 9;
                int gridStartY = this.editBoxElement.getY() - 66 + 10 + 18;
                int selectedX = ((int)mouseX - gridStartX) / 18;
                int selectedY = ((int)mouseY - gridStartY) / 18;
                if (gridStartX <= mouseX && selectedX < 10 && gridStartY <= mouseY && selectedY < 6) {
                    this.editBoxElement.setValue(Objects.requireNonNull(ForgeRegistries.ITEMS.getKey(this.searchResults.get(selectedY * 10 + selectedX))).toString());
                    this.showItemSearch = false;
                }
            }
            return true;
        }
        if (super.mouseReleased(mouseX, mouseY, buttons)) {
            return true;
        }
        if (this.width / 2 + 50 <= mouseX && this.width / 2 + 50 + 35 > mouseX && !this.isSigning) {
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
        if (this.signed || this.isSigning) {
            return false;
        }
        if (isDragging) {
            isDragging = false;
            return false;
        }
        int i = (this.width - 148) / 2;
        boolean intersected = false;
        for (int ii = pages.get(this.currentPage).size() - 1; ii >= 0; ii--) {
            BookElement element = pages.get(this.currentPage).get(ii);
            if (element.isIntersected(mouseX, mouseY, i + 7, 8)) {
                if (ii == this.selectedElement) {
                    intersected = true;
                    continue;
                }
                changeSelectedElement(ii);
                this.setFocused(null);
                if (element instanceof ColorableBookElement) {
                    int color = ((ColorableBookElement) element).getColor();
                    float[] hsb = Color.RGBtoHSB((color >> 16) & 0xff, (color >> 8) & 0xff, color & 0xff, null);
                    this.hueSlider.setValue(1 - hsb[0]);
                    this.sbSliders.setSaturation(hsb[1]);
                    this.sbSliders.setBrightness(1 - hsb[2]);
                    this.sbSliders.setColor(Color.HSBtoRGB(hsb[0], 1, 1));
                }
                return true;
            }
        }
        if (intersected) {
            return true;
        }
        if (!this.isFocused()) {
            changeSelectedElement(-1);
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int buttons, double p_94702_, double p_94703_) {
        if (super.mouseDragged(mouseX, mouseY, buttons, p_94702_, p_94703_)) {
            return true;
        }
        if (this.signed || this.isSigning) {
            return false;
        }
        int i = (this.width - 148) / 2;
        if (selectedElement < -1 && this.width / 2 + 50 <= mouseX && this.width / 2 + 85 > mouseX) {
            int position = Math.max(0, Math.min((int) ((mouseY - 8) / 15), 10));
            if (this.bookmarks.stream().noneMatch(x -> x.position == position)) {
                this.bookmarks.get(-2 - this.selectedElement).position = position;
            }
            return true;
        }
        if (selectedElement <= -1) {
            return false;
        }
        BookElement element = this.getCurrentElement();
        int xOffset = i + 7;
        int yOffset = 8;
        if (!isDragging) {
            mouseDragOffsetX = (int) mouseX - xOffset - element.x;
            mouseDragOffsetY = (int) mouseY - yOffset - element.y;
            isDragging = true;
            this.setFocused(null);
            cornerSelected = element.getIntersectedCorner(mouseX, mouseY, i + 7, 8);
        }
        switch (cornerSelected) {
            case 0:
                mouseX -= 2;
                mouseY -= 2;
                element.width = (element.x + element.width) - ((int) mouseX - xOffset);
                element.height = (element.y + element.height) - ((int) mouseY - yOffset);
                element.x = (int) mouseX - xOffset;
                element.y = (int) mouseY - yOffset;
                break;
            case 1:
                mouseX += 2;
                mouseY -= 2;
                element.width = (int) (mouseX - xOffset - element.x);
                element.height = (element.y + element.height) - ((int) mouseY - yOffset);
                element.y = (int) mouseY - yOffset;
                break;
            case 2:
                mouseX -= 2;
                mouseY += 2;
                element.width = (element.x + element.width) - ((int) mouseX - xOffset);
                element.height = (int) (mouseY - yOffset - element.y);
                element.x = (int) mouseX - xOffset;
                break;
            case 3:
                mouseX += 2;
                mouseY += 2;
                element.width = (int) (mouseX - xOffset - element.x);
                element.height = (int) (mouseY - yOffset - element.y);
                break;
            case -1:
                element.x = (int) mouseX - xOffset - mouseDragOffsetX;
                element.y = (int) mouseY - yOffset - mouseDragOffsetY;
                break;
        }
        if (element.width < 16) {
            if (cornerSelected == 0 || cornerSelected == 2) {
                element.x += element.width - 16;
            }
            element.width = 16;
        }
        if (element.height < 16) {
            if (cornerSelected == 0 || cornerSelected == 1) {
                element.y += element.height - 16;
            }
            element.height = 16;
        }
        return false;
    }
}
