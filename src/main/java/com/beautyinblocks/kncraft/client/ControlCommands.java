package com.beautyinblocks.kncraft.client;

import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.mojang.blaze3d.platform.InputConstants;
import java.nio.file.Files;
import java.util.*;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLPaths;

/** Nothing changes until the player explicitly invokes the preset command. */
@Mod.EventBusSubscriber(modid = "kncraft", value = Dist.CLIENT)
public final class ControlCommands {
    public record Binding(String key, String modifier) {
        static Binding of(KeyMapping mapping) { return new Binding(mapping.getKey().getName(), mapping.getKeyModifier().name()); }
        void apply(KeyMapping mapping) { mapping.setKeyModifierAndCode(KeyModifier.valueOf(modifier), InputConstants.getKey(key)); }
    }
    public record Saved(Binding original, Binding applied) {}
    private static final Map<String, Binding> PRESET = new LinkedHashMap<>();
    static {
        set("key.curios.open.desc", "n", false); set("key.sophisticatedbackpacks.open_backpack", "b", false);
        set("key.voice_chat", "v", false); set("key.ftbquests.quests", "j", false);
        set("key.aether.invisibility_toggle.desc", "i", true); set("key.voice_chat_group", "g", true);
        set("key.disable_voice_chat", "v", true); set("key.hide_icons", "h", true);
        set("key.betterminecarts.increase", "up", true); set("key.betterminecarts.decrease", "down", true);
        set("key.betterminecarts.lamp", "l", true); set("key.betterminecarts.whistle", "w", true);
        set("key.betterminecarts.redstone", "r", true); set("key.betterminecarts.data", "d", true);
    }
    private static void set(String name, String key, boolean alt) { PRESET.put(name, new Binding("key.keyboard." + key, alt ? "ALT" : "NONE")); }
    private static List<KeyMapping> keys() { return Arrays.asList(Minecraft.getInstance().options.keyMappings); }
    private static java.nio.file.Path backup() { return FMLPaths.CONFIGDIR.get().resolve("kncraft-controls-backup.json"); }
    private static void tell(String text) { if (Minecraft.getInstance().player != null) Minecraft.getInstance().player.sendSystemMessage(Component.literal(text)); }
    public static int report() {
        int count = 0;
        var keys = keys();
        for (int i = 0; i < keys.size(); i++) for (int j = i + 1; j < keys.size(); j++) {
            var a = keys.get(i); var b = keys.get(j);
            if (a.isUnbound() || b.isUnbound() || !(PRESET.containsKey(a.getName()) || PRESET.containsKey(b.getName()))) continue;
            if (a.same(b)) {
                tell(a.getTranslatedKeyMessage().getString() + ": " + Component.translatable(a.getName()).getString() + " / " + Component.translatable(b.getName()).getString()); count++;
            }
        }
        tell(count + " possible overlaps involving pack controls. Context-specific bindings can be intentional. /kncraft controls preset applies the optional preset; restore undoes its unchanged bindings.");
        return 1;
    }
    public static int preset() {
        try {
            if (Files.exists(backup())) { tell("A preset backup already exists. Use /kncraft controls restore before applying another preset."); return 0; }
            var changes = new LinkedHashMap<String, Saved>();
            for (var key : keys()) if (PRESET.containsKey(key.getName())) changes.put(key.getName(), new Saved(Binding.of(key), PRESET.get(key.getName())));
            Files.writeString(backup(), new GsonBuilder().setPrettyPrinting().create().toJson(changes));
            for (var key : keys()) if (changes.containsKey(key.getName())) changes.get(key.getName()).applied.apply(key);
            KeyMapping.resetMapping(); Minecraft.getInstance().options.save();
            tell("Applied KNCraft controls; previous bindings saved. B backpack, N Curios, V voice, J journal; Alt with arrow keys or letters for train/secondary controls. Check the guide and Controls menu.");
            return report();
        } catch (Exception ex) { tell("Controls were not fully applied: " + ex.getMessage() + ". Keep the backup for restoration."); return 0; }
    }
    public static int restore() {
        try {
            if (!Files.exists(backup())) { tell("No KNCraft control backup exists."); return 0; }
            Map<String, Saved> saved = new GsonBuilder().create().fromJson(Files.readString(backup()), new TypeToken<Map<String, Saved>>(){}.getType());
            int restored = 0;
            for (var key : keys()) {
                var entry = saved.get(key.getName());
                if (entry != null && Binding.of(key).equals(entry.applied)) { entry.original.apply(key); restored++; }
            }
            KeyMapping.resetMapping(); Minecraft.getInstance().options.save();
            Files.delete(backup());
            tell("Restored " + restored + " bindings. Controls customized after applying the preset were preserved."); return 1;
        } catch (Exception ex) { tell("Could not restore control backup: " + ex.getMessage()); return 0; }
    }
    @SubscribeEvent public static void register(RegisterClientCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("kncraft").then(Commands.literal("controls").executes(ctx -> report())
            .then(Commands.literal("preset").executes(ctx -> preset())).then(Commands.literal("restore").executes(ctx -> restore()))));
    }
}
