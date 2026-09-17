package com.beautyinblocks.kncraft.integration.tentclimate;

import com.beautyinblocks.kncraft.core.ExpansionConfig;
import com.momosoftworks.coldsweat.api.event.common.temperautre.TempModifierEvent;
import com.momosoftworks.coldsweat.api.temperature.modifier.BiomeTempModifier;
import com.momosoftworks.coldsweat.api.temperature.modifier.TempModifier;
import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.util.world.WorldHelper;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import nomadictents.structure.TentPlacer;
import nomadictents.tileentity.TentDoorBlockEntity;

/** Replaces only ambient climate; heat sources, wetness, armor and meals remain native. */
public final class TentClimate {
    private static final BlockPos DOOR = new BlockPos(0, 64, 0);
    private static final String SNAPSHOT = "KNCraftExteriorClimate";
    private static final Set<String> AMBIENT = Set.of("BiomeTempModifier", "CaveBiomeTempModifier",
        "ElevationTempModifier", "ShadeTempModifier", "StormTempModifier", "SereneSeasonsTempModifier");
    private static final Map<ServerLevel, Sample> samples = new WeakHashMap<>();
    private static final Map<Level, Geometry> geometry = new WeakHashMap<>();
    private record Sample(String source, long pos, long tick, double value) {}
    private record Geometry(Object type, Object size, int layers, AABB bounds) {}

    public static TentDoorBlockEntity door(Level level) {
        if (!(level instanceof ServerLevel) || !level.dimension().location().getNamespace().equals("nomadictents")
            || !level.hasChunkAt(DOOR)) return null;
        return level.getBlockEntity(DOOR) instanceof TentDoorBlockEntity door ? door : null;
    }

    /** The native template bounds include the entry and expandable excavatable floor. */
    public static AABB interior(Level level) {
        var door = door(level);
        if (door == null) return null;
        var tent = door.getTent();
        var cached = geometry.get(level);
        if (cached != null && cached.type.equals(tent.getType()) && cached.size.equals(tent.getSize())
            && cached.layers == tent.getLayers()) return cached.bounds;
        var template = TentPlacer.getTemplate(level, tent.getType(), tent.getSize());
        if (template == null) return null;
        var size = template.getSize();
        int z = DOOR.getZ() - size.getZ() / 2;
        var bounds = new AABB(0, DOOR.getY() - tent.getLayers() - 1, z,
            size.getX(), DOOR.getY() + size.getY(), z + size.getZ());
        geometry.put(level, new Geometry(tent.getType(), tent.getSize(), tent.getLayers(), bounds));
        return bounds;
    }

    public static boolean enclosed(Level level, BlockPos pos) {
        if (!ExpansionConfig.TENT_ENCLOSURE.get()) return false;
        var bounds = interior(level);
        return bounds != null && bounds.contains(pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5);
    }

    private static boolean ambient(TempModifier modifier) {
        return AMBIENT.contains(modifier.getClass().getSimpleName());
    }

    /** Never tickets or generates the exterior. A saved sample survives unloaded camps/restarts. */
    public static OptionalDouble sample(TentDoorBlockEntity door, boolean force) {
        if (!(door.getLevel() instanceof ServerLevel inside)) return OptionalDouble.empty();
        var outside = door.getSpawnDimension();
        if (outside == null || outside == inside || outside.dimension().location().getNamespace().equals("nomadictents"))
            return OptionalDouble.empty();
        var pos = BlockPos.containing(door.getSpawnpoint());
        String source = outside.dimension().location().toString();
        long now = inside.getGameTime();
        var previous = samples.get(inside);
        if (!force && previous != null && previous.source.equals(source) && previous.pos == pos.asLong()
            && now - previous.tick >= 0 && now - previous.tick < ExpansionConfig.CLIMATE_INTERVAL.get())
            return OptionalDouble.of(previous.value);
        if (!samplingAreaLoaded(outside, pos)) {
            var saved = door.getPersistentData().getCompound(SNAPSHOT);
            if (saved.getString("dimension").equals(source) && saved.getLong("position") == pos.asLong()
                && saved.contains("temperature") && Double.isFinite(saved.getDouble("temperature")))
                return OptionalDouble.of(saved.getDouble("temperature"));
            return OptionalDouble.empty();
        }
        // The dummy is never spawned or teleported. Its public modifier pipeline preserves
        // configured biome/day-night/dimension/elevation/weather rules without exterior appliances.
        var probe = WorldHelper.getDummyPlayer(outside);
        probe.setPos(pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5);
        double value = 0;
        for (var modifier : Temperature.getModifiers(probe, Temperature.Trait.WORLD)) {
            if (!ambient(modifier)) continue;
            if (modifier.getClass().getSimpleName().equals("StormTempModifier")
                && weather2.ServerTickHandler.getWeatherManagerFor(outside.dimension()) == null) continue;
            value = modifier.update(value, probe, Temperature.Trait.WORLD);
        }
        if (!Double.isFinite(value)) return OptionalDouble.empty();
        samples.put(inside, new Sample(source, pos.asLong(), now, value));
        var saved = new net.minecraft.nbt.CompoundTag();
        saved.putString("dimension", source); saved.putLong("position", pos.asLong());
        saved.putDouble("temperature", value);
        saved.putLong("sampleTick", outside.getGameTime());
        door.getPersistentData().put(SNAPSHOT, saved); door.setChanged();
        return OptionalDouble.of(value);
    }

    public static boolean samplingAreaLoaded(ServerLevel outside, BlockPos pos) {
        // Cold Sweat's player biome grid samples up to 30 blocks away, with
        // biome interpolation around each point. Never let that read generate
        // neighboring chunks after the camper has left the exterior.
        for (int x = (pos.getX() - 40) >> 4; x <= (pos.getX() + 40) >> 4; x++)
            for (int z = (pos.getZ() - 40) >> 4; z <= (pos.getZ() + 40) >> 4; z++)
                if (outside.getChunkSource().getChunkNow(x, z) == null) return false;
        // Native structure-temperature lookup can follow references back to a
        // start chunk. Require those to be resident as well before using it.
        for (var references : outside.structureManager().getAllStructuresAt(pos).values())
            for (long chunk : references)
                if (outside.getChunkSource().getChunkNow(net.minecraft.world.level.ChunkPos.getX(chunk),
                    net.minecraft.world.level.ChunkPos.getZ(chunk)) == null) return false;
        return true;
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void climate(TempModifierEvent.Calculate.Pre event) {
        if (!ExpansionConfig.TENT_CLIMATE.get() || event.getTrait() != Temperature.Trait.WORLD
            || !ambient(event.getModifier())) return;
        var door = door(event.getEntity().level());
        if (door == null) return;
        var bounds = interior(door.getLevel());
        if (bounds == null || !bounds.contains(event.getEntity().position())) return;
        var value = sample(door, false);
        if (value.isEmpty()) return;
        // Supply all exterior ambient components once, then preserve interior block/source effects.
        event.setFunction(event.getModifier() instanceof BiomeTempModifier
            ? temperature -> temperature + value.getAsDouble() : temperature -> temperature);
        event.setCanceled(true);
    }

    @SubscribeEvent public void unloaded(LevelEvent.Unload event) { samples.remove(event.getLevel()); geometry.remove(event.getLevel()); }
    @SubscribeEvent public void reloaded(net.minecraftforge.event.OnDatapackSyncEvent event) {
        if (event.getPlayer() == null) { samples.clear(); geometry.clear(); }
    }
    @SubscribeEvent public void stopped(ServerStoppedEvent event) { samples.clear(); geometry.clear(); }
}
