package com.eennou.advancedbook.blocks;

import com.eennou.advancedbook.screens.bookelements.BookElement;
import com.eennou.advancedbook.screens.bookelements.ItemElement;
import com.eennou.advancedbook.screens.bookelements.RectangleElement;
import com.eennou.advancedbook.screens.bookelements.StringElement;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.List;

import static com.eennou.advancedbook.blocks.IllustrationFrame.*;
import static net.minecraft.world.level.block.FaceAttachedHorizontalDirectionalBlock.FACE;
import static net.minecraft.world.level.block.HorizontalDirectionalBlock.FACING;

public class IllustrationFrameBlockEntity extends BlockEntity {
    public List<BookElement> getBookElements() {
        if (this.isSlave()) {
            if (this.getMaster() == null) return null;
            return this.getMaster().getBookElements();
        }
        return this.bookElements;
    }
    public void setBookElements(CompoundTag illustration, boolean placed) {
        this.illustration = illustration;
        this.bookElements = new ArrayList<>();
        for (Tag tag : illustration.getList("elements", Tag.TAG_COMPOUND)) {
            this.bookElements.add(switch (((CompoundTag) tag).getByte("type")) {
                case 1 -> new RectangleElement((CompoundTag) tag);
                case 2 -> new StringElement((CompoundTag) tag);
                case 3 -> new ItemElement((CompoundTag) tag);
                default ->
                    throw new IllegalStateException("Unexpected value: " + ((CompoundTag) tag).getByte("type"));
            });
        }
        Direction xDir = this.getBlockState().getValue(FACING).getCounterClockWise();
        Direction yDir = switch (this.getBlockState().getValue(FACE)) {
            case FLOOR -> this.getBlockState().getValue(FACING);
            case WALL -> Direction.DOWN;
            case CEILING -> this.getBlockState().getValue(FACING).getOpposite();
        };
        this.slaves.clear();
        if (placed) {
            this.level.setBlockAndUpdate(this.getBlockPos(), this.getBlockState()
                .setValue(DUST, 0)
                .setValue(DUST_CLEAN, 0)
                .setValue(SOAKED, false)
                .setValue(LAMINATED, this.illustration.contains("author"))
            );
        }
        for (short x = 0; x < this.illustration.getShort("width"); x++) {
            BlockPos columnTop = this.worldPosition.relative(xDir, x);
            for (short y = 0; y < this.illustration.getShort("height"); y++) {
                if (x == 0 && y == 0) continue;
                BlockEntity blockEntity = this.level.getExistingBlockEntity(columnTop.relative(yDir, y));
                if (blockEntity instanceof IllustrationFrameBlockEntity) {
                    if (blockEntity.getBlockState().getValue(FACING).equals(this.getBlockState().getValue(FACING))
                        && blockEntity.getBlockState().getValue(FACE).equals(this.getBlockState().getValue(FACE))
                        && ((IllustrationFrameBlockEntity) blockEntity).getBookElements() == null) {
                        ((IllustrationFrameBlockEntity) blockEntity).setMaster(this);
                        ((IllustrationFrameBlockEntity) blockEntity).offsetX = x;
                        ((IllustrationFrameBlockEntity) blockEntity).offsetY = y;
                        this.slaves.add((IllustrationFrameBlockEntity) blockEntity);
                        if (placed) {
                            this.level.setBlockAndUpdate(blockEntity.getBlockPos(), blockEntity.getBlockState()
                                .setValue(DUST, 0)
                                .setValue(DUST_CLEAN, 0)
                                .setValue(SOAKED, false)
                                .setValue(LAMINATED, this.illustration.contains("author"))
                            );
                            blockEntity.setChanged();
                        }
                    }
                }
            }
        }
    }

    public boolean isSoaked() {
        if (this.isSlave()) {
            return this.getMaster().isSoaked();
        } else {
            for (IllustrationFrameBlockEntity slave : this.slaves) {
                if (slave.getBlockState().getValue(SOAKED)) {
                    return true;
                }
            }
            return this.getBlockState().getValue(SOAKED);
        }
    }

    public void appendSlave(IllustrationFrameBlockEntity blockEntity) {
        blockEntity.setMaster(this);
        if (!this.slaves.contains(blockEntity)) {
            this.slaves.add(blockEntity);
        }
    }
    public CompoundTag getIllustration() {
        if (this.isSlave()) {
            return this.getMaster().getIllustration();
        }
        return this.illustration;
    }

