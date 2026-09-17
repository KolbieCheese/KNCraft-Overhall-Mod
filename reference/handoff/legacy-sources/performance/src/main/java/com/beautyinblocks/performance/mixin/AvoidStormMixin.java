package com.beautyinblocks.performance.mixin;
import com.beautyinblocks.performance.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
@Mixin(targets="nonamecrackers2.witherstormmod.common.entity.goal.AvoidWitherStormGoal", remap=false)
public class AvoidStormMixin {
    @Unique private PathfinderMob kncraft$mob;
    @Inject(method="<init>",at=@At("RETURN"),remap=false)
    private void rememberMob(PathfinderMob mob,float distance,double walk,double sprint,CallbackInfo ci) {kncraft$mob=mob;}
    @Inject(method="getNearestLoadedBlockPos",at=@At("HEAD"),cancellable=true,remap=false)
    private void search(Vec3 pos,int radius,Block block,CallbackInfoReturnable<BlockPos> cir) {
        if(Performance.PORTAL_SEARCH.get() && kncraft$mob!=null && kncraft$mob.level() instanceof ServerLevel level) {
            var result=PortalSearch.find(level,pos,radius,block);
            if(result.handled()) cir.setReturnValue(result.position());
        }
    }
}
