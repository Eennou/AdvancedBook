package com.eennou.advancedbook.screens.bookelements;

import com.eennou.advancedbook.ClientConfig;
import com.eennou.advancedbook.blocks.IllustrationFrameRenderer;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistries;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL11;

import java.util.Objects;

import static com.eennou.advancedbook.blocks.IllustrationFrameRenderer.drawFullQuad;
import static com.eennou.advancedbook.blocks.IllustrationFrameRenderer.fucking_shit;

public class ItemElement extends BookElement {
    protected ItemStack itemStack;
    public ItemElement(int x, int y, int width, int height, String item) {
        super(x, y, width, height);
        this.setItem(item);
    }
    public ItemElement(FriendlyByteBuf buf) {
        this(buf.readInt(), buf.readInt(), buf.readInt(), buf.readInt(), buf.readUtf());
    }
    public ItemElement(CompoundTag tag) {
        super(tag);
        this.setItem(tag.getString("item"));
    }

    public void setItem(String item) {
        try {
            this.itemStack = new ItemStack(Objects.requireNonNull(ForgeRegistries.ITEMS.getValue(new ResourceLocation(item))));
        } catch (Exception ignored) {

        }
    }

    public String getItem() {
        return Objects.requireNonNull(ForgeRegistries.ITEMS.getKey(this.itemStack.getItem())).toString();
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void render(GuiGraphics guiGraphics, int xOffset, int yOffset) {
        int minSide = Math.min(this.width, this.height);
        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale(minSide / 16F, minSide / 16F, 1);
        guiGraphics.pose().translate(0, 0, -140);
        guiGraphics.renderFakeItem(this.itemStack, ((xOffset + this.x + (this.width - minSide) / 2) * 16) / minSide, ((yOffset + this.y + (this.height - minSide) / 2) * 16) / minSide);
        guiGraphics.pose().popPose();
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void renderInWorld(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, int combinedLight, int combinedOverlay) {
        int minSide = Math.min(this.width, this.height);
        poseStack.pushPose();
//        poseStack.translate(this.x, this.y, 0);
        poseStack.translate(this.x + (this.width - minSide) / 2, this.y + (this.height - minSide) / 2, 0.05F);
        poseStack.scale(minSide, -minSide, 0.2F);
        poseStack.translate(0.5, -0.5, 0);
//        GL11.glColorMask(false, false, false, false);
//        float[] color = RenderSystem.getShaderColor();
        bufferSource.endBatch();
        Vector4f up = new Vector4f(0, 1, 0, 1);
        Vector4f zero = new Vector4f(0, 0, 0, 1);
        // hacky wacky way to fix odd lighting
        if (Minecraft.getInstance().player.getViewXRot(0) != 90 || this.itemStack.getItem() instanceof BlockItem || (poseStack.last().pose().transform(up).sub(poseStack.last().pose().transform(zero))).x == 0) {
            RenderSystem.setShaderColor(2.3F * 0.8F, 2.3F * 0.8F, 2.3F * 0.8F, 1.0F);
        } else {
            RenderSystem.setShaderColor(0.8F, 0.8F, 0.8F, 1.0F);
        }
        if (ClientConfig.shaderFix) {
            RenderSystem.setShaderColor(1.2F, 1.2F, 1.2F, 1.0F);
        }
        RenderSystem.depthMask(true);
        GL11.glDepthFunc(GL11.GL_LEQUAL);
        Minecraft.getInstance().getItemRenderer().renderStatic(null, this.itemStack, ItemDisplayContext.GUI,
    false, poseStack, bufferSource, Minecraft.getInstance().level, combinedLight, OverlayTexture.NO_OVERLAY, 0
        );
        bufferSource.endBatch();
        poseStack.popPose();
        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.solid());
        GL11.glColorMask(false, false, false, false);
        GL11.glDepthFunc(GL11.GL_ALWAYS);
        RenderSystem.disablePolygonOffset();
        drawFullQuad(poseStack, combinedLight, combinedOverlay, vertexConsumer, IllustrationFrameRenderer.WHITE.sprite(), 0, 0, 2, 2, 1.0F, 0.87F);

        bufferSource.endLastBatch();
        RenderSystem.enablePolygonOffset();
        GL11.glColorMask(true, true, true, true);
        RenderSystem.depthMask(false);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
//        GL11.glColorMask(true, true, true, true);
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeByte(3);
        super.toBytes(buf);
        buf.writeUtf(this.getItem());
    }
    public CompoundTag toCompound() {
        CompoundTag tag = super.toCompound();
        tag.putByte("type", (byte) 3);
        tag.putString("item", this.getItem());
        return tag;
    }
}
