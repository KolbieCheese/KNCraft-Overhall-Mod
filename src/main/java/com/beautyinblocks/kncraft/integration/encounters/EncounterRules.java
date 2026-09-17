package com.beautyinblocks.kncraft.integration.encounters;

import java.util.Set;
import java.util.UUID;

/** Persistent names are migration API: changing these would re-scale old encounters. */
public final class EncounterRules {
    public static final String STORM_TAG = "kncraft_storm_balanced", CORE_TAG = "kncraft_balanced";
    public static final UUID STORM_HEALTH = UUID.fromString("65b697a5-2a47-49fb-a6f1-bc06230990f4");
    public static final UUID STORM_DAMAGE = UUID.fromString("766a37b6-5376-47df-bd46-9f756891a4b1");
    public static final UUID STORM_ARMOR = UUID.fromString("7b9a2e4e-d880-4584-bb07-85ff369f12fd");
    public static final UUID CORE_HEALTH = UUID.fromString("7b12d760-cbf3-4489-a04f-7a01d1076982");
    private static final Set<String> DIMENSIONS = Set.of("minecraft:overworld", "minecraft:the_nether", "minecraft:the_end",
        "aether:the_aether", "callfromthedepth_:depth", "witherstormmod:bowels", "immersive_portals:alternate1",
        "immersive_portals:alternate2", "immersive_portals:alternate3", "immersive_portals:alternate4", "immersive_portals:alternate5");
    public static boolean eligible(String entity, String dimension, Set<String> tags) {
        if (entity.equals("witherstormmod:wither_storm")) return DIMENSIONS.contains(dimension) && !tags.contains(STORM_TAG);
        return entity.equals("witherstormmod:command_block") && dimension.equals("witherstormmod:bowels") && !tags.contains(CORE_TAG);
    }
    private EncounterRules() {}
}
