package com.beautyinblocks.tents.mixin;

import com.beautyinblocks.tents.TentPortals;
import net.minecraft.world.entity.Entity;
import qouteall.imm_ptl.core.portal.Portal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = Portal.class, remap = false)
public class PortalMixin {
    @Inject(method = "canTeleportEntity", at = @At("HEAD"), cancellable = true, remap = false)
    private void checkTent(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        Portal portal = (Portal)(Object)this;
        if (!portal.level().isClientSide && TentPortals.managed(portal) && !TentPortals.allows(portal, entity)) cir.setReturnValue(false);
    }
}
