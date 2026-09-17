package com.beautyinblocks.kncraft.integration.mixin;

import com.beautyinblocks.kncraft.integration.tentclimate.TentClimate;
import com.momosoftworks.coldsweat.util.world.WorldHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = WorldHelper.class, remap = false)
public class TentEnclosureMixin {
    @Inject(method = "canSeeSky", at = @At("HEAD"), cancellable = true)
    private static void kncraft$roof(Level level, BlockPos pos, int distance, CallbackInfoReturnable<Boolean> ci) {
        if (TentClimate.enclosed(level, pos)) ci.setReturnValue(false);
    }
    @Inject(method = "isSpreadBlocked", at = @At("HEAD"), cancellable = true)
    private static void kncraft$boundary(LevelAccessor level, BlockState state, BlockPos pos,
                                       Direction from, Direction to, CallbackInfoReturnable<Boolean> ci) {
        if (level instanceof Level world && TentClimate.enclosed(world, pos)
            && !TentClimate.enclosed(world, pos.relative(to))) ci.setReturnValue(true);
    }
}
