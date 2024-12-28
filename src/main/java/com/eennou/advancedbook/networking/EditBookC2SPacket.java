package com.eennou.advancedbook.networking;

import com.eennou.advancedbook.AdvancedBook;
import com.eennou.advancedbook.items.ModItems;
import com.eennou.advancedbook.screens.bookelements.BookElement;
import com.eennou.advancedbook.screens.bookelements.ItemElement;
import com.eennou.advancedbook.screens.bookelements.RectangleElement;
import com.eennou.advancedbook.screens.bookelements.StringElement;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

public class EditBookC2SPacket {
    private final List<BookElement> page;
    private final int index;

    public EditBookC2SPacket(int index, List<BookElement> page) {
        this.index = index;
        this.page = page;
    }

    public EditBookC2SPacket(FriendlyByteBuf buf) {
        this.index = buf.readByte();
        this.page = new ArrayList<>();
        int elementsCount = buf.readInt();
        for (int i = 0; i < elementsCount && i < 256; i++) {
            int element = buf.readByte();
            switch (element) {
                case 1:
                    this.page.add(new RectangleElement(buf));
                    break;
                case 2:
                    this.page.add(new StringElement(buf));
                    break;
                case 3:
                    this.page.add(new ItemElement(buf));
                    break;
            }
        }
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeByte(this.index);
        buf.writeInt(page.size());
        for (BookElement bookElement : page) {
            bookElement.toBytes(buf);
        }
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            if (this.index > 255) {
                return;
            }
            ItemStack itemStack = Objects.requireNonNull(context.getSender()).getMainHandItem();
            if (itemStack.getItem() != ModItems.BOOK.get()) return;
            CompoundTag tag = itemStack.getOrCreateTag();
            if (tag.contains("author")) {
                return;
            }
            ListTag pagesTag = tag.getList("pages", ListTag.TAG_LIST);
            ListTag pageTag = new ListTag();
            for (BookElement element : page) {
                pageTag.add(element.toCompound());
            }
            if (pagesTag.size() <= this.index) {
                pagesTag.add(pageTag);
            } else {
                pagesTag.set(this.index, pageTag);
            }
            tag.put("pages", pagesTag);
        });
        return true;
    }
}
