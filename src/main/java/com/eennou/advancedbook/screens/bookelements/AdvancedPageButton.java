package com.eennou.advancedbook.screens.bookelements;

import com.eennou.advancedbook.screens.AdvancedBookScreen;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.BookViewScreen;
import net.minecraft.client.gui.screens.inventory.PageButton;

public class AdvancedPageButton extends PageButton {
    private final boolean isForward;
    public AdvancedPageButton(int p_99225_, int p_99226_, boolean isForward, OnPress p_99228_, boolean p_99229_) {
        super(p_99225_, p_99226_, isForward, p_99228_, p_99229_);
        this.isForward = isForward;
    }
    @Override
    public void renderWidget(GuiGraphics guiGraphics, int p_282922_, int p_283637_, float p_282459_) {
        int i = 0;
        int j = 192;
        if (this.isHoveredOrFocused()) {
            i += 23;
        }

        if (!this.isForward) {
            j += 13;
        }
        RenderSystem.enableBlend();
        guiGraphics.blit(AdvancedBookScreen.BOOK_LOCATION, this.getX(), this.getY(), i, j, 23, 13, 512, 256);
        RenderSystem.disableBlend();
    }
}
