package com.beautyinblocks.kncraft;

import com.beautyinblocks.kncraft.integration.guide.GuideSnapshotCache;
import java.util.Map;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class GuideSnapshotCacheTest {
    @Test void serverValuesReplaceDefaultsAndExpire() {
        var cache = new GuideSnapshotCache();
        assertTrue(cache.text("food", "default", 0, true).contains("awaiting server"));
        cache.accept(Map.of("food", "server override"), 1);
        assertEquals("server override", cache.text("food", "default", 2, true));
        assertTrue(cache.text("food", "default", 16_000_000_001L, true).contains("awaiting server"));
    }
    @Test void reloadReplacesRemovedDefinitions() {
        var cache = new GuideSnapshotCache(); cache.accept(Map.of("food", "old"), 1);
        cache.accept(Map.of("other", "new"), 2);
        assertTrue(cache.text("food", "default", 3, true).contains("awaiting server"));
        assertEquals("new", cache.text("other", "default", 3, true));
    }
    @Test void disconnectNeverReusesAnotherServersValues() {
        var cache = new GuideSnapshotCache(); cache.accept(Map.of("food", "first server"), 1);
        assertTrue(cache.text("food", "default", 2, false).contains("offline"));
        cache.clear();
        assertTrue(cache.text("food", "default", 3, true).contains("awaiting server"));
    }
}
