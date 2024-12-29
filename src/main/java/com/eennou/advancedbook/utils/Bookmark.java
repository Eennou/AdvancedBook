package com.eennou.advancedbook.utils;

import net.minecraft.nbt.CompoundTag;

public class Bookmark {
    public int page;
    public int position;
    public int color;

    public Bookmark(int page, int position, int color) {
        this.page = page;
        this.position = position;
        this.color = color;
    }
    public CompoundTag toCompound() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("page", this.page);
        tag.putInt("position", this.position);
        tag.putInt("color", this.color);
        return tag;
    }
}
