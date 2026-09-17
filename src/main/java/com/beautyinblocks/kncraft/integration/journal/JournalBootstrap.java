package com.beautyinblocks.kncraft.integration.journal;

import com.beautyinblocks.kncraft.core.Compatibility;
import com.beautyinblocks.kncraft.core.ExpansionConfig;
import com.beautyinblocks.kncraft.core.ManagedFile;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

/** Runs before FTB's ServerStarted load; never writes team or player progress. */
public final class JournalBootstrap {
    public static String state = "not installed";
    private static final Pattern ID = Pattern.compile("\\b[\"]?id[\"]?\\s*:\\s*\"([0-9A-Fa-f]{16})\"");
    public static void install(Path config) {
        if (!Compatibility.present("ftbquests") || !ExpansionConfig.JOURNAL.get()) { state = "disabled or FTB Quests absent"; return; }
        Path folder = config.resolve("ftbquests/quests");
        Path ledger = config.resolve("kncraft-journal-manifest.json");
        try {
            var old = Files.exists(ledger) ? JsonParser.parseString(Files.readString(ledger)).getAsJsonObject() : new JsonObject();
            var next = old.deepCopy();
            JsonObject catalog;
            try (var stream = JournalBootstrap.class.getResourceAsStream("/journal/catalog.json")) {
                if (stream == null) throw new java.io.IOException("Missing journal catalog");
                catalog = JsonParser.parseString(new String(stream.readAllBytes(), StandardCharsets.UTF_8)).getAsJsonObject();
            }
            int installed = 0, preserved = 0;
            for (var entry : catalog.entrySet()) {
                if (!entry.getKey().matches("kncraft_[a-z0-9_]+\\.snbt")) throw new java.io.IOException("Invalid journal filename");
                boolean enabled = entry.getValue().getAsJsonArray().asList().stream().allMatch(mod -> Compatibility.present(mod.getAsString()));
                if (!enabled) continue;
                String name = entry.getKey();
                Path target = folder.resolve("chapters").resolve(name);
                byte[] bytes;
                try (var stream = JournalBootstrap.class.getResourceAsStream("/journal/" + name)) {
                    if (stream == null) throw new java.io.IOException("Missing " + name);
                    bytes = stream.readAllBytes();
                }
                Set<String> ids = ids(new String(bytes, StandardCharsets.UTF_8));
                boolean collision = false;
                if (Files.isDirectory(folder)) try (var files = Files.walk(folder)) {
                    for (Path other : files.filter(p -> p.toString().endsWith(".snbt") && !p.equals(target)).toList()) {
                        if (ids(Files.readString(other)).stream().anyMatch(ids::contains)) { collision = true; break; }
                    }
                }
                if (collision || !ManagedFile.install(target, bytes, old.has(name) ? old.get(name).getAsString() : "")) {
                    preserved++;
                    LogUtils.getLogger().warn("KNCraft journal preserved administrator file or conflicting IDs: {}", name);
                    continue;
                }
                next.addProperty(name, ManagedFile.hash(bytes)); installed++;
            }
            // A new journal needs its file-format marker. Never replace an existing root config.
            if (!Files.exists(folder.resolve("data.snbt"))) ManagedFile.install(folder.resolve("data.snbt"), "{version: 13}\n".getBytes(StandardCharsets.UTF_8), "");
            Files.createDirectories(config);
            Files.writeString(ledger, new GsonBuilder().setPrettyPrinting().create().toJson(next) + "\n");
            state = installed + " accomplishment chapters installed; " + preserved + " administrator/conflict files preserved";
            LogUtils.getLogger().info("KNCraft journal: {}", state);
        } catch (java.io.IOException | RuntimeException ex) {
            state = "installation failed: " + ex.getMessage();
            LogUtils.getLogger().error("Could not install KNCraft accomplishment journal", ex);
        }
    }
    private static Set<String> ids(String text) {
        Set<String> result = new HashSet<>();
        var matcher = ID.matcher(text);
        while (matcher.find()) result.add(matcher.group(1).toUpperCase(java.util.Locale.ROOT));
        return result;
    }
    private JournalBootstrap() {}
}
