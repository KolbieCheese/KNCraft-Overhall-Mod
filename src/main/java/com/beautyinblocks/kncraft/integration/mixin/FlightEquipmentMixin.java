package com.beautyinblocks.kncraft.integration.mixin;

import com.beautyinblocks.kncraft.integration.equipment.FlightEquipment;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** Redirect equipment reads only inside the installed mod's flight-specific procedures. */
@Mixin(targets = {
    "net.mcreator.moreenchantments.procedures.ActionKeyPressedProcedure",
    "net.mcreator.moreenchantments.procedures.AscendingProcProcedure",
    "net.mcreator.moreenchantments.procedures.BeatOfWingsProcProcedure",
    "net.mcreator.moreenchantments.procedures.GustOfWindProcProcedure",
    "net.mcreator.moreenchantments.procedures.GlidingProcProcedure",
    "net.mcreator.moreenchantments.procedures.DescendingProcProcedure",
    "net.mcreator.moreenchantments.procedures.WingingProcProcedure"
}, remap = false)
public class FlightEquipmentMixin {
    @Redirect(method = "execute", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/world/entity/LivingEntity;m_6844_(Lnet/minecraft/world/entity/EquipmentSlot;)Lnet/minecraft/world/item/ItemStack;"))
    private static ItemStack kncraft$equippedWings(LivingEntity entity, EquipmentSlot slot) {
        return FlightEquipment.equipped(entity, slot);
    }
}
