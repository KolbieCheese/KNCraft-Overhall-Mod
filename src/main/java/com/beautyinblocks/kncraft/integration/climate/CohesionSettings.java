package com.beautyinblocks.kncraft.integration.climate;

/** Parsing is independent of the Minecraft runtime and rejects unbounded/ambiguous entries. */
public final class CohesionSettings {
    public record Insulator(String group, String item, String slot, double cold, double heat) {}
    public record Meal(String item, double amount, int duration) {}
    private static String id(String value) {
        if (!value.matches("[a-z0-9_.-]+:[a-z0-9_/.-]+")) throw new IllegalArgumentException("Invalid item ID: " + value);
        return value;
    }
    public static Insulator insulator(String row) {
        String[] p = row.split("\\|", -1);
        if (p.length != 5 || !(p[0].equals("cotton") || p[0].equals("wildlife")) || !(p[2].equals("item") || p[2].equals("armor")))
            throw new IllegalArgumentException("Expected group|item_id|item or armor|cold|heat");
        double cold = Double.parseDouble(p[3]), heat = Double.parseDouble(p[4]);
        if (!Double.isFinite(cold) || !Double.isFinite(heat) || cold < 0 || heat < 0 || cold > 10 || heat > 10 || cold + heat == 0)
            throw new IllegalArgumentException("Insulation must be finite, 0..10 per side and nonzero");
        return new Insulator(p[0], id(p[1]), p[2], cold, heat);
    }
    public static Meal meal(String row) {
        String[] p = row.split("\\|", -1);
        if (p.length != 3) throw new IllegalArgumentException("Expected item_id|temperature|duration_ticks");
        double amount = Double.parseDouble(p[1]); int duration = Integer.parseInt(p[2]);
        if (!Double.isFinite(amount) || amount == 0 || Math.abs(amount) > .5 || duration < 20 || duration > 2400)
            throw new IllegalArgumentException("Meal must have nonzero finite amount +/-0.5 and 20..2400 ticks duration");
        return new Meal(id(p[0]), amount, duration);
    }
    private CohesionSettings() {}
}
