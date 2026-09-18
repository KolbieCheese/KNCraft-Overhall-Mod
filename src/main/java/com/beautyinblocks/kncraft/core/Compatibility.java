package com.beautyinblocks.kncraft.core;

import java.util.Map;
import java.util.Set;
import net.minecraftforge.fml.loading.LoadingModList;

/** Safe during Mixin bootstrap: no references to optional game classes. */
public final class Compatibility {
    public static final Map<String, String> PINS = Map.ofEntries(
        Map.entry("patchouli", "1.20.1-85-FORGE"),
        Map.entry("aether", "1.20.1-1.5.2-neoforge"),
        Map.entry("immersive_portals", "3.0.7"), Map.entry("nomadictents", "20.1.1"),
        Map.entry("infiniverse", "1.0.0.5"), Map.entry("witherstormmod", "4.2.1"),
        Map.entry("alexsmobs", "1.22.9"), Map.entry("weather2", "1.20.1-2.8.3"),
        Map.entry("callfromthedepth_", "1.22.1"), Map.entry("cold_sweat", "2.4.3"),
        Map.entry("more_enchantments", "1.4.3"), Map.entry("elytraslot", "6.4.4+1.20.1"),
        Map.entry("bettercombat", "1.9.0+1.20.1"), Map.entry("sophisticatedcore", "1.5.1.2335"),
        Map.entry("carryon", "2.1.2.7"));
    public static final Set<String> LEGACY = Set.of("kncraftnativeportals", "kncrafttentportals", "kncraftperformance");
    public static String version(String id) {
        return LoadingModList.get().getMods().stream().filter(m -> m.getModId().equals(id))
            .map(m -> m.getVersion().toString()).findFirst().orElse("absent");
    }
    public static boolean present(String id) { return !version(id).equals("absent"); }
    public static boolean exact(String id) { return PINS.get(id).equals(version(id)); }
    public static boolean tents() { return exact("immersive_portals") && exact("nomadictents") && exact("infiniverse"); }
    public static void validate() {
        for (String id : LEGACY) if (present(id))
            throw new IllegalStateException("KNCraft Architecture replaces " + id + ". Remove the old helper JAR; both cannot run together.");
        PINS.forEach((id, version) -> {
            if (present(id) && !exact(id)) throw new IllegalStateException("KNCraft integration requires " + id + " " + version + "; found " + version(id));
        });
    }
    private Compatibility() {}
}
