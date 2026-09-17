package com.beautyinblocks.kncraft.test;

import com.beautyinblocks.kncraft.core.ArchitectureConfig;
import com.beautyinblocks.kncraft.integration.encounters.Encounters;
import com.beautyinblocks.kncraft.integration.encounters.EncounterRules;
import com.momosoftworks.coldsweat.api.temperature.modifier.FoodTempModifier;
import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.config.ConfigLoadingHandler;
import com.mojang.authlib.GameProfile;
import java.util.UUID;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

@Mod("kncraftcohesiontests")
public final class CohesionChecks {
    public CohesionChecks() { if (!Boolean.getBoolean("kncraft.isolatedTests") || !java.nio.file.Files.isRegularFile(net.minecraftforge.fml.loading.FMLPaths.GAMEDIR.get().resolve("KNCraft-ISOLATED-TEST-WORLD"))) throw new IllegalStateException("Test harness requires an explicitly marked isolated server and -Dkncraft.isolatedTests=true"); MinecraftForge.EVENT_BUS.register(this); }
    static void check(boolean test, String message) { if (!test) throw new IllegalStateException(message); }
    static ItemStack stack(String id) { return new ItemStack(ForgeRegistries.ITEMS.getValue(new ResourceLocation(id))); }
    @SubscribeEvent public void commands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("kncraftcohesiontest").requires(s -> s.getEntity() == null && !s.getServer().usesAuthentication()).executes(ctx -> {
            try { run(ctx.getSource().getServer()); ctx.getSource().sendSuccess(() -> Component.literal("COHESION CHECKS PASSED"), false); return 1; }
            catch (Throwable ex) { ex.printStackTrace(); ctx.getSource().sendFailure(Component.literal("COHESION CHECKS FAILED: " + ex)); return 0; }
        }));
    }
    static void run(MinecraftServer server) throws Exception {
        var level = server.overworld();
        var cotton = stack("pamhc2crops:cottonitem");
        var before = ConfigSettings.INSULATION_ITEMS.get().get(cotton.getItem()).size();
        check(before == (ArchitectureConfig.COTTON.get() ? 1 : 0), "cotton entry count " + before);
        ConfigLoadingHandler.loadConfigs(server.registryAccess());
        check(ConfigSettings.INSULATION_ITEMS.get().get(cotton.getItem()).size() == before, "Cold Sweat reload duplicated insulation");
        if (before > 0) {
            var insulation = ConfigSettings.INSULATION_ITEMS.get().get(cotton.getItem()).iterator().next();
            check(insulation.getCold() == 1 && insulation.getHeat() == .5, "Cotton insulation values changed");
        }
        var player = FakePlayerFactory.get(level, new GameProfile(UUID.randomUUID(), "CohesionCheck"));
        if (ArchitectureConfig.MEALS.get()) {
            for (String id : new String[]{"pamhc2crops:hotteaitem", "pamhc2foodcore:carrotsoupitem", "pamhc2foodcore:icecreamitem", "pamhc2foodcore:applejuiceitem", "pamhc2foodcore:applejuiceitem"}) {
                var food = stack(id);
                check(ConfigSettings.FOOD_TEMPERATURES.get().get(food.getItem()).size() == 1, "Missing/duplicate food " + id);
                MinecraftForge.EVENT_BUS.post(new LivingEntityUseItemEvent.Finish(player, food, 0, ItemStack.EMPTY));
                var modifiers = Temperature.getModifiers(player, Temperature.Trait.BASE, m -> m instanceof FoodTempModifier && m.getNBT().getBoolean("kncraftMeal"));
                check(modifiers.size() == 1, "Meal stacking count " + modifiers.size() + " after " + id);
                check(modifiers.get(0).getNBT().getString("item").equals(id), "Previous meal was not replaced");
            }
            player.getFoodData().setFoodLevel(20);
            player.setItemInHand(InteractionHand.MAIN_HAND, stack("pamhc2crops:hotteaitem"));
            var full = player.getMainHandItem().use(level, player, InteractionHand.MAIN_HAND);
            System.out.println("COHESION OBSERVATION: hot tea at full hunger consumesAction=" + full.getResult().consumesAction());
            player.stopUsingItem();
        }
        var recipe = server.getRecipeManager().byKey(new ResourceLocation("kncraft:cotton_canvas"));
        check(recipe.isPresent() == ArchitectureConfig.FIBERS.get(), "Fiber recipe switch ignored");
        if (recipe.isPresent()) {
            var grid = new TransientCraftingContainer(new CraftingMenu(1, player.getInventory()), 3, 3);
            for (int i = 0; i < 9; i++) grid.setItem(i, i == 4 ? new ItemStack(Items.WHITE_WOOL) : cotton.copy());
            var canvas = (CraftingRecipe) recipe.get();
            check(canvas.matches(grid, level), "Canvas ingredient pattern mismatch");
            var result = canvas.assemble(grid, server.registryAccess());
            check(result.is(stack("nomadictents:tent_canvas").getItem()) && result.getCount() == 1, "Canvas yield changed");
            int[] vines = {2,3,8};
            for (int i = 0; i < 9; i++) grid.setItem(i, cotton.copy());
            for (int i : vines) grid.setItem(i, new ItemStack(Items.VINE));
            var rope = (CraftingRecipe) server.getRecipeManager().byKey(new ResourceLocation("kncraft:cotton_rope")).orElseThrow();
            check(rope.matches(grid, level), "Rope ingredients mismatch");
            check(rope.assemble(grid, server.registryAccess()).getCount() == 8, "Rope yield changed");
        }
        var boss = (LivingEntity) ForgeRegistries.ENTITY_TYPES.getValue(new ResourceLocation("witherstormmod:wither_storm")).create(level);
        Encounters.scale(boss);
        var health = boss.getAttribute(Attributes.MAX_HEALTH);
        check(health.getModifier(EncounterRules.STORM_HEALTH) != null, "Encounter modifier not installed");
        boss.setHealth(1);
        Encounters.scale(boss);
        check(boss.getHealth() == 1, "Already-tagged encounter was healed again");
        boss.removeTag(EncounterRules.STORM_TAG); Encounters.scale(boss);
        check(boss.getHealth() == 1, "Existing UUID without marker caused healing");
        var registry = Class.forName("vazkii.patchouli.common.book.BookRegistry");
        var books = (java.util.Map<?, ?>) registry.getField("books").get(registry.getField("INSTANCE").get(null));
        check(books.containsKey(new ResourceLocation("patchouli:kncraft_guide")), "Guide ID not registered");
        var guideItem = server.getRecipeManager().byKey(new ResourceLocation("kncraft:field_guide")).orElseThrow().getResultItem(server.registryAccess());
        check(guideItem.hasTag() && guideItem.getTag().getString("patchouli:book").equals("patchouli:kncraft_guide"), "Guide crafting result lost its book identity");
        System.out.println("COHESION: registry reload, recipes, meal cap, encounter idempotence and guide identity verified");
    }
}
