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
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(new ResourceLocation("kncraft", "guide"), () -> "2", "2"::equals, "2"::equals);
    private static final Map<ServerPlayer, Budget> LAST = new WeakHashMap<>();
    private record Budget(long tick, int count) {}
    public record Request(String key) {}
    public record Values(CompoundTag data) {}
    public static void register() {
        CHANNEL.messageBuilder(Request.class, 0, NetworkDirection.PLAY_TO_SERVER)
            .encoder((message, buffer) -> buffer.writeUtf(message.key(), 256)).decoder(buffer -> new Request(buffer.readUtf(256)))
            .consumerMainThread((message, context) -> {
                var player = context.get().getSender();
                if (player == null || !GuideValues.KEYS.containsKey(message.key())) return;
                long now = player.serverLevel().getGameTime();
                var budget = LAST.get(player);
                if (budget == null || now < budget.tick() || now - budget.tick() >= 20) budget = new Budget(now, 0);
                if (budget.count() >= 8) return;
                LAST.put(player, new Budget(budget.tick(), budget.count() + 1));
                CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new Values(GuideValues.snapshot(player, java.util.List.of(message.key()))));
            }).add();
        CHANNEL.messageBuilder(Values.class, 1, NetworkDirection.PLAY_TO_CLIENT)
            .encoder((message, buffer) -> buffer.writeNbt(message.data()))
            .decoder(buffer -> new Values(buffer.readNbt()))
            .consumerMainThread((message, context) -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> com.beautyinblocks.kncraft.client.GuideClient.accept(message.data()))).add();
    }
    private GuideNetwork() {}
}
