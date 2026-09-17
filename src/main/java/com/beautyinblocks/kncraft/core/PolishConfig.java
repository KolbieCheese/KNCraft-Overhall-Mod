package com.beautyinblocks.kncraft.core;

import java.util.List;
import net.minecraftforge.common.ForgeConfigSpec;

/** Follow-up integrations remain independently configurable and preserve upstream settings. */
public final class PolishConfig {
    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.BooleanValue ATTRIBUTES, MIGRATE_ATTRIBUTES, OFFERINGS, REGIONAL_CLIMATE, ALTAR;
    public static final ForgeConfigSpec.IntValue OFFERING_TICKS;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> BIOMES;
    public static final ForgeConfigSpec.BooleanValue RESERVED_FOOD, STARTER_GUIDE;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> RESERVED_ITEMS;
    static {
        var b = new ForgeConfigSpec.Builder();
        b.comment("Restart after changing module switches. Player control presets are opt-in client commands.");
        STARTER_GUIDE = b.comment("Give the KNCraft Guide Book once per player. Existing copies count; respawns and reconnects do not duplicate it.").define("giveStarterGuide", true);
        RESERVED_FOOD = b.comment("Keep reserved provisions out of backpack automatic feeding. Players can opt out with /kncraft feeding reserved false.").define("protectReservedFoods", true);
        RESERVED_ITEMS = b.comment("Exact item IDs reserved for manual eating; native backpack filters still apply to other food.")
            .defineListAllowEmpty(List.of("reservedFoods"), List.of("witherstormmod:golden_apple_stew", "minecraft:enchanted_golden_apple"), value -> value instanceof String);
        ATTRIBUTES = b.define("cooperativeEnchantmentAttributes", true);
        MIGRATE_ATTRIBUTES = b.comment("One-time migration only when ALL FOUR saved base values match More Enchantments' current formulas. Original values are backed up in player data. Set false to preserve every preexisting base value.")
            .define("migrateMatchingLegacyAttributes", true);
        OFFERINGS = b.define("protectWildlifeOfferings", true);
        OFFERING_TICKS = b.comment("Protect player-thrown items accepted by a nearby Alex's animal from backpack magnets for this many ticks. Ordinary loot is unaffected.")
            .defineInRange("offeringGraceTicks", 200, 20, 600);
        ALTAR = b.define("expeditionAltarRepairs", true);
        REGIONAL_CLIMATE = b.define("regionalClimateDefaults", true);
        BIOMES = b.comment("biome_id|min_F|max_F. Only supplies missing absolute biome temperatures; existing Cold Sweat JSON, TOML and explicit disables win. Other biome/dimension/weather modifiers remain native.")
            .defineList("regionalBiomes", List.of(
                "biomesoplenty:auroral_garden|18|36", "biomesoplenty:snowblossom_grove|26|42",
                "biomesoplenty:tundra|28|46", "biomesoplenty:muskeg|32|48",
                "biomesoplenty:cold_desert|28|64", "biomesoplenty:tropics|76|88",
                "biomesoplenty:rainforest|74|86", "biomesoplenty:moor|50|64",
                "biomesoplenty:crag|42|58", "biomesoplenty:lush_savanna|70|90",
                "terralith:cave/ice_caves|32|32", "terralith:cave/thermal_caves|86|86",
                "terralith:cave/frostfire_caves|48|48", "terralith:cave/mantle_caves|100|100",
                "terralith:cold_shrubland|34|54", "terralith:glacial_chasm|18|34",
                "terralith:emerald_peaks|30|48", "terralith:tropical_jungle|76|88",
                "terralith:skylands_winter|24|42", "terralith:desert_oasis|70|100"
            ), value -> value instanceof String);
        SPEC = b.build();
    }
    private PolishConfig() {}
}
