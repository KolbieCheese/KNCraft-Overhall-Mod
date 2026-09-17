package com.beautyinblocks.kncraft.integration.mixin;

import com.beautyinblocks.kncraft.integration.equipment.FlightEquipment;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** Leave the four armor-piece checks alone; adapt only Iron/Leaden Wings reads. */
@Mixin(targets = "net.mcreator.moreenchantments.procedures.ArmoringProcProcedure", remap = false)
public abstract class WingArmorMixin {
    @Redirect(method = "execute", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;m_6844_(Lnet/minecraft/world/entity/EquipmentSlot;)Lnet/minecraft/world/item/ItemStack;", ordinal = 4))
    private static ItemStack kncraft$iron(LivingEntity entity, EquipmentSlot slot) { return FlightEquipment.equipped(entity, slot); }
    @Redirect(method = "execute", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;m_6844_(Lnet/minecraft/world/entity/EquipmentSlot;)Lnet/minecraft/world/item/ItemStack;", ordinal = 5))
    private static ItemStack kncraft$leaden(LivingEntity entity, EquipmentSlot slot) { return FlightEquipment.equipped(entity, slot); }
}
