package com.beautyinblocks.kncraft.integration.mixin;

import com.beautyinblocks.kncraft.integration.equipment.ReservedFood;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net.p3pp3rf1y.sophisticatedcore.upgrades.feeding.FeedingUpgradeWrapper", remap = false)
public abstract class ReservedFoodMixin {
    @Inject(method = "isEdible", at = @At("HEAD"), cancellable = true)
    private static void kncraft$reserveFood(ItemStack stack, LivingEntity entity, CallbackInfoReturnable<Boolean> result) {
        if (entity instanceof Player player && ReservedFood.protects(player, stack)) result.setReturnValue(false);
    }
}
