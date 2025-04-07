package com.eennou.advancedbook.screens;

import com.eennou.advancedbook.AdvancedBook;
import com.eennou.advancedbook.Config;
import com.eennou.advancedbook.networking.ChangeIllustrationSizeC2SPacket;
import com.eennou.advancedbook.screens.bookelements.BookElement;
import com.eennou.advancedbook.screens.bookelements.ItemElement;
import com.eennou.advancedbook.screens.bookelements.RectangleElement;
import com.eennou.advancedbook.screens.bookelements.StringElement;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexSorting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import com.mojang.blaze3d.pipeline.TextureTarget;
import net.minecraft.client.Screenshot;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class IllustrationScreen extends AbstractGraphicsScreen {
    public static final ResourceLocation ILLUSTRATION_LOCATION = new ResourceLocation(AdvancedBook.MODID, "textures/gui/illustration.png");
    List<BookElement> bookElements = null;
    protected short illustrationWidth;
    protected short illustrationHeight;
    protected Button widthDecButton;
    protected Button widthIncButton;
    protected Button heightDecButton;
    protected Button heightIncButton;
    protected Button zoomInButton;
    protected Button zoomOutButton;

    public IllustrationScreen(ItemStack itemstack) {
        super(itemstack);
        if (!this.itemstack.getOrCreateTag().contains("elements")) {
            ListTag tag = new ListTag();
            tag.add(new ListTag());
            this.itemstack.getOrCreateTag().put("elements", tag);
        }
        CompoundTag tag = this.itemstack.getOrCreateTag();
        this.illustrationWidth = tag.contains("width") ? tag.getShort("width") : 1;
        this.illustrationHeight = tag.contains("height") ? tag.getShort("height") : 1;
    }

    @Override
    protected void init() {
        if (this.guiScale == 0) {
            this.guiScale = (int) Minecraft.getInstance().getWindow().getGuiScale();
        }
        this.graphicsWidth = 256 * this.illustrationWidth;
        this.graphicsHeight = 256 * this.illustrationHeight;
        this.graphicsX = (this.width - this.graphicsWidth / this.guiScale) / 2;
        this.graphicsY = 16;
        int rightBound = Math.max(this.graphicsX, 112);
        this.zoomInButton = this.addRenderableWidget(new ImageButton(rightBound - 105, 165, 20, 20, 416, 122, 20, BOOK_LOCATION, 512, 256, (idk) -> {
            int scale = this.guiScale - 1;
            if (scale < 1) {
                return;
            }
            this.guiScale = scale;
            this.rebuildWidgets();
        }));
        this.zoomOutButton = this.addRenderableWidget(new ImageButton(rightBound - 80, 165, 20, 20, 396, 122, 20, BOOK_LOCATION, 512, 256, (idk) -> {
            int scale = this.guiScale + 1;
            if (scale > Minecraft.getInstance().getWindow().getGuiScale()) {
                return;
            }
            this.guiScale = scale;
            this.rebuildWidgets();
        }));
        this.zoomOutButton.active = this.guiScale < Minecraft.getInstance().getWindow().getGuiScale();
        this.zoomInButton.active = this.guiScale > 1;
        int horCentralPos = this.graphicsX + (this.graphicsWidth / this.guiScale) / 2 + this.offsetX;
        int verCentralPos = this.graphicsY + (this.graphicsHeight / this.guiScale) / 2 + this.offsetY;
        this.widthDecButton = this.addRenderableWidget(new ImageButton(horCentralPos - 4 - 16, this.graphicsY + this.offsetY - 14, 8, 13, 496, 216, 13, BOOK_LOCATION, 512, 256, (idk) -> {
            if (this.illustrationWidth <= 1) return;
            this.illustrationWidth--;
            this.rebuildWidgets();
        }));
        this.widthIncButton = this.addRenderableWidget(new ImageButton(horCentralPos - 4 + 16, this.graphicsY + this.offsetY - 14, 8, 13, 504, 216, 13, BOOK_LOCATION, 512, 256, (idk) -> {
            if (this.illustrationWidth >= Config.illustrationMaxSize) return;
            this.illustrationWidth++;
            this.rebuildWidgets();
        }));
        this.heightDecButton = this.addRenderableWidget(new ImageButton(this.graphicsX + this.offsetX - 14, verCentralPos - 4 - 16, 13, 8, 496, 184, 8, BOOK_LOCATION, 512, 256, (idk) -> {
            if (this.illustrationHeight <= 1) return;
            this.illustrationHeight--;
            this.rebuildWidgets();
        }));
        this.heightIncButton = this.addRenderableWidget(new ImageButton(this.graphicsX + this.offsetX - 14, verCentralPos - 4 + 16, 13, 8, 496, 200, 8, BOOK_LOCATION, 512, 256, (idk) -> {
            if (this.illustrationHeight >= Config.illustrationMaxSize) return;
            this.illustrationHeight++;
            this.rebuildWidgets();
        }));
        super.init();
        this.widthIncButton.visible = this.illustrationWidth < Config.illustrationMaxSize;
        this.widthDecButton.visible = this.illustrationWidth > 1;
        this.heightIncButton.visible = this.illustrationHeight < Config.illustrationMaxSize;
        this.heightDecButton.visible = this.illustrationHeight > 1;
    }

    private void updatePositions() {
        int horCentralPos = this.graphicsX + (this.graphicsWidth / this.guiScale) / 2 + this.offsetX;
        int verCentralPos = this.graphicsY + (this.graphicsHeight / this.guiScale) / 2 + this.offsetY;
        this.widthDecButton.setPosition(horCentralPos - 4 - 16, this.graphicsY + this.offsetY - 14);
        this.widthIncButton.setPosition(horCentralPos - 4 + 16, this.graphicsY + this.offsetY - 14);
        this.heightDecButton.setPosition(this.graphicsX + this.offsetX - 14, verCentralPos - 4 - 16);
        this.heightIncButton.setPosition(this.graphicsX + this.offsetX - 14, verCentralPos - 4 + 16);
    }

    @Override
    protected void createMenuControls() {
        this.doneButton = this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, (p_289629_) -> {
            this.save();
            this.onClose();
        }).bounds(this.width / 2 - 100, this.height - 60, 200, 20).build());
        this.signButton = this.addRenderableWidget(Button.builder(Component.translatable("book.signButton"), (p_289629_) -> {
            this.updateSigning(true);
        }).bounds(this.width / 2 - 100, this.height - 35, 200, 20).build());
        super.createMenuControls();
    }

    @Override
    protected void updateSigning(boolean isSigning) {
        super.updateSigning(isSigning);
        this.widthIncButton.visible = this.illustrationWidth < Config.illustrationMaxSize && !isSigning;
        this.widthDecButton.visible = this.illustrationWidth > 1 && !isSigning;
        this.heightIncButton.visible = this.illustrationHeight < Config.illustrationMaxSize && !isSigning;
        this.heightDecButton.visible = this.illustrationHeight > 1 && !isSigning;
        this.zoomInButton.visible = !isSigning;
        this.zoomOutButton.visible = !isSigning;
    }

    @Override
    protected void specialColorChange() {

    }

    @Override
    protected List<BookElement> getCurrentPage() {
        return bookElements;
    }

    @Override
    protected BookElement getCurrentElement() {
        return bookElements.get(this.selectedElement);
    }

    @Override
    protected void loadPage(int index) {
        if (!this.itemstack.hasTag()) {
            return;
        }
        if (this.bookElements != null) {
            return;
        }
        this.bookElements = new ArrayList<>();

        ListTag tag = this.itemstack.getTag().getList("elements", ListTag.TAG_COMPOUND);
        for (Tag elementTag : tag) {
            this.bookElements.add(switch (((CompoundTag) elementTag).getByte("type")) {
                case 1 -> new RectangleElement((CompoundTag) elementTag);
                case 2 -> new StringElement((CompoundTag) elementTag);
                case 3 -> new ItemElement((CompoundTag) elementTag);
                default ->
                        throw new IllegalStateException("Unexpected value: " + ((CompoundTag) elementTag).getByte("type"));
            });
        }
        this.changeSelectedElement(-1);
    }

    @Override
    protected void updatePage() {

    }

    @Override
    protected void save() {
        AdvancedBook.PacketHandler.sendToServer(new ChangeIllustrationSizeC2SPacket(this.illustrationWidth, this.illustrationHeight));
        CompoundTag tag = this.itemstack.getOrCreateTag();
        tag.putShort("width", this.illustrationWidth);
        tag.putShort("height", this.illustrationHeight);
        super.save();
    }

    @Override
    public void resize(Minecraft minecraft, int width, int height) {
        super.resize(minecraft, width, height);
//        this.guiScale = (int) minecraft.getWindow().getGuiScale();
//        this.graphicsX = (width - this.graphicsWidth / this.guiScale) / 2;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        guiGraphics.pose().pushPose();
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(this.graphicsX, this.graphicsY, 0);
        guiGraphics.pose().translate(this.offsetX, this.offsetY, 0);
        if (!isSigning) {
            guiGraphics.blitNineSlicedSized(BOOK_LOCATION, -1, -10, this.graphicsWidth / this.guiScale + 2, 10, 3, 10, 10, 81, 246, 512, 256);
            guiGraphics.blitNineSlicedSized(BOOK_LOCATION, -10, -1, 10, this.graphicsHeight / this.guiScale + 2, 3, 10, 10, 91, 246, 512, 256);
            guiGraphics.blitNineSlicedSized(BOOK_LOCATION, this.graphicsWidth / this.guiScale / 2 - 8, -14, 16, 13, 4, 9, 9, 32, 224, 512, 256);
            guiGraphics.blitNineSlicedSized(BOOK_LOCATION, -14, this.graphicsHeight / this.guiScale / 2 - 8, 13, 16, 4, 9, 9, 32, 224, 512, 256);
            guiGraphics.drawString(this.font, String.valueOf(this.illustrationWidth), this.graphicsWidth / this.guiScale / 2 - this.font.width(String.valueOf(this.illustrationWidth)) / 2, -11, 0xFFFFFF, false);
            guiGraphics.drawString(this.font, String.valueOf(this.illustrationHeight), -10, this.graphicsHeight / this.guiScale / 2 - this.font.width(String.valueOf(this.illustrationHeight)) / 2, 0xFFFFFF, false);
        }
        guiGraphics.pose().scale(1F / this.guiScale, 1F / this.guiScale, 1F / this.guiScale);

        GL11.glEnable(GL11.GL_STENCIL_TEST);
        Minecraft.getInstance().getMainRenderTarget().enableStencil();
        RenderSystem.stencilFunc(GL11.GL_ALWAYS, 1, 0xFF);
        RenderSystem.stencilMask(0xFF);
        RenderSystem.clearStencil(0);
        GL11.glClear(GL11.GL_STENCIL_BUFFER_BIT);
        RenderSystem.stencilOp(GL11.GL_REPLACE, GL11.GL_REPLACE, GL11.GL_REPLACE);
        // Draw stencil & bg

        guiGraphics.fill(0, 0, this.graphicsWidth, this.graphicsHeight, 0xFFDDDDDD);

        RenderSystem.stencilFunc(GL11.GL_NOTEQUAL, 0, 0xFF);
        RenderSystem.stencilMask(0x00);
        // Draw elements cropped to stencil

        if (!isSigning) {
            renderBookElements(guiGraphics);
        }
        guiGraphics.pose().translate(0, 0, 100);

        GL11.glDisable(GL11.GL_STENCIL_TEST);

        // Draw overlay texture
        RenderSystem.enableBlend();

        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale(4, 4, 4);
        guiGraphics.blitRepeating(ILLUSTRATION_LOCATION, 0, 0, this.graphicsWidth / 4, this.graphicsHeight / 4, 0, 0, 64, 64, 128, 128);
        guiGraphics.pose().popPose();

        this.renderSelected(guiGraphics);
        RenderSystem.disableBlend();
        guiGraphics.pose().popPose();

        if (this.isSigning) {
            renderSigning(guiGraphics);
        }
        guiGraphics.pose().translate(0, 0, 50 * this.getCurrentPage().size() + 200);

        super.renderWidgets(guiGraphics, mouseX, mouseY, partialTick);
        if (this.isAddElementsChanged) {
            this.isAddElementsChanged = false;
            changeAddElementsVisibility(this.isAddElementsOpened);
        }
        guiGraphics.pose().translate(0, 0, 100);
        this.renderItemSearch(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.pose().popPose();
    }

    private int mouseDragOffsetX = 0;
    private int mouseDragOffsetY = 0;
    private boolean isDragging = false;

    @Override
    protected boolean mouseDraggedSpecial(double mouseX, double mouseY, int buttons) {
        if (buttons != 1) return false;
        if (!isDragging) {
            isDragging = true;
            this.mouseDragOffsetX = this.offsetX - ((int)mouseX);
            this.mouseDragOffsetY = this.offsetY - ((int)mouseY);
        }
        this.offsetX = ((int)mouseX) + mouseDragOffsetX;
        this.offsetY = ((int)mouseY) + mouseDragOffsetY;
        this.updatePositions();
        return true;
    }

    @Override
    protected boolean mouseReleaseSpecial(double mouseX, double mouseY, int buttons) {
        isDragging = false;
        return false;
    }
}
