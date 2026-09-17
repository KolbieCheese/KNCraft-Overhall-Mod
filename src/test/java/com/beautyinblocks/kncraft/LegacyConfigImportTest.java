package com.beautyinblocks.kncraft;

import com.beautyinblocks.kncraft.core.LegacyConfigImport;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

class LegacyConfigImportTest {
    @TempDir Path directory;
    @Test void importsFalseAndPreservesUserEditsAndOriginals() throws Exception {
        String original = "immersiveTentEntrances = false\n";
        Files.writeString(directory.resolve("kncraft-tent-portals.toml"), original);
        LegacyConfigImport.run(directory);
        Path target = directory.resolve("kncraft-common.toml");
        assertTrue(Files.readString(target).contains("false"));
        assertEquals(original, Files.readString(directory.resolve("kncraft-migration-v1/kncraft-tent-portals.toml")));
        Files.writeString(target, "# player customization\n");
        LegacyConfigImport.run(directory);
        assertEquals("# player customization\n", Files.readString(target));
        assertEquals(original, Files.readString(directory.resolve("kncraft-tent-portals.toml")));
    }
    @Test void noLegacyInputsDoNotInventAMigration() {
        LegacyConfigImport.run(directory);
        assertFalse(Files.exists(directory.resolve("kncraft-common.toml")));
    }
}
