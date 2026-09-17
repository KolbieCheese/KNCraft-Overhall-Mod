package com.beautyinblocks.performance.mixin;
import com.beautyinblocks.performance.Performance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import weather2.weathersystem.storm.StormObject;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import java.util.List;
@Mixin(targets="weather2.weathersystem.storm.TornadoHelper",remap=false)
public class TornadoMixin {
    @Shadow public StormObject storm;
    @SuppressWarnings({"unchecked","rawtypes"})
    @Redirect(method="forceRotate(Lnet/minecraft/world/level/Level;Z)Z",at=@At(value="INVOKE",target="Lnet/minecraft/world/level/Level;m_45976_(Ljava/lang/Class;Lnet/minecraft/world/phys/AABB;)Ljava/util/List;",remap=false),remap=false)
    private List<Entity> query(Level level,Class<Entity> type,AABB box) {
        if(!Performance.TORNADO_QUERY.get() || level.isClientSide || type!=Entity.class) return level.getEntitiesOfClass(type,box);
        // Upstream forceRotate only acts on living entities, or items for pet storms.
        // Keep all subsequent distance, shelter, player, mob and pet-grabbing checks unchanged.
        return storm.isPet() ? (List)level.getEntitiesOfClass(ItemEntity.class,box) : (List)level.getEntitiesOfClass(LivingEntity.class,box);
    }
}
