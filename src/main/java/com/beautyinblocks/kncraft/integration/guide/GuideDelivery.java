package com.beautyinblocks.kncraft.integration.guide;

import com.beautyinblocks.kncraft.core.PolishConfig;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;

/** One automatic gift; players can explicitly replace a lost guide without resetting that receipt. */
public final class GuideDelivery {
    public enum Delivery { GIVEN, ALREADY_CARRIED, FULL, UNAVAILABLE }
    public static final String RECEIVED = "KNCraftGuideReceivedV1";
    public static final ResourceLocation BOOK = new ResourceLocation("patchouli:kncraft_guide");
    private static final ResourceLocation ITEM = new ResourceLocation("patchouli:guide_book");
    public static boolean isGuide(ItemStack stack) {
        return ITEM.equals(ForgeRegistries.ITEMS.getKey(stack.getItem())) && stack.hasTag()
            && BOOK.toString().equals(stack.getTag().getString("patchouli:book"));
    }
    public static boolean deliver(ServerPlayer player) {
        if (!PolishConfig.STARTER_GUIDE.get() || player.getPersistentData().getBoolean(RECEIVED)) return false;
        return summon(player) == Delivery.GIVEN;
    }
    public static Delivery summon(ServerPlayer player) {
        if (!ForgeRegistries.ITEMS.containsKey(ITEM)) return Delivery.UNAVAILABLE;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) if (isGuide(player.getInventory().getItem(i))) {
            player.getPersistentData().putBoolean(RECEIVED, true); return Delivery.ALREADY_CARRIED;
        }
        // Do not drop a second book on every retry, or replace anything in a full inventory.
        int slot = player.getInventory().getFreeSlot();
        if (slot < 0) return Delivery.FULL;
        var stack = new ItemStack(ForgeRegistries.ITEMS.getValue(ITEM));
        stack.getOrCreateTag().putString("patchouli:book", BOOK.toString());
        player.getInventory().setItem(slot, stack);
        player.getInventory().setChanged();
        player.getPersistentData().putBoolean(RECEIVED, true);
        player.inventoryMenu.broadcastChanges();
        if (player.containerMenu != player.inventoryMenu) player.containerMenu.broadcastChanges();
        return Delivery.GIVEN;
    }
    @SubscribeEvent public void commands(RegisterCommandsEvent event) {
        com.mojang.brigadier.Command<net.minecraft.commands.CommandSourceStack> command = context -> {
            var source = context.getSource();
            var result = summon(source.getPlayerOrException());
            switch (result) {
                case GIVEN -> source.sendSuccess(() -> Component.literal("KNCraft Guide Book added to your inventory."), false);
                case ALREADY_CARRIED -> source.sendSuccess(() -> Component.literal("You already carry the KNCraft Guide Book. Check your inventory or offhand."), false);
                case FULL -> source.sendFailure(Component.literal("Your inventory is full. Free an inventory slot, then use /guide again."));
                case UNAVAILABLE -> source.sendFailure(Component.literal("The KNCraft Guide Book is unavailable because Patchouli is not installed on this server."));
            }
            return result == Delivery.GIVEN ? 1 : 0;
        };
        // Default permission level is zero: no operator privileges or cheats required.
        event.getDispatcher().register(Commands.literal("guide").executes(command));
        event.getDispatcher().register(Commands.literal("kncraft").then(Commands.literal("guide").executes(command)));
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
