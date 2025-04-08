package com.eennou.advancedbook.blocks;

import com.eennou.advancedbook.Config;
import com.eennou.advancedbook.items.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.FaceAttachedHorizontalDirectionalBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.ArrayList;
import java.util.List;

public class IllustrationFrame extends FaceAttachedHorizontalDirectionalBlock implements EntityBlock {
    public static final IntegerProperty DUST = IntegerProperty.create("dust", 0, 20);
    public static final IntegerProperty DUST_CLEAN = IntegerProperty.create("dust_clean", 0, 3);
    public static final Property<Boolean> SOAKED = BooleanProperty.create("soaked");
    public static final Property<Boolean> LAMINATED = BooleanProperty.create("laminated");

    public IllustrationFrame(Properties properties) {
        super(properties.destroyTime(1F).noOcclusion().sound(SoundType.WOOD).randomTicks());
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(FACE, AttachFace.WALL).setValue(DUST, 0).setValue(DUST_CLEAN, 0).setValue(SOAKED, false).setValue(LAMINATED, false));
    }

    @Override
    public InteractionResult use(BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (((IllustrationFrameBlockEntity)level.getExistingBlockEntity(blockPos)).getBookElements() == null) {
            return InteractionResult.PASS;
        }
        int dust = Math.max(0, blockState.getValue(DUST) - 3);
        level.setBlockAndUpdate(blockPos, blockState
            .setValue(DUST, dust)
            .setValue(DUST_CLEAN, dust > 0 ? Math.min(blockState.getValue(DUST_CLEAN) + 1, dust < 4 ? 3 : 2) : 0)
        );
        return InteractionResult.SUCCESS;
    }

    @Override
    public List<ItemStack> getDrops(BlockState p_287732_, LootParams.Builder p_287596_) {
        ArrayList<ItemStack> itemStacks = new ArrayList<>();
        itemStacks.add(new ItemStack(ModItems.ILLUSTRATION_FRAME.get()));
        return itemStacks;
    }

    @Override
    public void randomTick(BlockState blockState, ServerLevel level, BlockPos blockPos, RandomSource randomSource) {
        if (blockState.getValue(LAMINATED)) return;
        if (level.isRaining() && level.canSeeSky(blockPos)) {
            level.setBlockAndUpdate(blockPos, blockState
                .setValue(SOAKED, true)
            );
        }
        if (Config.dustAccumulation) {
            if (((IllustrationFrameBlockEntity)level.getExistingBlockEntity(blockPos)).getBookElements() != null
                && randomSource.nextIntBetweenInclusive(1, 100) <= Config.dustChance) {
                level.setBlockAndUpdate(blockPos, blockState
                    .setValue(DUST, Math.min(blockState.getValue(DUST) + 1, 20))
                    .setValue(DUST_CLEAN, Math.min(Math.max(0, blockState.getValue(DUST_CLEAN) - 1), 3))
                );
            }
        }
        super.randomTick(blockState, level, blockPos, randomSource);
    }

    @Override
    public boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, Player player, boolean willHarvest, FluidState fluid) {
        IllustrationFrameBlockEntity blockEntity = (IllustrationFrameBlockEntity) level.getExistingBlockEntity(pos);
        if (blockEntity != null && blockEntity.getBookElements() != null) {
            if (player.isShiftKeyDown()) {
                ItemStack itemStack = new ItemStack(blockEntity.isSoaked() ? ModItems.SOAKED_ILLUSTRATION.get() : ModItems.ILLUSTRATION.get(), 1);
                itemStack.setTag(blockEntity.getIllustration());
                player.getInventory().add(itemStack);
                blockEntity.clearIllustration();
            } else if (level.isClientSide()) {
                Minecraft.getInstance().gui.setOverlayMessage(Component.translatable("gui.advancedbook.remove_with_shift"), false);
            }
            return false;
        }
        return super.onDestroyedByPlayer(state, level, pos, player, willHarvest, fluid);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return ModBlocks.ILLUSTRATION_FRAME_BE.get().create(blockPos, blockState);
    }

    @Override
    public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext context) {
        return switch (blockState.getValue(FACE)) {
            case FLOOR -> Block.box(0, 0, 0, 16, 1, 16);
            case CEILING -> Block.box(0, 15, 0, 16, 16, 16);
            case WALL -> switch (blockState.getValue(FACING)) {
                case NORTH -> Block.box(0, 0, 15, 16, 16, 16);
                case SOUTH -> Block.box(0, 0, 0, 16, 16, 1);
                case WEST -> Block.box(15, 0, 0, 16, 16, 16);
                case EAST -> Block.box(0, 0, 0, 1, 16, 16);
                default -> throw new IllegalStateException("Unexpected value: " + blockState.getValue(FACING));
            };
        };
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext p_53184_) {
        for(Direction direction : p_53184_.getNearestLookingDirections()) {
            BlockState blockstate;
            if (direction.getAxis() == Direction.Axis.Y) {
                blockstate = this.defaultBlockState().setValue(FACE, direction == Direction.UP ? AttachFace.CEILING : AttachFace.FLOOR).setValue(FACING, p_53184_.getHorizontalDirection().getOpposite());
            } else {
                blockstate = this.defaultBlockState().setValue(FACE, AttachFace.WALL).setValue(FACING, direction.getOpposite());
            }

            if (blockstate.canSurvive(p_53184_.getLevel(), p_53184_.getClickedPos())) {
                return blockstate;
            }
        }

        return null;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, FACE, DUST, DUST_CLEAN, SOAKED, LAMINATED);
    }
}
