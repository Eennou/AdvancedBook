package com.eennou.advancedbook.screens.bookelements;

import com.eennou.advancedbook.AdvancedBook;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.awt.*;

public class RectangleElement extends BookElement implements ColorableBookElement {
    private static final Material WHITE = new Material(InventoryMenu.BLOCK_ATLAS,
            new ResourceLocation(AdvancedBook.MODID, "block/white"));
    protected int color;
    public RectangleElement(int x, int y, int width, int height, int color) {
        super(x, y, width, height);
        this.color = color;
    }
    public RectangleElement(FriendlyByteBuf buf) {
        this(buf.readInt(), buf.readInt(), buf.readInt(), buf.readInt(), buf.readInt());
    }
    public RectangleElement(CompoundTag tag) {
        super(tag);
        this.color = tag.getInt("color");
    }

    public void setColor(int color) {
        this.color = color;
    }
    public int getColor() {
        return this.color;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void render(GuiGraphics guiGraphics, int xOffset, int yOffset) {
        guiGraphics.fill(x + xOffset, y + yOffset, x + xOffset + width, y + yOffset + height, color);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void renderInWorld(PoseStack poseStack, MultiBufferSource bufferSource, int combinedLight, int combinedOverlay) {
        final TextureAtlasSprite tex = WHITE.sprite();
        Color dimColor = new Color(this.color);

        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.solid());
        Matrix4f matrix = poseStack.last().pose();
        Vector3f topLeft = new Vector3f(this.x, this.height + this.y, 0);
        Vector3f bottomRight = new Vector3f(this.width + this.x, this.y, 0);
        float r = dimColor.getRed() * 0.87F;
        float g = dimColor.getGreen() * 0.87F;
        float b = dimColor.getBlue() * 0.87F;
        vertexConsumer.vertex(matrix, topLeft.x(), topLeft.y(), topLeft.z())
            .color(r, g, b, 1F)
            .uv(tex.getU(0), tex.getV(0)).overlayCoords(combinedOverlay)
            .uv2(combinedLight)
            .normal(poseStack.last().normal(), 0, 0, 1)
            .endVertex();
        vertexConsumer.vertex(matrix, bottomRight.x(), topLeft.y(), topLeft.z())
            .color(r, g, b, 1F)
            .uv(tex.getU(0), tex.getV(2)).overlayCoords(combinedOverlay)
            .uv2(combinedLight)
            .normal(poseStack.last().normal(), 0, 0, 1)
            .endVertex();
        vertexConsumer.vertex(matrix, bottomRight.x(), bottomRight.y(), topLeft.z())
            .color(r, g, b, 1F)
            .uv(tex.getU(2), tex.getV(2)).overlayCoords(combinedOverlay)
            .uv2(combinedLight)
            .normal(poseStack.last().normal(), 0, 0, 1)
            .endVertex();
        vertexConsumer.vertex(matrix, topLeft.x(), bottomRight.y(), topLeft.z())
            .color(r, g, b, 1F)
            .uv(tex.getU(2), tex.getV(0)).overlayCoords(combinedOverlay)
            .uv2(combinedLight)
            .normal(poseStack.last().normal(), 0, 0, 1)
            .endVertex();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeByte(1);
        super.toBytes(buf);
        buf.writeInt(color);
    }
    public CompoundTag toCompound() {
        CompoundTag tag = super.toCompound();
        tag.putByte("type", (byte) 1);
        tag.putInt("color", this.color);
        return tag;
    }

}
