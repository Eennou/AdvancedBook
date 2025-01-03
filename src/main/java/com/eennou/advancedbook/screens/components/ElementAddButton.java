package com.eennou.advancedbook.screens.components;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ElementAddButton extends ImageButton {
    private Component text;
    private Font font;
    private int iconX;
    private int iconY;
    private int iconWidth;
    private int iconHeight;

    public ElementAddButton(Font font, Component text, int p_169011_, int p_169012_, int p_169013_, int p_169014_, int iconX, int iconY, int iconWidth, int iconHeight, int p_169015_, int p_169016_, ResourceLocation p_169017_, OnPress p_169018_) {
        super(p_169011_, p_169012_, p_169013_, p_169014_, p_169015_, p_169016_, p_169017_, p_169018_);
        this.font = font;
        this.text = text;
        this.iconX = iconX;
        this.iconY = iconY;
        this.iconWidth = iconWidth;
        this.iconHeight = iconHeight;
    }

    public ElementAddButton(Font font, Component text, int p_94269_, int p_94270_, int p_94271_, int p_94272_, int iconX, int iconY, int iconWidth, int iconHeight, int p_94273_, int p_94274_, int p_94275_, ResourceLocation p_94276_, OnPress p_94277_) {
        super(p_94269_, p_94270_, p_94271_, p_94272_, p_94273_, p_94274_, p_94275_, p_94276_, p_94277_);
        this.font = font;
        this.text = text;
        this.iconX = iconX;
        this.iconY = iconY;
        this.iconWidth = iconWidth;
        this.iconHeight = iconHeight;
    }

    public ElementAddButton(Font font, Component text, int p_94230_, int p_94231_, int p_94232_, int p_94233_, int iconX, int iconY, int iconWidth, int iconHeight, int p_94234_, int p_94235_, int p_94236_, ResourceLocation p_94237_, int p_94238_, int p_94239_, OnPress p_94240_) {
        super(p_94230_, p_94231_, p_94232_, p_94233_, p_94234_, p_94235_, p_94236_, p_94237_, p_94238_, p_94239_, p_94240_);
        this.font = font;
        this.text = text;
        this.iconX = iconX;
        this.iconY = iconY;
        this.iconWidth = iconWidth;
        this.iconHeight = iconHeight;
    }

    public ElementAddButton(Font font, Component text, int p_94256_, int p_94257_, int p_94258_, int p_94259_, int iconX, int iconY, int iconWidth, int iconHeight, int p_94260_, int p_94261_, int p_94262_, ResourceLocation p_94263_, int p_94264_, int p_94265_, OnPress p_94266_, Component p_94267_) {
        super(p_94256_, p_94257_, p_94258_, p_94259_, p_94260_, p_94261_, p_94262_, p_94263_, p_94264_, p_94265_, p_94266_, p_94267_);
        this.font = font;
        this.text = text;
        this.iconX = iconX;
        this.iconY = iconY;
        this.iconWidth = iconWidth;
        this.iconHeight = iconHeight;
    }

    @Override
    public void renderWidget(PoseStack pose, int p_281473_, int p_283021_, float p_282518_) {
        super.renderWidget(pose, p_281473_, p_283021_, p_282518_);
        pose.pushPose();
        Gui.drawCenteredString(pose, this.font, this.text, this.getX() + 20 + (this.width - 20) / 2, this.getY() + 6, 0xFFFFFF);
        RenderSystem.setShaderTexture(0, this.resourceLocation);
        Gui.blit(pose, this.getX(), this.getY(), this.iconX, this.iconY, this.iconWidth, this.iconHeight, 512, 256);
        pose.popPose();
    }
}
