package com.eennou.advancedbook.screens.components;

import com.eennou.advancedbook.screens.AdvancedBookScreen;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class HueSlider extends AbstractWidget {
    protected float value;
    protected HueSlider.OnChange callback;
    public HueSlider(int x, int y, float initialValue, HueSlider.OnChange callback) {
        super(x, y, 12, 77, Component.empty());
        this.value = initialValue;
        this.callback = callback;
    }

    public float getValue() {
        return 1 - value;
    }
    public void setValue(float value) {
        this.value = value;
    }

    @Override
    public void renderWidget(PoseStack pose, int p_268034_, int p_268009_, float p_268085_) {
        RenderSystem.setShaderTexture(0, AdvancedBookScreen.BOOK_LOCATION);
        pose.pushPose();
        pose.translate(-this.getX(), 0, 0);
        pose.scale(2, 1, 1);
        Gui.blitNineSliced(pose, this.getX(), this.getY(), 12 / 2, 77, 4 / 2, 4, 10 / 2, 10, 32 / 2, 224);
        pose.popPose();
        Gui.blit(pose, this.getX() + 2, this.getY() + 2, 46, 182, 8, 73, 512, 256);
        Gui.blit(pose, this.getX(), this.getY() + (int)(this.value * (this.getHeight() - 6)), 32, 234, 12, 6, 512, 256);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int p_93647_, double p_93648_, double p_93649_) {
//        if (super.mouseDragged(mouseX, mouseY, p_93647_, p_93648_, p_93649_)) {
//            return true;
//        }
        this.value = Math.max(Math.min(((int)mouseY - 2 - this.getY()) / (float)(this.getHeight() - 6), 1), 0);
        callback.onChange(this);
        return true;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int p_93643_) {
        if (isHovered()) {
            this.value = Math.max(Math.min(((int)mouseY - 2 - this.getY()) / (float)(this.getHeight() - 6), 1), 0);
        }
        callback.onChange(this);
        return super.mouseClicked(mouseX, mouseY, p_93643_);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {

    }
    @OnlyIn(Dist.CLIENT)
    public interface OnChange {
        void onChange(HueSlider hueSlider);
    }
}
