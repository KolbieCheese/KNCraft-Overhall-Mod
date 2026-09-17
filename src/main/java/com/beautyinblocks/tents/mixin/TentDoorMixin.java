package com.beautyinblocks.tents.mixin;

import com.beautyinblocks.tents.TentPortals;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import nomadictents.tileentity.TentDoorBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = TentDoorBlockEntity.class, remap = false)
public class TentDoorMixin {
    @Inject(method = "onEnter", at = @At("HEAD"), cancellable = true, remap = false)
    private void enter(Entity entity, CallbackInfo ci) {
        if (TentPortals.open((TentDoorBlockEntity)(Object)this, entity)) ci.cancel();
    }
    @Inject(method = "entityInside", at = @At("HEAD"), cancellable = true, remap = false)
    private void collide(BlockState state, Level level, BlockPos pos, Entity entity, CallbackInfo ci) {
        if (!level.isClientSide && TentPortals.hasLivePortal((TentDoorBlockEntity)(Object)this)) ci.cancel();
    }
}
