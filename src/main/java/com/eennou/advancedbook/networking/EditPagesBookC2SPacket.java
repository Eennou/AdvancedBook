package com.eennou.advancedbook.networking;

import com.eennou.advancedbook.items.ModItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

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
            if (itemStack.getItem() != ModItems.BOOK.get()) {
                itemStack = Objects.requireNonNull(context.getSender()).getOffhandItem();
            }
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
