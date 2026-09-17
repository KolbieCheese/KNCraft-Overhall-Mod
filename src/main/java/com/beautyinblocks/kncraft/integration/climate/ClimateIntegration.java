package com.beautyinblocks.kncraft.integration.climate;

import com.beautyinblocks.kncraft.core.ArchitectureConfig;
import com.momosoftworks.coldsweat.api.event.core.registry.LoadRegistriesEvent;
import com.momosoftworks.coldsweat.api.event.common.temperautre.TempModifierEvent;
import com.momosoftworks.coldsweat.api.insulation.Insulation;
import com.momosoftworks.coldsweat.api.temperature.modifier.FoodTempModifier;
import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.config.spec.ItemSettingsConfig;
import com.momosoftworks.coldsweat.data.ModRegistries;
import com.momosoftworks.coldsweat.data.codec.configuration.FoodData;
import com.momosoftworks.coldsweat.data.codec.configuration.InsulatorData;
import com.mojang.logging.LogUtils;
import java.util.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;

/** Version-pinned public registry-event integration. Never writes Cold Sweat TOML or armor NBT. */
public final class ClimateIntegration {
    private static final Set<String> managedMeals = new HashSet<>();
    private static final List<String> diagnostics = new ArrayList<>();
    public static List<String> diagnostics() { return List.copyOf(diagnostics); }
    private ItemStack item(String id) {
        var key = new ResourceLocation(id);
        if (!ForgeRegistries.ITEMS.containsKey(key)) throw new IllegalArgumentException("Missing item " + id);
        return new ItemStack(ForgeRegistries.ITEMS.getValue(key));
    }
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void load(LoadRegistriesEvent.Pre event) {
        managedMeals.clear(); diagnostics.clear();
        Set<String> seen = new HashSet<>();
        for (String row : ArchitectureConfig.INSULATORS.get()) {
            try {
                var setting = CohesionSettings.insulator(row);
                if (!(setting.group().equals("cotton") ? ArchitectureConfig.COTTON.get() : ArchitectureConfig.WILDLIFE.get())) continue;
                var stack = item(setting.item());
                var slot = setting.slot().equals("armor") ? Insulation.Slot.ARMOR : Insulation.Slot.ITEM;
                if (!seen.add(setting.item() + "/" + slot)) throw new IllegalArgumentException("Duplicate insulation row " + setting.item());
                boolean external = event.getRegistry(ModRegistries.INSULATOR_DATA).stream().anyMatch(h -> h.value().slot() == slot && h.value().test(stack));
                var toml = slot == Insulation.Slot.ITEM ? ItemSettingsConfig.INSULATION_ITEMS.get() : ItemSettingsConfig.INSULATING_ARMOR.get();
                external |= toml.stream().map(entry -> InsulatorData.fromToml(entry, slot)).filter(Objects::nonNull).anyMatch(data -> data.test(stack));
                var existing = slot == Insulation.Slot.ITEM ? ConfigSettings.INSULATION_ITEMS.get() : ConfigSettings.INSULATING_ARMORS.get();
                if (external || !existing.get(stack.getItem()).isEmpty()) {
                    diagnostics.add("Preserved existing insulation for " + setting.item()); continue;
                }
                var data = InsulatorData.fromToml(List.of(setting.item(), setting.cold(), setting.heat(), "static", "", true), slot);
                if (data == null) throw new IllegalArgumentException("Cold Sweat rejected " + setting.item());
                event.addRegistryEntry(ModRegistries.INSULATOR_DATA, data);
                diagnostics.add("Insulation active: " + setting.item() + " " + setting.cold() + "/" + setting.heat());
            } catch (RuntimeException ex) { diagnostics.add("Rejected insulation: " + row + " (" + ex.getMessage() + ")"); }
        }
        if (ArchitectureConfig.MEALS.get()) for (String row : ArchitectureConfig.FOODS.get()) {
            try {
                var setting = CohesionSettings.meal(row); var stack = item(setting.item());
                if (!seen.add("food/" + setting.item())) throw new IllegalArgumentException("Duplicate meal row " + setting.item());
                if (event.getRegistry(ModRegistries.FOOD_DATA).stream().anyMatch(h -> h.value().test(stack))
                    || ItemSettingsConfig.FOOD_TEMPERATURES.get().stream().map(FoodData::fromToml).filter(Objects::nonNull).anyMatch(data -> data.test(stack))) {
                    diagnostics.add("Preserved existing meal effect for " + setting.item()); continue;
                }
                var data = FoodData.fromToml(List.of(setting.item(), setting.amount(), "{}", setting.duration(), 1));
                if (data == null) throw new IllegalArgumentException("Cold Sweat rejected " + setting.item());
                event.addRegistryEntry(ModRegistries.FOOD_DATA, data);
                managedMeals.add(setting.item());
            } catch (RuntimeException ex) { diagnostics.add("Rejected meal: " + row + " (" + ex.getMessage() + ")"); }
        }
        diagnostics.add("Managed thermal meals: " + managedMeals.size() + "; one shared effect, newest replaces previous");
        diagnostics.forEach(line -> LogUtils.getLogger().info("KNCraft climate: {}", line));
    }
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void meal(TempModifierEvent.Add event) {
        var modifier = event.getModifier();
        if (!ArchitectureConfig.MEALS.get() || event.getEntity().level().isClientSide
            || event.getTrait() != Temperature.Trait.BASE || !(modifier instanceof FoodTempModifier)
            || !managedMeals.contains(modifier.getNBT().getString("item"))) return;
        // Use Cold Sweat's removal API so sibling bookkeeping and client synchronization stay intact.
        Temperature.removeModifiers(event.getEntity(), Temperature.Trait.BASE,
            old -> old instanceof FoodTempModifier && (old.getNBT().getBoolean("kncraftMeal") || managedMeals.contains(old.getNBT().getString("item"))));
        modifier.getNBT().putBoolean("kncraftMeal", true);
        modifier.expires(Math.max(20, Math.min(2400, modifier.getExpireTime())));
    }
}
