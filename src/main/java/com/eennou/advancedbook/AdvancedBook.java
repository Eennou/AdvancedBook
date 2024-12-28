package com.eennou.advancedbook;

import com.eennou.advancedbook.items.ModItems;
import com.eennou.advancedbook.items.ModRecipes;
import com.eennou.advancedbook.networking.*;
import com.mojang.logging.LogUtils;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(AdvancedBook.MODID)
public class AdvancedBook
{
    // Define mod id in a common place for everything to reference
    public static final String MODID = "advancedbook";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    public static final RegistryObject<CreativeModeTab> MAIN_TAB = CREATIVE_MODE_TABS.register("advancedbook_tab", () -> CreativeModeTab.builder()
            .withTabsBefore(CreativeModeTabs.COMBAT)
            .icon(() -> ModItems.PAINT.get().getDefaultInstance())
            .displayItems((parameters, output) -> {
                output.accept(ModItems.BOOK.get());
                output.accept(ModItems.PAINT.get());
            }).build());

    public AdvancedBook()
    {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::onClientSetup);

        ModItems.register(modEventBus);
        ModRecipes.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);

        MinecraftForge.EVENT_BUS.register(this);

        PacketHandler.instance.messageBuilder(EditBookC2SPacket.class, PacketHandler.getAndAppendId(), NetworkDirection.PLAY_TO_SERVER)
            .decoder(EditBookC2SPacket::new)
            .encoder(EditBookC2SPacket::toBytes)
            .consumerMainThread(EditBookC2SPacket::handle)
            .add();
        PacketHandler.instance.messageBuilder(ChangePageBookC2SPacket.class, PacketHandler.getAndAppendId(), NetworkDirection.PLAY_TO_SERVER)
            .decoder(ChangePageBookC2SPacket::new)
            .encoder(ChangePageBookC2SPacket::toBytes)
            .consumerMainThread(ChangePageBookC2SPacket::handle)
            .add();
        PacketHandler.instance.messageBuilder(SignBookC2SPacket.class, PacketHandler.getAndAppendId(), NetworkDirection.PLAY_TO_SERVER)
            .decoder(SignBookC2SPacket::new)
            .encoder(SignBookC2SPacket::toBytes)
            .consumerMainThread(SignBookC2SPacket::handle)
            .add();
        PacketHandler.instance.messageBuilder(EditPagesBookC2SPacket.class, PacketHandler.getAndAppendId(), NetworkDirection.PLAY_TO_SERVER)
            .decoder(EditPagesBookC2SPacket::new)
            .encoder(EditPagesBookC2SPacket::toBytes)
            .consumerMainThread(EditPagesBookC2SPacket::handle)
            .add();
        PacketHandler.instance.messageBuilder(UpdateBookmarksC2SPacket.class, PacketHandler.getAndAppendId(), NetworkDirection.PLAY_TO_SERVER)
            .decoder(UpdateBookmarksC2SPacket::new)
            .encoder(UpdateBookmarksC2SPacket::toBytes)
            .consumerMainThread(UpdateBookmarksC2SPacket::handle)
            .add();

        // Register the item to a creative tab
        modEventBus.addListener(this::addCreative);

        // Register our mod's ForgeConfigSpec so that Forge can create and load the config file for us
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }
    private void onClientSetup(FMLClientSetupEvent event)
    {
        event.enqueueWork(() ->
        {
            ItemProperties.register(ModItems.BOOK.get(), new ResourceLocation(MODID, "opened"),
                    (itemstack, world, entity, some_int) -> itemstack.getOrCreateTag().contains("openedPage") ? 1.0F : 0.0F
            );
        });
    }

    private void commonSetup(final FMLCommonSetupEvent event)
    {

    }

    // Add the example block item to the building blocks tab
    private void addCreative(BuildCreativeModeTabContentsEvent event)
    {
//        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES)
//            event.accept(ModItems.BOOK);
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event)
    {

    }

    public static class PacketHandler
    {
        private static final String PROTOCOL_VERSION = "1";
        public static int id = 0;

        public static int getAndAppendId() {
            return id++;
        }

        public static final SimpleChannel instance = NetworkRegistry.newSimpleChannel(
                new ResourceLocation(MODID, "messages"),
                () -> PROTOCOL_VERSION,
                PROTOCOL_VERSION::equals,
                PROTOCOL_VERSION::equals
        );

        public static <MSG> void sendToServer(MSG message) {
            instance.sendToServer(message);
        }

        public static <MSG> void sendToPlayer(MSG message, ServerPlayer player) {
            instance.send(PacketDistributor.PLAYER.with(() -> player), message);
        }
    }

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents
    {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event)
        {

        }
    }
}