package com.eennou.advancedbook.events;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;

public class ForgeClientEvents {
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
//        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_SOLID_BLOCKS) {
//            Player player = Minecraft.getInstance().player;
//            Vec3 playerPos = player.getEyePosition(1.0F);
//            Vec3 playerLook = player.getViewVector(1.0F).scale(5.0); // Adjust the distance as needed
//
//            HitResult hitResult = player.level().clip(new ClipContext(playerPos, playerPos.add(playerLook), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
//
//            if (hitResult.getType() == HitResult.Type.BLOCK) {
//                BlockHitResult blockHitResult = (BlockHitResult) hitResult;
//                // Get the block position
//                BlockPos blockPos = blockHitResult.getBlockPos();
//            }
//            MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
////                Matrix4f matrix = event.getPoseStack().last().pose();
//            Matrix4f matrix = new Matrix4f();
//            VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.debugQuads());
//            vertexConsumer.vertex(matrix, 0, 0, 0).color(1F, 1F, 1F, 1F);
//            vertexConsumer.vertex(matrix, 0, 100, 0).color(1F, 1F, 1F, 1F);
//            vertexConsumer.vertex(matrix, 100, 100, 0).color(1F, 1F, 1F, 1F);
//            vertexConsumer.vertex(matrix, 100, 0, 0).color(1F, 1F, 1F, 1F);
////                vertexConsumer.vertex(matrix, blockPos.getX(), blockPos.getY(), blockPos.getZ()).color(0, 0, 0, 1F);
////                vertexConsumer.vertex(matrix, blockPos.getX(), blockPos.getY()+100, blockPos.getZ()).color(0, 0, 0, 1F);
////                vertexConsumer.vertex(matrix, blockPos.getX(), blockPos.getY()+100, blockPos.getZ()+100).color(0, 0, 0, 1F);
////                vertexConsumer.vertex(matrix, blockPos.getX(), blockPos.getY(), blockPos.getZ()+100).color(0, 0, 0, 1F);
//            bufferSource.endBatch(RenderType.debugQuads());
////            }
//        }
    }
}
