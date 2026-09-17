package com.beautyinblocks.expansiontest;

import com.beautyinblocks.kncraft.core.ExpansionConfig;
import com.beautyinblocks.kncraft.integration.supplies.KnownTags;
import java.util.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tags.*;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.*;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.loot.*;
import net.minecraft.world.level.storage.loot.parameters.*;
import net.minecraft.world.phys.Vec3;
import static com.beautyinblocks.expansiontest.ExpansionChecks.*;

final class SuppliesChecks {
    static void run(MinecraftServer server) {
        check(Blocks.RAIL.defaultBlockState().is(BlockTags.RAILS), "Broken upstream entry destroyed vanilla rails tag");
        String source = net.minecraftforge.fml.ModList.get().getModFileById("betterminecarts").getFile().getFileName();
        var missing = new ResourceLocation("betterminecarts:glowing_rail");
        var upstream = new TagLoader.EntryWithSource(TagEntry.element(missing), source);
        var administrator = new TagLoader.EntryWithSource(TagEntry.element(missing), "custom administrator pack");
        var entries = new ArrayList<>(List.of(upstream, administrator));
        var fixture = new HashMap<ResourceLocation, List<TagLoader.EntryWithSource>>();
        fixture.put(new ResourceLocation("minecraft:rails"), entries);
        KnownTags.repair(fixture);
        check(!entries.get(0).entry().isRequired() && entries.get(1) == administrator, "Tag repair changed an administrator contributor");
        var params = new LootParams.Builder(server.overworld()).withParameter(LootContextParams.ORIGIN, Vec3.ZERO).create(LootContextParamSets.CHEST);
        int additions = 0;
        for (String tableId : List.of("dungeons_enhanced:chests/black_citadel/kitchen", "structory:outcast/farm_ruin", "structory:mood/taiga", "abridged:chests/badlands_mining")) {
            var table = server.getLootData().getLootTable(new ResourceLocation(tableId));
            check(table != LootTable.EMPTY, "Missing curated chest " + tableId);
            for (long seed = 1; seed <= 32; seed++) {
                ExpansionConfig.THEMED_LOOT.set(false);
                var original = table.getRandomItems(params, seed);
                ExpansionConfig.THEMED_LOOT.set(true);
                var extended = table.getRandomItems(params, seed);
                check(extended.size() >= original.size() && extended.size() <= original.size() + 1, "Loot replaced or inflated native pools");
                for (int i = 0; i < original.size(); i++)
                    check(net.minecraft.world.item.ItemStack.matches(original.get(i), extended.get(i)), "Native treasure changed");
                additions += extended.size() - original.size();
            }
        }
        check(additions > 10 && additions < 90, "Themed supplies not appearing at expected modest frequency: " + additions);
        var villager = EntityType.VILLAGER.create(server.overworld());
        for (var row : List.of(new Object[]{VillagerProfession.FARMER, 1, "pamhc2crops:cottonseeditem"},
                new Object[]{VillagerProfession.FARMER, 2, "pamhc2crops:tealeafseeditem"},
                new Object[]{VillagerProfession.BUTCHER, 2, "pamhc2foodcore:stewitem"},
                new Object[]{VillagerProfession.BUTCHER, 3, "pamhc2foodcore:carrotsoupitem"})) {
            boolean found = false;
            for (var listing : VillagerTrades.TRADES.get(row[0]).get((int)row[1])) {
                var offer = listing.getOffer(villager, villager.getRandom());
                if (offer != null && offer.getResult().is(item((String)row[2]).getItem())) {
                    check(offer.getBaseCostA().is(Items.EMERALD) && offer.getMaxUses() == 4, "Supply trade balance changed"); found = true;
                }
            }
            check(found, "Supply trade not registered: " + row[2]);
        }
        System.out.println("SUPPLIES: merged rail tags, administrator contributor preservation, 128 real loot rolls and four native trades passed");
    }
}
