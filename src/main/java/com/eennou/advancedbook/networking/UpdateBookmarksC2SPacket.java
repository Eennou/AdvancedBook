package com.eennou.advancedbook.networking;

import com.eennou.advancedbook.items.ModItems;
import com.eennou.advancedbook.screens.AdvancedBookScreen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

public class UpdateBookmarksC2SPacket {
    private final List<AdvancedBookScreen.Bookmark> bookmarks;
    public UpdateBookmarksC2SPacket(List<AdvancedBookScreen.Bookmark> bookmarks) {
        this.bookmarks = bookmarks;
    }
    public UpdateBookmarksC2SPacket(FriendlyByteBuf buf) {
        this.bookmarks = new ArrayList<>();
        byte bookmarkCount = buf.readByte();
        for (int i = 0; i < bookmarkCount; i++) {
            this.bookmarks.add(new AdvancedBookScreen.Bookmark(buf.readInt(), buf.readInt(), buf.readInt()));
        }
    }
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeByte(this.bookmarks.size());
        for (AdvancedBookScreen.Bookmark bookmark : this.bookmarks) {
            buf.writeInt(bookmark.page);
            buf.writeInt(bookmark.position);
            buf.writeInt(bookmark.color);
        }
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ItemStack itemStack = Objects.requireNonNull(context.getSender()).getMainHandItem();
            if (itemStack.getItem() != ModItems.BOOK.get()) return;
            CompoundTag tag = itemStack.getOrCreateTag();
            if (tag.contains("author")) {
                return;
            }
            ListTag bookmarksTag = new ListTag();
            for (AdvancedBookScreen.Bookmark bookmark : this.bookmarks) {
                bookmarksTag.add(bookmark.toCompound());
            }
            tag.put("bookmarks", bookmarksTag);
        });
        return true;
    }
}
