package com.beautyinblocks.expansiontest;

import static com.beautyinblocks.expansiontest.ExpansionChecks.check;
import com.beautyinblocks.kncraft.integration.tentclimate.TentClimate;
import com.momosoftworks.coldsweat.api.temperature.modifier.BiomeTempModifier;
import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.common.blockentity.HearthBlockEntity;
import com.momosoftworks.coldsweat.util.world.WorldHelper;
import com.mojang.authlib.GameProfile;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.registries.ForgeRegistries;
import nomadictents.NTSavedData;
import nomadictents.dimension.DimensionFactory;
import nomadictents.dimension.DynamicDimensionHelper;
import nomadictents.structure.TentPlacer;
import nomadictents.tileentity.TentDoorBlockEntity;
import nomadictents.util.Tent;
import nomadictents.util.TentSize;
import nomadictents.util.TentType;

final class TentClimateChecks {
    static void persistence(MinecraftServer server) {
        for (TentType type : TentType.values()) {
            int id = 9000000 + type.ordinal();
            var key = NTSavedData.get(server).getOrCreateKey(server, id);
            var level = DynamicDimensionHelper.getOrCreateWorld(server, key, DimensionFactory::createDimension);
            var pos = Tent.calculatePos(id); level.getChunk(pos);
            var door = (TentDoorBlockEntity)level.getBlockEntity(pos);
            check(door != null, "Saved tent door absent after restart");
            var saved = door.getPersistentData().getCompound("KNCraftExteriorClimate");
            check(saved.contains("temperature") && saved.contains("dimension"), "Saved campsite lost after restart");
            check(TentClimate.sample(door, false).isPresent(), "Saved campsite could not supply climate after restart");
        }
    }
    static void run(MinecraftServer server) throws Exception {
        var source = server.overworld();
        var outside = new BlockPos(2100, 150, 2100);
        loadSampleArea(source, outside);
        int shapes = 0;
        for (TentType type : TentType.values()) {
            int id = 9000000 + type.ordinal();
            var key = NTSavedData.get(server).getOrCreateKey(server, id);
            ServerLevel inside = DynamicDimensionHelper.getOrCreateWorld(server, key, DimensionFactory::createDimension);
            var pos = Tent.calculatePos(id); inside.getChunk(pos);
            for (TentSize size : TentSize.values()) {
                var tent = new Tent(id, type, size, (byte)2);
                check(TentPlacer.getInstance().placeOrUpgradeTent(inside, pos, tent, source, Vec3.atBottomCenterOf(outside), 0), "Native placement failed");
                var door = (TentDoorBlockEntity)inside.getBlockEntity(pos);
                check(door != null, "Interior door missing");
                var bounds = TentClimate.interior(inside);
                check(bounds != null && bounds.contains(2.5, 62.5, .5), "Expanded floor missing: " + type + "/" + size);
                check(TentClimate.enclosed(inside, new BlockPos(2, 64, 0)), "Interior not enclosed: " + type + "/" + size);
                com.beautyinblocks.kncraft.core.ExpansionConfig.TENT_ENCLOSURE.set(false);
                try { check(!TentClimate.enclosed(inside, new BlockPos(2,64,0)), "Tent enclosure switch ignored"); }
                finally { com.beautyinblocks.kncraft.core.ExpansionConfig.TENT_ENCLOSURE.set(true); }
                check(!WorldHelper.canSeeSky(inside, new BlockPos(2, 64, 0), 64), "Roof leaks: " + type + "/" + size);
                check(!TentClimate.enclosed(inside, new BlockPos(-2, 64, 0)), "Enclosure escapes template");
                check(WorldHelper.isSpreadBlocked(inside, Blocks.AIR.defaultBlockState(), new BlockPos(0,64,0), Direction.EAST, Direction.WEST), "Heat spread escapes entry");
                var temperature = TentClimate.sample(door, true);
                check(temperature.isPresent(), "Exterior sample missing");
                var player = FakePlayerFactory.get(inside, new GameProfile(UUID.randomUUID(), "TentClimateCheck"));
                player.setPos(2.5, 64, .5);
                double applied = new BiomeTempModifier().update(0, player, Temperature.Trait.WORLD);
                check(Math.abs(applied - temperature.getAsDouble()) < .000001, "Interior climate differs from exterior sample");
                check(door.saveWithFullMetadata().toString().contains("KNCraftExteriorClimate"), "Climate snapshot not persisted");
                shapes++;
            }
            var door = (TentDoorBlockEntity)inside.getBlockEntity(pos);
            double oldClimate = TentClimate.sample(door, true).orElseThrow();
            var nether = server.getLevel(Level.NETHER); loadSampleArea(nether, outside);
            door.setSpawnpoint(nether, Vec3.atBottomCenterOf(outside));
            double movedClimate = TentClimate.sample(door, true).orElseThrow();
            check(Math.abs(oldClimate - movedClimate) > .05, "Moved tent did not follow new dimension climate");
            var nativePlayer = FakePlayerFactory.get(inside, new GameProfile(UUID.randomUUID(), "ClimateToggle")); nativePlayer.setPos(2.5,64,.5);
            com.beautyinblocks.kncraft.core.ExpansionConfig.TENT_CLIMATE.set(false);
            try { check(Math.abs(new BiomeTempModifier().update(0,nativePlayer,Temperature.Trait.WORLD) - movedClimate) > .05, "Tent climate switch ignored"); }
            finally { com.beautyinblocks.kncraft.core.ExpansionConfig.TENT_CLIMATE.set(true); }
            // Verify persistence fallback without force-loading a far-away source chunk.
            var absent = new BlockPos(28000000, 150, 28000000);
            check(!source.hasChunkAt(absent), "Unloaded test source unexpectedly loaded");
            door.setSpawnpoint(source, Vec3.atBottomCenterOf(absent));
            var snapshot = door.getPersistentData().getCompound("KNCraftExteriorClimate");
            snapshot.putString("dimension", source.dimension().location().toString()); snapshot.putLong("position", absent.asLong()); snapshot.putDouble("temperature", oldClimate);
            check(TentClimate.sample(door, true).orElseThrow() == oldClimate, "Saved exterior sample was lost");
            check(!source.hasChunkAt(absent), "Climate sampling loaded exterior chunk");
            door.setSpawnpoint(source, Vec3.atBottomCenterOf(outside));
            TentClimate.sample(door, true);
            hearth(inside, type);
        }
        System.out.println("TENT CLIMATE: " + shapes + " shape/size combinations, expanded floors, source changes and unloaded snapshots checked");
    }
    private static void loadSampleArea(ServerLevel level, BlockPos pos) {
        for (int x = (pos.getX() - 48) >> 4; x <= (pos.getX() + 48) >> 4; x++)
            for (int z = (pos.getZ() - 48) >> 4; z <= (pos.getZ() + 48) >> 4; z++) level.getChunk(x,z);
    }
    private static void hearth(ServerLevel level, TentType type) throws Exception {
        var pos = new BlockPos(4,64,0);
        for (int x=2;x<=8;x++) for (int y=64;y<=67;y++) for (int z=-2;z<=2;z++) level.setBlock(new BlockPos(x,y,z), Blocks.AIR.defaultBlockState(), 3);
        level.setBlock(pos.below(), Blocks.STONE.defaultBlockState(), 3);
        level.setBlock(pos, ForgeRegistries.BLOCKS.getValue(new ResourceLocation("cold_sweat:hearth_bottom")).defaultBlockState(), 2);
        level.setBlock(pos.above(), ForgeRegistries.BLOCKS.getValue(new ResourceLocation("cold_sweat:hearth_top")).defaultBlockState(), 3);
        level.setBlock(pos.east(), Blocks.REDSTONE_BLOCK.defaultBlockState(), 3);
        var hearth = (HearthBlockEntity)level.getBlockEntity(pos);
        check(hearth != null, "Hearth did not place");
        var player = FakePlayerFactory.get(level, new GameProfile(UUID.randomUUID(), "HearthCheck"));
        player.setPos(6.5, 64, .5);
        level.addNewPlayer(player);
        try {
            Temperature.set(player, Temperature.Trait.WORLD, -5);
            Temperature.set(player, Temperature.Trait.CORE, -40);
            hearth.setHotFuel(500, false);
            for (int tick=0;tick<500;tick++) hearth.tick(level, pos);
            System.out.println("HEARTH " + type + ": paths=" + hearth.getPaths().size() + " fuel=" + hearth.getHotFuel() + " contains=" + hearth.areaContainsEntity(player) + " effects=" + player.getActiveEffects());
            check(hearth.areaContainsEntity(player), "Hearth did not reach tent occupant: " + type);
            var warmth = ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation("cold_sweat:warmth"));
            check(player.hasEffect(warmth), "Hearth did not warm tent occupant: " + type);
            check(hearth.getHotFuel() < 500 && hearth.getHotFuel() > 0, "Hearth fuel rules changed");
            check(hearth.getPathLookup().keySet().stream().allMatch(path -> TentClimate.enclosed(level, path)), "Hearth spread escaped tent bounds");
        } finally { player.discard(); level.removeBlock(pos, false); level.removeBlock(pos.above(), false); }
    }
}
