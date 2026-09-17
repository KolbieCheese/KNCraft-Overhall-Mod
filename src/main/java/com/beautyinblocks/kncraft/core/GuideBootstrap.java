package com.beautyinblocks.kncraft.core;

import com.google.gson.JsonParser;
import com.google.gson.GsonBuilder;
import com.mojang.logging.LogUtils;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.IOException;

/** Patchouli 85 only scans a mod's own namespace. The tiny external declaration retains the old book ID. */
public final class GuideBootstrap {
    public static String state = "Patchouli absent";
    public static void install(Path gameDirectory) {
        if (!Compatibility.present("patchouli")) return;
        Path file = gameDirectory.resolve("patchouli_books/kncraft_guide/book.json");
        try {
            if (Files.exists(file)) {
                renameLegacyDeclaration(file);
                var book = JsonParser.parseString(Files.readString(file)).getAsJsonObject();
                boolean assets = book.has("use_resource_pack") && book.get("use_resource_pack").getAsBoolean();
                state = assets ? "resource-backed guide declaration present" : "legacy guide present: run tools/migrate_guide.py to enable bundled updates";
                if (!assets) LogUtils.getLogger().warn("KNCraft: {}. Existing book files preserved.", state);
                return;
            }
            Files.createDirectories(file.getParent());
            try (var stream = GuideBootstrap.class.getResourceAsStream("/data/patchouli/patchouli_books/kncraft_guide/book.json")) {
                if (stream == null) throw new IOException("Bundled guide declaration missing");
                Files.copy(stream, file);
            }
            state = "created resource-backed guide declaration; book identity preserved";
        } catch (IOException | RuntimeException ex) {
            state = "guide unavailable: " + ex.getMessage();
            LogUtils.getLogger().error("Could not prepare KNCraft Guide Book", ex);
        }
    }

    /** Change only the original title; retain operator settings and the external book identity. */
    static boolean renameLegacyDeclaration(Path file) throws IOException {
        byte[] original = Files.readAllBytes(file);
        var book = JsonParser.parseString(new String(original, StandardCharsets.UTF_8)).getAsJsonObject();
        if (!book.has("name") || !"KNCraft Field Guide".equals(book.get("name").getAsString())) return false;
        book.addProperty("name", "KNCraft Guide Book");
        if (book.has("subtitle")) {
            String subtitle = book.get("subtitle").getAsString();
            if (subtitle.equals("KNCraft | Field Guide") || subtitle.equals("KNCraft | Draft 0.8"))
                book.addProperty("subtitle", "KNCraft Guide Book");
        }
        if (book.has("use_resource_pack") && book.get("use_resource_pack").getAsBoolean())
            book.addProperty("version", Math.max(14, book.has("version") ? book.get("version").getAsInt() : 0));
        String updated = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create().toJson(book) + "\n";
        return ManagedFile.install(file, updated.getBytes(StandardCharsets.UTF_8), ManagedFile.hash(original));
    }
    private GuideBootstrap() {}
}
