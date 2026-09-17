package com.beautyinblocks.kncraft.integration.guide;

import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public final class GuideNetwork {
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(new ResourceLocation("kncraft", "guide"), () -> "1", "1"::equals, "1"::equals);
    private static final Map<ServerPlayer, Long> LAST = new WeakHashMap<>();
    public record Request() {}
    public record Values(CompoundTag data) {}
    public static void register() {
        CHANNEL.messageBuilder(Request.class, 0, NetworkDirection.PLAY_TO_SERVER)
            .encoder((message, buffer) -> {}).decoder(buffer -> new Request())
            .consumerMainThread((message, context) -> {
                var player = context.get().getSender();
                if (player == null) return;
                long now = player.serverLevel().getGameTime();
                if (now - LAST.getOrDefault(player, Long.MIN_VALUE / 2) < 40) return;
                LAST.put(player, now);
                CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new Values(GuideValues.snapshot(player)));
            }).add();
        CHANNEL.messageBuilder(Values.class, 1, NetworkDirection.PLAY_TO_CLIENT)
            .encoder((message, buffer) -> buffer.writeNbt(message.data()))
            .decoder(buffer -> new Values(buffer.readNbt()))
            .consumerMainThread((message, context) -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> com.beautyinblocks.kncraft.client.GuideClient.accept(message.data()))).add();
    }
    private GuideNetwork() {}
}
