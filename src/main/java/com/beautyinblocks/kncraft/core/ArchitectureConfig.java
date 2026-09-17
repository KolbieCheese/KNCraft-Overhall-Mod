package com.beautyinblocks.kncraft.core;

import net.minecraftforge.common.ForgeConfigSpec;

/** Common configuration is fixed for a server session; restart after changing module switches. */
public final class ArchitectureConfig {
    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.BooleanValue NATIVE_PORTALS, TENTS, PORTAL_SEARCH, ITEM_SELECTION, TORNADO_QUERY;
    public static final ForgeConfigSpec.BooleanValue WAYSTONES, ENCOUNTERS;
    public static final ForgeConfigSpec.BooleanValue COTTON, WILDLIFE, MEALS, FIBERS;
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
        b.pop();
        SPEC = b.build();
    }
    private ArchitectureConfig() {}
}
