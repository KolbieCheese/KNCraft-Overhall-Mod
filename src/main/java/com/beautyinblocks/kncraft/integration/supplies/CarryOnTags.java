package com.beautyinblocks.kncraft.integration.supplies;

import com.beautyinblocks.kncraft.core.ExpansionConfig;
import net.minecraftforge.event.TagsUpdatedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/** Carry On initially caches its tag lists before server data has been bound. */
public final class CarryOnTags {
    @SubscribeEvent public void tags(TagsUpdatedEvent event) {
        if (!event.shouldUpdateStaticData() || !ExpansionConfig.SAFE_CARRY.get()) return;
        try {
            // Public API, resolved only when the audited optional mod is installed.
            Class.forName("tschipp.carryon.common.config.ListHandler").getMethod("initConfigLists").invoke(null);
        } catch (ReflectiveOperationException ex) {
            throw new IllegalStateException("Could not refresh Carry On's configured tag policy", ex);
        }
    }
}
