package com.beautyinblocks.kncraft.core;

import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraftforge.fml.loading.LoadingModList;

/** Safe during Mixin bootstrap: no references to optional game classes. */
public final class Compatibility {
    public static final Map<String, List<String>> SUPPORTED_VERSIONS = Map.ofEntries(
        Map.entry("patchouli", List.of("1.20.1-85-FORGE")),
        Map.entry("aether", List.of("1.20.1-1.5.2-neoforge")),
        Map.entry("immersive_portals", List.of("3.0.7")), Map.entry("nomadictents", List.of("20.1.1")),
        Map.entry("infiniverse", List.of("1.0.0.5")), Map.entry("witherstormmod", List.of("4.2.1")),
        Map.entry("alexsmobs", List.of("1.22.9")), Map.entry("weather2", List.of("1.20.1-2.8.3")),
        Map.entry("callfromthedepth_", List.of("1.22.1")), Map.entry("cold_sweat", List.of("2.4.3", "2.4.3.2")),
        Map.entry("more_enchantments", List.of("1.4.3")), Map.entry("elytraslot", List.of("6.4.4+1.20.1")),
        Map.entry("bettercombat", List.of("1.9.0+1.20.1")), Map.entry("sophisticatedcore", List.of("1.5.1.2335", "1.5.2.2346")),
        Map.entry("carryon", List.of("2.1.2.7")));
    public static final Set<String> LEGACY = Set.of("kncraftnativeportals", "kncrafttentportals", "kncraftperformance");
    public static String version(String id) {
        return LoadingModList.get().getMods().stream().filter(m -> m.getModId().equals(id))
            .map(m -> m.getVersion().toString()).findFirst().orElse("absent");
    }
    public static boolean present(String id) { return !version(id).equals("absent"); }
    /** Only explicitly audited versions enable integrations, including during Mixin bootstrap. */
    public static boolean supported(String id, String version) {
        return SUPPORTED_VERSIONS.getOrDefault(id, List.of()).contains(version);
    }
    public static boolean supported(String id) { return supported(id, version(id)); }
    public static boolean tents() { return supported("immersive_portals") && supported("nomadictents") && supported("infiniverse"); }
    public static void validate() {
        for (String id : LEGACY) if (present(id))
            throw new IllegalStateException("KNCraft Architecture replaces " + id + ". Remove the old helper JAR; both cannot run together.");
        SUPPORTED_VERSIONS.forEach((id, versions) -> {
            if (present(id) && !supported(id)) throw new IllegalStateException("KNCraft integration requires " + id + " " + String.join(" or ", versions) + "; found " + version(id));
        });
    }
    private Compatibility() {}
}
