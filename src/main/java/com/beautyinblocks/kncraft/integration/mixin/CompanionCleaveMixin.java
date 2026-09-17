package com.beautyinblocks.kncraft.integration.mixin;

import com.beautyinblocks.kncraft.core.ExpansionConfig;
import net.bettercombat.logic.TargetHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = TargetHelper.class, remap = false)
public class CompanionCleaveMixin {
    @Inject(method = "getRelation", at = @At("RETURN"), cancellable = true)
    private static void kncraft$knownOwner(Player attacker, Entity target, CallbackInfoReturnable<TargetHelper.Relation> ci) {
        // Better Combat already protects loaded owners. Cover an absent owner without
        // changing wild animals or making another player's companion invulnerable.
        if (ExpansionConfig.COMPANIONS.get() && ci.getReturnValue() == TargetHelper.Relation.HOSTILE
            && target instanceof OwnableEntity companion && companion.getOwnerUUID() != null)
            ci.setReturnValue(companion.getOwnerUUID().equals(attacker.getUUID())
                ? TargetHelper.Relation.FRIENDLY : TargetHelper.Relation.NEUTRAL);
    }
}
