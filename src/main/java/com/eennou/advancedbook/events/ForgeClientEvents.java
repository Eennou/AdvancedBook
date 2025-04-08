package com.eennou.advancedbook.events;

import com.eennou.advancedbook.blocks.IllustrationFrame;
import com.eennou.advancedbook.blocks.IllustrationFrameBlockEntity;
import com.eennou.advancedbook.blocks.ModBlocks;
import com.eennou.advancedbook.items.ModItems;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;

import static com.eennou.advancedbook.AdvancedBook.MODID;
import static com.eennou.advancedbook.blocks.IllustrationFrameRenderer.*;
import static net.minecraft.world.level.block.FaceAttachedHorizontalDirectionalBlock.FACE;
import static net.minecraft.world.level.block.HorizontalDirectionalBlock.FACING;

@Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ForgeClientEvents {
    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
//            RenderSystem.clear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT, Minecraft.ON_OSX);
            Player player = Minecraft.getInstance().player;
            if (player == null || !player.getMainHandItem().is(ModItems.ILLUSTRATION.get())) return;
            int width = Math.max(1, player.getMainHandItem().getOrCreateTag().getShort("width"));
            int height = Math.max(1, player.getMainHandItem().getOrCreateTag().getShort("height"));
            Vec3 playerPos = player.getEyePosition(1.0F);
            Vec3 playerLook = player.getViewVector(1.0F).scale(5.0); // Adjust the distance as needed

            HitResult hitResult = player.level().clip(new ClipContext(playerPos, playerPos.add(playerLook), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));

            if (hitResult.getType() == HitResult.Type.BLOCK) {
                BlockHitResult blockHitResult = (BlockHitResult) hitResult;
                // Get the block position
                BlockPos blockPos = blockHitResult.getBlockPos();
                BlockEntity idk = player.level().getExistingBlockEntity(blockPos);
                if (!(idk instanceof IllustrationFrameBlockEntity hitBlockEntity)) return;

                MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
                PoseStack poseStack = event.getPoseStack();
                poseStack.pushPose();

                Vec3 position = event.getCamera().getPosition();

                poseStack.translate((float) -position.x, (float) -position.y, (float) -position.z);
                poseStack.pushPose();
                Direction xDir = hitBlockEntity.getBlockState().getValue(FACING).getCounterClockWise();
                Direction yDir = switch (hitBlockEntity.getBlockState().getValue(FACE)) {
                    case FLOOR -> hitBlockEntity.getBlockState().getValue(FACING);
                    case WALL -> Direction.DOWN;
                    case CEILING -> hitBlockEntity.getBlockState().getValue(FACING).getOpposite();
                };
                VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.debugQuads());
                for (short x = 0; x < width; x++) {
                    BlockPos columnTop = blockPos.relative(xDir, x);
                    for (short y = 0; y < height; y++) {
                        BlockPos pos = columnTop.relative(yDir, y);
                        boolean isFrame = player.level().getBlockState(pos).is(ModBlocks.ILLUSTRATION_FRAME.get());
                        if (isFrame) {
                            IllustrationFrameBlockEntity blockEntity = (IllustrationFrameBlockEntity) player.level().getExistingBlockEntity(pos);
                            isFrame = blockEntity.getBlockState().getValue(FACING).equals(hitBlockEntity.getBlockState().getValue(FACING))
                                    && blockEntity.getBlockState().getValue(FACE).equals(hitBlockEntity.getBlockState().getValue(FACE))
                                    && blockEntity.getBookElements() == null;
                        }
                        poseStack.pushPose();
                        poseStack.rotateAround(hitBlockEntity.getBlockState().getValue(IllustrationFrame.FACING).getRotation(), pos.getX() + 0.5F, pos.getY() + 0.5F, pos.getZ() + 0.5F);
                        poseStack.rotateAround(switch (hitBlockEntity.getBlockState().getValue(IllustrationFrame.FACE)) {
                            case CEILING -> CEILING_ROT;
                            case WALL -> WALL_ROT;
                            case FLOOR -> FLOOR_ROT;
                        }, pos.getX() + 0.5F, pos.getY() + 0.5F, pos.getZ() + 0.5F);
//                        poseStack.rotateAround(new Quaternionf(new AxisAngle4d(1, 0, 1F, 0)), blockPos.getX() + 0.5F, blockPos.getY() + 0.5F, blockPos.getZ() + 0.5F);
                        Matrix4f matrix = poseStack.last().pose();
                        vertexConsumer.vertex(matrix, pos.getX() + 1.0F, pos.getY() + 0.0F, pos.getZ() + 1.5F / 16F).color(isFrame ? 0.1F : 0.9F, isFrame ? 0.9F : 0.1F, 0.1F, 0.3F).endVertex();
                        vertexConsumer.vertex(matrix, pos.getX() + 1.0F, pos.getY() + 1.0F, pos.getZ() + 1.5F / 16F).color(isFrame ? 0.1F : 0.9F, isFrame ? 0.9F : 0.1F, 0.1F, 0.3F).endVertex();
                        vertexConsumer.vertex(matrix, pos.getX() + 0.0F, pos.getY() + 1.0F, pos.getZ() + 1.5F / 16F).color(isFrame ? 0.1F : 0.9F, isFrame ? 0.9F : 0.1F, 0.1F, 0.3F).endVertex();
                        vertexConsumer.vertex(matrix, pos.getX() + 0.0F, pos.getY() + 0.0F, pos.getZ() + 1.5F / 16F).color(isFrame ? 0.1F : 0.9F, isFrame ? 0.9F : 0.1F, 0.1F, 0.3F).endVertex();
                        poseStack.popPose();
                    }
                }
                bufferSource.endLastBatch();
//                RenderSystem.depthMask(true);
//                RenderSystem.enableDepthTest();
                poseStack.popPose();
                poseStack.popPose();
            }
//            }
        }
    }
}
