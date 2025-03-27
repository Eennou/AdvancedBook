package com.eennou.advancedbook.screens;

import com.eennou.advancedbook.AdvancedBook;
import com.eennou.advancedbook.networking.*;
import com.eennou.advancedbook.screens.bookelements.*;
import com.eennou.advancedbook.screens.components.*;
import net.minecraft.client.GameNarrator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistries;

import java.awt.*;
import java.util.*;
import java.util.List;

// Sorry for that code, I promise, I can do better! But... I'm tired

@OnlyIn(Dist.CLIENT)
public abstract class AbstractGraphicsScreen extends Screen {
    public static final ResourceLocation BOOK_LOCATION = new ResourceLocation(AdvancedBook.MODID, "textures/gui/book.png");
    public static final ResourceLocation ITEM_SEARCH = new ResourceLocation(AdvancedBook.MODID,"textures/gui/item_search.png");
    protected Button copyButton;
    protected Button cutButton;
    protected Button pasteButton;
    protected Button addButton;
    protected List<ElementAddButton> addElementButtons;
    protected List<ImageButton> alignElementButtons;
    protected boolean isAddElementsOpened = false;
    protected boolean isAddElementsChanged = true;
    protected Button elementFGButton;
    protected Button elementBGButton;
    protected Button elementDeleteButton;
    protected EditBox editBoxElement;
    protected HueSlider hueSlider;
    protected SBSliders sbSliders;
    protected BookElement clipboard;
    protected int selectedElement = -1;
    protected final ItemStack itemstack;
    protected boolean signed;
    protected boolean isSigning;
    protected Button doneButton;
    protected int graphicsX;
    protected int graphicsY;
    protected int graphicsWidth;
    protected int graphicsHeight;
    protected int guiScale;
    protected Button signButton;
    private Button confirmSignButton;
    private Button cancelSignButton;
    private EditBox titleEditBox;
    private EditBox searchBox;
    private boolean showItemSearch;
    private Button openSearchButton;

    protected int offsetX = 0;
    protected int offsetY = 0;

    public AbstractGraphicsScreen(ItemStack itemstack) {
        super(GameNarrator.NO_TITLE);
        this.itemstack = itemstack;
    }

    protected void init() {
        super.init();
        this.signed = this.itemstack.getOrCreateTag().contains("author");
        this.isSigning = false;
        this.showItemSearch = false;
        this.searchResults = getItems("");
        this.createMenuControls();
        this.createElementEditControls();
        this.titleEditBox = this.addRenderableWidget(new EditBox(this.font, this.width / 2 - 50, 70, 100, 20, Component.literal("")));
        this.titleEditBox.setMaxLength(32);
        this.titleEditBox.setBordered(false);
        this.titleEditBox.setTextColor(0x000000);
        this.searchBox = this.addWidget(new EditBox(this.font, this.editBoxElement.getX() - 195 - 5 + 82, this.editBoxElement.getY() - 66 + 10 + 6, 80, 9, Component.translatable("itemGroup.search")));
        this.searchBox.setBordered(false);
        this.searchBox.setResponder((search) -> this.searchResults = getItems(search));
        this.loadInitialPage();

        changeAddElementsVisibility(false);
        this.updateSigning(false);
        this.copyButton.visible = !signed;
        this.cutButton.visible = !signed;
        this.pasteButton.visible = !signed;
        this.addButton.visible = !signed;
        this.signButton.visible = !signed;
    }

    protected void loadInitialPage() {
        this.loadPage(0);
    }

