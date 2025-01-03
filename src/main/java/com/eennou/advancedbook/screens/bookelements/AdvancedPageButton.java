package com.eennou.advancedbook.screens.bookelements;

import com.eennou.advancedbook.screens.AdvancedBookScreen;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.screens.inventory.PageButton;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class AdvancedPageButton extends PageButton {
    private final boolean isForward;
    public AdvancedPageButton(int p_99225_, int p_99226_, boolean isForward, OnPress p_99228_, boolean p_99229_) {
        super(p_99225_, p_99226_, isForward, p_99228_, p_99229_);
        this.isForward = isForward;
    }
    @Override
    public void renderWidget(PoseStack pose, int p_282922_, int p_283637_, float p_282459_) {
        int i = 0;
        int j = 192;
        if (this.isHoveredOrFocused()) {
            i += 23;
        }

        if (!this.isForward) {
            j += 13;
        }
        RenderSystem.enableBlend();
        RenderSystem.setShaderTexture(0, AdvancedBookScreen.BOOK_LOCATION);
        Gui.blit(pose, this.getX(), this.getY(), i, j, 23, 13, 512, 256);
        RenderSystem.disableBlend();
    }
}
