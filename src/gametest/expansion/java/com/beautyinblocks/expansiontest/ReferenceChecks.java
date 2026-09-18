package com.beautyinblocks.expansiontest;

import static com.beautyinblocks.expansiontest.ExpansionChecks.*;
import com.beautyinblocks.kncraft.core.PolishConfig;
import com.beautyinblocks.kncraft.integration.equipment.ReservedFood;
import com.beautyinblocks.kncraft.integration.guide.*;
import com.momosoftworks.coldsweat.api.insulation.Insulation;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.data.codec.configuration.FoodData;
import com.momosoftworks.coldsweat.data.codec.configuration.InsulatorData;
import com.mojang.authlib.GameProfile;
import java.util.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.*;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.registries.ForgeRegistries;

final class ReferenceChecks {
    static ServerPlayer player(MinecraftServer server) { return FakePlayerFactory.get(server.overworld(), new GameProfile(UUID.randomUUID(), "GuideCheck")); }
    static void run(MinecraftServer server) throws Exception {
        gift(server); guideCommand(server); feeding(server); values(server); links();
        System.out.println("REFERENCE: one-time guide delivery, full inventory retry, native feeding reserves, player opt-out persistence, authoritative overrides, packet data and 344 guide routes verified");
    }
    static void gift(MinecraftServer server) {
        var p = player(server);
        check(GuideDelivery.isGuide(vazkii.patchouli.api.PatchouliAPI.get().getBookStack(GuideDelivery.BOOK)), "Guide gift differs from native Patchouli book identity");
        check(GuideDelivery.deliver(p), "First guide was not delivered");
        check(p.getInventory().items.stream().filter(GuideDelivery::isGuide).count() == 1, "Wrong first gift");
        check(!GuideDelivery.deliver(p), "Reconnect duplicates guide");
        var clone = player(server); new GuideDelivery().clone(new PlayerEvent.Clone(clone, p, true));
        check(!GuideDelivery.deliver(clone) && clone.getInventory().isEmpty(), "Respawn duplicates guide");
        var stored = new CompoundTag(); p.saveWithoutId(stored); var restored = player(server); restored.load(stored);
        check(restored.getPersistentData().getBoolean(GuideDelivery.RECEIVED) && !GuideDelivery.deliver(restored), "Saved receipt was lost");
        var existing = player(server); var book = item("patchouli:guide_book"); book.getOrCreateTag().putString("patchouli:book", GuideDelivery.BOOK.toString());
        existing.getInventory().offhand.set(0, book);
        check(!GuideDelivery.deliver(existing) && existing.getPersistentData().getBoolean(GuideDelivery.RECEIVED), "Existing offhand guide not recognized");
        var full = player(server); full.getInventory().items.replaceAll(ignored -> new ItemStack(Items.STONE, 64));
        check(!GuideDelivery.deliver(full) && !full.getPersistentData().getBoolean(GuideDelivery.RECEIVED), "Full inventory lost or duplicated pending gift");
        full.getInventory().items.set(0, ItemStack.EMPTY);
        check(GuideDelivery.deliver(full) && GuideDelivery.isGuide(full.getInventory().getItem(0)), "Pending retry failed");
        boolean setting = PolishConfig.STARTER_GUIDE.get();
        try { PolishConfig.STARTER_GUIDE.set(false); check(!GuideDelivery.deliver(player(server)), "Guide opt-out ignored"); }
        finally { PolishConfig.STARTER_GUIDE.set(setting); }
    }
    static void feeding(MinecraftServer server) throws Exception {
        var p = player(server); p.getFoodData().setFoodLevel(2);
        var method = Class.forName("net.p3pp3rf1y.sophisticatedcore.upgrades.feeding.FeedingUpgradeWrapper").getDeclaredMethod("isEdible", ItemStack.class, net.minecraft.world.entity.LivingEntity.class);
        method.setAccessible(true);
        var stew = item("witherstormmod:golden_apple_stew"); var apple = new ItemStack(Items.ENCHANTED_GOLDEN_APPLE);
        check(stew.isEdible() && apple.isEdible(), "Reserved items lost manual edibility");
        check(!(boolean) method.invoke(null, stew, p) && !(boolean) method.invoke(null, apple, p), "Native feeding consumed reserves during emergency hunger");
        check((boolean) method.invoke(null, new ItemStack(Items.BREAD), p), "Reserve rule rejected ordinary food");
        ReservedFood.setProtection(p, false);
        check((boolean) method.invoke(null, stew, p) && (boolean) method.invoke(null, apple, p), "Player opt-out did not restore native feeding");
        var clone = player(server); new ReservedFood().clone(new PlayerEvent.Clone(clone, p, true));
        check(!ReservedFood.protects(clone, stew), "Feeding preference lost on respawn");
        var nbt = new CompoundTag(); p.saveWithoutId(nbt); var restored = player(server); restored.load(nbt);
        check(!ReservedFood.protects(restored, stew), "Feeding preference lost on save/load");
        ReservedFood.setProtection(p, true);
        boolean enabled = PolishConfig.RESERVED_FOOD.get(); var list = List.copyOf(PolishConfig.RESERVED_ITEMS.get());
        try {
            PolishConfig.RESERVED_FOOD.set(false); check((boolean) method.invoke(null, stew, p), "Server opt-out ignored");
            PolishConfig.RESERVED_FOOD.set(true); PolishConfig.RESERVED_ITEMS.set(List.of("minecraft:bread"));
            check(!(boolean) method.invoke(null, new ItemStack(Items.BREAD), p) && (boolean) method.invoke(null, stew, p), "Administrator reserve list ignored");
        } finally { PolishConfig.RESERVED_FOOD.set(enabled); PolishConfig.RESERVED_ITEMS.set(list); }
    }
    static void guideCommand(MinecraftServer server) throws Exception {
        var p = player(server);
        var source = p.createCommandSourceStack().withPermission(0).withSuppressedOutput();
        var dispatcher = server.getCommands().getDispatcher();
        // A previous first-join receipt must never prevent deliberate replacement.
        p.getPersistentData().putBoolean(GuideDelivery.RECEIVED, true);
        p.getInventory().setItem(0, new ItemStack(Items.DIAMOND, 7));
        check(dispatcher.execute("guide", source) == 1 && GuideDelivery.isGuide(p.getInventory().getItem(1)), "Non-op /guide did not replace a lost book in the first empty slot");
        check(p.getInventory().getItem(0).is(Items.DIAMOND) && p.getInventory().getItem(0).getCount() == 7, "/guide overwrote an occupied slot");
        check(dispatcher.execute("guide", source) == 0 && p.getInventory().items.stream().filter(GuideDelivery::isGuide).count() == 1, "Repeated /guide duplicated a carried copy");
        p.getInventory().offhand.set(0, p.getInventory().removeItemNoUpdate(1));
        check(dispatcher.execute("kncraft guide", source) == 0 && p.getInventory().getItem(1).isEmpty(), "Guide alias duplicated an offhand copy");
        p.getInventory().offhand.set(0, ItemStack.EMPTY);
        p.getInventory().items.replaceAll(ignored -> new ItemStack(Items.STONE, 64));
        check(dispatcher.execute("guide", source) == 0, "Full inventory accepted /guide");
        check(p.getInventory().items.stream().allMatch(s -> s.is(Items.STONE) && s.getCount() == 64), "Full inventory contents changed");
        p.getInventory().setItem(17, ItemStack.EMPTY);
        check(dispatcher.execute("kncraft guide", source) == 1 && GuideDelivery.isGuide(p.getInventory().getItem(17)), "Retry through alias failed to use the sole free slot");
        boolean setting = PolishConfig.STARTER_GUIDE.get();
        try {
            PolishConfig.STARTER_GUIDE.set(false);
            p.getInventory().setItem(17, ItemStack.EMPTY);
            check(dispatcher.execute("guide", source) == 1, "Disabling automatic gifts also disabled explicit recovery");
        } finally { PolishConfig.STARTER_GUIDE.set(setting); }
        var other = player(server);
        var otherBook = item("patchouli:guide_book"); otherBook.getOrCreateTag().putString("patchouli:book", "patchouli:another_book");
        other.getInventory().setItem(0, otherBook);
        check(dispatcher.execute("guide", other.createCommandSourceStack().withPermission(0).withSuppressedOutput()) == 1
            && other.getInventory().getItem(0) == otherBook && GuideDelivery.isGuide(other.getInventory().getItem(1)), "A different Patchouli book blocked recovery or was replaced");
        try {
            dispatcher.execute("guide", server.createCommandSourceStack().withSuppressedOutput());
            throw new IllegalStateException("Console /guide must require a player");
        } catch (com.mojang.brigadier.exceptions.CommandSyntaxException expected) { }
        System.out.println("GUIDE COMMAND PASSED: permission zero, received/lost guide, empty-slot placement, repeated and offhand copies, full inventory, retry, alias, disabled automatic gift, other book and console rejection");
    }
    static void values(MinecraftServer server) {
        var p = player(server); var tea = item("pamhc2crops:hotteaitem").getItem(); var cotton = item("pamhc2crops:cottonitem").getItem();
        var foods = ConfigSettings.FOOD_TEMPERATURES.get(); var insulation = ConfigSettings.INSULATION_ITEMS.get();
        var oldFood = List.copyOf(foods.get(tea)); var oldInsulation = List.copyOf(insulation.get(cotton));
        var oldRecipes = List.copyOf(server.getRecipeManager().getRecipes());
        try {
            foods.removeAll(tea); foods.put(tea, FoodData.fromToml(List.of("pamhc2crops:hotteaitem", .35, "{}", 200, 1)));
            insulation.removeAll(cotton); insulation.put(cotton, InsulatorData.fromToml(List.of("pamhc2crops:cottonitem", 3.0, 2.0, "static", "", true), Insulation.Slot.ITEM));
            var values = GuideValues.snapshot(p);
            check(values.getAllKeys().size() == GuideValues.KEYS.size(), "Incomplete guide snapshot");
            var selected = GuideValues.snapshot(p, List.of("food/pamhc2crops:hotteaitem", "food/invalid:unknown"));
            check(selected.getAllKeys().equals(java.util.Set.of("food/pamhc2crops:hotteaitem")), "Guide query leaked unrelated values or accepted an unknown key");
            check(values.getString("food/pamhc2crops:hotteaitem").contains("+0.350 base temperature; 10.0 seconds"), "Guide uses a stale food default");
            check(values.getString("item/pamhc2crops:cottonitem").contains("3.00 cold / 2.00 heat"), "Guide uses a stale material default");
            var bytes = new FriendlyByteBuf(io.netty.buffer.Unpooled.buffer());
            try { bytes.writeNbt(values); check(values.equals(bytes.readNbt()), "Guide packet data changed in transit"); }
            finally { bytes.release(); }
            foods.removeAll(tea); check(GuideValues.snapshot(p).getString("food/pamhc2crops:hotteaitem").contains("no matching effect"), "Disabled effect still advertised");
            foods.put(tea, FoodData.fromToml(List.of("pamhc2crops:hotteaitem", .35, "{}", 0, 1)));
            check(GuideValues.snapshot(p).getString("food/pamhc2crops:hotteaitem").contains("core temperature (instant)"), "Instant food mislabeled as base effect");
            var id = new ResourceLocation("kncraft:frontier_cap_altar_repair");
            var json = com.google.gson.JsonParser.parseString("{\"category\":\"enchantable_misc\",\"ingredient\":{\"item\":\"alexsmobs:frontier_cap\"},\"repairTime\":250}").getAsJsonObject();
            var nativeRecipe = ForgeRegistries.RECIPE_SERIALIZERS.getValue(new ResourceLocation("aether:repairing")).fromJson(id, json);
            var recipes = new ArrayList<net.minecraft.world.item.crafting.Recipe<?>>(oldRecipes); recipes.removeIf(r -> r.getId().equals(id)); recipes.add(nativeRecipe);
            server.getRecipeManager().replaceRecipes(recipes);
            check(GuideValues.snapshot(p).getString("recipe/" + id).contains("12.5 seconds"), "Guide ignored native recipe override");
            recipes.remove(nativeRecipe); server.getRecipeManager().replaceRecipes(recipes);
            check(GuideValues.snapshot(p).getString("recipe/" + id).contains("unavailable"), "Missing recipe still advertised");
        } finally { foods.removeAll(tea); foods.putAll(tea, oldFood); insulation.removeAll(cotton); insulation.putAll(cotton, oldInsulation); server.getRecipeManager().replaceRecipes(oldRecipes); }
    }
    static void links() throws Exception {
        var journal = Class.forName("dev.ftb.mods.ftbquests.quest.ServerQuestFile").getField("INSTANCE").get(null);
        var fileClass = Class.forName("dev.ftb.mods.ftbquests.quest.BaseQuestFile");
        var catalog = com.google.gson.JsonParser.parseReader(new java.io.InputStreamReader(GuideValues.class.getResourceAsStream("/journal/catalog.json"))).getAsJsonObject();
        int count = 0;
        for (String filename : catalog.keySet()) {
            var chapter = com.google.gson.JsonParser.parseReader(new java.io.InputStreamReader(GuideValues.class.getResourceAsStream("/journal/" + filename))).getAsJsonObject();
            for (var element : chapter.getAsJsonArray("quests")) {
                var q = element.getAsJsonObject(); var actual = fileClass.getMethod("get", long.class).invoke(journal, Long.parseUnsignedLong(q.get("id").getAsString(), 16));
                String route = (String) actual.getClass().getMethod("getGuidePage").invoke(actual);
                check(route.equals(q.get("guide_page").getAsString()), "Native FTB guide_page failed to load");
                check(JournalGuideRoute.entry(new ResourceLocation("guide", route)) != null, "Unroutable journal guide target"); count++;
            }
        }
        check(count == 344, "Missing guide routes");
        var opened = new java.util.concurrent.atomic.AtomicReference<ResourceLocation>();
        try (var listener = JournalGuideEvents.register(opened::set)) {
            var click = Class.forName("dev.ftb.mods.ftblibrary.ui.CustomClickEvent");
            var event = click.getField("EVENT").get(null);
            var invoker = Class.forName("dev.architectury.event.Event").getMethod("invoker").invoke(event);
            var result = Class.forName("dev.architectury.event.EventActor").getMethod("act", Object.class).invoke(invoker,
                click.getConstructor(ResourceLocation.class).newInstance(new ResourceLocation("guide:kncraft/chapters/food")));
            check((boolean) result.getClass().getMethod("isTrue").invoke(result) && new ResourceLocation("patchouli:chapters/food").equals(opened.get()), "Native FTB click event did not open the chapter route");
            opened.set(null);
            Class.forName("dev.architectury.event.EventActor").getMethod("act", Object.class).invoke(invoker,
                click.getConstructor(ResourceLocation.class).newInstance(new ResourceLocation("guide:another/book")));
            check(opened.get() == null, "Native event bridge swallowed another guide");
        }
        check(JournalGuideRoute.entry(new ResourceLocation("guide:other/book")) == null && JournalGuideRoute.entry(new ResourceLocation("http:kncraft/chapters/food")) == null
            && JournalGuideRoute.entry(new ResourceLocation("guide:kncraft/chapters/unknown")) == null, "Foreign guide links intercepted");
    }
}
