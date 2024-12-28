package com.eennou.advancedbook.items;

import com.eennou.advancedbook.AdvancedBook;
import com.eennou.advancedbook.recipes.AdvancedBookCloningRecipe;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModRecipes {
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, AdvancedBook.MODID);
    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES = DeferredRegister.create(ForgeRegistries.RECIPE_TYPES, AdvancedBook.MODID);

    public static final RegistryObject<AdvancedBookCloningRecipe.Serializer> BOOK_CLONING_SERIALIZER = RECIPE_SERIALIZERS.register("book_cloning", AdvancedBookCloningRecipe.Serializer::new);
//    public static final RegistryObject<AdvancedBookCloningRecipe.Type> BOOK_CLONING_TYPE = RECIPE_TYPES.register("book_cloning", AdvancedBookCloningRecipe.Type::new);

    public static void register(IEventBus eventBus) {
        RECIPE_SERIALIZERS.register(eventBus);
        RECIPE_TYPES.register(eventBus);
    }
}
