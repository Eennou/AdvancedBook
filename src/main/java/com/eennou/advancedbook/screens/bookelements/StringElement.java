package com.eennou.advancedbook.screens.bookelements;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.font.TextFieldHelper;
import net.minecraft.locale.Language;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;
import org.apache.commons.lang3.reflect.FieldUtils;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Objects;

public class StringElement extends BookElement implements ColorableBookElement {
    protected int color;
    protected Component text;
    @OnlyIn(Dist.CLIENT)
    private TextFieldHelper textEdit;

    @OnlyIn(Dist.CLIENT)
    private void setClipboard(String contents) {
        TextFieldHelper.setClipboardContents(Minecraft.getInstance(), contents);
    }

    @OnlyIn(Dist.CLIENT)
    private String getClipboard() {
        return TextFieldHelper.getClipboardContents(Minecraft.getInstance());
    }

    protected int scale;
    protected float hAlign;
    protected float vAlign;
    protected int frameTick;
    protected int cursorPos;
    protected int selectionCursorPos;

    public StringElement(int x, int y, int width, int height, int color, Component text) {
        super(x, y, width, height);
        this.color = color;
        this.text = text;
        this.scale = 1;
        this.hAlign = 0;
        this.vAlign = 0;
        this.frameTick = 0;
        this.selectionCursorPos = this.cursorPos = text.getString().length();
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            this.textEdit = new TextFieldHelper(this::getString, this::setText, this::getClipboard, this::setClipboard, (value) -> {
                return value.length() < 512;
            });
            this.textEdit.setCursorToEnd(true);
        });
    }
    public StringElement(int x, int y, int width, int height, int scale, float hAlign, float vAlign, int color, Component text) {
        this(x, y, width, height, color, text);
        this.scale = Math.max(1, scale);
        this.hAlign = hAlign;
        this.vAlign = vAlign;
    }
    public StringElement(FriendlyByteBuf buf) {
        this(buf.readInt(), buf.readInt(), buf.readInt(), buf.readInt(), buf.readInt(), buf.readFloat(), buf.readFloat(), buf.readInt(), Component.literal(buf.readUtf()));
    }
    public StringElement(CompoundTag tag) {
        super(tag);
        this.scale = Math.max(1, tag.getInt("scale"));
        this.hAlign = tag.getFloat("hAlign");
        this.vAlign = tag.getFloat("vAlign");
        this.color = tag.getInt("color");
        this.text = Component.literal(tag.getString("text"));
    }
    @Override
    public void setColor(int color) {
        this.color = color;
    }

    @Override
    public int getColor() {
        return this.color;
    }

    public void setText(Component text) {
        this.text = text;
    }

    public Component getText() {
        return this.text;
    }

    public void setText(String text) {
        this.text = Component.literal(text);
    }

    public String getString() {
        return this.text != null ? this.text.getString() : "";
    }

    public void setScale(int scale) {
        this.scale = Math.max(1, scale);
    }

    public void setHAlign(float hAlign) {
        this.hAlign = hAlign;
    }

    public void setVAlign(float vAlign) {
        this.vAlign = vAlign;
    }

    public int getScale() {
        return this.scale;
    }
    public float getHAlign() {
        return this.hAlign;
    }

    public float getVAlign() {
        return this.vAlign;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void render(GuiGraphics guiGraphics, int xOffset, int yOffset) {
        Font font = Minecraft.getInstance().font;
        List<FormattedText> lines = font.getSplitter().splitLines(this.text, (this.width + 1) / scale, Style.EMPTY);
        List<FormattedCharSequence> sequenceList = Language.getInstance().getVisualOrder(lines);
        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale(scale, scale, 1);
        int yOffsetChangeable = yOffset;
        for (FormattedText line : lines) {
            Style style;
            try {
                style = (Style) FieldUtils.readField(line, "style", true);
            } catch (IllegalArgumentException | IllegalAccessException e) {
                style = Style.EMPTY;
                try {
                    for (Field a : line.getClass().getDeclaredFields()) {
                        if ((Objects.equals(a.getType().getTypeName(), "net.minecraft.network.chat.Style"))) {
                            a.setAccessible(true);
                            try {
                                style = (Style) a.get(line);
                            } catch (IllegalAccessException ex) {
                                throw new RuntimeException(ex);
                            }
                        }
                    }
                } catch (Exception ignored) {}
            }
            String styleStr = styleToStr(style);
            guiGraphics.drawString(font, Component.literal(styleStr + line.getString()), (int)(x + xOffset + (this.width - font.width(line) * scale) * this.hAlign) / scale, (int)(y + yOffsetChangeable + (this.height - font.lineHeight * lines.size() * scale) * this.vAlign) / scale, color, false);
            yOffsetChangeable += font.lineHeight * scale;
        }
        guiGraphics.pose().popPose();
        this.frameTick++;
    }

    @OnlyIn(Dist.CLIENT)
    public void renderSelection(GuiGraphics guiGraphics) { // TODO: fix for scaled text
        super.renderSelection(guiGraphics);
        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale(scale, scale, 1);
        Font font = Minecraft.getInstance().font;
        List<FormattedText> lines = font.getSplitter().splitLines(this.text, (this.width + 1) / scale, Style.EMPTY);
//        int xCursorPos = this.cursorPos;
//        int yCursorPos = 0;
//        while (lines.size() > yCursorPos && xCursorPos > lines.get(yCursorPos).getString().length()) {
//            xCursorPos -= lines.get(yCursorPos).getString().length();
//            yCursorPos += 1;
//            if (yCursorPos >= lines.size()) {
//                yCursorPos = lines.size() - 1;
//                xCursorPos = lines.get(yCursorPos).getString().length();
//                break;
//            }
//        }
        Pos2i selectionPosRC = getCursorRC(lines, this.textEdit.getSelectionPos());
        Pos2i cursorPosRC = getCursorRC(lines, this.textEdit.getCursorPos());
//        guiGraphics.drawString(font,
//            FormattedCharSequence.forward("|", Style.EMPTY.withColor(ChatFormatting.RESET)),
//            (int)(x + (this.width - ((lines.size() > selectionPosRC.y) ? font.width(lines.get(selectionPosRC.y)) * scale : 0)) * this.hAlign) / scale - 1 + ((lines.size() > selectionPosRC.y) ? font.width(lines.get(selectionPosRC.y).getString().substring(0, selectionPosRC.x)) : 0),
//            (int)(y + selectionPosRC.y * font.lineHeight * scale + (this.height - font.lineHeight * lines.size() * scale) * this.vAlign) / scale, (this.frameTick % 60 < 30) ? 0xFF44FFFF : 0xFFFF4444,
//            false
//        );
        int minY = Math.min(selectionPosRC.y, cursorPosRC.y);
        int xForFirstPos = (selectionPosRC.y == cursorPosRC.y) ? Math.min(selectionPosRC.x, cursorPosRC.x) : ((selectionPosRC.y < cursorPosRC.y) ? selectionPosRC.x : cursorPosRC.x);
        int maxY = Math.max(selectionPosRC.y, cursorPosRC.y);
        int xForLastPos = (selectionPosRC.y == cursorPosRC.y) ? Math.max(selectionPosRC.x, cursorPosRC.x) : ((selectionPosRC.y > cursorPosRC.y) ? selectionPosRC.x : cursorPosRC.x);
        if (!lines.isEmpty() || !(minY == maxY && xForFirstPos == xForLastPos)) {
            int strStart = Math.min(this.textEdit.getCursorPos(), this.textEdit.getSelectionPos());
            for (int i = minY; i <= maxY; i++) {
                try {
                    int selX = (int) (x + (this.width - font.width(lines.get(i)) * scale) * this.hAlign) / scale;
                    Style style = Style.EMPTY;
                    Style style2 = Style.EMPTY;
//                    try {
//                        style = (Style) FieldUtils.readField(lines.get(i), "style", true);
//                    } catch (IllegalArgumentException | IllegalAccessException e) {
//                        style = Style.EMPTY;
//                    }

                    if (i == minY) {
                        style = calcTextStyleAtIndex(strStart);
                        style2 = calcTextStyleAtIndex(strStart - xForFirstPos);
                        strStart += lines.get(i).getString().length() - xForFirstPos;
                    } else {
                        style = calcTextStyleAtIndex(strStart);
                        style2 = style;
                        strStart += lines.get(i).getString().length();
                    }
//                    if (xForFirstPos > lines.get(i).getString().length()) continue;
                    String styleStr = styleToStr(style);
                    String style2Str = styleToStr(style2);
                    int lTextWidth = (i == minY) ? font.width(Component.literal(style2Str + lines.get(i).getString().substring(0, xForFirstPos))) : 0;
//                    lTextWidth = 0;
//                    guiGraphics.drawString(font, Component.literal(style2Str + lines.get(i).getString().substring(0, xForFirstPos)), 0, 100, 0xFFFF0000, false);
                    guiGraphics.fill(
                            selX + lTextWidth,
                            (int)(y / scale + font.lineHeight * i + ((this.height - font.lineHeight * lines.size() * scale) * this.vAlign) / scale),
                            selX + ((i == maxY) ? font.width(Component.literal(style2Str + lines.get(i).getString().substring(0, xForLastPos))) : font.width(lines.get(i))),
                            (int) (y / scale + (i + 1) * font.lineHeight + ((this.height - font.lineHeight * lines.size() * scale) * this.vAlign) / scale),
                            this.color
                    );
                    guiGraphics.drawString(font,
                            Component.literal(styleStr + lines.get(i).getString().substring((i == minY) ? xForFirstPos : 0, (i == maxY) ? xForLastPos : lines.get(i).getString().length())),
                            selX + ((i == minY) ? lTextWidth : 0),
                            (int)(y / scale + font.lineHeight * i + ((this.height - font.lineHeight * lines.size() * scale) * this.vAlign) / scale),
                            invertRGB(this.color),
                            false
                    );
                } catch (Exception e) {
                    throw e;
                }
            }
        }
        Style style;
        try {
            style = (Style) FieldUtils.readField(lines.get(cursorPosRC.y), "style", true);
        } catch (Exception e) {
            style = Style.EMPTY;
            try {
                for (Field a : lines.get(cursorPosRC.y).getClass().getDeclaredFields()) {
                    if ((Objects.equals(a.getType().getTypeName(), "net.minecraft.network.chat.Style"))) {
                        a.setAccessible(true);
                        try {
                            style = (Style) a.get(lines.get(cursorPosRC.y));
                        } catch (IllegalAccessException ex) {
                            throw new RuntimeException(ex);
                        }
                    }
                }
            } catch (Exception ignored) {}
        }
        String styleStr = (style.isBold() ? "§l" : "") + (style.isItalic() ? "§o" : "") + (style.isUnderlined() ? "§n" : "") + (style.isStrikethrough() ? "§m" : "");
        guiGraphics.drawString(font,
            FormattedCharSequence.forward("|", Style.EMPTY.withColor(ChatFormatting.RESET)),
            (int)(x + (this.width - ((lines.size() > cursorPosRC.y) ? font.width(Component.literal(styleStr + lines.get(cursorPosRC.y).getString())) * scale : 0)) * this.hAlign) / scale - 1
            + ((lines.size() > cursorPosRC.y) ? font.width(Component.literal(styleStr + lines.get(cursorPosRC.y).getString().substring(0, cursorPosRC.x))) : 0),
            (int)(y + cursorPosRC.y * font.lineHeight * scale + (this.height - font.lineHeight * lines.size() * scale) * this.vAlign) / scale, (this.frameTick % 60 < 30) ? 0xFFFFFFFF : 0xFF444444,
            false
        );
        guiGraphics.pose().popPose();
    }

    @OnlyIn(Dist.CLIENT)
    Style calcTextStyleAtIndex(int index) {
        Style result = Style.EMPTY;
        String s = this.text.getString();
        for (int i = 0; i < index && i < s.length(); i++) {
            if (s.charAt(i) == '§') {
                if (i + 1 < s.length()) {
                    switch (s.charAt(i + 1)) {
                        case 'l':
                            result = result.withBold(true);
                            break;
                        case 'o':
                            result = result.withItalic(true);
                            break;
                        case 'n':
                            result = result.withUnderlined(true);
                            break;
                        case 'm':
                            result = result.withStrikethrough(true);
                            break;
                        case 'r':
                            result = Style.EMPTY;
                            break;
                        default:
                            break;
                    }
                }
            }
        }
        return result;
    }

    @OnlyIn(Dist.CLIENT)
    int invertRGB(int color) {
        int a = (color >>> 24) & 0xFF;
        int r = (color >>> 16) & 0xFF;
        int g = (color >>> 8) & 0xFF;
        int b = color & 0xFF;
        r = 255 - r;
        g = 255 - g;
        b = 255 - b;
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    @OnlyIn(Dist.CLIENT)
    private Pos2i getCursorRC(List<FormattedText> lines, int pos) {
        int xCursorPos = pos;
        int yCursorPos = 0;
        while (lines.size() > yCursorPos && xCursorPos > lines.get(yCursorPos).getString().length()) {
            xCursorPos -= lines.get(yCursorPos).getString().length();
            char chr = this.text.getString().charAt(pos - xCursorPos);
            if (chr == ' ' || chr == '\n') {
                xCursorPos -= 1;
            }
            yCursorPos += 1;
            if (yCursorPos >= lines.size()) {
                yCursorPos = lines.size() - 1;
                xCursorPos = lines.get(yCursorPos).getString().length();
                break;
            }
        }
        return new Pos2i(xCursorPos, yCursorPos);
    }

    @OnlyIn(Dist.CLIENT)
    static class Pos2i {
        public final int x;
        public final int y;

        Pos2i(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    private boolean isDragged = false;

    @OnlyIn(Dist.CLIENT)
    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int buttons) {
        if (buttons != 0) return super.mouseDragged(mouseX, mouseY, buttons);
        int pos = getIndexByMousePos(mouseX, mouseY);
        this.textEdit.setCursorPos(pos);
        if (!this.isDragged) {
            this.textEdit.setSelectionPos(pos);
        }
        this.isDragged = true;
        return true;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public boolean mouseRelease(double mouseX, double mouseY, int buttons) {
        int pos = getIndexByMousePos(mouseX, mouseY);
        if (!this.isDragged) {
            this.textEdit.setSelectionPos(pos);
            this.textEdit.setCursorPos(pos);
        }
        this.isDragged = false;
        return true;
    }

    @OnlyIn(Dist.CLIENT)
    int getIndexByMousePos(double mouseX, double mouseY) { // TODO: fix for scaled text
        int pos = 0;
        Font font = Minecraft.getInstance().font;
        List<FormattedText> lines = font.getSplitter().splitLines(this.text, (this.width + 1) / scale, Style.EMPTY);
        if (lines.isEmpty()) return 0;
        int yPos = (int)((mouseY - (this.height - font.lineHeight * scale * lines.size()) * this.vAlign / scale - this.y) / (font.lineHeight * scale));
        if (yPos < 0) return 0;
        if (yPos >= lines.size()) {
            return this.text.getString().length();
        }
        for (int i = 0; i < yPos; i++) {
            pos += lines.get(i).getString().length();
            char chr = this.text.getString().charAt(pos);
            if (chr == ' ' || chr == '\n') {
                pos += 1;
            }
        }
        Style style = calcTextStyleAtIndex(pos);
        String styleStr = styleToStr(style);
        Component t = Component.literal(styleStr + lines.get(yPos).getString());
        pos += font.substrByWidth(t, (int) Math.ceil(mouseX / scale - (this.width - font.width(t) * scale) * this.hAlign / scale + 2 - (double) this.x / scale)).getString().length() - styleStr.length();
        return pos;
    }

    @OnlyIn(Dist.CLIENT)
    private String styleToStr(Style style) {
        return (style.isBold() ? "§l" : "") + (style.isItalic() ? "§o" : "") + (style.isUnderlined() ? "§n" : "") + (style.isStrikethrough() ? "§m" : "");
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public boolean keyPressed(int key, int scan, int modifiers) {
        String s = this.text.getString();
        while (this.textEdit.getCursorPos() == this.textEdit.getSelectionPos() && key == 259 && this.textEdit.getCursorPos() > 1 && s.charAt(this.textEdit.getCursorPos() - 2) == '§') {
            this.textEdit.moveByChars(-2);
        }
        while (this.textEdit.getCursorPos() == this.textEdit.getSelectionPos() && key == 261 && this.textEdit.getCursorPos() < s.length() && s.charAt(this.textEdit.getCursorPos()) == '§') {
            this.textEdit.moveByChars(2);
        }
        if (key == 259 && textEdit.getSelectionPos() != this.textEdit.getCursorPos()) {
            this.textEdit.insertText(
                "§r" + styleToStr(calcTextStyleAtIndex(Math.max(this.textEdit.getCursorPos(), this.textEdit.getSelectionPos())))
            );
            optimizeFormatting();
            return true;
        }
        this.textEdit.keyPressed(key);
        if (key == 257) {
            this.textEdit.insertText("\n");
        }
        while (key == 262 && this.textEdit.getCursorPos() < s.length() && s.charAt(this.textEdit.getCursorPos()) == '§') {
            this.textEdit.moveByChars(2, true);
        }
        while (key == 263 && this.textEdit.getCursorPos() > 0 && s.charAt(this.textEdit.getCursorPos() - 1) == '§') {
            this.textEdit.moveByChars(-2, true);
        }
        // TODO: implement up/down cursor keys
//        this.cursorPos = this.textEdit.getCursorPos();
        return true;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public boolean charTyped(char chr, int modifiers) {
        this.textEdit.charTyped(chr);
        return super.charTyped(chr, modifiers);
    }

    @OnlyIn(Dist.CLIENT)
    public void applyFormatting(Style target) {
        int startSel = Math.min(this.textEdit.getCursorPos(), this.textEdit.getSelectionPos());
        String s = this.text.getString();
        while (startSel < s.length() && s.charAt(startSel) == '§') startSel += 2;
        int endSel = Math.max(this.textEdit.getCursorPos(), this.textEdit.getSelectionPos());
        if (endSel < startSel) endSel = startSel;
        Style styleStart = calcTextStyleAtIndex(startSel);
        Style styleEnd = calcTextStyleAtIndex(endSel);
        if (target.isBold()) {
            this.textEdit.insertText(
                "§r" + styleToStr(styleStart.withBold(!styleStart.isBold()))
                + s.substring(startSel, endSel) +
                "§r" + styleToStr(styleEnd)
            );
        }
        if (target.isItalic()) {
            this.textEdit.insertText(
                    "§r" + styleToStr(styleStart.withItalic(!styleStart.isItalic()))
                            + s.substring(startSel, endSel) +
                            "§r" + styleToStr(styleEnd)
            );
        }
        if (target.isUnderlined()) {
            this.textEdit.insertText(
                    "§r" + styleToStr(styleStart.withUnderlined(!styleStart.isUnderlined()))
                            + s.substring(startSel, endSel) +
                            "§r" + styleToStr(styleEnd)
            );
        }
        if (target.isStrikethrough()) {
            this.textEdit.insertText(
                    "§r" + styleToStr(styleStart.withStrikethrough(!styleStart.isStrikethrough()))
                            + s.substring(startSel, endSel) +
                            "§r" + styleToStr(styleEnd)
            );
        }
        optimizeFormatting();
    }

    @OnlyIn(Dist.CLIENT)
    private void optimizeFormatting() { // TODO: rewrite that nonsense
        String s = this.text.getString();
        StringBuilder optimized = new StringBuilder();
//        AdvancedBook.LOGGER.debug("{}", s.replaceAll("§", "<"));
        Style current = Style.EMPTY;
        Style seqStyle = Style.EMPTY;
        int foundSeq = 0;
        for (int i = 0; i < s.length(); i++) {
            char chr = s.charAt(i);
            if (chr == '§') {
                if (i + 1 < s.length()) {
                    i++;
                } else {
                    optimized.append(chr);
                    continue;
                }
                char mod = s.charAt(i);
                Style t = current;
                if (mod == 'r' && !current.isEmpty()) {
                    current = Style.EMPTY;
                } else if (mod == 'l' && !current.isBold()) {
                    current = current.withBold(true);
                } else if (mod == 'o' && !current.isItalic()) {
                    current = current.withItalic(true);
                } else if (mod == 'n' && !current.isUnderlined()) {
                    current = current.withUnderlined(true);
                } else if (mod == 'm' && !current.isStrikethrough()) {
                    current = current.withStrikethrough(true);
                } else {
                    continue;
                }
                optimized.append(chr);
                optimized.append(mod);
                if (foundSeq == 0) {
                    seqStyle = t;
                }
                foundSeq += 2;
                continue;
            } else if (foundSeq > 0) {
                optimized.delete(optimized.length() - foundSeq, optimized.length());
                if (!current.isBold() && seqStyle.isBold()
                    || !current.isItalic() && seqStyle.isItalic()
                    || !current.isUnderlined() && seqStyle.isUnderlined()
                    || !current.isStrikethrough() && seqStyle.isStrikethrough()
                ) {
                    optimized.append("§r").append(styleToStr(current));
                } else {
                    if (current.isBold() && !seqStyle.isBold()) {
                        optimized.append("§l");
                    }
                    if (current.isItalic() && !seqStyle.isItalic()) {
                        optimized.append("§o");
                    }
                    if (current.isUnderlined() && !seqStyle.isUnderlined()) {
                        optimized.append("§n");
                    }
                    if (current.isStrikethrough() && !seqStyle.isStrikethrough()) {
                        optimized.append("§m");
                    }
                }
                foundSeq = 0;
            }
            optimized.append(chr);
        }
        optimized.delete(optimized.length() - foundSeq, optimized.length());
        this.text = Component.literal(optimized.toString());
//        AdvancedBook.LOGGER.debug("{}", optimized.toString().replaceAll("§", "<"));
//        AdvancedBook.LOGGER.debug("Optimize finished, resetting selection"); // TODO: reposition cursor instead resetting
        this.textEdit.setSelectionRange(0, 0);
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeByte(2);
        super.toBytes(buf);
        buf.writeInt(this.scale);
        buf.writeFloat(this.hAlign);
        buf.writeFloat(this.vAlign);
        buf.writeInt(this.color);
        buf.writeUtf(this.text.getString());
    }
    public CompoundTag toCompound() {
        CompoundTag tag = super.toCompound();
        tag.putByte("type", (byte) 2);
        tag.putInt("scale", this.scale);
        tag.putFloat("hAlign", this.hAlign);
        tag.putFloat("vAlign", this.vAlign);
        tag.putInt("color", this.color);
        tag.putString("text", text.getString());
        return tag;
    }
}
