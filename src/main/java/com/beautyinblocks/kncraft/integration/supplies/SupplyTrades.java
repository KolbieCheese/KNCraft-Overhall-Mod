package com.beautyinblocks.kncraft.integration.supplies;

import com.beautyinblocks.kncraft.core.ExpansionConfig;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraftforge.event.village.VillagerTradesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;

/** Finite-stock sales only: no crop-to-emerald arbitrage or rare progression rewards. */
public final class SupplyTrades {
    @SubscribeEvent public void trades(VillagerTradesEvent event) {
        if (!ExpansionConfig.TRADES.get()) return;
        if (event.getType() == VillagerProfession.FARMER) {
            sale(event, 1, "pamhc2crops:cottonseeditem", 2, 2);
            sale(event, 2, "pamhc2crops:tealeafseeditem", 2, 3);
        } else if (event.getType() == VillagerProfession.BUTCHER) {
            sale(event, 2, "pamhc2foodcore:stewitem", 2, 3);
            sale(event, 3, "pamhc2foodcore:carrotsoupitem", 2, 3);
        }
    }
    private static void sale(VillagerTradesEvent event, int level, String item, int count, int emeralds) {
        var key = new ResourceLocation(item);
        if (!ForgeRegistries.ITEMS.containsKey(key)) return;
        event.getTrades().get(level).add((trader, random) -> new MerchantOffer(
            new ItemStack(Items.EMERALD, emeralds), new ItemStack(ForgeRegistries.ITEMS.getValue(key), count), 4, 5, .05f));
    }
}