    public void clearIllustration() {
        if (this.isSlave()) {
            this.getMaster().clearIllustration();
        } else {
            for (IllustrationFrameBlockEntity slave : this.slaves) {
                try {
                    slave.clearIllustrationInternal();
                } catch (Exception ignored) {

                }
            }
            this.slaves.clear();
            this.clearIllustrationInternal();
        }
    }

    protected void clearIllustrationInternal() {
        this.illustration = null;
        this.bookElements = null;
        this.setMaster((BlockPos) null);
        this.offsetX = 0;
        this.offsetY = 0;
        this.level.setBlockAndUpdate(this.getBlockPos(), this.getBlockState()
                .setValue(DUST, 0)
                .setValue(DUST_CLEAN, 0)
        );
        this.setChanged();
    }

    private List<BookElement> bookElements;

    private CompoundTag illustration;
    public short offsetX = 0;
    public short offsetY = 0;
    private IllustrationFrameBlockEntity master;
    private BlockPos masterPos;
    public IllustrationFrameBlockEntity getMaster() {
        if (this.masterPos == null && this.master == null) return null;
        if (this.master == null) {
            this.master = (IllustrationFrameBlockEntity) this.level.getExistingBlockEntity(this.masterPos);
            if (this.master != null) {
                this.master.appendSlave(this);
            }
        }
        return this.master;
    }
    public void setMaster(BlockPos pos) {
        this.masterPos = pos;
        this.master = null;
    }
    public void setMaster(IllustrationFrameBlockEntity blockEntity) {
        this.masterPos = blockEntity.getBlockPos();
        this.master = blockEntity;
    }
    public boolean isSlave() {
        return this.masterPos != null;
    }
    protected List<IllustrationFrameBlockEntity> slaves;
    public IllustrationFrameBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(ModBlocks.ILLUSTRATION_FRAME_BE.get(), blockPos, blockState);
        this.slaves = new ArrayList<>();
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (this.isSlave()) {
            CompoundTag blockPosTag = new CompoundTag();
            blockPosTag.putInt("x", this.masterPos.getX());
            blockPosTag.putInt("y", this.masterPos.getY());
            blockPosTag.putInt("z", this.masterPos.getZ());
            tag.put("inherited", blockPosTag);
            tag.putShort("offsetX", this.offsetX);
            tag.putShort("offsetY", this.offsetY);
        } else if (this.illustration != null) {
            tag.put("illustration", this.illustration);
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("illustration")) {
            this.illustration = tag.getCompound("illustration");
            if (this.level != null) {
                this.setBookElements(this.illustration, false);
            }
        } else if (this.level != null && tag.contains("inherited")) {
            this.offsetX = tag.getShort("offsetX");
            this.offsetY = tag.getShort("offsetY");
            CompoundTag blockPosTag = tag.getCompound("inherited");
            this.setMaster(new BlockPos(blockPosTag.getInt("x"), blockPosTag.getInt("y"), blockPosTag.getInt("z")));
//            IllustrationFrameBlockEntity blockEntity = (IllustrationFrameBlockEntity) this.level.getExistingBlockEntity(new BlockPos(blockPosTag.getInt("x"), blockPosTag.getInt("y"), blockPosTag.getInt("z")));
//            if (blockEntity != null) {
//                blockEntity.appendSlave(this);
//            }
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (this.level.isClientSide()) return;
        if (this.illustration != null) {
            this.setBookElements(this.illustration, false);
        }
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        if (this.illustration != null) {
            tag.put("illustration", this.illustration);
        } else if (this.isSlave()) {
            CompoundTag blockPosTag = new CompoundTag();
            BlockPos blockPos = this.masterPos;
            blockPosTag.putInt("x", blockPos.getX());
            blockPosTag.putInt("y", blockPos.getY());
            blockPosTag.putInt("z", blockPos.getZ());
            tag.put("inherited", blockPosTag);
            tag.putShort("offsetX", this.offsetX);
            tag.putShort("offsetY", this.offsetY);
        }
        return tag;
    }

    @Override
    public AABB getRenderBoundingBox() {
        return new AABB(this.getBlockPos().offset(-1, -1, -1), this.getBlockPos().offset(1, 1, 1));
    }
}
