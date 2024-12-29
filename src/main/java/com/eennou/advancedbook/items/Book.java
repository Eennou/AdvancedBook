package com.eennou.advancedbook.items;

import com.eennou.advancedbook.screens.AdvancedBookScreen;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

public class Book extends Item {
    public Book(Properties properties) {
        super(properties);
    }
    @OnlyIn(Dist.CLIENT)
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        if (level.isClientSide()) {
            Minecraft.getInstance().setScreen(new AdvancedBookScreen(itemstack));
        }
        return InteractionResultHolder.sidedSuccess(itemstack, level.isClientSide());
    }

    @Override
    public String getDescriptionId(ItemStack itemstack) {
        if (!itemstack.hasTag() || !itemstack.getTag().contains("title")) {
            return Component.translatable("item.advancedbook.book").getString();
        }
        return itemstack.getTag().getString("title");
    }

    @Override
    public void appendHoverText(ItemStack itemstack, Level level, List<Component> components, TooltipFlag tooltipFlag) {
        if (!itemstack.hasTag()) {
            return;
        }
        components.add(Component.translatable("gui.advancedbook.pages", itemstack.getTag().getList("pages", Tag.TAG_LIST).size()).withStyle(ChatFormatting.GRAY));
        if (!itemstack.getTag().contains("author")) {
            return;
        }
        components.add(Component.translatable("book.byAuthor", itemstack.getTag().getString("author")).withStyle(ChatFormatting.GRAY));
        components.add(Component.translatable("book.generation." + itemstack.getTag().getInt("generation")).withStyle(ChatFormatting.GRAY));
    }
}
