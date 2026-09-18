package com.beautyinblocks.kncraft.client;

import java.util.ArrayList;
import java.util.List;

/** Page-space allocation independent of Minecraft, shared by layout and regression checks. */
public final class GuideEntryPagination {
    public static final int FIRST_TOP = 38;
    public static final int TOP = 18;
    public static final int BOTTOM = 162;
    public static final int GAP = 2;
    public record Row(int index, int y, int height) {}
    public record Page(List<Row> rows) {}

    public static List<Page> paginate(List<Integer> heights) {
        var pages = new ArrayList<Page>();
        var rows = new ArrayList<Row>();
        int y = FIRST_TOP;
        for (int i = 0; i < heights.size(); i++) {
            int height = heights.get(i);
            if (height < 1 || height > BOTTOM - FIRST_TOP) throw new IllegalArgumentException("Invalid guide row height");
            if (y + height > BOTTOM) {
                pages.add(new Page(List.copyOf(rows)));
                rows.clear();
                y = TOP;
            }
            rows.add(new Row(i, y, height));
            y += height + GAP;
        }
        pages.add(new Page(List.copyOf(rows)));
        return List.copyOf(pages);
    }

    public static int spreadCount(List<Page> pages) { return 1 + pages.size() / 2; }
    public static int pageIndex(int spread, boolean right) { return spread * 2 - (right ? 0 : 1); }
    private GuideEntryPagination() {}
}
