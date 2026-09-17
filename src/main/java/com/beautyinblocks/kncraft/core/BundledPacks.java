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
