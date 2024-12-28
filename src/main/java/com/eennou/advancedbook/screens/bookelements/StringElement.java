package com.eennou.advancedbook.screens.bookelements;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class StringElement extends BookElement implements ColorableBookElement {
    protected int color;
    protected Font font;
    protected Component text;
    protected float hAlign;
    protected float vAlign;

    public StringElement(int x, int y, int width, int height, int color, Component text) {
        super(x, y, width, height);
        this.color = color;
        this.font = Minecraft.getInstance().font;
        this.text = text;
        this.hAlign = 0;
        this.vAlign = 0;
    }
    public StringElement(int x, int y, int width, int height, float hAlign, float vAlign, int color, Component text) {
        this(x, y, width, height, color, text);
        this.hAlign = hAlign;
        this.vAlign = vAlign;
    }
    public StringElement(FriendlyByteBuf buf) {
        this(buf.readInt(), buf.readInt(), buf.readInt(), buf.readInt(), buf.readFloat(), buf.readFloat(), buf.readInt(), Component.literal(buf.readUtf()));
    }
    public StringElement(CompoundTag tag) {
        super(tag);
        this.hAlign = tag.getFloat("hAlign");
        this.vAlign = tag.getFloat("vAlign");
        this.color = tag.getInt("color");
        this.font = Minecraft.getInstance().font;
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

    public void setHAlign(float hAlign) {
        this.hAlign = hAlign;
    }

    public void setVAlign(float vAlign) {
        this.vAlign = vAlign;
    }

    public float getHAlign() {
        return this.hAlign;
    }

    public float getVAlign() {
        return this.vAlign;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int xOffset, int yOffset) {
        List<FormattedCharSequence> lines = this.font.split(this.text, this.width + 1);
        int maxWidth = lines.stream().map((x) -> this.font.width(x)).max(Comparator.naturalOrder()).orElse(10);
        for (FormattedCharSequence line : lines) {
            guiGraphics.drawString(this.font, line, (int)(x + xOffset + (this.width - this.font.width(line)) * this.hAlign), (int)(y + yOffset + (this.height - this.font.lineHeight * lines.size()) * this.vAlign), color, false);
            yOffset += this.font.lineHeight;
        }
    }
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeByte(2);
        super.toBytes(buf);
        buf.writeFloat(this.hAlign);
        buf.writeFloat(this.vAlign);
        buf.writeInt(this.color);
        buf.writeUtf(this.text.getString());
    }
    public CompoundTag toCompound() {
        CompoundTag tag = super.toCompound();
        tag.putByte("type", (byte) 2);
        tag.putFloat("hAlign", this.hAlign);
        tag.putFloat("vAlign", this.vAlign);
        tag.putInt("color", this.color);
        tag.putString("text", text.getString());
        return tag;
    }
}
