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

public class EditPagesBookC2SPacket {
    private final int index;
    private final boolean action;

    public EditPagesBookC2SPacket(int index, boolean action) {
        this.index = index;
        this.action = action;
    }

    public EditPagesBookC2SPacket(FriendlyByteBuf buf) {
        this.index = buf.readByte();
        this.action = buf.readBoolean();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeByte(this.index);
        buf.writeBoolean(this.action);
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
            if (this.action) {
                pagesTag.add(this.index + 1, new ListTag());
            } else {
                pagesTag.remove(this.index);
            }
            tag.put("pages", pagesTag);
        });
        return true;
    }
}
