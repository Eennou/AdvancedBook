package com.eennou.advancedbook.blocks;

import com.eennou.advancedbook.AdvancedBook;
import com.eennou.advancedbook.items.ModItems;
import com.eennou.advancedbook.screens.bookelements.BookElement;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.Material;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.AxisAngle4f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;

@OnlyIn(Dist.CLIENT)
public class IllustrationFrameRenderer implements BlockEntityRenderer<IllustrationFrameBlockEntity> {
    public static final Material WHITE = new Material(InventoryMenu.BLOCK_ATLAS,
            new ResourceLocation(AdvancedBook.MODID, "block/white"));
    public static final Material OVERLAY = new Material(InventoryMenu.BLOCK_ATLAS,
            new ResourceLocation(AdvancedBook.MODID, "block/illustration_frame_overlay"));
    public static final Material DUST = new Material(InventoryMenu.BLOCK_ATLAS,
            new ResourceLocation(AdvancedBook.MODID, "block/dust"));
    public static final Quaternionf CEILING_ROT = new Quaternionf(0.0F, 0.0F, 0.0F, 1.0F);
    public static final Quaternionf WALL_ROT = new Quaternionf(Math.sqrt(2) / 2, 0.0F, 0.0F, -Math.sqrt(2) / 2);
    public static final Quaternionf FLOOR_ROT = new Quaternionf(-1.0F, 0.0F, 0.0F, 0.0F);
    private final BlockEntityRendererProvider.Context context;
    public IllustrationFrameRenderer(BlockEntityRendererProvider.Context context) {
        this.context = context;
    }
    @Override
    public void render(IllustrationFrameBlockEntity blockEntity, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int combinedLight, int combinedOverlay) {
//        GL11.glColorMask(false, false, false, false);
        if (blockEntity.getBookElements() == null) {
            return;
        }
        ((MultiBufferSource.BufferSource)bufferSource).endBatch();
//        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
        poseStack.pushPose();
        poseStack.rotateAround(blockEntity.getBlockState().getValue(IllustrationFrame.FACING).getRotation(), 0.5F, 0.5F, 0.5F);
        poseStack.rotateAround(switch (blockEntity.getBlockState().getValue(IllustrationFrame.FACE)) {
            case CEILING -> CEILING_ROT;
            case WALL -> WALL_ROT;
            case FLOOR -> FLOOR_ROT;
        }, 0.5F, 0.5F, 0.5F);
        poseStack.translate(0, 1F, 1 / 16F);
        poseStack.scale(1 / 256F, -1 / 256F, 1 / 256F);

        RenderSystem.enablePolygonOffset();
        RenderSystem.polygonOffset(-1.0F, -10.0F);
        RenderSystem.depthMask(false);

        GL11.glEnable(GL11.GL_STENCIL_TEST);
        Minecraft.getInstance().getMainRenderTarget().enableStencil();
        RenderSystem.stencilFunc(GL11.GL_ALWAYS, 1, 0xFF);
        RenderSystem.stencilMask(0xFF);
        RenderSystem.clearStencil(0);
        GL11.glClear(GL11.GL_STENCIL_BUFFER_BIT);
        RenderSystem.stencilOp(GL11.GL_REPLACE, GL11.GL_KEEP, GL11.GL_REPLACE);

        // Draw full frame
//        GL11.glColorMask(false, false, false, false);
        final TextureAtlasSprite tex = WHITE.sprite();

        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.solid());
        drawFullQuad(poseStack, combinedLight, combinedOverlay, vertexConsumer, tex, 0, 2, 2, 1.0F, 0.87F);

        ((MultiBufferSource.BufferSource)bufferSource).endLastBatch();
//        GL11.glColorMask(true, true, true, true);

        RenderSystem.stencilFunc(GL11.GL_NOTEQUAL, 0, 0xFF);
        RenderSystem.stencilMask(0x00);
        GL11.glDepthFunc(GL11.GL_ALWAYS);
        poseStack.pushPose();
        poseStack.translate(-256 * blockEntity.offsetX, -256 * blockEntity.offsetY, 0);
        for (BookElement element: blockEntity.getBookElements()) {
            element.renderInWorld(poseStack, bufferSource, combinedLight, combinedOverlay);
        }
        ((MultiBufferSource.BufferSource)bufferSource).endBatch(RenderType.solid());
        poseStack.popPose();

        final TextureAtlasSprite overlay = OVERLAY.sprite();
        final TextureAtlasSprite dust = DUST.sprite();
        vertexConsumer = bufferSource.getBuffer(RenderType.translucent());
        drawFullQuad(poseStack, combinedLight, combinedOverlay, vertexConsumer, overlay, 0, 16, 16, 0.2F, 0.87F);
        drawFullQuad(poseStack, combinedLight, combinedOverlay, vertexConsumer, dust,
                4 * blockEntity.getBlockState().getValue(IllustrationFrame.DUST_CLEAN), 4, 16,
                blockEntity.getBlockState().getValue(IllustrationFrame.DUST) / 20F, 1.0F
        );

        ((MultiBufferSource.BufferSource)bufferSource).endLastBatch();

        GL11.glDisable(GL11.GL_STENCIL_TEST);
        GL11.glDepthFunc(GL11.GL_LEQUAL);
//        GL11.glClear(GL11.GL_STENCIL_BUFFER_BIT);
        RenderSystem.disablePolygonOffset();
        RenderSystem.polygonOffset(0, 0);
        RenderSystem.depthMask(true);
        poseStack.popPose();
    }

    public static void drawFullQuad(PoseStack poseStack, int combinedLight, int combinedOverlay, VertexConsumer vertexConsumer, TextureAtlasSprite tex, int x, int width, int height, float alpha, float brightness) {
        Matrix4f matrix = poseStack.last().pose();
        Vector3f topLeft = new Vector3f(0, 256F, 0);
        Vector3f bottomRight = new Vector3f(256F, 0, 0);
        vertexConsumer.vertex(matrix, topLeft.x(), topLeft.y(), topLeft.z())
            .color(brightness, brightness, brightness, alpha)
            .uv(tex.getU(x), tex.getV(height)).overlayCoords(combinedOverlay)
            .uv2(combinedLight)
            .normal(poseStack.last().normal(), 0, 0, 1)
            .endVertex();
        vertexConsumer.vertex(matrix, bottomRight.x(), topLeft.y(), topLeft.z())
            .color(brightness, brightness, brightness, alpha)
            .uv(tex.getU(x + width), tex.getV(height)).overlayCoords(combinedOverlay)
            .uv2(combinedLight)
            .normal(poseStack.last().normal(), 0, 0, 1)
            .endVertex();
        vertexConsumer.vertex(matrix, bottomRight.x(), bottomRight.y(), topLeft.z())
            .color(brightness, brightness, brightness, alpha)
            .uv(tex.getU(x + width), tex.getV(0)).overlayCoords(combinedOverlay)
            .uv2(combinedLight)
            .normal(poseStack.last().normal(), 0, 0, 1)
            .endVertex();
        vertexConsumer.vertex(matrix, topLeft.x(), bottomRight.y(), topLeft.z())
            .color(brightness, brightness, brightness, alpha)
            .uv(tex.getU(x), tex.getV(0)).overlayCoords(combinedOverlay)
            .uv2(combinedLight)
            .normal(poseStack.last().normal(), 0, 0, 1)
            .endVertex();
    }

    @Override
    public int getViewDistance() {
        return 100;
    }
}
