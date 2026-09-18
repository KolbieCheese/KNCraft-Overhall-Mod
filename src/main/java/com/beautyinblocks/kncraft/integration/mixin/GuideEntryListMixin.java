package com.beautyinblocks.kncraft.integration.mixin;

import com.beautyinblocks.kncraft.client.GuideEntryButton;
import com.beautyinblocks.kncraft.client.GuideEntryPagination;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import vazkii.patchouli.client.book.BookEntry;
import vazkii.patchouli.client.book.gui.GuiBook;
import vazkii.patchouli.client.book.gui.GuiBookEntryList;
import vazkii.patchouli.common.book.Book;

/** Lay out the KNCraft index, categories, search results and history by actual title height. */
@Mixin(value = GuiBookEntryList.class, remap = false)
public abstract class GuideEntryListMixin extends GuiBook {
    @Shadow @Final private List<BookEntry> visibleEntries;
    @Shadow @Final protected List<Button> entryButtons;
    @Shadow private List<BookEntry> allEntries;
    @Shadow private EditBox searchField;
    @Shadow public abstract void handleButtonEntry(Button button);
    @Shadow protected abstract void addSubcategoryButtons();
    @Unique private List<BookEntry> kncraft$source;
    @Unique private String kncraft$query;
    @Unique private List<GuideEntryButton.Title> kncraft$titles;
    @Unique private List<GuideEntryPagination.Page> kncraft$pages;

    protected GuideEntryListMixin(Book book, Component title) { super(book, title); }
    @Unique private boolean kncraft$isGuide() { return book.id.toString().equals("patchouli:kncraft_guide"); }

    @Inject(method = "buildEntryButtons", at = @At("HEAD"), cancellable = true)
    private void kncraft$fitEntryLists(CallbackInfo ci) {
        if (!kncraft$isGuide()) return;
        removeDrawablesIn(entryButtons);
        entryButtons.clear();
        String query = searchField.getValue().toLowerCase(Locale.ROOT);
        // Native init replaces allEntries on reopen/resize/reload. Page turns reuse measurements.
        if (kncraft$source != allEntries || !query.equals(kncraft$query)) {
            visibleEntries.clear();
            allEntries.stream().filter(entry -> entry.isFoundByQuery(query)).forEach(visibleEntries::add);
            kncraft$titles = visibleEntries.stream().map(GuideEntryButton::measure).toList();
            kncraft$pages = GuideEntryPagination.paginate(kncraft$titles.stream().map(GuideEntryButton.Title::height).toList());
            kncraft$source = allEntries;
            kncraft$query = query;
        }
        maxSpreads = GuideEntryPagination.spreadCount(kncraft$pages);
        spread = Math.max(0, Math.min(spread, maxSpreads - 1));
        if (spread > 0) kncraft$addColumn(15, GuideEntryPagination.pageIndex(spread, false));
        kncraft$addColumn(141, GuideEntryPagination.pageIndex(spread, true));
        if (spread == 0) addSubcategoryButtons();
        ci.cancel();
    }

    @Unique private void kncraft$addColumn(int x, int page) {
        if (page >= kncraft$pages.size()) return;
        for (var row : kncraft$pages.get(page).rows()) {
            var button = new GuideEntryButton(this, bookLeft + x, bookTop + row.y(),
                    visibleEntries.get(row.index()), kncraft$titles.get(row.index()), this::handleButtonEntry);
            addRenderableWidget(button);
            entryButtons.add(button);
        }
    }

    // Patchouli guesses whether the right page is empty from the old fixed row count.
    @Redirect(method = "drawForegroundElements", at = @At(value = "INVOKE",
            target = "Lvazkii/patchouli/client/book/gui/GuiBookEntryList;drawPageFiller(Lnet/minecraft/client/gui/GuiGraphics;Lvazkii/patchouli/common/book/Book;)V"))
    private void kncraft$skipFixedRowFiller(GuiGraphics graphics, Book book) {
        if (!kncraft$isGuide()) GuiBook.drawPageFiller(graphics, book);
    }

    @Inject(method = "drawForegroundElements", at = @At("TAIL"))
    private void kncraft$fillEmptyRightPage(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
        if (kncraft$isGuide() && spread > 0 && kncraft$pages != null
                && GuideEntryPagination.pageIndex(spread, true) >= kncraft$pages.size()) GuiBook.drawPageFiller(graphics, book);
    }
}
