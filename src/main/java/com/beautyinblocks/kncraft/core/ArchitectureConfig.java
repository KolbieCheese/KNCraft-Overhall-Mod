package com.beautyinblocks.kncraft.core;

import net.minecraftforge.common.ForgeConfigSpec;

/** Common configuration is fixed for a server session; restart after changing module switches. */
public final class ArchitectureConfig {
    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.BooleanValue NATIVE_PORTALS, TENTS, PORTAL_SEARCH, ITEM_SELECTION, TORNADO_QUERY;
    public static final ForgeConfigSpec.BooleanValue WAYSTONES, ENCOUNTERS;
    public static final ForgeConfigSpec.BooleanValue COTTON, WILDLIFE, MEALS, FIBERS;
    public static final ForgeConfigSpec.ConfigValue<java.util.List<? extends String>> INSULATORS;
    public static final ForgeConfigSpec.ConfigValue<java.util.List<? extends String>> FOODS;
    static {
        var b = new ForgeConfigSpec.Builder();
        b.comment("KNCraft Architecture. Restart after changes. Survival additions are opt-in.");
        b.push("parity");
        NATIVE_PORTALS = b.define("nativePortalLighting", true);
        TENTS = b.comment("Disabling entrances keeps the tent dimension acknowledgement repair active.").define("immersiveTentEntrances", true);
        WAYSTONES = b.comment("Data protection; retain the externally owned Waystones config as documented.").define("naturalWaystones", true);
        ENCOUNTERS = b.define("encounterScaling", true);
        b.pop().push("performance");
        PORTAL_SEARCH = b.define("portalSearch", true);
        ITEM_SELECTION = b.define("itemSelection", true);
        TORNADO_QUERY = b.define("tornadoQuery", true);
        b.pop().push("cohesion");
        COTTON = b.define("cottonInsulation", false);
        WILDLIFE = b.define("wildlifeInsulation", false);
        MEALS = b.define("thermalMeals", false);
        FIBERS = b.define("fiberRecipes", false);
        INSULATORS = b.comment("group|item_id|item or armor|cold|heat. Invalid rows are rejected in the status report. Existing Cold Sweat entries take precedence.")
            .defineListAllowEmpty(java.util.List.of("insulators"), java.util.List.of(
                "cotton|pamhc2crops:cottonitem|item|1|0.5",
                "wildlife|alexsmobs:bear_fur|item|1.5|0",
                "wildlife|alexsmobs:bison_fur|item|1.75|0",
                "wildlife|alexsmobs:kangaroo_hide|item|0.75|0.75",
                "wildlife|alexsmobs:frontier_cap|armor|3|1"), value -> value instanceof String);
        FOODS = b.comment("item_id|temperature|duration_ticks. All KNCraft meals share one active effect. Bounds: +/-0.5, 20..2400 ticks. Native hunger and container rules remain.")
            .defineListAllowEmpty(java.util.List.of("foods"), java.util.List.of(
                "pamhc2crops:hotteaitem|0.2|1200", "pamhc2crops:hotcoffeeitem|0.2|1200",
                "pamhc2crops:hotnettleteaitem|0.2|1200", "pamhc2foodcore:carrotsoupitem|0.2|1800",
                "pamhc2foodcore:potatosoupitem|0.2|1800", "pamhc2foodcore:stewitem|0.2|1800",
                "pamhc2foodcore:melonsmoothieitem|-0.2|1200", "pamhc2foodcore:icecreamitem|-0.2|1200",
                "pamhc2foodcore:applejuiceitem|-0.1|600"), value -> value instanceof String);
        b.pop();
        SPEC = b.build();
    }
    private ArchitectureConfig() {}
}
