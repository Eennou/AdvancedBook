package com.eennou.advancedbook;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

@Mod.EventBusSubscriber(modid = AdvancedBook.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientConfig
{
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    private static final ForgeConfigSpec.BooleanValue SHADER_FIX = BUILDER
            .comment("Enable this if items in illustrations are too bright")
            .define("shaderFix", false);

    static final ForgeConfigSpec SPEC = BUILDER.build();
    public static boolean shaderFix;

    @SubscribeEvent
    static void onLoad(final FMLCommonSetupEvent event)
    {
        shaderFix = SHADER_FIX.get();
    }
}
