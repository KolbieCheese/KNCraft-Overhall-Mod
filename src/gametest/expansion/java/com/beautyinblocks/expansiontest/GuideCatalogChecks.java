package com.beautyinblocks.expansiontest;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.nio.file.Files;
import java.util.Comparator;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.registries.ForgeRegistries;

/** Exports resolved recipe results from the isolated pack, including conditional recipes. */
final class GuideCatalogChecks {
    static void export(MinecraftServer server) throws Exception {
        var root = new JsonObject();
        var items = new JsonObject();
        ForgeRegistries.ITEMS.getEntries().stream().sorted(Comparator.comparing(e -> e.getKey().location().toString())).forEach(entry -> {
            var stack = new ItemStack(entry.getValue());
            var data = new JsonObject();
            data.addProperty("food", stack.isEdible() || entry.getValue() == Items.MILK_BUCKET);
            data.addProperty("translation", stack.getDescriptionId());
            items.add(entry.getKey().location().toString(), data);
        });
        root.add("items", items);
        var recipes = new JsonArray();
        for (var recipe : server.getRecipeManager().getRecipes().stream().sorted(Comparator.comparing(r -> r.getId().toString())).toList()) {
            var data = new JsonObject();
            var stack = recipe.getResultItem(server.registryAccess());
            data.addProperty("id", recipe.getId().toString());
            data.addProperty("type", ForgeRegistries.RECIPE_SERIALIZERS.getKey(recipe.getSerializer()).toString());
            data.addProperty("crafting", recipe instanceof CraftingRecipe);
            var ingredients = new JsonArray();
            for (var ingredient : recipe.getIngredients()) {
                var choices = new JsonArray();
                for (var choice : ingredient.getItems()) choices.add(ForgeRegistries.ITEMS.getKey(choice.getItem()).toString());
                ingredients.add(choices);
            }
            data.add("ingredients", ingredients);
            if (!stack.isEmpty()) {
                data.addProperty("result", ForgeRegistries.ITEMS.getKey(stack.getItem()).toString());
                data.addProperty("count", stack.getCount());
            }
            recipes.add(data);
        }
        root.add("recipes", recipes);
        Files.writeString(FMLPaths.GAMEDIR.get().resolve("guide-recipe-registry.json"), new GsonBuilder().setPrettyPrinting().create().toJson(root) + "\n");
        System.out.println("GUIDE CATALOG EXPORTED: " + items.size() + " items; " + recipes.size() + " resolved recipes");
    }
    private GuideCatalogChecks() {}
}
