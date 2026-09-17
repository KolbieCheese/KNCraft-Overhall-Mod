package com.beautyinblocks.expansiontest;

import static com.beautyinblocks.expansiontest.ExpansionChecks.*;
import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.Minecart;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.registries.ForgeRegistries;

final class CampingChecks {
    static void run(ServerPlayer player) throws Exception {
        var permitted = Class.forName("tschipp.carryon.common.config.ListHandler").getMethod("isPermitted", Block.class);
        for (String id : List.of("cold_sweat:hearth_bottom", "cold_sweat:hearth_top", "nomadictents:tiny_yurt_door", "nomadictents:mega_shamiyana_door", "sophisticatedbackpacks:backpack"))
            check(!(boolean)permitted.invoke(null, ForgeRegistries.BLOCKS.getValue(new ResourceLocation(id))), "Unsafe Carry On pickup: " + id);
        check((boolean)permitted.invoke(null, Blocks.CHEST), "Ordinary storage lost Carry On support");
        var fuelClass = Class.forName("hu.xannosz.betterminecarts.utils.FuelHolder");
        var fuels = fuelClass.getMethod("getINSTANCE").invoke(null);
        var diesel = item("betterminecarts:bio_diesel_fuel");
        check(fuelClass.getMethod("getLeftover", net.minecraft.world.item.ItemStack.class).invoke(fuels, diesel) == Items.GLASS_BOTTLE, "Native fuel bottle return changed");
        check((int)fuelClass.getMethod("getFuelAmount", net.minecraft.world.item.ItemStack.class).invoke(fuels, diesel) == 200, "Native diesel amount changed");
        var passenger = new Minecart(player.level(), 0, 100, 0);
        passenger.setDisplayBlockState(ForgeRegistries.BLOCKS.getValue(new ResourceLocation("cold_sweat:minecart_insulation")).defaultBlockState());
        var parent = new Minecart(player.level(), 1, 100, 0);
        var link = Class.forName("hu.xannosz.betterminecarts.utils.Linkable");
        check(link.isInstance(passenger), "Insulated native cart is not linkable");
        link.getMethod("setLinkedParent", net.minecraft.world.entity.vehicle.AbstractMinecart.class).invoke(passenger, parent);
        check(link.getMethod("getLinkedParent").invoke(passenger) == parent, "Insulated cart link did not retain parent");
        var pos = new BlockPos(2120, 151, 2120);
        player.serverLevel().getChunk(pos);
        var bag = ForgeRegistries.BLOCKS.getValue(new ResourceLocation("comforts:sleeping_bag_white"));
        check(bag != Blocks.AIR, "Sleeping bag missing");
        player.serverLevel().setBlock(pos, bag.defaultBlockState(), 2);
        Temperature.set(player, Temperature.Trait.CORE, 100);
        var sleepType = Class.forName("com.illusivesoulworks.comforts.platform.ForgeSleepEvents");
        var sleep = sleepType.getConstructor().newInstance();
        var getSleep = sleepType.getMethod("getSleepResult", Player.class, BlockPos.class);
        var original = new ArrayList<>(ConfigSettings.SLEEP_CHECK_IGNORE_BLOCKS.get());
        try {
            ConfigSettings.SLEEP_CHECK_IGNORE_BLOCKS.get().remove(bag);
            check(getSleep.invoke(sleep, player, pos) == Player.BedSleepingProblem.OTHER_PROBLEM, "Comforts bypassed enabled Cold Sweat danger check");
            ConfigSettings.SLEEP_CHECK_IGNORE_BLOCKS.get().add(bag);
            check(getSleep.invoke(sleep, player, pos) == null, "Comforts sleeping-bag override was ignored");
        } finally {
            ConfigSettings.SLEEP_CHECK_IGNORE_BLOCKS.get().clear(); ConfigSettings.SLEEP_CHECK_IGNORE_BLOCKS.get().addAll(original);
            player.serverLevel().removeBlock(pos, false); Temperature.set(player, Temperature.Trait.CORE, 0);
        }
        // Inventory Totem reads the native inventory, not nested backpack storage.
        var death = Class.forName("com.natamus.inventorytotem_common_forge.events.TotemEvent").getMethod("allowPlayerDeath", net.minecraft.server.level.ServerLevel.class, ServerPlayer.class);
        player.getInventory().clearContent();
        check((boolean)death.invoke(null, player.serverLevel(), player), "Empty inventory unexpectedly revived");
        player.getInventory().setItem(9, new net.minecraft.world.item.ItemStack(Items.TOTEM_OF_UNDYING));
        check(!(boolean)death.invoke(null, player.serverLevel(), player), "Inventory totem failed");
        check(player.getInventory().getItem(9).isEmpty() && player.getHealth() == 1, "Totem not consumed exactly once");
        System.out.println("CAMPING: Carry On structure exclusions, native fuel containers, insulated cart links, both Comforts sleep policies and inventory totems passed");
    }
}
