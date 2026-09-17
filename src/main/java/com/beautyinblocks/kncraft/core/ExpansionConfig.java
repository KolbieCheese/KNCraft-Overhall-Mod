package com.beautyinblocks.kncraft.core;

import net.minecraftforge.common.ForgeConfigSpec;

/** Independent switches for the cohesive pack defaults; administrators may opt out. */
public final class ExpansionConfig {
    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.BooleanValue TENT_CLIMATE, TENT_ENCLOSURE, INGREDIENTS,
        RECIPE_REPAIRS, RAIL_FUEL, AETHER_THERMAL, THEMED_LOOT, TRADES, JOURNAL,
        FLIGHT, COMBAT, COMPANIONS, WILDLIFE_FOOD, SMART_FEEDING, ECOLOGY, KNOWN_TAGS, SAFE_CARRY;
    public static final ForgeConfigSpec.IntValue CLIMATE_INTERVAL;
    public static final ForgeConfigSpec.DoubleValue FEEDING_THRESHOLD, ACCESSORY_CAP;
    static {
        var b = new ForgeConfigSpec.Builder();
        b.comment("KNCraft Compatibility: restart after changing module switches.");
        b.push("camping");
        TENT_CLIMATE = b.define("inheritExteriorClimate", true);
        TENT_ENCLOSURE = b.comment("Treat the bounded interior of each Nomadic Tent as enclosed. Hearth fuel, range and warm-up still apply.")
            .define("enclosedTentInteriors", true);
        CLIMATE_INTERVAL = b.defineInRange("exteriorSampleIntervalTicks", 40, 20, 1200);
        SAFE_CARRY = b.comment("Use normal tent packing and hearth dismantling instead of carrying one part of a linked structure.").define("safeCarryOnStructures", true);
        b.pop().push("connections");
        INGREDIENTS = b.define("ingredientBridges", true);
        RECIPE_REPAIRS = b.define("repairKnownBrokenRecipes", true);
        KNOWN_TAGS = b.comment("Make verified missing entries in upstream rail/Depth tags optional, preserving all other tag contributors.").define("repairKnownBrokenTags", true);
        RAIL_FUEL = b.define("cookingOilDiesel", true);
        AETHER_THERMAL = b.define("aetherThermalEquipment", true);
        ACCESSORY_CAP = b.defineInRange("maximumAccessoryHeatInsulation", 4.0, 0, 10);
        THEMED_LOOT = b.define("themedExplorationLoot", true);
        TRADES = b.define("supplyTrades", true);
        WILDLIFE_FOOD = b.define("wildlifeFoods", true);
        ECOLOGY = b.comment("Bridge Pam's missing savanna biome tag to installed terrain biome tags. Affects newly generated chunks.").define("savannaGardenCoverage", true);
        b.pop().push("equipment");
        FLIGHT = b.define("curiosFlightEnchantments", true);
        COMBAT = b.define("curatedWeaponProfiles", true);
        COMPANIONS = b.define("protectCompanionsFromCleave", true);
        SMART_FEEDING = b.define("temperatureAwareFeeding", true);
        FEEDING_THRESHOLD = b.comment("Cold Sweat BODY units. Avoid counterproductive managed meals beyond this threshold.")
            .defineInRange("feedingTemperatureThreshold", 20.0, 1, 100);
        b.pop().push("journal");
        JOURNAL = b.comment("Install the bundled FTB accomplishment journal without replacing existing quests or progress.")
            .define("installAccomplishmentJournal", true);
        b.pop();
        SPEC = b.build();
    }
    private ExpansionConfig() {}
}
