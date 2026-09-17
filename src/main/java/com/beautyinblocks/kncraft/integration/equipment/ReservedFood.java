package com.beautyinblocks.kncraft.integration.equipment;

import com.beautyinblocks.kncraft.core.PolishConfig;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;

public final class ReservedFood {
    public static final String OPT_OUT = "KNCraftAllowReservedAutoFeed";
    public static boolean protects(Player player, ItemStack stack) {
        return PolishConfig.RESERVED_FOOD.get() && !player.getPersistentData().getBoolean(OPT_OUT)
            && PolishConfig.RESERVED_ITEMS.get().contains(ForgeRegistries.ITEMS.getKey(stack.getItem()).toString());
    }
    public static void setProtection(Player player, boolean protect) { player.getPersistentData().putBoolean(OPT_OUT, !protect); }
    @SubscribeEvent public void clone(PlayerEvent.Clone event) {
        if (event.getOriginal().getPersistentData().contains(OPT_OUT))
            event.getEntity().getPersistentData().putBoolean(OPT_OUT, event.getOriginal().getPersistentData().getBoolean(OPT_OUT));
    }
}
