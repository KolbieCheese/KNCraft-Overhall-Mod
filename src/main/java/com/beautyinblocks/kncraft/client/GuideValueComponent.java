package com.beautyinblocks.kncraft.client;

import java.util.List;
import java.util.function.UnaryOperator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import vazkii.patchouli.api.*;

/** Public Patchouli custom-component API: refreshes without recompiling cached book pages. */
public final class GuideValueComponent implements ICustomComponent {
    public String reference, fallback;
    private transient int x, y, part;
    private transient String previous;
    private transient List<FormattedCharSequence> lines;
    public void onVariablesAvailable(UnaryOperator<IVariable> lookup) {
        reference = lookup.apply(IVariable.wrap(reference)).asString();
        fallback = lookup.apply(IVariable.wrap(fallback)).asString();
    }
    public void build(int x, int y, int page) { this.x = x; this.y = y; }
    public void render(GuiGraphics graphics, IComponentRenderContext context, float ticks, int mouseX, int mouseY) {
        var font = Minecraft.getInstance().font;
        String text = GuideClient.text(reference, fallback);
        if (!text.equals(previous)) { previous = text; part = 0; lines = font.split(Component.literal(text).withStyle(context.getFont()), 114); }
        int start = part * 11;
        for (int i = start; i < Math.min(start + 11, lines.size()); i++) graphics.drawString(font, lines.get(i), x, y + (i - start) * 9, context.getTextColor(), false);
        if (lines.size() > 11) graphics.drawString(font, "More " + (part + 1) + "/" + ((lines.size() + 10) / 11), x, y + 108, context.getTextColor(), false);
    }
    public boolean mouseClicked(IComponentRenderContext context, double mouseX, double mouseY, int button) {
        if (button != 0 || lines == null || lines.size() <= 11 || mouseX < x || mouseX > x + 114 || mouseY < y + 104 || mouseY > y + 119) return false;
        part = (part + 1) % ((lines.size() + 10) / 11); return true;
    }
}
