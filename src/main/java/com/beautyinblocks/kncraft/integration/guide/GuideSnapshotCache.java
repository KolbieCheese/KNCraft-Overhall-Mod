package com.beautyinblocks.kncraft.integration.guide;

import java.util.Map;

/** A short-lived view, never a saved substitute for authoritative server definitions. */
public final class GuideSnapshotCache {
    private record Value(String text, long received) {}
    private final Map<String, Value> values = new java.util.HashMap<>();
    public void accept(Map<String, String> values, long now) { values.forEach((key, text) -> this.values.put(key, new Value(text, now))); }
    public void clear() { values.clear(); }
    public String text(String key, String fallback, long now, boolean connected) {
        if (!connected) return "Pack default (offline):\n" + fallback;
        var value = values.get(key);
        if (value == null || now - value.received() > 15_000_000_000L) return "Pack default; awaiting server:\n" + fallback;
        return value.text();
    }
}
