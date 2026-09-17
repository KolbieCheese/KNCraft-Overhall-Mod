package com.beautyinblocks.kncraft.integration.guide;

import java.util.Map;

/** A short-lived view, never a saved substitute for authoritative server definitions. */
public final class GuideSnapshotCache {
    private Map<String, String> values = Map.of();
    private long received;
    public void accept(Map<String, String> values, long now) { this.values = Map.copyOf(values); received = now; }
    public void clear() { values = Map.of(); received = 0; }
    public String text(String key, String fallback, long now, boolean connected) {
        if (!connected) return "Pack default (offline):\n" + fallback;
        if (!values.containsKey(key) || now - received > 15_000_000_000L) return "Pack default; awaiting server:\n" + fallback;
        return values.get(key);
    }
}
