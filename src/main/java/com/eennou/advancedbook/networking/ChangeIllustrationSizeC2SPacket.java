package com.eennou.advancedbook.networking;

import com.eennou.advancedbook.Config;
import com.eennou.advancedbook.items.ModItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.Objects;
import java.util.function.Supplier;

public class ChangeIllustrationSizeC2SPacket {
    private final short width;
    private final short height;
    public ChangeIllustrationSizeC2SPacket(short width, short height) {
        this.width = width;
        this.height = height;
    }
    public ChangeIllustrationSizeC2SPacket(FriendlyByteBuf buf) {
        this.width = buf.readShort();
        this.height = buf.readShort();
    }
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeShort(this.width);
        buf.writeShort(this.height);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ItemStack itemStack = Objects.requireNonNull(context.getSender()).getMainHandItem();
            if (itemStack.getItem() != ModItems.ILLUSTRATION.get()) {
                itemStack = Objects.requireNonNull(context.getSender()).getOffhandItem();
            }
            if (itemStack.getItem() != ModItems.ILLUSTRATION.get()) return;
            CompoundTag tag = itemStack.getOrCreateTag();
            if (tag.contains("author")) {
                return;
            }
            if (this.width > Config.illustrationMaxSize || this.height > Config.illustrationMaxSize || this.width < 1 || this.height < 1){
                return;
            }
            tag.putShort("width", this.width);
            tag.putShort("height", this.height);
        });
        return true;
    }
}
