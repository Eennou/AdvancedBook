package com.eennou.advancedbook.integrations;

import com.eennou.advancedbook.AdvancedBook;
import com.eennou.advancedbook.recipes.AdvancedBookCloningRecipe;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.resources.ResourceLocation;
@JeiPlugin
public class JEIIntegration implements IModPlugin {
    private static final ResourceLocation ID = new ResourceLocation(AdvancedBook.MODID);
//    @Override
//    public void register(IModRegistry registry) {
//        registry.addRecipes(Lists.newArrayList(new DropsRecipeWrapper()), DropsRecipeCategory.UID); // Регистрация рецептов для Вашего UID.
//        registry.addRecipeCatalyst(new ItemStack(TMRegistry.blockDrops), DropsRecipeCategory.UID); // Регистрация показывающихся блоков для Вашего UID.
//
//        IIngredientBlacklist blackList = registry.getJeiHelpers().getIngredientBlacklist(); // Блек лист для JEI, используемый для скрытия предметов.
//        blackList.addIngredientToBlacklist(new ItemStack(Blocks.DIRT)); // Скрытие блока земли из JEI.
//    }


    @Override
    public void registerRecipes(IRecipeRegistration registration) {
//        List<CraftingRecipe> some_another_shit = new ArrayList<>();
//        some_another_shit.add(new AdvancedBookCloningRecipe(new ResourceLocation("advancedbook:book_cloning"), CraftingBookCategory.MISC));
//        registration.addRecipes(RecipeTypes.CRAFTING, some_another_shit);
    }

    @Override
    public ResourceLocation getPluginUid() {
        return ID;
    }
}