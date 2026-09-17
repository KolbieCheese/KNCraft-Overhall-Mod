package com.beautyinblocks.kncraft.integration.climate;

import com.beautyinblocks.kncraft.core.ExpansionConfig;
import com.momosoftworks.coldsweat.api.event.core.registry.LoadRegistriesEvent;
import com.momosoftworks.coldsweat.api.event.common.insulation.InsulationTickEvent;
import com.momosoftworks.coldsweat.api.insulation.Insulation;
import com.momosoftworks.coldsweat.config.spec.ItemSettingsConfig;
import com.momosoftworks.coldsweat.data.ModRegistries;
import com.momosoftworks.coldsweat.data.codec.configuration.FuelData;
import com.momosoftworks.coldsweat.data.codec.configuration.InsulatorData;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;
import top.theillusivec4.curios.api.CuriosApi;

/** Adds defaults through Cold Sweat's registry API; existing server definitions win. */
public final class AetherClimate {
    private static final Map<String, Double> managedHeat = new HashMap<>();
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void load(LoadRegistriesEvent.Pre event) {
        managedHeat.clear();
        if (!ExpansionConfig.AETHER_THERMAL.get()) return;
        accessory(event, "ice_ring", 0, 2);
        accessory(event, "ice_pendant", 0, 2);
        for (String color : List.of("white", "orange", "magenta", "light_blue", "yellow", "lime", "pink", "gray", "light_gray", "cyan", "purple", "blue", "brown", "green", "red", "black"))
            accessory(event, color + "_cape", .5, .5);
        fuel(event, FuelData.FuelType.ICEBOX, 100, ItemSettingsConfig.ICEBOX_FUELS.get());
        fuel(event, FuelData.FuelType.HEARTH, -100, ItemSettingsConfig.HEARTH_FUELS.get());
    }
    private static void accessory(LoadRegistriesEvent.Pre event, String path, double cold, double heat) {
        String id = "aether:" + path;
        var key = new ResourceLocation(id);
        if (!ForgeRegistries.ITEMS.containsKey(key)) return;
        var stack = new ItemStack(ForgeRegistries.ITEMS.getValue(key));
        boolean external = event.getRegistry(ModRegistries.INSULATOR_DATA).stream()
            .anyMatch(holder -> holder.value().slot() == Insulation.Slot.CURIO && holder.value().test(stack));
        external |= ItemSettingsConfig.INSULATING_CURIOS.get().stream()
            .map(row -> InsulatorData.fromToml(row, Insulation.Slot.CURIO)).filter(Objects::nonNull).anyMatch(data -> data.test(stack));
        if (external) return;
        var data = InsulatorData.fromToml(List.of(id, cold, heat, "static", "", true), Insulation.Slot.CURIO);
        if (data != null) { event.addRegistryEntry(ModRegistries.INSULATOR_DATA, data); managedHeat.put(id, heat); }
    }
    private static void fuel(LoadRegistriesEvent.Pre event, FuelData.FuelType type, int amount, List<? extends List<?>> toml) {
        var key = new ResourceLocation("aether:icestone");
        if (!ForgeRegistries.ITEMS.containsKey(key)) return;
        var stack = new ItemStack(ForgeRegistries.ITEMS.getValue(key));
        boolean external = event.getRegistry(ModRegistries.FUEL_DATA).stream().anyMatch(h -> h.value().fuelType() == type && h.value().test(stack));
        external |= toml.stream().map(row -> FuelData.fromToml(row, type)).filter(Objects::nonNull).anyMatch(data -> data.test(stack));
        if (!external) event.addRegistryEntry(ModRegistries.FUEL_DATA, FuelData.fromToml(List.of("aether:icestone", amount), type));
    }
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void cap(InsulationTickEvent event) {
        if (!ExpansionConfig.AETHER_THERMAL.get()) return;
        double ownHeat = CuriosApi.getCuriosInventory(event.getPlayer()).map(inventory -> {
            var items = inventory.getEquippedCurios();
            double total = 0;
            for (int slot = 0; slot < items.getSlots(); slot++) {
                var stack = items.getStackInSlot(slot);
                if (!stack.isEmpty()) total += managedHeat.getOrDefault(ForgeRegistries.ITEMS.getKey(stack.getItem()).toString(), 0.0);
            }
            return total;
        }).orElse(0.0);
        double excess = Math.max(0, ownHeat - ExpansionConfig.ACCESSORY_CAP.get());
        if (excess == 0) return;
        // Cold Sweat 2.4.3 folds Curios into the armor modifier through this event.
        // Remove only our excess, preserving other accessories and armor contributions.
        event.setProperty("heat_curios", event.getProperty("heat_curios") - excess);
    }
}
