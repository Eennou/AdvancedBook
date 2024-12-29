package com.eennou.advancedbook.recipes;

import com.eennou.advancedbook.items.ModItems;
import com.eennou.advancedbook.items.ModRecipes;
import com.google.gson.JsonObject;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
public class AdvancedBookCloningRecipe implements CraftingRecipe {
    private final ResourceLocation id;

    public AdvancedBookCloningRecipe(ResourceLocation id) {
        this.id = id;
    }
    @Override
    public boolean matches(CraftingContainer container, Level level) {
        int books = 0;
        int paint = 0;
        ItemStack itemstack = ItemStack.EMPTY;

        for (int j = 0; j < container.getContainerSize(); ++j) {
            ItemStack itemstack1 = container.getItem(j);
            if (!itemstack1.isEmpty()) {
                if (itemstack1.is(ModItems.BOOK.get())) {
                    if (!itemstack.isEmpty()) {
                        return false;
                    }

                    itemstack = itemstack1;
                } else {
                    if (itemstack1.is(Items.BOOK)) {
                        ++books;
                    } else if (itemstack1.is(ModItems.PAINT.get())) {
                        ++paint;
                    } else {
                        return false;
                    }
                }
            }
        }

        return !itemstack.isEmpty() && itemstack.hasTag() && books > 0 && paint > 0;
    }

    @Override
    public ItemStack assemble(CraftingContainer container, RegistryAccess registryAccess) {
        int books = 0;
        int paint = 0;
        ItemStack itemstack = ItemStack.EMPTY;

        for(int j = 0; j < container.getContainerSize(); ++j) {
            ItemStack itemstack1 = container.getItem(j);
            if (!itemstack1.isEmpty()) {
                if (itemstack1.is(ModItems.BOOK.get())) {
                    if (!itemstack.isEmpty()) {
                        return ItemStack.EMPTY;
                    }

                    itemstack = itemstack1;
                } else {;
                    if (itemstack1.is(Items.BOOK)) {
                        ++books;
                    } else if (itemstack1.is(ModItems.PAINT.get())) {
                        ++paint;
                    } else {
                        return ItemStack.EMPTY;
                    }
                }
            }
        }
        if (!itemstack.isEmpty() && itemstack.hasTag() && books >= 1 && paint >= 1) {
            ItemStack itemstack2 = new ItemStack(ModItems.BOOK.get(), Math.min(books, paint));
            CompoundTag compoundtag = itemstack.getTag().copy();
            if (!compoundtag.contains("author")) {
                return ItemStack.EMPTY;
            }
            if (compoundtag.contains("generation")) {
                compoundtag.putInt("generation", Math.min(2, compoundtag.getInt("generation") + 1));
            }
            itemstack2.setTag(compoundtag);
            return itemstack2;
        } else {
            return ItemStack.EMPTY;
        }
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 3;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess registryAccess) {
        return new ItemStack(ModItems.BOOK.get());
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> ingredients = NonNullList.create();
        ingredients.add(Ingredient.of(ModItems.BOOK.get()));
        ingredients.add(Ingredient.of(ModItems.BOOK.get()));
        ingredients.add(Ingredient.of(ModItems.PAINT.get()));
        return ingredients;
    }

    @Override
    public ResourceLocation getId() {
        return this.id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.BOOK_CLONING_SERIALIZER.get();
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingContainer container) {
        NonNullList<ItemStack> nonnulllist = NonNullList.withSize(container.getContainerSize(), ItemStack.EMPTY);
        boolean bookRemained = false;
        boolean paintRemained = false;

        for(int i = 0; i < nonnulllist.size(); ++i) {
            ItemStack itemstack = container.getItem(i);
            if (itemstack.is(ModItems.PAINT.get())) {
                if (!paintRemained) {
                    paintRemained = true;
                    nonnulllist.set(i, itemstack.copyWithCount(1));
                }
            } else if (itemstack.is(Items.BOOK)) {
                if (!bookRemained) {
                    bookRemained = true;
                    nonnulllist.set(i, itemstack.copyWithCount(1));
                }
            } else if (itemstack.is(ModItems.BOOK.get())) {
                nonnulllist.set(i, itemstack.copyWithCount(1));
                break;
            }
        }

        return nonnulllist;
    }

    @Override
    public CraftingBookCategory category() {
        return CraftingBookCategory.MISC;
    }

    public static class Serializer implements RecipeSerializer<AdvancedBookCloningRecipe> {

        @Override
        public AdvancedBookCloningRecipe fromJson(ResourceLocation resourceLocation, JsonObject json) {
            return new AdvancedBookCloningRecipe(resourceLocation);
        }

        @Override
        public AdvancedBookCloningRecipe fromNetwork(ResourceLocation resourceLocation, FriendlyByteBuf buf) {
            return new AdvancedBookCloningRecipe(resourceLocation);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, AdvancedBookCloningRecipe recipe) {

        }
    }
}
