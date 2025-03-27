package com.eennou.advancedbook.blocks;

import com.eennou.advancedbook.AdvancedBook;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, AdvancedBook.MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, AdvancedBook.MODID);

    public static final RegistryObject<Block> ILLUSTRATION_FRAME = BLOCKS.register("illustration_frame", () -> new IllustrationFrame(BlockBehaviour.Properties.of()));
    public static final RegistryObject<BlockEntityType<IllustrationFrameBlockEntity>> ILLUSTRATION_FRAME_BE = BLOCK_ENTITIES.register("illustration_frame",
            () -> BlockEntityType.Builder.of(IllustrationFrameBlockEntity::new, ILLUSTRATION_FRAME.get()).build(null));

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
        BLOCK_ENTITIES.register(eventBus);
    }
}

