package com.beautyinblocks.kncraft;

import com.beautyinblocks.kncraft.client.GuideEntryPagination;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class GuideEntryPaginationTest {
    @Test void anExactFitStaysOnTheFirstPage() {
        var pages = GuideEntryPagination.paginate(List.of(60, 62, 19));
        assertEquals(2, pages.size());
        assertEquals(162, pages.get(0).rows().get(1).y() + pages.get(0).rows().get(1).height());
        assertEquals(18, pages.get(1).rows().get(0).y());
        assertEquals(2, GuideEntryPagination.spreadCount(pages));
    }

    @Test void everyEntryAppearsOnceInOrderWithoutOverlappingOrCrossingThePage() {
        var random = new Random(71031);
        var heights = IntStream.range(0, 5000).map(i -> 10 + random.nextInt(5) * 9).boxed().toList();
        var pages = GuideEntryPagination.paginate(heights);
        int index = 0;
        for (int page = 0; page < pages.size(); page++) {
            int nextY = page == 0 ? 38 : 18;
            for (var row : pages.get(page).rows()) {
                assertEquals(index, row.index());
                assertEquals(heights.get(index++), row.height());
                assertEquals(nextY, row.y());
                assertTrue(row.y() + row.height() <= 162);
                nextY = row.y() + row.height() + 2;
            }
        }
        assertEquals(heights.size(), index);
        assertEquals(0, GuideEntryPagination.pageIndex(0, true));
        for (int spread = 1; spread < GuideEntryPagination.spreadCount(pages); spread++) {
            assertEquals(2 * spread - 1, GuideEntryPagination.pageIndex(spread, false));
            assertEquals(2 * spread, GuideEntryPagination.pageIndex(spread, true));
        }
    }

    @Test void emptySearchKeepsOneSpreadAndOversizedRowsAreRejected() {
        assertEquals(1, GuideEntryPagination.spreadCount(GuideEntryPagination.paginate(List.of())));
        assertTrue(GuideEntryPagination.paginate(List.of()).get(0).rows().isEmpty());
        assertThrows(IllegalArgumentException.class, () -> GuideEntryPagination.paginate(List.of(125)));
        assertThrows(IllegalArgumentException.class, () -> GuideEntryPagination.paginate(List.of(0)));
    }
}
