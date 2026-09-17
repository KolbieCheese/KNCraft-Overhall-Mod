package com.beautyinblocks.kncraft.integration.encounters;

import com.beautyinblocks.kncraft.core.ArchitectureConfig;
import java.util.Set;
import java.util.UUID;
import java.util.Collections;
import java.util.WeakHashMap;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;

public final class Encounters {
    private final Set<LivingEntity> pending = Collections.newSetFromMap(new WeakHashMap<>());
    @SubscribeEvent public void joined(EntityJoinLevelEvent event) {
        if (!event.getLevel().isClientSide && event.getEntity() instanceof LivingEntity entity
            && eligible(entity)) pending.add(entity);
    }
    private boolean eligible(LivingEntity entity) {
        return EncounterRules.eligible(String.valueOf(ForgeRegistries.ENTITY_TYPES.getKey(entity.getType())),
            entity.level().dimension().location().toString(), entity.getTags());
    }
    @SubscribeEvent public void started(ServerStartedEvent event) {
        // Retire only the legacy scheduled function; do not leave a stale world timer after migration.
        event.getServer().getWorldData().overworldData().getScheduledEvents().remove("kncraft:encounter");
    }
    @SubscribeEvent public void stopped(ServerStoppedEvent event) { pending.clear(); }
    @SubscribeEvent public void tick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.getServer().getTickCount() % 20 != 0) return;
        for (var entity : Set.copyOf(pending)) {
            pending.remove(entity);
            if (!ArchitectureConfig.ENCOUNTERS.get() || entity.isRemoved() || !eligible(entity)) continue;
            scale(entity);
        }
    }
    public static void scale(LivingEntity entity) {
        boolean storm = String.valueOf(ForgeRegistries.ENTITY_TYPES.getKey(entity.getType())).equals("witherstormmod:wither_storm");
        String tag = storm ? EncounterRules.STORM_TAG : EncounterRules.CORE_TAG;
        if (entity.getTags().contains(tag)) return;
        UUID healthId = storm ? EncounterRules.STORM_HEALTH : EncounterRules.CORE_HEALTH;
        var health = entity.getAttribute(Attributes.MAX_HEALTH);
        if (health == null) return;
        boolean initialized = health.getModifier(healthId) != null;
        add(entity, Attributes.MAX_HEALTH, healthId, storm ? "kncraft_storm_health" : "kncraft_community", storm ? .5 : 1, AttributeModifier.Operation.MULTIPLY_BASE);
        if (storm) {
            add(entity, Attributes.ATTACK_DAMAGE, EncounterRules.STORM_DAMAGE, "kncraft_storm_damage", .5, AttributeModifier.Operation.MULTIPLY_BASE);
            add(entity, Attributes.ARMOR, EncounterRules.STORM_ARMOR, "kncraft_storm_armor", 4, AttributeModifier.Operation.ADDITION);
        }
        if (!initialized) entity.setHealth((float) Math.floor(health.getValue()));
        entity.addTag(tag);
    }
    private static void add(LivingEntity entity, Attribute attribute, UUID id, String name, double amount, AttributeModifier.Operation operation) {
        var instance = entity.getAttribute(attribute);
        if (instance != null && instance.getModifier(id) == null) instance.addPermanentModifier(new AttributeModifier(id, name, amount, operation));
    }
}
