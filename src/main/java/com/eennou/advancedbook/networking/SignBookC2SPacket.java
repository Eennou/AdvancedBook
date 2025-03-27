package com.eennou.advancedbook.networking;

import com.eennou.advancedbook.items.ModItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.Objects;
import java.util.function.Supplier;

public class SignBookC2SPacket {
    private final String title;
    public SignBookC2SPacket(String title) {
        this.title = title;
    }
    public SignBookC2SPacket(FriendlyByteBuf buf) {
        this.title = buf.readUtf();
    }
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUtf(this.title);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ItemStack itemStack = Objects.requireNonNull(context.getSender()).getMainHandItem();
            if (itemStack.getItem() != ModItems.BOOK.get()) {
                itemStack = Objects.requireNonNull(context.getSender()).getOffhandItem();
            }
            if (itemStack.getItem() != ModItems.BOOK.get()) return;
            CompoundTag tag = itemStack.getOrCreateTag();
            if (tag.contains("author")) {
                return;
            }
            tag.putInt("generation", 0);
            tag.putString("author", context.getSender().getName().getString());
            tag.putString("title", this.title.length() > 32 ? this.title.substring(0, 32) : this.title);
        });
        return true;
    }
}
