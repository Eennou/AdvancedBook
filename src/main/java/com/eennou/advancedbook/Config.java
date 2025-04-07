package com.eennou.advancedbook;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

@Mod.EventBusSubscriber(modid = AdvancedBook.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config
{
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    private static final ForgeConfigSpec.BooleanValue DUST_ACCUMULATION = BUILDER
            .comment("Enable accumulating dust for illustrations")
            .define("dustAccumulation", true);
    private static final ForgeConfigSpec.IntValue DUST_CHANCE = BUILDER
            .comment("Chance of dust appearing every random tick in %")
            .defineInRange("dustChance", 3, 0, 100);
    private static final ForgeConfigSpec.IntValue ILLUSTRATION_MAX_SIZE = BUILDER
            .comment("Maximum illustration size in blocks")
            .defineInRange("illustrationMaxSize", 5, 2, 16);

    static final ForgeConfigSpec SPEC = BUILDER.build();
    public static boolean dustAccumulation;
    public static int dustChance;
    public static int illustrationMaxSize;

    @SubscribeEvent
    static void onLoad(final FMLCommonSetupEvent event)
    {
        dustAccumulation = DUST_ACCUMULATION.get();
        dustChance = DUST_CHANCE.get();
        illustrationMaxSize = ILLUSTRATION_MAX_SIZE.get();
    }
}
