package com.beautyinblocks.kncraft.integration.equipment;

/** Emergency hunger takes precedence; otherwise avoid making an extreme temperature worse. */
public final class FeedingPolicy {
    public static boolean allows(double body, double meal, double threshold, int foodLevel) {
        if (foodLevel <= 6 || meal == 0 || !Double.isFinite(body)) return true;
        return !(body >= threshold && meal > 0 || body <= -threshold && meal < 0);
    }
    private FeedingPolicy() {}
}
