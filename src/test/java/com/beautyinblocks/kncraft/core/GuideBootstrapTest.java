package com.beautyinblocks.kncraft.core;

import com.google.gson.JsonParser;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

class GuideBootstrapTest {
    @TempDir Path directory;

    @Test void upgradesExistingBooksWithoutLosingSettingsAndIsIdempotent() throws Exception {
        Path file = directory.resolve("book.json");
        Files.writeString(file, """
            {"name":"KNCraft Field Guide","subtitle":"KNCraft | Field Guide","version":13,
             "use_resource_pack":true,"book_texture":"custom:cover","landing_text":"$(l:chapter)Read$(/l)",
             "custom":{"keep":true}}
            """);
        var expected = JsonParser.parseString(Files.readString(file)).getAsJsonObject();
        expected.addProperty("name", "KNCraft Guide Book");
        expected.addProperty("subtitle", "KNCraft Guide Book");
        expected.addProperty("version", 16);
        assertTrue(GuideBootstrap.renameLegacyDeclaration(file));
        assertEquals(expected, JsonParser.parseString(Files.readString(file)));
        byte[] renamed = Files.readAllBytes(file);
        assertFalse(GuideBootstrap.renameLegacyDeclaration(file));
        assertArrayEquals(renamed, Files.readAllBytes(file));
    }

    @Test void respectsCustomTitlesSubtitlesAndNewerVersions() throws Exception {
        Path file = directory.resolve("book.json");
        String custom = "{\"name\":\"Our Handbook\",\"subtitle\":\"Custom\",\"version\":20,\"use_resource_pack\":true}";
        Files.writeString(file, custom);
        assertFalse(GuideBootstrap.renameLegacyDeclaration(file));
        assertEquals(custom, Files.readString(file));
        Files.writeString(file, custom.replace("Our Handbook", "KNCraft Field Guide"));
        assertTrue(GuideBootstrap.renameLegacyDeclaration(file));
        var renamed = JsonParser.parseString(Files.readString(file)).getAsJsonObject();
        assertEquals("Custom", renamed.get("subtitle").getAsString());
        assertEquals(20, renamed.get("version").getAsInt());
    }

    @Test void migratedAlreadyRenamedBookAdvancesWithoutAnotherRename() throws Exception {
        Path file = directory.resolve("book.json");
        Files.writeString(file, "{\"name\":\"KNCraft Guide Book\",\"version\":10,\"use_resource_pack\":true}");
        assertTrue(GuideBootstrap.renameLegacyDeclaration(file));
        assertEquals(16, JsonParser.parseString(Files.readString(file)).getAsJsonObject().get("version").getAsInt());
        assertFalse(GuideBootstrap.renameLegacyDeclaration(file));
    }

    @Test void renamingLegacyBookDoesNotEnableBundledContentOrChangeItsVersion() throws Exception {
        Path file = directory.resolve("book.json");
        Files.writeString(file, """
            {"name":"KNCraft Field Guide","subtitle":"KNCraft | Draft 0.8",
             "version":8,"use_resource_pack":false}
            """);
        assertTrue(GuideBootstrap.renameLegacyDeclaration(file));
        var renamed = JsonParser.parseString(Files.readString(file)).getAsJsonObject();
        assertEquals("KNCraft Guide Book", renamed.get("name").getAsString());
        assertEquals("KNCraft Guide Book", renamed.get("subtitle").getAsString());
        assertEquals(8, renamed.get("version").getAsInt());
        assertFalse(renamed.get("use_resource_pack").getAsBoolean());
    }
}
