package com.beautyinblocks.kncraft.integration.climate;

import com.beautyinblocks.kncraft.core.PolishConfig;
import com.momosoftworks.coldsweat.api.event.core.registry.LoadRegistriesEvent;
import com.momosoftworks.coldsweat.config.spec.WorldSettingsConfig;
import com.momosoftworks.coldsweat.data.ModRegistries;
import com.momosoftworks.coldsweat.data.codec.configuration.BiomeTempData;
import com.mojang.logging.LogUtils;
import java.util.*;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class RegionalClimate {
    private static final List<String> report = new ArrayList<>();
    public static List<String> diagnostics() { return List.copyOf(report); }
    public static boolean matches(BiomeTempData data, Holder<Biome> biome) {
        return data.biomes().test(value -> value.map(biome::is, h -> h.is(biome)));
    }
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void load(LoadRegistriesEvent.Pre event) {
        report.clear();
        if (!PolishConfig.REGIONAL_CLIMATE.get()) return;
        var registry = event.getRegistryAccess().registryOrThrow(Registries.BIOME);
        var external = new ArrayList<BiomeTempData>();
        event.getRegistry(ModRegistries.BIOME_TEMP_DATA).forEach(h -> external.add(h.value()));
        for (var row : WorldSettingsConfig.BIOME_TEMPERATURES.get()) {
            var value = BiomeTempData.fromToml(row, false, event.getRegistryAccess());
            if (value != null) external.add(value);
        }
        Set<String> seen = new HashSet<>();
        for (String row : PolishConfig.BIOMES.get()) try {
            var parts = row.split("\\|");
            if (parts.length != 3 || !seen.add(parts[0])) throw new IllegalArgumentException("Expected unique biome|min_F|max_F");
            double min = Double.parseDouble(parts[1]), max = Double.parseDouble(parts[2]);
            if (!Double.isFinite(min) || !Double.isFinite(max) || min > max || min < -100 || max > 200) throw new IllegalArgumentException("Invalid temperature range");
            var key = ResourceKey.create(Registries.BIOME, new ResourceLocation(parts[0]));
            var biome = registry.getHolder(key);
            if (biome.isEmpty()) continue;
            if (external.stream().anyMatch(d -> !d.isOffset() && matches(d, biome.get()))) {
                report.add("Preserved external biome temperature: " + parts[0]); continue;
            }
            var value = BiomeTempData.fromToml(List.of(parts[0], min, max, "F"), false, event.getRegistryAccess());
            if (value == null) throw new IllegalArgumentException("Cold Sweat rejected biome");
            event.addRegistryEntry(ModRegistries.BIOME_TEMP_DATA, value);
            report.add("Regional climate: " + parts[0] + " " + min + ".." + max + " F");
        } catch (RuntimeException ex) { report.add("Rejected regional climate: " + row + " (" + ex.getMessage() + ")"); }
        report.forEach(s -> LogUtils.getLogger().info("KNCraft {}", s));
    }
}
