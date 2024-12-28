package com.eennou.advancedbook.items;

import com.eennou.advancedbook.AdvancedBook;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, AdvancedBook.MODID);

    public static final RegistryObject<Item> BOOK = ITEMS.register("book", () -> new Book(new Item.Properties().stacksTo(8).rarity(Rarity.COMMON)));
//    public static final RegistryObject<Item> DOCUMENT = ITEMS.register("document", () -> new Book(new Item.Properties().stacksTo(8).rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<Item> PAINT = ITEMS.register("paint", () -> new Item(new Item.Properties().stacksTo(64).rarity(Rarity.COMMON)));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}

