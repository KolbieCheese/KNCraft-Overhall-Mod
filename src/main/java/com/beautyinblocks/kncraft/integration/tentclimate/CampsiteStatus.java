package com.beautyinblocks.kncraft.integration.tentclimate;

import com.beautyinblocks.kncraft.core.ExpansionConfig;
import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.common.blockentity.HearthBlockEntity;
import java.util.*;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class CampsiteStatus {
    public static List<String> inspect(ServerPlayer player) {
        var lines = new ArrayList<String>();
        var level = player.serverLevel();
        var door = TentClimate.door(level);
        if (door == null) { lines.add("Enter a Nomadic Tent to inspect its campsite."); return lines; }
        var outside = door.getSpawnDimension();
        var source = BlockPos.containing(door.getSpawnpoint());
        if (outside == null) lines.add("Campsite source dimension is unavailable.");
        else {
            lines.add("Campsite: " + outside.dimension().location() + " at " + source.toShortString());
            var value = ExpansionConfig.TENT_CLIMATE.get() ? TentClimate.sample(door, false) : OptionalDouble.empty();
            boolean loaded = TentClimate.samplingAreaLoaded(outside, source);
            if (!ExpansionConfig.TENT_CLIMATE.get()) lines.add("Exterior climate inheritance is disabled by the server.");
            else if (value.isEmpty()) lines.add("No valid exterior sample yet; native interior climate is in use.");
            else {
                double f = Temperature.convert(value.getAsDouble(), Temperature.Units.MC, Temperature.Units.F, true);
                double c = Temperature.convert(value.getAsDouble(), Temperature.Units.MC, Temperature.Units.C, true);
                lines.add(String.format(Locale.ROOT, "Exterior ambient: %.1f F / %.1f C (%s).", f, c, loaded ? "live campsite" : "saved sample; exterior unloaded"));
            }
        }
        lines.add("Your position is " + (TentClimate.enclosed(level, player.blockPosition()) ? "inside the enclosed tent bounds." : "outside managed enclosure, or enclosure is disabled."));
        // Inspect existing nearby chunks only. A command must not ticket or generate any terrain.
        var hearths = new ArrayList<HearthBlockEntity>();
        for (int x = (player.getBlockX() - 48) >> 4; x <= (player.getBlockX() + 48) >> 4; x++)
            for (int z = (player.getBlockZ() - 48) >> 4; z <= (player.getBlockZ() + 48) >> 4; z++) {
                var chunk = level.getChunkSource().getChunkNow(x, z);
                if (chunk == null) continue;
                for (var be : chunk.getBlockEntities().values()) if (be instanceof HearthBlockEntity hearth && hearth.getBlockPos().distSqr(player.blockPosition()) <= 48 * 48) hearths.add(hearth);
            }
        hearths.sort(Comparator.comparingDouble(h -> h.getBlockPos().distSqr(player.blockPosition())));
        for (var hearth : hearths.stream().limit(4).toList()) {
            boolean covers = hearth.areaContainsEntity(player);
            lines.add(hearth.getBlockState().getBlock().getName().getString() + " at " + hearth.getBlockPos().toShortString()
                + ": hot/cold fuel " + hearth.getHotFuel() + "/" + hearth.getColdFuel() + "; heating/cooling enabled "
                + hearth.isHeatingOn() + "/" + hearth.isCoolingOn() + "; warmth/cooling level " + hearth.getHeatingLevel() + "/" + hearth.getCoolingLevel()
                + "; spread reaches you=" + covers + "; automatic=" + hearth.isSmartEnabled());
        }
        if (hearths.isEmpty()) lines.add("No loaded hearth, boiler or icebox within 48 blocks. Check fuel, smokestacks and native redstone controls.");
        lines.add("Field Guide: Cold Sweat > A hearth inside a tent. Equipment range and warm-up still apply.");
        return lines;
    }
    @SubscribeEvent public void commands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("kncraft").then(Commands.literal("camp").executes(ctx -> {
            inspect(ctx.getSource().getPlayerOrException()).forEach(s -> ctx.getSource().sendSuccess(() -> Component.literal(s), false)); return 1;
        })));
    }
}
