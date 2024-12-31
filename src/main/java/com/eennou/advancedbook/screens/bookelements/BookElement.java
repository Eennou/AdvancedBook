package com.eennou.advancedbook.screens.bookelements;

import com.eennou.advancedbook.screens.AdvancedBookScreen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public abstract class BookElement implements Cloneable {
    public int x;
    public int y;
    public int width;
    public int height;
    public BookElement(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }
    public BookElement(CompoundTag tag) {
        this.x = tag.getInt("x");
        this.y = tag.getInt("y");
        this.width = tag.getInt("width");
        this.height = tag.getInt("height");
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(this.x);
        buf.writeInt(this.y);
        buf.writeInt(this.width);
        buf.writeInt(this.height);
    }
    public CompoundTag toCompound() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("x", this.x);
        tag.putInt("y", this.y);
        tag.putInt("width", this.width);
        tag.putInt("height", this.height);
        return tag;
    }
    @OnlyIn(Dist.CLIENT)
    public abstract void render(GuiGraphics guiGraphics, int xOffset, int yOffset);
    @OnlyIn(Dist.CLIENT)
    public void renderSelection(GuiGraphics guiGraphics, int xOffset, int yOffset) {
        guiGraphics.blit(AdvancedBookScreen.BOOK_LOCATION, x + xOffset - 23, y + yOffset + height + 2, 56, 242, 21, 14, 512, 256);
        guiGraphics.blitNineSlicedSized(AdvancedBookScreen.BOOK_LOCATION, x + xOffset - 1, y + yOffset - 1, width + 2, height + 2, 8, 8, 32, 32, 0, 218, 512, 256);
    }

    public boolean isIntersected(double mouseX, double mouseY, int xOffset, int yOffset) {
        return x + xOffset - 2 <= mouseX && y + yOffset - 2 <= mouseY && x + xOffset + width + 2 > mouseX && y + yOffset + height + 2 > mouseY;
    }
    public int getIntersectedCorner(double mouseX, double mouseY, int xOffset, int yOffset) { // TODO: fix that shit
        if (x + xOffset - 4 <= mouseX && y + yOffset - 4 <= mouseY && x + xOffset + 8 > mouseX && y + yOffset + 8 > mouseY) {
            return 0;
        } else if (x + width - 8 + xOffset <= mouseX && y + yOffset - 4 <= mouseY && x + xOffset + width + 4 > mouseX && y + yOffset + 8 > mouseY) {
            return 1;
        } else if (x + xOffset - 4 <= mouseX && y + height - 8 + yOffset <= mouseY && x + xOffset + 8 > mouseX && y + yOffset + height + 4 > mouseY) {
            return 2;
        } else if (x + width - 8 + xOffset <= mouseX && y + height - 8 + yOffset <= mouseY && x + xOffset + width + 4 > mouseX && y + yOffset + height + 4 > mouseY) {
            return 3;
        }
        return -1;
    }

    @Override
    public BookElement clone() {
        try {
            return (BookElement) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
