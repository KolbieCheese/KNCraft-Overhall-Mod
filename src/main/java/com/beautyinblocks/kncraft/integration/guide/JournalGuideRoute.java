package com.beautyinblocks.kncraft.integration.guide;

import java.util.Set;
import net.minecraft.resources.ResourceLocation;

public final class JournalGuideRoute {
    private static final Set<String> CHAPTERS = Set.of("intro", "wither", "cold", "weather", "aether", "depths", "animals", "travel", "food", "explore", "catalog");
    public static ResourceLocation entry(ResourceLocation click) {
        String prefix = "kncraft/chapters/";
        if (!click.getNamespace().equals("guide") || !click.getPath().startsWith(prefix)) return null;
        String chapter = click.getPath().substring(prefix.length());
        return CHAPTERS.contains(chapter) ? new ResourceLocation("patchouli", "chapters/" + chapter) : null;
    }
    private JournalGuideRoute() {}
}
