package com.beautyinblocks.kncraft.core;

import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
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
            LogUtils.getLogger().error("Could not prepare KNCraft Field Guide", ex);
        }
    }
    private GuideBootstrap() {}
}
