package com.eennou.advancedbook.screens.bookelements;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Objects;

public class ItemElement extends BookElement {
    protected ItemStack itemStack;
    public ItemElement(int x, int y, int width, int height, String item) {
        super(x, y, width, height);
        this.setItem(item);
    }
    public ItemElement(FriendlyByteBuf buf) {
        this(buf.readInt(), buf.readInt(), buf.readInt(), buf.readInt(), buf.readUtf());
    }
    public ItemElement(CompoundTag tag) {
        super(tag);
        this.setItem(tag.getString("item"));
    }

    public void setItem(String item) {
        try {
            this.itemStack = new ItemStack(Objects.requireNonNull(ForgeRegistries.ITEMS.getValue(new ResourceLocation(item))));
        } catch (Exception ignored) {

        }
    }

    public String getItem() {
        return Objects.requireNonNull(ForgeRegistries.ITEMS.getKey(this.itemStack.getItem())).toString();
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void render(GuiGraphics guiGraphics, int xOffset, int yOffset) {
        int minSide = Math.min(this.width, this.height);
        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale(minSide / 16F, minSide / 16F, 1);
        guiGraphics.pose().translate(0, 0, -140);
        guiGraphics.renderFakeItem(this.itemStack, ((xOffset + this.x + (this.width - minSide) / 2) * 16) / minSide, ((yOffset + this.y + (this.height - minSide) / 2) * 16) / minSide);
        guiGraphics.pose().popPose();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeByte(3);
        super.toBytes(buf);
        buf.writeUtf(this.getItem());
    }
    public CompoundTag toCompound() {
        CompoundTag tag = super.toCompound();
        tag.putByte("type", (byte) 3);
        tag.putString("item", this.getItem());
        return tag;
    }
}
