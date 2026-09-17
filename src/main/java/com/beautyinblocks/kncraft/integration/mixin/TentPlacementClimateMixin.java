package com.beautyinblocks.kncraft.integration.mixin;

import com.beautyinblocks.kncraft.core.ExpansionConfig;
import com.beautyinblocks.kncraft.integration.tentclimate.TentClimate;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import nomadictents.structure.TentPlacer;
import nomadictents.tileentity.TentDoorBlockEntity;
import nomadictents.util.Tent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = TentPlacer.class, remap = false)
public class TentPlacementClimateMixin {
    @Inject(method = "placeOrUpgradeTent", at = @At("RETURN"))
    private void kncraft$capture(Level level, BlockPos pos, Tent tent, ServerLevel source,
                                Vec3 exit, float rotation, CallbackInfoReturnable<Boolean> callback) {
        if (callback.getReturnValue() && ExpansionConfig.TENT_CLIMATE.get()
            && level.getBlockEntity(pos) instanceof TentDoorBlockEntity door) TentClimate.sample(door, true);
    }
}
