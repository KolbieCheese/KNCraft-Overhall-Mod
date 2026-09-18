package com.beautyinblocks.guideclienttest;

import com.beautyinblocks.kncraft.client.GuideEntryButton;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLPaths;
import vazkii.patchouli.client.book.BookEntry;
import vazkii.patchouli.client.book.gui.GuiBook;
import vazkii.patchouli.client.book.gui.GuiBookCategory;
import vazkii.patchouli.client.book.gui.GuiBookEntryList;
import vazkii.patchouli.client.book.gui.GuiBookIndex;
import vazkii.patchouli.client.book.gui.button.GuiButtonEntry;
import vazkii.patchouli.common.book.BookRegistry;

/** Temporary, separately packaged client test. No network or world mutations. */
@Mod("kncraftguideclienttests")
public final class GuideLayoutChecks {
    @Mod.EventBusSubscriber(modid = "kncraftguideclienttests", value = Dist.CLIENT)
    public static final class Client {
        @SubscribeEvent public static void commands(RegisterClientCommandsEvent event) {
            event.getDispatcher().register(Commands.literal("kncraftguidelayouttest").executes(context -> {
                try {
                    check(Minecraft.getInstance().isLocalServer(), "Run only in a local test world");
                    String report = audit();
                    System.out.println(report);
                    Files.writeString(FMLPaths.GAMEDIR.get().resolve("kncraft-guide-layout-report.txt"), report);
                    context.getSource().sendSuccess(() -> Component.literal(report), false);
                    return 1;
                } catch (Exception e) { throw new IllegalStateException("Guide client layout audit failed", e); }
            }));
        }

        private static String audit() throws Exception {
            var mc = Minecraft.getInstance();
            var book = BookRegistry.INSTANCE.books.get(new ResourceLocation("patchouli:kncraft_guide"));
            check(book != null && book.getContents().entries.size() > 3000, "Loaded guide missing its catalog");
            check(book.getSubtitle().getString().equals("1st Edition"), "Wrong publication edition");
            var previousGui = book.getContents().currentGui;
            try {
                List<BookEntry> entries = book.getContents().entries.values().stream().filter(e -> !e.shouldHide()).sorted().toList();
                int wrapped = 0;
                for (var entry : entries) {
                    var title = GuideEntryButton.measure(entry);
                    check(title.scale() == 1, "An ordinary title was shrunk: " + entry.getId());
                    if (title.lines().size() > 1) wrapped++;
                    for (var line : title.lines()) check(mc.font.width(line) <= GuideEntryButton.TEXT_WIDTH, "Text overflows: " + entry.getId());
                }
                var index = new GuiBookIndex(book);
                init(index);
                int indexSpreads = auditPages(index, entries);
                int categorySpreads = 0;
                for (var category : book.getContents().categories.values()) {
                    var gui = new GuiBookCategory(book, category);
                    init(gui);
                    categorySpreads += auditPages(gui, category.getEntries().stream().filter(e -> !e.shouldHide()).sorted().toList());
                }
                // Search after reaching the final index spread must clamp the page and rebuild rows.
                var searchField = GuiBookEntryList.class.getDeclaredField("searchField");
                searchField.setAccessible(true);
                var field = (net.minecraft.client.gui.components.EditBox) searchField.get(index);
                var rebuild = GuiBookEntryList.class.getDeclaredMethod("buildEntryButtons");
                rebuild.setAccessible(true);
                for (String query : List.of("red nether", "golden apple stew", "zzzzzz_no_results", "")) {
                    field.setValue(query);
                    rebuild.invoke(index);
                    // Return to the first spread, then verify the complete filtered result.
                    var change = GuiBook.class.getDeclaredMethod("changePage", boolean.class, boolean.class);
                    change.setAccessible(true);
                    while (index.canSeePageButton(true)) change.invoke(index, true, false);
                    auditPages(index, entries.stream().filter(e -> e.isFoundByQuery(query)).toList());
                }
                index.init(mc, mc.getWindow().getGuiScaledWidth() / 2, mc.getWindow().getGuiScaledHeight() / 2);
                auditPages(index, entries);
                return "GUIDE CLIENT LAYOUT PASSED: " + entries.size() + " entries, " + wrapped + " wrapped titles, "
                        + indexSpreads + " index spreads, " + book.getContents().categories.size() + " categories / "
                        + categorySpreads + " category spreads; full names, normal font size, width/height bounds, "
                        + "no overlapping rows, exact ordered coverage, search/clamping/empty results, resize and 1st Edition.";
            } finally { book.getContents().currentGui = previousGui; }
        }

        private static void init(GuiBook gui) {
            var mc = Minecraft.getInstance();
            gui.init(mc, mc.getWindow().getGuiScaledWidth(), mc.getWindow().getGuiScaledHeight());
        }

        private static int auditPages(GuiBookEntryList gui, List<BookEntry> expected) throws Exception {
            var seen = new ArrayList<BookEntry>();
            var change = GuiBook.class.getDeclaredMethod("changePage", boolean.class, boolean.class);
            change.setAccessible(true);
            while (gui.canSeePageButton(true)) change.invoke(gui, true, false);
            int pages = 0;
            do {
                var bottom = new HashMap<Integer, Integer>();
                for (var child : gui.children()) if (child instanceof GuiButtonEntry entry) {
                    check(entry instanceof GuideEntryButton, "Default single-line button still present");
                    check(entry.getMessage().getString().equals(entry.getEntry().getName().getString()), "Title was abbreviated");
                    int x = entry.getX() - gui.bookLeft;
                    check(x == 15 || x == 141, "Wrong column");
                    check(entry.getWidth() == 116, "Button exceeds page width");
                    check(entry.getY() >= bottom.getOrDefault(x, gui.bookTop + (pages == 0 ? 38 : 18)), "Overlapping row");
                    int end = entry.getY() + entry.getHeight();
                    check(end <= gui.bookTop + 162, "Row falls below the page");
                    bottom.put(x, end + 2);
                    seen.add(entry.getEntry());
                }
                pages++;
                check(pages < 1000, "Page navigation did not terminate");
                if (!gui.canSeePageButton(false)) break;
                change.invoke(gui, false, false);
            } while (true);
            check(seen.equals(expected), "Entry skipped, repeated or reordered");
            return pages;
        }

        private static void check(boolean condition, String message) {
            if (!condition) throw new IllegalStateException(message);
        }
    }
}
