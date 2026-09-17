package com.beautyinblocks.kncraft.integration.guide;

import com.beautyinblocks.kncraft.core.PolishConfig;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;

/** A single guide gift, with a pending retry if an upgrading player's inventory is full. */
public final class GuideDelivery {
    public static final String RECEIVED = "KNCraftGuideReceivedV1";
    public static final ResourceLocation BOOK = new ResourceLocation("patchouli:kncraft_guide");
    private static final ResourceLocation ITEM = new ResourceLocation("patchouli:guide_book");
    public static boolean isGuide(ItemStack stack) {
        return ITEM.equals(ForgeRegistries.ITEMS.getKey(stack.getItem())) && stack.hasTag()
            && BOOK.toString().equals(stack.getTag().getString("patchouli:book"));
    }
    public static boolean deliver(ServerPlayer player) {
        if (!PolishConfig.STARTER_GUIDE.get() || player.getPersistentData().getBoolean(RECEIVED)
            || !ForgeRegistries.ITEMS.containsKey(ITEM)) return false;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) if (isGuide(player.getInventory().getItem(i))) {
            player.getPersistentData().putBoolean(RECEIVED, true); return false;
        }
        // Do not drop a second book on every retry, or replace anything in a full inventory.
        if (player.getInventory().getFreeSlot() < 0) return false;
        var stack = new ItemStack(ForgeRegistries.ITEMS.getValue(ITEM));
        stack.getOrCreateTag().putString("patchouli:book", BOOK.toString());
        if (!player.getInventory().add(stack)) return false;
        player.getPersistentData().putBoolean(RECEIVED, true);
        player.inventoryMenu.broadcastChanges();
        return true;
    }
    @SubscribeEvent public void login(PlayerEvent.PlayerLoggedInEvent event) { if (event.getEntity() instanceof ServerPlayer p) deliver(p); }
    @SubscribeEvent public void tick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && event.player instanceof ServerPlayer p && p.tickCount % 100 == 0
            && !p.getPersistentData().getBoolean(RECEIVED)) deliver(p);
    }
    @SubscribeEvent public void clone(PlayerEvent.Clone event) {
        if (event.getOriginal().getPersistentData().getBoolean(RECEIVED)) event.getEntity().getPersistentData().putBoolean(RECEIVED, true);
    }
}
