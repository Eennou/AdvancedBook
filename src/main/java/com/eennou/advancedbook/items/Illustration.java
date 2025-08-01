package com.eennou.advancedbook.items;

import com.eennou.advancedbook.blocks.IllustrationFrameBlockEntity;
import com.eennou.advancedbook.screens.IllustrationScreen;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

public class Illustration extends Item {
    public Illustration(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        BlockHitResult result = getPlayerPOVHitResult(level, player, ClipContext.Fluid.NONE);
        BlockEntity blockEntity = level.getExistingBlockEntity(result.getBlockPos());
        if (result.getType() == HitResult.Type.BLOCK && blockEntity instanceof IllustrationFrameBlockEntity) {
            CompoundTag illustration = ((IllustrationFrameBlockEntity) blockEntity).getIllustration();
            ((IllustrationFrameBlockEntity) blockEntity).setBookElements(itemstack.getOrCreateTag(), true);
            blockEntity.setChanged();
            level.sendBlockUpdated(blockEntity.getBlockPos(), blockEntity.getBlockState(), blockEntity.getBlockState(), Block.UPDATE_CLIENTS);
            level.setBlocksDirty(blockEntity.getBlockPos(), blockEntity.getBlockState(), blockEntity.getBlockState());
            itemstack.shrink(1);
            if (illustration != null) {
                ItemStack givenItemStack = new ItemStack(ModItems.ILLUSTRATION.get(), 1);
                givenItemStack.setTag(illustration);
                player.getInventory().add(givenItemStack);
            }
            return InteractionResultHolder.success(itemstack);
        }
        if (level.isClientSide()) {
            this.openIllustration(itemstack);
        }
        return InteractionResultHolder.sidedSuccess(itemstack, level.isClientSide());
    }
    @OnlyIn(Dist.CLIENT)
    private void openIllustration(ItemStack itemstack) {
        Minecraft.getInstance().setScreen(new IllustrationScreen(itemstack));
    }

    @Override
    public boolean overrideOtherStackedOnMe(ItemStack itemStack, ItemStack itemStack1, Slot slot, ClickAction action, Player player, SlotAccess slotAccess) {
        if (itemStack.getItem() == itemStack1.getItem()) {
            if (itemStack.getTag() != null && itemStack1.getTag() != null) {
                if (itemStack1.getTag().getList("elements", Tag.TAG_COMPOUND).isEmpty()
                    && itemStack1.getTag().getList("elements", Tag.TAG_COMPOUND).isEmpty() && itemStack.getTag().getShort("width") == itemStack1.getTag().getShort("width") && itemStack.getTag().getShort("height") == itemStack1.getTag().getShort("height")) {
                    int moveCount = action == ClickAction.PRIMARY ? itemStack1.getCount() : 1;
                    itemStack.grow(moveCount);
                    itemStack1.shrink(moveCount);
                    return true;
                }
            }
        }
        return super.overrideOtherStackedOnMe(itemStack, itemStack1, slot, action, player, slotAccess);
    }

    @Override
    public String getDescriptionId(ItemStack itemstack) {
        if (!itemstack.hasTag() || !itemstack.getTag().contains("title")) {
            return Component.translatable("item.advancedbook.illustration").getString();
        }
        return itemstack.getTag().getString("title");
    }

    @Override
    public void appendHoverText(ItemStack itemstack, Level level, List<Component> components, TooltipFlag tooltipFlag) {
        if (!itemstack.hasTag()) {
            components.add(Component.translatable("gui.advancedbook.size", 1, 1).withStyle(ChatFormatting.GRAY));
            return;
        }
        components.add(Component.translatable("gui.advancedbook.size", itemstack.getTag().getShort("width"), itemstack.getTag().getShort("height")).withStyle(ChatFormatting.GRAY));
        if (!itemstack.getTag().contains("author")) {
            return;
        }
        components.add(Component.translatable("book.byAuthor", itemstack.getTag().getString("author")).withStyle(ChatFormatting.GRAY));
        components.add(Component.translatable("book.generation." + itemstack.getTag().getInt("generation")).withStyle(ChatFormatting.GRAY));
        components.add(Component.translatable("gui.advancedbook.laminated").withStyle(ChatFormatting.YELLOW));
    }
}
