package com.eennou.advancedbook.screens.bookelements;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Comparator;
import java.util.List;

public class StringElement extends BookElement implements ColorableBookElement {
    protected int color;
    protected Component text;
    protected int scale;
    protected float hAlign;
    protected float vAlign;

    public StringElement(int x, int y, int width, int height, int color, Component text) {
        super(x, y, width, height);
        this.color = color;
        this.text = text;
        this.scale = 1;
        this.hAlign = 0;
        this.vAlign = 0;
    }
    public StringElement(int x, int y, int width, int height, int scale, float hAlign, float vAlign, int color, Component text) {
        this(x, y, width, height, color, text);
        this.scale = Math.max(1, scale);
        this.hAlign = hAlign;
        this.vAlign = vAlign;
    }
    public StringElement(FriendlyByteBuf buf) {
        this(buf.readInt(), buf.readInt(), buf.readInt(), buf.readInt(), buf.readInt(), buf.readFloat(), buf.readFloat(), buf.readInt(), Component.literal(buf.readUtf()));
    }
    public StringElement(CompoundTag tag) {
        super(tag);
        this.scale = Math.max(1, tag.getInt("scale"));
        this.hAlign = tag.getFloat("hAlign");
        this.vAlign = tag.getFloat("vAlign");
        this.color = tag.getInt("color");
        this.text = Component.literal(tag.getString("text"));
    }
    @Override
    public void setColor(int color) {
        this.color = color;
    }

    @Override
    public int getColor() {
        return this.color;
    }

    public void setText(Component text) {
        this.text = text;
    }

    public Component getText() {
        return this.text;
    }
    public void setScale(int scale) {
        this.scale = Math.max(1, scale);
    }

    public void setHAlign(float hAlign) {
        this.hAlign = hAlign;
    }

    public void setVAlign(float vAlign) {
        this.vAlign = vAlign;
    }

    public int getScale() {
        return this.scale;
    }
    public float getHAlign() {
        return this.hAlign;
    }

    public float getVAlign() {
        return this.vAlign;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void render(GuiGraphics guiGraphics, int xOffset, int yOffset) {
        Font font = Minecraft.getInstance().font;
        List<FormattedCharSequence> lines = font.split(this.text, (this.width + 1) / scale);
//        int maxWidth = lines.stream().map((x) -> font.width(x)).max(Comparator.naturalOrder()).orElse(10);
        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale(scale, scale, scale);
        for (FormattedCharSequence line : lines) {
            guiGraphics.drawString(font, line, (int)(x + xOffset + (this.width - font.width(line) * scale) * this.hAlign) / scale, (int)(y + yOffset + (this.height - font.lineHeight * lines.size() * scale) * this.vAlign) / scale, color, false);
            yOffset += font.lineHeight * scale;
        }
        guiGraphics.pose().popPose();
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void renderInWorld(PoseStack poseStack, MultiBufferSource bufferSource, int combinedLight, int combinedOverlay) {
        poseStack.pushPose();
        Font font = Minecraft.getInstance().font;
        List<FormattedCharSequence> lines = font.split(this.text, (this.width + 1) / scale);
        poseStack.scale(scale, scale, scale);
        for (FormattedCharSequence line : lines) {
            font.drawInBatch(
                line,
                ((int)(this.x + (this.width - font.width(line) * scale) * this.hAlign)) / scale,
                ((int)(this.y + (this.height - font.lineHeight * lines.size() * scale) * this.vAlign)) / scale,
                this.color, // 0xCFCFCF
                false,
                poseStack.last().pose(),
                bufferSource,
                Font.DisplayMode.NORMAL,
                0,
                combinedLight
            );
            poseStack.translate(0, font.lineHeight, 0);
        }
        poseStack.popPose();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeByte(2);
        super.toBytes(buf);
        buf.writeInt(this.scale);
        buf.writeFloat(this.hAlign);
        buf.writeFloat(this.vAlign);
        buf.writeInt(this.color);
        buf.writeUtf(this.text.getString());
    }
    public CompoundTag toCompound() {
        CompoundTag tag = super.toCompound();
        tag.putByte("type", (byte) 2);
        tag.putInt("scale", this.scale);
        tag.putFloat("hAlign", this.hAlign);
        tag.putFloat("vAlign", this.vAlign);
        tag.putInt("color", this.color);
        tag.putString("text", text.getString());
        return tag;
    }
}
