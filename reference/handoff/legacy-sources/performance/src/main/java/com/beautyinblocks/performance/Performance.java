package com.beautyinblocks.performance;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import com.mojang.logging.LogUtils;
@Mod("kncraftperformance")
public final class Performance {
    public static final ForgeConfigSpec.BooleanValue PORTAL_SEARCH, ITEM_SELECTION, TORNADO_QUERY;
    private static final ForgeConfigSpec SPEC;
    static {
        var b = new ForgeConfigSpec.Builder();
        PORTAL_SEARCH=b.comment("Skip portal-free sections during Wither Storm flee-goal searches. Unloaded areas use the original search.").define("portalSearch",true);
        ITEM_SELECTION=b.comment("Select the nearest Alex's Mobs item target without sorting other candidates.").define("itemSelection",true);
        TORNADO_QUERY=b.comment("Query only entity classes used by Weather2's forceRotate logic.").define("tornadoQuery",true);
        SPEC=b.build();
    }
    public Performance() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON,SPEC);
        LogUtils.getLogger().info("KNCraft Performance 1.0.0: conservative portal, item-target and tornado queries enabled by configuration; no asynchronous world access");
    }
}
