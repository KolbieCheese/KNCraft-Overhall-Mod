package com.beautyinblocks.kncraft.integration.mixin;

import com.beautyinblocks.kncraft.core.ExpansionConfig;
import com.beautyinblocks.kncraft.integration.climate.ClimateIntegration;
import com.beautyinblocks.kncraft.integration.equipment.FeedingPolicy;
import com.momosoftworks.coldsweat.api.util.Temperature;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net.p3pp3rf1y.sophisticatedcore.upgrades.feeding.FeedingUpgradeWrapper", remap = false)
public class FeedingClimateMixin {
    @Inject(method = "isEdible", at = @At("HEAD"), cancellable = true)
    private static void kncraft$avoidWrongMeal(ItemStack stack, LivingEntity entity, CallbackInfoReturnable<Boolean> ci) {
        if (ExpansionConfig.SMART_FEEDING.get() && entity instanceof Player player
            && !FeedingPolicy.allows(Temperature.get(player, Temperature.Trait.BODY),
                ClimateIntegration.mealTemperature(stack), ExpansionConfig.FEEDING_THRESHOLD.get(), player.getFoodData().getFoodLevel()))
            ci.setReturnValue(false);
    }
}
