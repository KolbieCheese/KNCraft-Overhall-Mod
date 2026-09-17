package com.beautyinblocks.kncraft.integration.equipment;

import com.beautyinblocks.kncraft.core.PolishConfig;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.p3pp3rf1y.sophisticatedcore.upgrades.magnet.MagnetUpgradeWrapper;

/** Native magnet extension point; offers are detected once and never extend their deadline. */
public final class WildlifeOfferings {
    private record Offer(long deadline) {}
    private static final Map<ItemEntity, Offer> offers = new WeakHashMap<>();
    private final Class<?> targets;
    private final Method accepts;
    public WildlifeOfferings() {
        try {
            targets = Class.forName("com.github.alexthe666.alexsmobs.entity.ITargetsDroppedItems");
            accepts = targets.getMethod("canTargetItem", ItemStack.class);
        } catch (ReflectiveOperationException ex) { throw new IllegalStateException("Audited Alex's item-targeting API unavailable", ex); }
        MagnetUpgradeWrapper.addMagnetPreventionChecker(this::protectedOffering);
    }
    public boolean protectedOffering(Entity entity) {
        if (!PolishConfig.OFFERINGS.get() || !(entity instanceof ItemEntity item) || entity.level().isClientSide || !item.isAlive()) return false;
        var known = offers.get(item);
        long now = item.level().getGameTime();
        if (known != null) return now < known.deadline;
        // Only a recent player throw, never farm drops, mob loot or permanently ignored items.
        if (!(item.getOwner() instanceof net.minecraft.world.entity.player.Player) || item.getAge() > 20) return false;
        boolean accepted = item.level().getEntitiesOfClass(Mob.class, item.getBoundingBox().inflate(8), m -> m.isAlive() && targets.isInstance(m))
            .stream().anyMatch(m -> accepts(m, item.getItem()) && m.hasLineOfSight(item));
        // Remember rejected throws as well, avoiding repeated animal scans for the same entity.
        long deadline = accepted ? now + Math.max(0, PolishConfig.OFFERING_TICKS.get() - item.getAge()) : now;
        offers.put(item, new Offer(deadline));
        return now < deadline;
    }
    private boolean accepts(Mob mob, ItemStack stack) {
        try { return (boolean)accepts.invoke(mob, stack); }
        catch (ReflectiveOperationException ex) { throw new IllegalStateException("Unable to query native wildlife food eligibility", ex); }
    }
}
