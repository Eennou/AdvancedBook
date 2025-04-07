package com.eennou.advancedbook.blocks;

import com.eennou.advancedbook.AdvancedBook;
import com.eennou.advancedbook.items.ModItems;
import com.eennou.advancedbook.screens.bookelements.BookElement;
import com.eennou.advancedbook.screens.bookelements.RectangleElement;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexSorting;
import me.jellysquid.mods.sodium.client.gl.shader.ShaderLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.Material;
import net.minecraft.nbt.CompoundTag;
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

import java.io.File;
import java.util.concurrent.TimeUnit;

@OnlyIn(Dist.CLIENT)
public class IllustrationFrameRenderer implements BlockEntityRenderer<IllustrationFrameBlockEntity> {
public static final ResourceLocation OVERLAY = new ResourceLocation(AdvancedBook.MODID, "textures/block/illustration_frame_overlay.png");
    public static final ResourceLocation SOAKED = new ResourceLocation(AdvancedBook.MODID, "textures/block/illustration_frame_soaked.png");
    public static final ResourceLocation DUST = new ResourceLocation(AdvancedBook.MODID, "textures/block/dust.png");
//    public static final MultiBufferSource.BufferSource fucking_shit = MultiBufferSource.immediate(new BufferBuilder(256));
    public static final MultiBufferSource.BufferSource fucking_shit = Minecraft.getInstance().renderBuffers().bufferSource();
    public static final Quaternionf CEILING_ROT = new Quaternionf(0.0F, 0.0F, 0.0F, 1.0F);
    public static final Quaternionf WALL_ROT = new Quaternionf(Math.sqrt(2) / 2, 0.0F, 0.0F, -Math.sqrt(2) / 2);
    public static final Quaternionf FLOOR_ROT = new Quaternionf(-1.0F, 0.0F, 0.0F, 0.0F);
    private final Cache<Integer, ResourceLocation> imageCache = CacheBuilder.newBuilder()
            .maximumSize(128)
            .expireAfterAccess(4, TimeUnit.MINUTES)
            .removalListener((RemovalListener<Integer, ResourceLocation>) notification -> Minecraft.getInstance().getTextureManager().release(notification.getValue()))
            .build();
    private final BlockEntityRendererProvider.Context context;
    public IllustrationFrameRenderer(BlockEntityRendererProvider.Context context) {
        this.context = context;
    }
    @Override
    public void render(IllustrationFrameBlockEntity blockEntity, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int combinedLight, int combinedOverlay) {
        if (blockEntity.getBookElements() == null) {
            return;
        }
        int key = blockEntity.getBookElements().hashCode();
        ResourceLocation illustrationImage = imageCache.getIfPresent(key);
        if (illustrationImage == null) {
            illustrationImage = renderToTexture(blockEntity);
            imageCache.put(key, illustrationImage);
        }

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

        if (blockEntity.getBlockState().getValue(IllustrationFrame.SOAKED)) {
            VertexConsumer vertexConsumer = fucking_shit.getBuffer(RenderType.text(SOAKED));
            drawFullQuad(poseStack, combinedLight, combinedOverlay, vertexConsumer, SOAKED, 0, 0, 16, 16, 1.0F, 0.87F);
            fucking_shit.endLastBatch();
        } else {
            VertexConsumer illustrationBuffer = fucking_shit.getBuffer(RenderType.text(illustrationImage));
            drawIllustrationQuad(poseStack, combinedLight, combinedOverlay, illustrationBuffer, blockEntity.offsetX, blockEntity.offsetY, blockEntity.getIllustration().getShort("width"), blockEntity.getIllustration().getShort("height"), illustrationImage, 1.0F, 0.87F);

            fucking_shit.endLastBatch();
            VertexConsumer vertexConsumer = fucking_shit.getBuffer(RenderType.text(OVERLAY));
            drawFullQuad(poseStack, combinedLight, combinedOverlay, vertexConsumer, OVERLAY, 0, 0, 16, 16, 1.0F, 0.87F);
            if (blockEntity.getBlockState().getValue(IllustrationFrame.DUST) >= 5) {
                vertexConsumer = fucking_shit.getBuffer(RenderType.text(DUST));
                drawFullQuad(poseStack, combinedLight, combinedOverlay, vertexConsumer, DUST,
                        4 * blockEntity.getBlockState().getValue(IllustrationFrame.DUST_CLEAN), 4 * (blockEntity.getBlockState().getValue(IllustrationFrame.DUST) / 5 - 1), 4, 4,
                        1.0F, 1.0F
                );
            }

            fucking_shit.endLastBatch();
            RenderSystem.defaultBlendFunc();
        }
        RenderSystem.disablePolygonOffset();
        RenderSystem.polygonOffset(0, 0);
        RenderSystem.depthMask(true);
        poseStack.popPose();
    }

