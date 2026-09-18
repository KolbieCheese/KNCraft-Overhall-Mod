package com.beautyinblocks.expansiontest;

import com.beautyinblocks.kncraft.core.ArchitectureConfig;
import com.beautyinblocks.kncraft.integration.climate.ClimateIntegration;
import com.google.gson.JsonParser;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.config.ConfigLoadingHandler;
import com.momosoftworks.coldsweat.config.spec.ItemSettingsConfig;
import com.momosoftworks.coldsweat.api.temperature.modifier.FoodTempModifier;
import com.momosoftworks.coldsweat.api.util.Temperature;
import com.mojang.authlib.GameProfile;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import static com.beautyinblocks.expansiontest.ExpansionChecks.*;

final class ThermalFoodChecks {
    static void run(MinecraftServer server) {
        var catalog = JsonParser.parseReader(new InputStreamReader(ClimateIntegration.class.getResourceAsStream("/thermal-foods.json"))).getAsJsonObject();
        var oldRows = List.copyOf(ArchitectureConfig.FOODS.get());
        var oldExclusions = List.copyOf(ArchitectureConfig.FOOD_EXCLUSIONS.get());
        boolean oldExpanded = ArchitectureConfig.EXPANDED_MEALS.get();
        boolean oldMeals = ArchitectureConfig.MEALS.get();
        var oldNative = List.copyOf(ItemSettingsConfig.FOOD_TEMPERATURES.get());
        var player = FakePlayerFactory.get(server.overworld(), new GameProfile(UUID.randomUUID(), "ThermalGuideCheck"));
        try {
            ArchitectureConfig.MEALS.set(true); ArchitectureConfig.EXPANDED_MEALS.set(true);
            ArchitectureConfig.FOOD_EXCLUSIONS.set(List.of());
            ConfigLoadingHandler.loadConfigs(server.registryAccess());
            for (var entry : catalog.entrySet()) {
                var stack = item(entry.getKey());
                check(!ConfigSettings.FOOD_TEMPERATURES.get().get(stack.getItem()).isEmpty(), "Thermal food is not active: " + entry.getKey());
                double managed = ClimateIntegration.mealTemperature(stack);
                if (managed != 0) check(Math.abs(managed - entry.getValue().getAsJsonObject().get("amount").getAsDouble()) < .0001, "Food amount differs from guide: " + entry.getKey());
            }
            for (String id : List.of("pamhc2foodextended:strawberrysmoothieitem", "pamhc2foodextended:tomatosoupitem", "minecraft:rabbit_stew", "pamhc2foodextended:deluxechickencurryitem", "pamhc2foodcore:caramelicecreamitem")) {
                MinecraftForge.EVENT_BUS.post(new LivingEntityUseItemEvent.Finish(player, item(id), 0, ItemStack.EMPTY));
                var active = Temperature.getModifiers(player, Temperature.Trait.BASE, m -> m instanceof FoodTempModifier && m.getNBT().getBoolean("kncraftMeal"));
                check(active.size() == 1 && active.get(0).getNBT().getString("item").equals(id), "Expanded meal stacking/replacement failed");
                check(active.get(0).getExpireTime() == catalog.getAsJsonObject(id).get("duration").getAsInt(), "Recipe-tier duration not applied: " + id);
            }
            MinecraftForge.EVENT_BUS.post(new LivingEntityUseItemEvent.Finish(player, item("minecraft:bread"), 0, ItemStack.EMPTY));
            var retained = Temperature.getModifiers(player, Temperature.Trait.BASE, m -> m instanceof FoodTempModifier && m.getNBT().getBoolean("kncraftMeal"));
            check(retained.size() == 1 && retained.get(0).getNBT().getString("item").equals("pamhc2foodcore:caramelicecreamitem"), "Neutral food cleared the active thermal meal");
            check(ClimateIntegration.mealTemperature(item("minecraft:bread")) == 0, "Neutral bread has a managed temperature effect");
            var target = item("pamhc2foodextended:strawberrysmoothieitem");
            ArchitectureConfig.EXPANDED_MEALS.set(false); ConfigLoadingHandler.loadConfigs(server.registryAccess());
            check(ClimateIntegration.mealTemperature(target) == 0, "Expanded meal switch ignored");
            check(ClimateIntegration.mealTemperature(item("pamhc2foodcore:melonsmoothieitem")) == -.2, "Expanded switch removed original settings");
            ArchitectureConfig.EXPANDED_MEALS.set(true);
            ArchitectureConfig.FOOD_EXCLUSIONS.set(List.of("pamhc2foodextended:strawberrysmoothieitem"));
            ConfigLoadingHandler.loadConfigs(server.registryAccess());
            check(ClimateIntegration.mealTemperature(target) == 0, "Expanded food exclusion ignored");
            ArchitectureConfig.FOOD_EXCLUSIONS.set(List.of());
            var explicit = new ArrayList<String>(oldRows); explicit.add("pamhc2foodextended:strawberrysmoothieitem|-0.35|400");
            ArchitectureConfig.FOODS.set(explicit); ConfigLoadingHandler.loadConfigs(server.registryAccess());
            check(ClimateIntegration.mealTemperature(target) == -.35, "Explicit administrator food row lost to expanded default");
            var nativeRows = new ArrayList<List<?>>(oldNative);
            nativeRows.add(List.of("pamhc2foodextended:strawberrysmoothieitem", -.4, "{}", 600, 1));
            ItemSettingsConfig.FOOD_TEMPERATURES.set(nativeRows); ConfigLoadingHandler.loadConfigs(server.registryAccess());
            check(ClimateIntegration.mealTemperature(target) == 0, "Existing Cold Sweat definition was taken over");
            check(ConfigSettings.FOOD_TEMPERATURES.get().get(target.getItem()).size() == 1, "Native override duplicated thermal effect");
            ArchitectureConfig.MEALS.set(false); ConfigLoadingHandler.loadConfigs(server.registryAccess());
            check(ClimateIntegration.mealTemperature(item("pamhc2foodcore:melonsmoothieitem")) == 0, "Master thermal switch ignored");
        } finally {
            ArchitectureConfig.FOODS.set(oldRows); ArchitectureConfig.FOOD_EXCLUSIONS.set(oldExclusions);
            ArchitectureConfig.EXPANDED_MEALS.set(oldExpanded); ArchitectureConfig.MEALS.set(oldMeals);
            ItemSettingsConfig.FOOD_TEMPERATURES.set(oldNative); ConfigLoadingHandler.loadConfigs(server.registryAccess());
        }
        System.out.println("THERMAL CATALOG PASSED: " + catalog.size() + " foods; recipe-tier strength/duration, neutral food, shared effect, reload, switches, exclusions, explicit and native overrides");
    }
    private ThermalFoodChecks() {}
}
