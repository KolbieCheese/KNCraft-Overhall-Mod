package com.beautyinblocks.kncraft.integration.guide;

import com.momosoftworks.coldsweat.config.ConfigSettings;
import java.util.ArrayList;
import java.util.Locale;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

/** Reads effective native registries after administrator overrides, for a plain item and this player. */
public final class ClimateGuideValues {
    public static String describe(String key, String id, ServerPlayer player) {
        var item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(id));
        if (item == null || item == net.minecraft.world.item.Items.AIR) return "This item is unavailable on this server.";
        var stack = new ItemStack(item);
        var lines = new ArrayList<String>();
        if (key.startsWith("food/")) {
            for (var data : ConfigSettings.FOOD_TEMPERATURES.get().get(item)) {
                // Cold Sweat 2.4.3's actual consumption handler tests the stack here;
                // its value expressions still evaluate against the consuming player.
                if (!data.test(stack)) continue;
                int duration = data.duration(stack, player);
                lines.add(duration > 0
                    ? String.format(Locale.ROOT, "%+.3g base temperature; %.1f seconds.", data.temperature(stack, player), duration / 20.0)
                    : String.format(Locale.ROOT, "%+.3g core temperature (instant).", data.temperature(stack, player)));
                if (!data.modifiers().isEmpty()) lines.add("Includes additional native modifiers; inspect the item tooltip.");
            }
        } else {
            var definitions = key.startsWith("armor/") ? ConfigSettings.INSULATING_ARMORS.get() : ConfigSettings.INSULATION_ITEMS.get();
            for (var data : definitions.get(item)) {
                if (!data.test(stack) || !data.test(player)) continue;
                lines.add(String.format(Locale.ROOT, "%.3g cold / %.3g heat insulation. %s", data.getCold(), data.getHeat(), data.fillSlots(stack) ? "Uses sewing capacity." : "Does not use sewing capacity."));
            }
        }
        if (lines.isEmpty()) return "Server: no matching effect for a plain item and your current conditions. Special item data or conditions may differ.";
        if (lines.size() > 4) return "Server: multiple matching definitions. Use the item tooltip to inspect this item's effects.";
        return "Server, plain item:\n" + String.join("\n", lines) + "\nConditions and item data can change the result.";
    }
    private ClimateGuideValues() {}
}
