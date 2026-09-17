package com.beautyinblocks.expansiontest;

import static com.beautyinblocks.expansiontest.ExpansionChecks.*;
import com.beautyinblocks.kncraft.integration.journal.JournalBootstrap;
import java.nio.file.Files;
import net.minecraftforge.fml.loading.FMLPaths;

final class JournalMigrationChecks {
    static void run() throws Exception {
        var fixtures = FMLPaths.GAMEDIR.get().resolve("journal-migration-tests"); Files.createDirectories(fixtures);
        var folder = Files.createTempDirectory(fixtures, "preservation-");
        JournalBootstrap.install(folder);
        var chapter = folder.resolve("ftbquests/quests/chapters/kncraft_bosses.snbt");
        String initial = Files.readString(chapter);
        JournalBootstrap.install(folder);
        check(Files.readString(chapter).equals(initial), "Repeat journal install changed stable IDs");
        String edited = initial.replace("Vanilla Bosses", "Administrator Boss Journal");
        Files.writeString(chapter, edited);
        var root = folder.resolve("ftbquests/quests/data.snbt");
        Files.writeString(root, "{version: 13, title: \"Administrator quests\"}\n");
        JournalBootstrap.install(folder);
        check(Files.readString(chapter).equals(edited), "Journal overwrote administrator chapter");
        check(Files.readString(root).contains("Administrator quests"), "Journal overwrote existing root settings");
        var conflict = Files.createTempDirectory(fixtures, "collision-");
        var other = conflict.resolve("ftbquests/quests/chapters/admin_copy.snbt"); Files.createDirectories(other.getParent()); Files.writeString(other, initial);
        JournalBootstrap.install(conflict);
        check(!Files.exists(other.resolveSibling("kncraft_bosses.snbt")), "Journal installed duplicate IDs");
        check(Files.readString(other).equals(initial), "Journal overwrote conflicting administrator content");
        JournalBootstrap.install(FMLPaths.CONFIGDIR.get());
        System.out.println("JOURNAL MIGRATION: repeat installation, stable IDs, administrator edits, root settings and collision preservation passed");
    }
}
