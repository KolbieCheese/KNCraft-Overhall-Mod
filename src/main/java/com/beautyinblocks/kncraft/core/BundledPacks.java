package com.beautyinblocks.kncraft.core;

import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraftforge.event.AddPackFindersEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.resource.PathPackResources;

public final class BundledPacks {
    public static void register(AddPackFindersEvent event) {
        if (event.getPackType() != PackType.SERVER_DATA) return;
        add(event, "aether_portals", Compatibility.exact("immersive_portals") && Compatibility.present("aether"));
        add(event, "depth_portals", Compatibility.exact("immersive_portals") && Compatibility.exact("callfromthedepth_"));
        add(event, "waystones", ArchitectureConfig.WAYSTONES.get() && Compatibility.present("waystones"));
        add(event, "fiber_recipes", ArchitectureConfig.FIBERS.get());
        add(event, "ingredients", ExpansionConfig.INGREDIENTS.get());
        add(event, "recipe_repairs", ExpansionConfig.RECIPE_REPAIRS.get() && (Compatibility.present("pamhc2foodcore") || Compatibility.present("pamhc2crops")));
        add(event, "camp_safety", ExpansionConfig.SAFE_CARRY.get() && Compatibility.present("carryon"));
        add(event, "rail_fuel", ExpansionConfig.RAIL_FUEL.get());
        add(event, "aether_freezer", ExpansionConfig.AETHER_THERMAL.get() && Compatibility.present("aether"));
        add(event, "wildlife_food", ExpansionConfig.WILDLIFE_FOOD.get() && Compatibility.present("alexsmobs"));
        add(event, "combat", ExpansionConfig.COMBAT.get() && Compatibility.exact("bettercombat") && Compatibility.present("aether"));
        add(event, "exploration", ExpansionConfig.THEMED_LOOT.get());
        add(event, "ecology", ExpansionConfig.ECOLOGY.get() && Compatibility.present("pamhc2crops"));
        if (ExpansionConfig.JOURNAL.get() && Compatibility.present("ftbquests")) {
            for (String mod : java.util.List.of("minecraft", "dungeons_enhanced", "structory", "t_and_t", "abridged", "callfromthedepth_", "pamhc2crops", "pamhc2foodcore", "betterminecarts"))
                add(event, "journal_" + mod, Compatibility.present(mod));
        }
    }
    private static void add(AddPackFindersEvent event, String name, boolean enabled) {
        if (!enabled) return;
        var path = ModList.get().getModFileById("kncraft").getFile().findResource("packs", name);
        event.addRepositorySource(consumer -> {
            Pack pack = Pack.readMetaAndCreate("kncraft/" + name, Component.literal("KNCraft " + name), true,
                id -> new PathPackResources(id, true, path), PackType.SERVER_DATA, Pack.Position.TOP, PackSource.BUILT_IN);
            if (pack == null) throw new IllegalStateException("Missing bundled KNCraft pack: " + name);
            consumer.accept(pack);
        });
    }
    private BundledPacks() {}
}
