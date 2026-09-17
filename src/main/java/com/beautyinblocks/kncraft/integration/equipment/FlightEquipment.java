package com.beautyinblocks.kncraft.integration.equipment;

import com.beautyinblocks.kncraft.core.ExpansionConfig;
import com.illusivesoulworks.elytraslot.platform.Services;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public final class FlightEquipment {
    public static ItemStack equipped(LivingEntity entity, EquipmentSlot slot) {
        ItemStack armor = entity.getItemBySlot(slot);
        if (!ExpansionConfig.FLIGHT.get() || slot != EquipmentSlot.CHEST || armor.canElytraFly(entity)) return armor;
        ItemStack wings = Services.ELYTRA.getEquipped(entity);
        return !wings.isEmpty() && wings.canElytraFly(entity) ? wings : armor;
    }
    private FlightEquipment() {}
}