    private void createElementEditControls() {
        int leftBound = Math.min(this.graphicsX + this.graphicsWidth / this.guiScale + 16, this.width - 152);
        this.hueSlider = this.addRenderableWidget(new HueSlider(leftBound + 78, 33, 0, (slider) -> {
            this.sbSliders.setColor(Color.HSBtoRGB(slider.getValue(), 1, 1));
            if (selectedElement > -1) {
                BookElement element = this.getCurrentElement();
                if (element instanceof ColorableBookElement) {
                    ((ColorableBookElement) element).setColor(Color.HSBtoRGB(slider.getValue(), this.sbSliders.getSaturation(), this.sbSliders.getBrightness()));
                }
            } else if (selectedElement < -1) {
                specialColorChange();
            }
        }));
        this.sbSliders = this.addRenderableWidget(new SBSliders(leftBound, 33, 0, 0, (sliders) -> {
            if (selectedElement > -1) {
                BookElement element = this.getCurrentElement();
                if (element instanceof ColorableBookElement) {
                    ((ColorableBookElement) element).setColor(Color.HSBtoRGB(this.hueSlider.getValue(), sliders.getSaturation(), sliders.getBrightness()));
                }
            } else if (selectedElement < -1) {
                specialColorChange();
            }
        }));
        this.elementFGButton = this.addRenderableWidget(new ImageButton(leftBound + 25, 8, 20, 20, 296, 182, 20, BOOK_LOCATION, 512, 256, (idk) -> {
            if (this.selectedElement > -1 && this.selectedElement < getCurrentPage().size() - 1) {
                Collections.swap(getCurrentPage(), this.selectedElement, this.selectedElement + 1);
                changeSelectedElement(this.selectedElement + 1);
            }
        }));
        this.elementFGButton.setTooltip(Tooltip.create(Component.translatable("gui.advancedbook.moveToFG")));
        this.elementBGButton = this.addRenderableWidget(new ImageButton(leftBound + 45, 8, 20, 20, 316, 182, 20, BOOK_LOCATION, 512, 256, (idk) -> {
            if (this.selectedElement > 0) {
                Collections.swap(getCurrentPage(), this.selectedElement, this.selectedElement - 1);
                changeSelectedElement(this.selectedElement - 1);
            }
        }));
        this.elementBGButton.setTooltip(Tooltip.create(Component.translatable("gui.advancedbook.moveToBG")));
        this.elementDeleteButton = this.addRenderableWidget(new ImageButton(leftBound + 65, 8, 20, 20, 336, 182, 20, BOOK_LOCATION, 512, 256, (idk) -> {
            if (this.selectedElement > -1) {
                getCurrentPage().remove(this.selectedElement);
                changeSelectedElement(-1);
            }
        }));
        this.elementDeleteButton.setTooltip(Tooltip.create(Component.translatable("gui.advancedbook.elementDelete")));

        this.editBoxElement = this.addRenderableWidget(new EditBox(this.font, leftBound + 1, 116, 123, 18, Component.literal("Text")));
        this.editBoxElement.setMaxLength(320);
        this.openSearchButton = this.addRenderableWidget(new ImageButton(leftBound + 125, 115, 20, 20, 476, 122, 20, BOOK_LOCATION, 512, 256, (idk) -> {
            this.showItemSearch ^= true;
            updateItemSearch();
        }));

        this.alignElementButtons = new ArrayList<>();
        for (int button = 0; button < 3; button++) {
            int finalButton = button;
            this.alignElementButtons.add(this.addRenderableWidget(new ImageButton(leftBound + button * 20, 140, 20, 20, 356 + button * 20, 182, 20, BOOK_LOCATION, 512, 256, (idk) -> {
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
            this.alignElementButtons.add(this.addRenderableWidget(new ImageButton(leftBound + 65 + button * 20, 140, 20, 20, 416 + button * 20, 182, 20, BOOK_LOCATION, 512, 256, (idk) -> {
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

    protected abstract void specialColorChange();

    @Override
    public boolean shouldCloseOnEsc() {
        this.save();
        return true;
    }
    protected void save() {
        AdvancedBook.PacketHandler.sendToServer(new EditBookC2SPacket(0, this.getCurrentPage()));
        this.closeBook();
    }

    protected void createMenuControls() {
        this.confirmSignButton = this.addRenderableWidget(Button.builder(Component.translatable("book.finalizeButton"), (p_289629_) -> {
            if (this.titleEditBox.getValue().isEmpty())
                return;
            this.save();
            AdvancedBook.PacketHandler.sendToServer(new SignBookC2SPacket(this.titleEditBox.getValue()));
            this.onClose();
        }).bounds(this.width / 2 - 100, 196, 98, 20).build());
        this.cancelSignButton = this.addRenderableWidget(Button.builder(Component.translatable("gui.cancel"), (p_289629_) -> {
            this.updateSigning(false);
        }).bounds(this.width / 2 + 2, 196, 98, 20).build());
        int rightBound = Math.max(this.graphicsX, 112);
        this.copyButton = this.addRenderableWidget(new ImageButton(rightBound - 105, 8, 20, 20, 56, 182, 20, BOOK_LOCATION, 512, 256, (idk) -> {
            this.clipboard = this.getCurrentElement();
            this.pasteButton.active = true;
        }));
        this.copyButton.setTooltip(Tooltip.create(Component.translatable("gui.advancedbook.copy")));
        this.cutButton = this.addRenderableWidget(new ImageButton(rightBound - 80, 8, 20, 20, 76, 182, 20, BOOK_LOCATION, 512, 256, (idk) -> {
            this.clipboard = this.getCurrentElement();
            getCurrentPage().remove(this.selectedElement);
            changeSelectedElement(-1);
            this.pasteButton.active = true;
        }));
        this.cutButton.setTooltip(Tooltip.create(Component.translatable("gui.advancedbook.cut")));
        this.pasteButton = this.addRenderableWidget(new ImageButton(rightBound - 55, 8, 20, 20, 96, 182, 20, BOOK_LOCATION, 512, 256, (idk) -> {
            if (this.clipboard != null) {
                getCurrentPage().add(this.clipboard.clone());
                changeSelectedElement(getCurrentPage().size() - 1);
            }
        }));
        this.pasteButton.setTooltip(Tooltip.create(Component.translatable("gui.advancedbook.paste")));
        this.pasteButton.active = false;

        this.addButton = this.addRenderableWidget(new DropRightButton(this.font, Component.translatable("gui.advancedbook.add"), rightBound - 105, 33, 70, 20, 116, 182, 20, BOOK_LOCATION, 512, 256, (isOpened) -> {
            this.isAddElementsOpened = isOpened;
            this.isAddElementsChanged = true;
        }));
        int dropdownX = rightBound - 105 + 70;
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
    }

    protected void updateSigning(boolean isSigning) {
        changeSelectedElement(-1);
        this.isSigning = isSigning;
        this.confirmSignButton.visible = isSigning;
        this.cancelSignButton.visible = isSigning;
        this.titleEditBox.visible = isSigning;
        this.titleEditBox.setFocused(isSigning);
        this.doneButton.visible = !isSigning;
        this.signButton.visible = !isSigning;
        this.copyButton.visible = !isSigning;
        this.cutButton.visible = !isSigning;
        this.pasteButton.visible = !isSigning;
        this.addButton.visible = !isSigning;
    }

    protected abstract List<BookElement> getCurrentPage();
    protected abstract BookElement getCurrentElement();

    protected void changeAddElementsVisibility(boolean visible) {
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
    protected void updateItemSearch() {
        this.searchBox.setVisible(this.showItemSearch);
    }

    protected abstract void loadPage(int index);

    protected abstract void updatePage();

    protected void closeBook() {

    }

    protected void changeSelectedElement(int index) {
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

//    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
//        this.renderBackground(guiGraphics);
//        int i = (this.width - 148) / 2;
//        guiGraphics.blit(BOOK_LOCATION, i, 0, 0, 0, 148, 182, 512, 256);
//        guiGraphics.blit(BOOK_LOCATION, i, 0, 148, 0, 148, 182, 512, 256);
////        guiGraphics.enableScissor(i, 0, 148, 182);
//        GL11.glEnable(GL11.GL_STENCIL_TEST);
//        Minecraft.getInstance().getMainRenderTarget().enableStencil();
////        GL11.glAlphaFunc(GL11.GL_GREATER, 0);
//        RenderSystem.stencilFunc(GL11.GL_ALWAYS, 1, 0xFF);
//        RenderSystem.stencilMask(0xFF);
//        RenderSystem.clearStencil(0);
//        RenderSystem.stencilOp(GL11.GL_REPLACE, GL11.GL_REPLACE, GL11.GL_REPLACE);
//        GL11.glColorMask(false, false, false, false);
//        guiGraphics.blit(BOOK_LOCATION, i, 0, 148 * 2, 0, 148, 182, 512, 256);
//
//        RenderSystem.stencilFunc(GL11.GL_NOTEQUAL, 0, 0xFF);
//        RenderSystem.stencilMask(0x00);
//        GL11.glColorMask(true, true, true, true);
//        guiGraphics.pose().pushPose();
//        if (!isSigning) {
//            renderBookElements(guiGraphics);
//        }
//        guiGraphics.pose().translate(0, 0, 100);
//        GL11.glDisable(GL11.GL_STENCIL_TEST);
//        RenderSystem.enableBlend();
//        guiGraphics.blit(BOOK_LOCATION, i, 0, 148 * 2, 0, 148, 182, 512, 256);
//        afterBookElementsRender(guiGraphics, i);
//
//        renderSelected(guiGraphics);
//
//        RenderSystem.disableBlend();
//
//        if (this.isSigning) {
//            renderSigning(guiGraphics);
//        }
//
//        this.renderWidgets(guiGraphics, mouseX, mouseY, partialTick);
//        RenderSystem.defaultBlendFunc();
//        if (this.isAddElementsChanged) {
//            this.isAddElementsChanged = false;
//            changeAddElementsVisibility(this.isAddElementsOpened);
//        }
//        guiGraphics.pose().translate(0, 0, 100);
//        this.renderItemSearch(guiGraphics, mouseX, mouseY, partialTick);
//        guiGraphics.pose().popPose();
//    }


    public abstract void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick);

    protected void renderSelected(GuiGraphics guiGraphics) {
        if (selectedElement > -1) {
            this.getCurrentElement().renderSelection(guiGraphics);
        } else if (selectedElement < -1) {
            renderSelectedSpecial(guiGraphics);
        }
    }

    protected void renderWidgets(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    protected void afterBookElementsRender(GuiGraphics guiGraphics) {

    };
    protected void renderSelectedSpecial(GuiGraphics guiGraphics) {

    };

    protected void renderSigning(GuiGraphics guiGraphics) {
        Component title = Component.translatable("book.editTitle");
        guiGraphics.drawString(this.font, title, (this.width - this.font.width(title)) / 2, 50, 0xFF111111, false);
        Component text = Component.translatable("book.byAuthor", Minecraft.getInstance().player.getName().getString());
        guiGraphics.drawString(this.font, text, (this.width - this.font.width(text)) / 2, 100, 0xFF111111, false);
    }

    protected void renderBookElements(GuiGraphics guiGraphics) {
        for (BookElement element : this.getCurrentPage()) {
            guiGraphics.pose().translate(0, 0, 50);
            element.render(guiGraphics, 0, 0);
        }
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
        boolean alt = (modifiers & 0b100) == 0b100;
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
        } else if (alt) {
            switch (key) {
                case 262: // RIGHT
                    this.getCurrentElement().width += step;
                    break;
                case 263: // LEFT
                    this.getCurrentElement().width -= step;
                    break;
                case 264: // DOWN
                    this.getCurrentElement().height += step;
                    break;
                case 265: // UP
                    this.getCurrentElement().height -= step;
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
        if (mouseReleaseSpecial(mouseX, mouseY, buttons)) return true;
        if (this.signed || this.isSigning) {
            return false;
        }
        if (isDragging) {
            isDragging = false;
            return false;
        }
        boolean intersected = false;
        for (int ii = this.getCurrentPage().size() - 1; ii >= 0; ii--) {
            BookElement element = this.getCurrentPage().get(ii);
            if (element.isIntersected((mouseX - this.graphicsX) * this.guiScale, (mouseY - this.graphicsY) * this.guiScale, this.offsetX * this.guiScale, this.offsetY * this.guiScale)) {
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
        if (mouseDraggedSpecial(mouseX, mouseY, buttons)) return true;
        if (selectedElement <= -1) {
            return false;
        }
        BookElement element = this.getCurrentElement();
        if (!isDragging) {
            mouseDragOffsetX = (int) (mouseX - this.graphicsX) * this.guiScale - element.x;
            mouseDragOffsetY = (int) (mouseY - this.graphicsY) * this.guiScale - element.y;
            isDragging = true;
            this.setFocused(null);
            cornerSelected = element.getIntersectedCorner((mouseX - this.graphicsX) * this.guiScale, (mouseY - this.graphicsY) * this.guiScale, this.offsetX * this.guiScale, this.offsetY * this.guiScale);
        }
        switch (cornerSelected) {
            case 0:
                mouseX -= 2;
                mouseY -= 2;
                element.width += element.x;
                element.height += element.y;
                element.x = (int) (mouseX - this.graphicsX - this.offsetX) * this.guiScale;
                element.y = (int) (mouseY - this.graphicsY - this.offsetY) * this.guiScale;
                element.width -= element.x;
                element.height -= element.y;
                break;
            case 1:
                mouseX += 2;
                mouseY -= 2;
                element.width = (int) ((mouseX - this.graphicsX - this.offsetX) * this.guiScale - element.x);
                element.height += element.y;
                element.y = (int) (mouseY - this.graphicsY - this.offsetY) * this.guiScale;
                element.height -= element.y;
                break;
            case 2:
                mouseX -= 2;
                mouseY += 2;
                element.width += element.x;
                element.height = (int) ((mouseY - this.graphicsY - this.offsetY) * this.guiScale - element.y);
                element.x = (int) (mouseX - this.graphicsX - this.offsetX) * this.guiScale;
                element.width -= element.x;
                break;
            case 3:
                mouseX += 2;
                mouseY += 2;
                element.width = (int) ((mouseX - this.graphicsX - this.offsetX) * this.guiScale - element.x);
                element.height = (int) ((mouseY - this.graphicsY - this.offsetY) * this.guiScale - element.y);
                break;
            case -1:
                element.x = (int) (mouseX - this.graphicsX) * this.guiScale - mouseDragOffsetX;
                element.y = (int) (mouseY - this.graphicsY) * this.guiScale - mouseDragOffsetY;
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

    protected boolean mouseReleaseSpecial(double mouseX, double mouseY, int buttons) {
        return false;
    };

    protected boolean mouseDraggedSpecial(double mouseX, double mouseY, int buttons) {
        return false;
    };
}
