package com.beautyinblocks.kncraft.integration.supplies;

import com.beautyinblocks.kncraft.core.ExpansionConfig;
import java.util.List;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagEntry;
import net.minecraft.tags.TagLoader;
import net.minecraftforge.fml.ModList;

/** Repair only the offending contributor; never replace a merged vanilla tag. */
public final class KnownTags {
    private static final Map<String, String> MISSING = Map.of(
        "rails", "betterminecarts:glowing_rail",
        "arthropod", "callfromthedepth_:deepspider",
        "head_armor", "callfromthedepth_:depth_armor_helmet",
        "chest_armor", "callfromthedepth_:depth_armor_chestplate",
        "leg_armor", "callfromthedepth_:depth_armor_leggings",
        "foot_armor", "callfromthedepth_:depth_armor_boots",
        "enchantable/durability", "callfromthedepth_:pillsfordarkness");

    public static void repair(Map<ResourceLocation, List<TagLoader.EntryWithSource>> tags) {
        if (!ExpansionConfig.KNOWN_TAGS.get()) return;
        MISSING.forEach((tag, missing) -> {
            var entries = tags.get(new ResourceLocation("minecraft", tag));
            if (entries == null) return;
            var id = new ResourceLocation(missing);
            var modFile = ModList.get().getModFileById(id.getNamespace());
            if (modFile == null) return;
            String source = modFile.getFile().getFileName();
            entries.replaceAll(entry -> !entry.remove() && entry.source().equals(source)
                && entry.entry().isRequired() && !entry.entry().isTag() && entry.entry().getId().equals(id)
                ? new TagLoader.EntryWithSource(TagEntry.optionalElement(id), entry.source()) : entry);
        });
    }
    private KnownTags() {}
}
