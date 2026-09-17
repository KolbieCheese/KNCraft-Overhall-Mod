package com.beautyinblocks.expansiontest;

import com.beautyinblocks.kncraft.core.ExpansionConfig;
import com.beautyinblocks.kncraft.integration.equipment.FlightEquipment;
import com.momosoftworks.coldsweat.api.temperature.modifier.ArmorInsulationTempModifier;
import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.mojang.authlib.GameProfile;
import java.util.UUID;
import net.bettercombat.logic.TargetHelper;
import net.bettercombat.logic.WeaponRegistry;
import net.minecraft.commands.Commands;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import top.theillusivec4.curios.api.CuriosApi;

@Mod("kncraftexpansiontests")
public final class ExpansionChecks {
    public ExpansionChecks() {
        if (!Boolean.getBoolean("kncraft.isolatedTests") || !java.nio.file.Files.isRegularFile(net.minecraftforge.fml.loading.FMLPaths.GAMEDIR.get().resolve("KNCraft-ISOLATED-TEST-WORLD")))
            throw new IllegalStateException("Expansion tests require a marked isolated server");
        MinecraftForge.EVENT_BUS.register(this);
    }
    static void check(boolean success, String message) { if (!success) throw new IllegalStateException(message); }
    static ItemStack item(String id) {
        var key = new ResourceLocation(id);
        check(ForgeRegistries.ITEMS.containsKey(key), "Missing item " + id);
        return new ItemStack(ForgeRegistries.ITEMS.getValue(key));
    }
    static boolean tag(String item, String tag) { return item(item).is(TagKey.create(Registries.ITEM, new ResourceLocation(tag))); }
    @SubscribeEvent public void commands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("kncraftreferencetest").requires(s -> s.getEntity() == null && !s.getServer().usesAuthentication()).executes(ctx -> {
            try { ReferenceChecks.run(ctx.getSource().getServer()); ctx.getSource().sendSuccess(() -> Component.literal("REFERENCE CHECKS PASSED"), false); return 1; }
            catch (Throwable ex) { ex.printStackTrace(); ctx.getSource().sendFailure(Component.literal("REFERENCE CHECKS FAILED: " + ex)); return 0; }
        }));
        event.getDispatcher().register(Commands.literal("kncraftpolishtest").requires(s -> s.getEntity() == null && !s.getServer().usesAuthentication()).executes(ctx -> {
            try { PolishChecks.run(ctx.getSource().getServer()); ctx.getSource().sendSuccess(() -> Component.literal("POLISH CHECKS PASSED"), false); return 1; }
            catch (Throwable ex) { ex.printStackTrace(); ctx.getSource().sendFailure(Component.literal("POLISH CHECKS FAILED: " + ex)); return 0; }
        }));
        event.getDispatcher().register(Commands.literal("kncraftclimatepersist").requires(s -> s.getEntity() == null && !s.getServer().usesAuthentication()).executes(ctx -> {
            try { TentClimateChecks.persistence(ctx.getSource().getServer()); ctx.getSource().sendSuccess(() -> Component.literal("CLIMATE RESTART CHECKS PASSED"), false); return 1; }
            catch (Throwable ex) { ex.printStackTrace(); ctx.getSource().sendFailure(Component.literal("CLIMATE RESTART CHECKS FAILED: " + ex)); return 0; }
        }));
        event.getDispatcher().register(Commands.literal("kncraftexpansiontest").requires(s -> s.getEntity() == null && !s.getServer().usesAuthentication()).executes(ctx -> {
            try { run(ctx.getSource().getServer()); ctx.getSource().sendSuccess(() -> Component.literal("EXPANSION CHECKS PASSED"), false); return 1; }
            catch (Throwable ex) { ex.printStackTrace(); ctx.getSource().sendFailure(Component.literal("EXPANSION CHECKS FAILED: " + ex)); return 0; }
        }));
        event.getDispatcher().register(Commands.literal("kncraftclimatetest").requires(s -> s.getEntity() == null && !s.getServer().usesAuthentication()).executes(ctx -> {
            try { TentClimateChecks.run(ctx.getSource().getServer()); ctx.getSource().sendSuccess(() -> Component.literal("TENT CLIMATE CHECKS PASSED"), false); return 1; }
            catch (Throwable ex) { ex.printStackTrace(); ctx.getSource().sendFailure(Component.literal("TENT CLIMATE CHECKS FAILED: " + ex)); return 0; }
        }));
    }
    static void run(MinecraftServer server) throws Exception {
        SuppliesChecks.run(server);
        var level = server.overworld();
        check(tag("aether:blue_moa_egg", "forge:egg") && tag("alexsmobs:emu_egg", "forge:egg"), "Animal egg bridge missing");
        check(!tag("pamhc2trees:bananaitem", "forge:eggs"), "Fruit leaked into animal eggs");
        check(tag("aether:skyroot_milk_bucket", "forge:milk"), "Skyroot milk tag missing");
        check(tag("aether:blue_berry", "forge:berries"), "Aether berry tag missing");
        check(!tag("aether:enchanted_berry", "forge:berries"), "Enchanted berry became generic food");
        check(tag("alexsmobs:raw_catfish", "forge:rawfish") && tag("alexsmobs:moose_ribs", "forge:rawmeats"), "Meat tags missing");
        check(!tag("alexsmobs:moose_ribs", "forge:rawmeats/rawchicken"), "Moose became chicken");
        var player = FakePlayerFactory.get(level, new GameProfile(UUID.randomUUID(), "ExpansionCheck"));
        var grid = new TransientCraftingContainer(new CraftingMenu(1, player.getInventory()), 3, 3);
        grid.setItem(0, item("aether:skyroot_milk_bucket"));
        var milk = (CraftingRecipe) server.getRecipeManager().byKey(new ResourceLocation("kncraft:skyroot_fresh_milk")).orElseThrow();
        check(milk.matches(grid, level), "Skyroot milk craft failed");
        check(milk.assemble(grid, server.registryAccess()).getCount() == 8, "Wrong milk yield");
        check(milk.getRemainingItems(grid).get(0).is(item("aether:skyroot_bucket").getItem()), "Skyroot bucket was not returned");
        grid.clearContent(); grid.setItem(0, item("pamhc2foodcore:cookingoilitem")); grid.setItem(1, item("pamhc2foodcore:cookingoilitem")); grid.setItem(2, new ItemStack(Items.GLASS_BOTTLE));
        var diesel = (CraftingRecipe) server.getRecipeManager().byKey(new ResourceLocation("kncraft:cooking_oil_diesel")).orElseThrow();
        check(diesel.matches(grid, level) && diesel.assemble(grid, server.registryAccess()).is(item("betterminecarts:bio_diesel_fuel").getItem()), "Diesel craft failed");
        grid.setItem(1, ItemStack.EMPTY); check(!diesel.matches(grid, level), "Diesel needs two oil");
        for (String broken : java.util.List.of("caramelcupcakeitem_x4", "honeymuffinitem", "melonpieitem"))
            check(server.getRecipeManager().byKey(new ResourceLocation("pamhc2foodcore:" + broken)).isEmpty(), "Broken recipe active: " + broken);
        for (String fruit : java.util.List.of("apple", "melon", "sweetberry")) {
            var recipe = server.getRecipeManager().byKey(new ResourceLocation("kncraft:" + fruit + "_smoothie_freezing")).orElseThrow();
            check(recipe.getResultItem(server.registryAccess()).is(item("pamhc2foodcore:" + fruit + "smoothieitem").getItem()), "Freezer recipe result");
            check(recipe.getIngredients().get(0).test(item("pamhc2foodcore:" + fruit + "juiceitem")), "Freezer bypasses prepared juice");
        }
        var icestone = item("aether:icestone");
        check(ConfigSettings.ICEBOX_FUEL.get().get(icestone.getItem()).stream().anyMatch(data -> data.fuel(icestone) == 100), "Icestone cooling fuel missing");
        check(ConfigSettings.HEARTH_FUEL.get().get(icestone.getItem()).stream().anyMatch(data -> data.fuel(icestone) == -100), "Icestone hearth fuel sign");
        check(ConfigSettings.INSULATING_CURIOS.get().get(item("aether:ice_ring").getItem()).size() == 1, "Ice Ring insulation missing");
        var inventory = CuriosApi.getCuriosInventory(player).resolve().orElseThrow();
        System.out.println("EXPANSION Curios slots: " + inventory.getCurios().keySet());
        var back = inventory.getStacksHandler("back").orElseThrow().getStacks();
        var wings = new ItemStack(Items.ELYTRA); back.setStackInSlot(0, wings);
        var chest = new ItemStack(Items.DIAMOND_CHESTPLATE); player.setItemSlot(EquipmentSlot.CHEST, chest);
        check(FlightEquipment.equipped(player, EquipmentSlot.CHEST) == wings, "Curios wings not recognized");
        check(FlightEquipment.equipped(player, EquipmentSlot.LEGS) == player.getItemBySlot(EquipmentSlot.LEGS), "Non-flight armor changed");
        var chestWings = new ItemStack(Items.ELYTRA); player.setItemSlot(EquipmentSlot.CHEST, chestWings);
        check(FlightEquipment.equipped(player, EquipmentSlot.CHEST) == chestWings, "Chest wings lost precedence");
        ExpansionConfig.FLIGHT.set(false);
        try { player.setItemSlot(EquipmentSlot.CHEST, chest); check(FlightEquipment.equipped(player, EquipmentSlot.CHEST) == chest, "Flight switch ignored"); }
        finally { ExpansionConfig.FLIGHT.set(true); }
        player.setItemSlot(EquipmentSlot.CHEST, ItemStack.EMPTY);
        var rings = inventory.getStacksHandler("aether_ring").orElseThrow().getStacks();
        var pendant = inventory.getStacksHandler("aether_pendant").orElseThrow().getStacks();
        check(rings.getSlots() >= 2, "Aether ring slots missing");
        rings.setStackInSlot(0, item("aether:ice_ring")); rings.setStackInSlot(1, item("aether:ice_ring")); pendant.setStackInSlot(0, item("aether:ice_pendant"));
        player.tickCount = 20;
        com.momosoftworks.coldsweat.common.event.ProcessEquipmentInsulation.applyArmorInsulation(new net.minecraftforge.event.TickEvent.PlayerTickEvent(net.minecraftforge.event.TickEvent.Phase.END, player));
        var insulation = Temperature.getModifier(player, Temperature.Trait.RATE, ArmorInsulationTempModifier.class).orElseThrow();
        check(insulation.getNBT().getDouble("hot") == 4, "Three cooling accessories bypassed combined cap: " + insulation.getNBT());
        var aggregate = new java.util.HashMap<String, Double>(); aggregate.put("heat_curios", 9.0); aggregate.put("heat_armor", 8.0);
        var capEvent = new com.momosoftworks.coldsweat.api.event.common.insulation.InsulationTickEvent(player, aggregate);
        MinecraftForge.EVENT_BUS.post(capEvent);
        check(capEvent.getProperty("heat_curios") == 7 && capEvent.getProperty("heat") == 15, "Cap changed unrelated accessory/armor insulation");
        var accessoryConfig = com.momosoftworks.coldsweat.config.spec.ItemSettingsConfig.INSULATING_CURIOS;
        var savedAccessories = accessoryConfig.get();
        var customAccessories = new java.util.ArrayList<java.util.List<?>>(); customAccessories.addAll(savedAccessories);
        customAccessories.add(java.util.List.of("aether:ice_ring", 0.0, 7.0, "static", "", true));
        try {
            accessoryConfig.set(customAccessories);
            com.momosoftworks.coldsweat.config.ConfigLoadingHandler.loadConfigs(server.registryAccess());
            var overridden = ConfigSettings.INSULATING_CURIOS.get().get(item("aether:ice_ring").getItem());
            check(overridden.size() == 1 && overridden.iterator().next().getHeat() == 7, "Administrator accessory override was replaced or duplicated");
            com.momosoftworks.coldsweat.common.event.ProcessEquipmentInsulation.applyArmorInsulation(new net.minecraftforge.event.TickEvent.PlayerTickEvent(net.minecraftforge.event.TickEvent.Phase.END, player));
            check(Temperature.getModifier(player, Temperature.Trait.RATE, ArmorInsulationTempModifier.class).orElseThrow().getNBT().getDouble("hot") == 16, "KNCraft cap altered administrator-owned insulation");
        } finally {
            accessoryConfig.set(savedAccessories);
            com.momosoftworks.coldsweat.config.ConfigLoadingHandler.loadConfigs(server.registryAccess());
        }
        for (String procedure : java.util.List.of("ActionKeyPressed", "AscendingProc", "BeatOfWingsProc", "GustOfWindProc", "GlidingProc", "DescendingProc", "WingingProc", "ArmoringProc"))
            Class.forName("net.mcreator.moreenchantments.procedures." + procedure + "Procedure");
        var feeding = Class.forName("net.p3pp3rf1y.sophisticatedcore.upgrades.feeding.FeedingUpgradeWrapper").getDeclaredMethod("isEdible", ItemStack.class, net.minecraft.world.entity.LivingEntity.class);
        feeding.setAccessible(true);
        player.getFoodData().setFoodLevel(10); Temperature.set(player, Temperature.Trait.CORE, 50);
        check(!(boolean)feeding.invoke(null, item("pamhc2foodcore:stewitem"), player), "Feeding chose warming meal while hot");
        check((boolean)feeding.invoke(null, item("pamhc2foodcore:melonsmoothieitem"), player), "Feeding rejected cooling meal while hot");
        player.getFoodData().setFoodLevel(6);
        check((boolean)feeding.invoke(null, item("pamhc2foodcore:stewitem"), player), "Feeding emergency exception failed");
        var wolf = EntityType.WOLF.create(level); wolf.setTame(true); wolf.setOwnerUUID(UUID.randomUUID());
        check(TargetHelper.getRelation(player, wolf) == TargetHelper.Relation.NEUTRAL, "Offline-owned wolf eligible for cleave");
        wolf.setOwnerUUID(player.getUUID()); check(TargetHelper.getRelation(player, wolf) == TargetHelper.Relation.FRIENDLY, "Own companion relation");
        check(WeaponRegistry.getAttributes(item("aether:valkyrie_lance")).attackRange() == 6.5, "Valkyrie Lance range not preserved");
        var savanna = level.registryAccess().registryOrThrow(Registries.BIOME).getTag(TagKey.create(Registries.BIOME, new ResourceLocation("forge:is_savanna"))).orElseThrow();
        check(savanna.size() > 3, "Savanna garden coverage missing modded biomes");
        var journal = Class.forName("dev.ftb.mods.ftbquests.quest.ServerQuestFile").getField("INSTANCE").get(null);
        var fileClass = Class.forName("dev.ftb.mods.ftbquests.quest.BaseQuestFile");
        var index = com.google.gson.JsonParser.parseReader(new java.io.InputStreamReader(com.beautyinblocks.kncraft.integration.journal.JournalBootstrap.class.getResourceAsStream("/journal/catalog.json")));
        check(index.getAsJsonObject().size() == 17, "Journal catalog missing");
        int records = 0;
        Object bossTask = null;
        for (String filename : index.getAsJsonObject().keySet()) {
            var data = com.google.gson.JsonParser.parseReader(new java.io.InputStreamReader(com.beautyinblocks.kncraft.integration.journal.JournalBootstrap.class.getResourceAsStream("/journal/" + filename))).getAsJsonObject();
            for (var node : data.getAsJsonArray("quests")) {
                var quest = node.getAsJsonObject();
                check(ForgeRegistries.ITEMS.containsKey(new ResourceLocation(quest.get("icon").getAsString())), "Journal icon is absent: " + quest.get("icon"));
                var actualQuest = fileClass.getMethod("get", long.class).invoke(journal, Long.parseUnsignedLong(quest.get("id").getAsString(), 16));
                check(actualQuest != null, "FTB did not load a stable quest ID");
                var task = quest.getAsJsonArray("tasks").get(0).getAsJsonObject();
                String advancement = task.get("advancement").getAsString();
                check(server.getAdvancements().getAdvancement(new ResourceLocation(advancement)) != null, "Journal points to absent advancement: " + advancement);
                if (advancement.equals("kncraft:journal/minecraft/bosses/wither")) bossTask = fileClass.getMethod("get", long.class).invoke(journal, Long.parseUnsignedLong(task.get("id").getAsString(), 16));
                records++;
            }
        }
        check(records == 344 && bossTask != null, "Journal record count");
        var canSubmit = bossTask.getClass().getMethod("canSubmit", Class.forName("dev.ftb.mods.ftbquests.quest.TeamData"), net.minecraft.server.level.ServerPlayer.class);
        // Forge deliberately refuses advancement awards for FakePlayer. Use a real
        // ServerPlayer with the fixture's no-op network connection for this check.
        var explorer = new net.minecraft.server.level.ServerPlayer(server, level, new GameProfile(UUID.randomUUID(), "JournalCheck"));
        explorer.connection = player.connection;
        check(!(boolean)canSubmit.invoke(bossTask, null, explorer), "Boss record completed before accomplishment");
        var accomplishment = server.getAdvancements().getAdvancement(new ResourceLocation("kncraft:journal/minecraft/bosses/wither"));
        net.minecraft.advancements.CriteriaTriggers.PLAYER_KILLED_ENTITY.trigger(explorer, EntityType.WITHER.create(level), level.damageSources().playerAttack(explorer));
        check(explorer.getAdvancements().getOrStartProgress(accomplishment).isDone(), "Native boss criterion failed");
        check((boolean)canSubmit.invoke(bossTask, null, explorer), "FTB did not recognize completed boss advancement");
        CampingChecks.run(explorer);
        JournalMigrationChecks.run();
        var guide = server.getResourceManager().getResource(new ResourceLocation("kncraft:advancements/journal/minecraft/bosses/wither.json"));
        check(guide.isPresent(), "Boss detection absent");
        System.out.println("EXPANSION: ingredients, crafting remainders, fuel, freezing, equipment, feeding, companions, ecology and journal resources verified");
    }
}
