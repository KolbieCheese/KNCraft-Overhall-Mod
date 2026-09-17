package com.beautyinblocks.kncraft.core;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.LinkedHashMap;
import java.util.Map;
import com.mojang.logging.LogUtils;

/** Only creates our new config. Existing settings and legacy files are never rewritten. */
public final class LegacyConfigImport {
    public static String state = "not checked";
    public static void run(Path directory) {
        Path target = directory.resolve("kncraft-common.toml");
        if (Files.exists(target)) { state = "existing unified config preserved"; return; }
        Map<String, Boolean> values = new LinkedHashMap<>();
        try {
            Path backup = directory.resolve("kncraft-migration-v1");
            for (String file : new String[]{"kncraft-tent-portals.toml", "kncraftperformance-common.toml"}) {
                Path source = directory.resolve(file);
                if (!Files.isRegularFile(source)) continue;
                Files.createDirectories(backup);
                if (!Files.exists(backup.resolve(file))) Files.copy(source, backup.resolve(file));
                try (var old = CommentedFileConfig.of(source)) {
                    old.load();
                    for (String key : new String[]{"immersiveTentEntrances", "portalSearch", "itemSelection", "tornadoQuery"}) {
                        Object value = old.get(key);
                        if (value instanceof Boolean flag) values.put(key, flag);
                        else if (value != null) LogUtils.getLogger().warn("Rejected legacy config value {} in {}: expected boolean", key, file);
                    }
                }
            }
            if (values.isEmpty()) { state = "no legacy settings to import; parity defaults"; return; }
            Path temporary = directory.resolve("kncraft-common.importing.toml");
            try (var output = CommentedFileConfig.of(temporary)) {
                values.forEach((key, value) -> output.set((key.equals("immersiveTentEntrances") ? "parity." : "performance.") + key, value));
                output.save();
            }
            Files.move(temporary, target, StandardCopyOption.ATOMIC_MOVE);
            Files.writeString(backup.resolve("schema-version.txt"), "1\n");
            state = "imported legacy settings once; originals and schema-v1 backups retained";
        } catch (IOException | RuntimeException ex) {
            throw new IllegalStateException("Cannot safely import legacy KNCraft settings. Restore/check config before starting.", ex);
        }
    }
    private LegacyConfigImport() {}
}
