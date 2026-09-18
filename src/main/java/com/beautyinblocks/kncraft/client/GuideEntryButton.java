package com.beautyinblocks.kncraft.client;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import vazkii.patchouli.client.book.BookEntry;
import vazkii.patchouli.client.book.gui.GuiBook;
import vazkii.patchouli.client.book.gui.button.GuiButtonEntry;

/** Wrap full names while retaining Patchouli's native navigation, narration and bookmarking. */
public final class GuideEntryButton extends GuiButtonEntry {
    public static final int TEXT_WIDTH = 92;
    public record Title(List<FormattedCharSequence> lines, int height, float scale) {}
    private final Title title;

    public static Title measure(BookEntry entry) {
        var name = entry.isLocked() ? Component.translatable("patchouli.gui.lexicon.locked") : entry.getName().copy();
        if (entry.isPriority()) name.withStyle(ChatFormatting.ITALIC);
        name.withStyle(entry.getBook().getFontStyle());
        var font = Minecraft.getInstance().font;
        var lines = List.copyOf(font.split(name, TEXT_WIDTH));
        int textHeight = Math.max(1, lines.size() * font.lineHeight);
        // Only pathological resource-pack titles need scaling; ordinary names use full-size text.
        float scale = Math.min(1F, (GuideEntryPagination.BOTTOM - GuideEntryPagination.FIRST_TOP - 1F) / textHeight);
        return new Title(lines, Math.max(10, (int) Math.ceil(textHeight * scale) + 1), scale);
    }

    public GuideEntryButton(GuiBook parent, int x, int y, BookEntry entry, Title title, OnPress onPress) {
        super(parent, x, y, entry, onPress);
        this.title = title;
        setHeight(title.height());
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        if (!visible) return;
        var entry = getEntry();
        var book = entry.getBook();
        var font = Minecraft.getInstance().font;
        if (isHoveredOrFocused()) graphics.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), 0x22000000);

        graphics.pose().pushPose();
        graphics.pose().translate(getX() + 1, getY() + 1, 0);
        graphics.pose().scale(0.5F, 0.5F, 1);
        if (entry.isLocked()) GuiBook.drawLock(graphics, book, 0, 0);
        else entry.getIcon().render(graphics, 0, 0);
        graphics.pose().popPose();

        int color = entry.isLocked() ? 0x77000000 | book.textColor & 0xFFFFFF
                : entry.isSecret() ? 0xAA000000 | book.textColor & 0xFFFFFF : entry.getEntryColor();
        graphics.pose().pushPose();
        graphics.pose().translate(getX() + 12, getY(), 0);
        graphics.pose().scale(title.scale(), title.scale(), 1);
        for (int i = 0; i < title.lines().size(); i++) {
            graphics.drawString(font, title.lines().get(i), 0, i * font.lineHeight, color, false);
        }
        graphics.pose().popPose();
        if (!entry.isLocked()) GuiBook.drawMarking(graphics, book, getX() + getWidth() - 9, getY() + 1,
                entry.hashCode(), entry.getReadState());
    }
}
