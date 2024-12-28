package com.eennou.advancedbook.networking;

import com.eennou.advancedbook.items.ModItems;
import com.eennou.advancedbook.screens.bookelements.BookElement;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.Objects;
import java.util.function.Supplier;

public class ChangePageBookC2SPacket {
    private final int index;
    public ChangePageBookC2SPacket(int index) {
        this.index = index;
    }
    public ChangePageBookC2SPacket(FriendlyByteBuf buf) {
        this.index = buf.readInt();
    }
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(this.index);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ItemStack itemStack = Objects.requireNonNull(context.getSender()).getMainHandItem();
            if (itemStack.getItem() != ModItems.BOOK.get()) return;
            CompoundTag tag = itemStack.getOrCreateTag();
            if (this.index < 0) {
                tag.remove("openedPage");
            } else {
                tag.putInt("openedPage", this.index);
            }
        });
        return true;
    }
}
