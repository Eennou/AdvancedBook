package com.eennou.advancedbook.screens.bookelements;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class RectangleElement extends BookElement implements ColorableBookElement {
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
