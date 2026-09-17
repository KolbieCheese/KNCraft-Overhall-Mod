package com.beautyinblocks.kncraft;

import com.beautyinblocks.kncraft.core.ManagedFile;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

class ManagedFileTest {
    @TempDir Path directory;
    @Test void updatesOwnedFilesButPreservesAdministratorChanges() throws Exception {
        var path = directory.resolve("quests/chapter.snbt");
        byte[] first = "first".getBytes(), second = "second".getBytes();
        assertTrue(ManagedFile.install(path, first, ""));
        assertTrue(ManagedFile.install(path, second, ManagedFile.hash(first)));
        Files.writeString(path, "administrator edits");
        assertFalse(ManagedFile.install(path, first, ManagedFile.hash(second)));
        assertEquals("administrator edits", Files.readString(path));
    }
    @Test void doesNotClaimAnUnrelatedExistingFile() throws Exception {
        var path = directory.resolve("existing.snbt");
        Files.writeString(path, "existing quests");
        assertFalse(ManagedFile.install(path, "new quests".getBytes(), ""));
        assertEquals("existing quests", Files.readString(path));
    }
}
