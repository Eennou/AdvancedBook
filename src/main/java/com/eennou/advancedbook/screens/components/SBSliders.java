package com.eennou.advancedbook.screens.components;

import com.eennou.advancedbook.screens.AdvancedBookScreen;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;

@OnlyIn(Dist.CLIENT)
public class SBSliders extends AbstractWidget {
    protected int color;
    protected float saturation;
    protected float brightness;
    protected SBSliders.OnChange callback;
    public SBSliders(int x, int y, float initialSaturation, float initialBrightness, SBSliders.OnChange callback) {
        super(x, y, 77, 77, Component.empty());
        this.saturation = initialSaturation;
        this.brightness = initialBrightness;
        this.callback = callback;
        this.color = 0xFFFF0000;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public float getSaturation() {
        return saturation;
    }
    public void setSaturation(float value) {
        this.saturation = value;
    }
    public float getBrightness() {
        return 1 - brightness;
    }
    public void setBrightness(float value) {
        this.brightness = value;
    }
    float lerp(float a, float b, float f) {
        return a + f * (b - a);
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int p_268034_, int p_268009_, float p_268085_) {
        guiGraphics.blitNineSlicedSized(AdvancedBookScreen.BOOK_LOCATION, this.getX(), this.getY(), 77, 77, 4, 4, 9, 9, 32, 224, 512, 256);

        Matrix4f matrix4f = guiGraphics.pose().last().pose();
        VertexConsumer vertexconsumer = guiGraphics.bufferSource().getBuffer(RenderType.gui());
        for (int x = 0; x < this.getWidth() - 4; x++) {
            float r = ((this.color >> 16) & 0xFF) / 255F;
            float g = ((this.color >> 8) & 0xFF) / 255F;
            float b = (this.color & 0xFF) / 255F;
            r = lerp(1, r, (float) x / (this.getWidth() - 4));
            g = lerp(1, g, (float) x / (this.getWidth() - 4));
            b = lerp(1, b, (float) x / (this.getWidth() - 4));
            vertexconsumer.vertex(matrix4f, (float) this.getX() + x + 2, (float) this.getY() + 2, (float) 0).color(r, g, b, 1F).endVertex();
            vertexconsumer.vertex(matrix4f, (float) this.getX() + x + 2, (float) this.getY() + 2 + this.getHeight() - 4, (float) 0).color(0, 0, 0, 1F).endVertex();
            vertexconsumer.vertex(matrix4f, (float) this.getX() + x + 2 + 1, (float) this.getY() + 2 + this.getHeight() - 4, (float) 0).color(0, 0, 0, 1F).endVertex();
            vertexconsumer.vertex(matrix4f, (float) this.getX() + x + 2 + 1, (float) this.getY() + 2, (float) 0).color(r, g, b, 1F).endVertex();
        }
        guiGraphics.flush();

        guiGraphics.blit(AdvancedBookScreen.BOOK_LOCATION, this.getX() + (int)(this.saturation * (this.getHeight() - 6)), this.getY() + (int)(this.brightness * (this.getHeight() - 6)), 32, 218, 6, 6, 512, 256);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int p_93647_, double p_93648_, double p_93649_) {
//        if (super.mouseDragged(mouseX, mouseY, p_93647_, p_93648_, p_93649_)) {
//            return true;
//        }
        this.saturation = Math.max(Math.min(((int)mouseX - this.getX()) / (float)(this.getWidth() - 6), 1), 0);
        this.brightness = Math.max(Math.min(((int)mouseY - this.getY()) / (float)(this.getHeight() - 6), 1), 0);
        callback.onChange(this);
        return true;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {

    }
    @OnlyIn(Dist.CLIENT)
    public interface OnChange {
        void onChange(SBSliders sbSliders);
    }
}