    public static void drawIllustrationQuad(PoseStack poseStack, int combinedLight, int combinedOverlay, VertexConsumer vertexConsumer, int x, int y, int width, int height, ResourceLocation tex, float alpha, float brightness) {
        RenderSystem.setShaderTexture(0, tex);
        RenderSystem.setShader(GameRenderer::getPositionColorTexLightmapShader);
        Matrix4f matrix = poseStack.last().pose();
        vertexConsumer.vertex(matrix, 0, 256F, 0)
            .color(brightness, brightness, brightness, alpha)
            .uv(x/((float)width), (y+1)/((float)height)).overlayCoords(combinedOverlay)
            .uv2(combinedLight)
            .normal(poseStack.last().normal(), 0, 0, 1)
            .endVertex();
        vertexConsumer.vertex(matrix, 256F, 256F, 0)
            .color(brightness, brightness, brightness, alpha)
            .uv((x+1)/((float)width), (y+1)/((float)height)).overlayCoords(combinedOverlay)
            .uv2(combinedLight)
            .normal(poseStack.last().normal(), 0, 0, 1)
            .endVertex();
        vertexConsumer.vertex(matrix, 256F, 0, 0)
            .color(brightness, brightness, brightness, alpha)
            .uv((x+1)/((float)width), y/((float)height)).overlayCoords(combinedOverlay)
            .uv2(combinedLight)
            .normal(poseStack.last().normal(), 0, 0, 1)
            .endVertex();
        vertexConsumer.vertex(matrix, 0, 0, 0)
            .color(brightness, brightness, brightness, alpha)
            .uv(x/((float)width), y/((float)height)).overlayCoords(combinedOverlay)
            .uv2(combinedLight)
            .normal(poseStack.last().normal(), 0, 0, 1)
            .endVertex();
    }
    public static void drawFullQuad(PoseStack poseStack, int combinedLight, int combinedOverlay, VertexConsumer vertexConsumer, ResourceLocation tex, int x, int y, int width, int height, float alpha, float brightness) {
        RenderSystem.setShaderTexture(0, tex);
        RenderSystem.setShader(GameRenderer::getPositionColorTexLightmapShader);
        Matrix4f matrix = poseStack.last().pose();
        Vector3f topLeft = new Vector3f(0, 256F, 0);
        Vector3f bottomRight = new Vector3f(256F, 0, 0);
        vertexConsumer.vertex(matrix, topLeft.x(), topLeft.y(), topLeft.z())
                .color(brightness, brightness, brightness, alpha)
                .uv(x / 16F, (y + height) / 16F).overlayCoords(combinedOverlay)
                .uv2(combinedLight)
                .normal(poseStack.last().normal(), 0, 0, 1)
                .endVertex();
        vertexConsumer.vertex(matrix, bottomRight.x(), topLeft.y(), topLeft.z())
                .color(brightness, brightness, brightness, alpha)
                .uv((x + width) / 16F, (y + height) / 16F).overlayCoords(combinedOverlay)
                .uv2(combinedLight)
                .normal(poseStack.last().normal(), 0, 0, 1)
                .endVertex();
        vertexConsumer.vertex(matrix, bottomRight.x(), bottomRight.y(), topLeft.z())
                .color(brightness, brightness, brightness, alpha)
                .uv((x + width) / 16F, y / 16F).overlayCoords(combinedOverlay)
                .uv2(combinedLight)
                .normal(poseStack.last().normal(), 0, 0, 1)
                .endVertex();
        vertexConsumer.vertex(matrix, topLeft.x(), bottomRight.y(), topLeft.z())
                .color(brightness, brightness, brightness, alpha)
                .uv(x / 16F, y / 16F).overlayCoords(combinedOverlay)
                .uv2(combinedLight)
                .normal(poseStack.last().normal(), 0, 0, 1)
                .endVertex();
    }

    @Override
    public int getViewDistance() {
        return 100;
    }

    protected ResourceLocation renderToTexture(IllustrationFrameBlockEntity blockEntity) {
        CompoundTag illustration = blockEntity.getIllustration();
        int width = illustration.getShort("width") * 256;
        int height = illustration.getShort("height") * 256;
        RenderTarget renderTarget = new TextureTarget(width, height, true, Minecraft.ON_OSX);
        Minecraft minecraft = Minecraft.getInstance();
        try {
            renderTarget.bindWrite(false);
            RenderSystem.enableBlend();
            RenderSystem.enableDepthTest();
            RenderSystem.clearColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.clearDepth(1.0F);
            RenderSystem.setShaderFogColor(0, 0, 0, 0);
            RenderSystem.clear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT, Minecraft.ON_OSX);
            Matrix4f matrix4f = (new Matrix4f()).setOrtho(0.0F, (float) width, (float) height, 0.0F, 1000.0F, net.minecraftforge.client.ForgeHooksClient.getGuiFarPlane());
            RenderSystem.setProjectionMatrix(matrix4f, VertexSorting.ORTHOGRAPHIC_Z);
            PoseStack posestack = RenderSystem.getModelViewStack();
            posestack.pushPose();
            posestack.setIdentity();
            posestack.translate(0.0D, 0.0D, 1000F-net.minecraftforge.client.ForgeHooksClient.getGuiFarPlane());
            RenderSystem.applyModelViewMatrix();
            Lighting.setupFor3DItems();
            GuiGraphics guiGraphics = new GuiGraphics(minecraft, minecraft.renderBuffers().bufferSource());
            for (BookElement element : blockEntity.getBookElements()) {
//                if (element instanceof RectangleElement) continue;
                guiGraphics.pose().translate(0, 0, 50);
                element.render(guiGraphics, 0, 0);
            }
            guiGraphics.flush();
            NativeImage image = Screenshot.takeScreenshot(renderTarget);
            RenderSystem.disableDepthTest();
            RenderSystem.disableBlend();
            posestack.popPose();
            RenderSystem.applyModelViewMatrix();
//            image.writeToFile(new File("./cool-image.png"));
            return Minecraft.getInstance().getTextureManager().register(String.valueOf(blockEntity.getBookElements().hashCode()), new DynamicTexture(image));
        } catch (Exception e) {
            AdvancedBook.LOGGER.error("Failed to render illustration into texture.");
            return Minecraft.getInstance().getTextureManager().register(String.valueOf(blockEntity.getBookElements().hashCode()), new DynamicTexture(width, height, true));
        } finally {
            renderTarget.destroyBuffers();
            minecraft.getMainRenderTarget().bindWrite(true);
        }
    }
}
