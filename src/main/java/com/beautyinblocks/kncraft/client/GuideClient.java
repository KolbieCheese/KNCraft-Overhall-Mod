package com.beautyinblocks.kncraft.client;

import com.beautyinblocks.kncraft.integration.guide.GuideNetwork;
import com.beautyinblocks.kncraft.integration.guide.GuideSnapshotCache;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "kncraft", value = Dist.CLIENT)
public final class GuideClient {
    private static final GuideSnapshotCache values = new GuideSnapshotCache();
    private static long requested;
    public static void accept(CompoundTag data) {
        var map = new java.util.HashMap<String, String>();
        if (data != null) data.getAllKeys().forEach(key -> map.put(key, data.getString(key)));
        values.accept(map, System.nanoTime());
    }
    public static String text(String key, String fallback) {
        long now = System.nanoTime();
        boolean connected = Minecraft.getInstance().getConnection() != null;
        if (connected && now - requested > 5_000_000_000L) { requested = now; GuideNetwork.CHANNEL.sendToServer(new GuideNetwork.Request()); }
        return values.text(key, fallback, now, connected);
    }
    @SubscribeEvent public static void logout(ClientPlayerNetworkEvent.LoggingOut event) { values.clear(); requested = 0; }
    private GuideClient() {}
}
