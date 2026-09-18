package com.beautyinblocks.kncraft.integration.climate;

import com.google.gson.JsonParser;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/** Explicit pack food definitions; no broad name matching or scans during eating/ticks. */
public final class ThermalFoodCatalog {
    private static final List<CohesionSettings.Meal> ADDITIONS;
    static {
        var entries = new ArrayList<CohesionSettings.Meal>();
        try (var stream = ThermalFoodCatalog.class.getResourceAsStream("/thermal-foods.json")) {
            var root = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
            for (var entry : root.entrySet()) {
                var value = entry.getValue().getAsJsonObject();
                if (value.get("legacy").getAsBoolean()) continue;
                entries.add(CohesionSettings.meal(entry.getKey() + "|" + value.get("amount").getAsDouble() + "|" + value.get("duration").getAsInt()));
            }
            ADDITIONS = List.copyOf(entries);
        } catch (Exception ex) { throw new IllegalStateException("Invalid thermal food catalog", ex); }
    }
    public static List<String> additions(Set<String> explicitItems, Set<String> excludedItems) {
        return ADDITIONS.stream().filter(m -> !explicitItems.contains(m.item()) && !excludedItems.contains(m.item()))
            .map(m -> m.item() + "|" + m.amount() + "|" + m.duration()).toList();
    }
    private ThermalFoodCatalog() {}
}
