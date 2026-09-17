package com.beautyinblocks.kncraft.integration.guide;

import com.beautyinblocks.kncraft.core.Compatibility;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;

/** Only the checked-in book's public reference keys can be queried. */
public final class GuideValues {
    public static final Map<String, String> KEYS;
    static {
        try (var input = GuideValues.class.getResourceAsStream("/guide-values.json")) {
            KEYS = Map.copyOf(new Gson().fromJson(new InputStreamReader(input, StandardCharsets.UTF_8),
                new TypeToken<Map<String, String>>() {}.getType()));
        } catch (Exception ex) { throw new IllegalStateException("Missing guide reference index", ex); }
    }
    public static CompoundTag snapshot(ServerPlayer player) {
        var result = new CompoundTag();
        KEYS.forEach((key, item) -> {
            String text;
            if (key.startsWith("recipe/")) {
                var recipe = player.server.getRecipeManager().byKey(new net.minecraft.resources.ResourceLocation(item));
                text = recipe.filter(r -> r instanceof net.minecraft.world.item.crafting.AbstractCookingRecipe)
                    .map(r -> String.format(java.util.Locale.ROOT, "Server recipe:\n%.1f seconds with normal machine fuel.\nInspect the current ingredients and result in JEI.", ((net.minecraft.world.item.crafting.AbstractCookingRecipe) r).getCookingTime() / 20.0))
                    .orElse("This machine recipe is unavailable on this server.");
            } else text = Compatibility.exact("cold_sweat") ? ClimateGuideValues.describe(key, item, player) : "Cold Sweat is unavailable on this server.";
            result.putString(key, text);
        });
        return result;
    }
    private GuideValues() {}
}
