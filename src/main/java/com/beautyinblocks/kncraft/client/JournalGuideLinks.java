package com.beautyinblocks.kncraft.client;

import com.beautyinblocks.kncraft.core.Compatibility;
import com.beautyinblocks.kncraft.integration.guide.*;
import com.mojang.logging.LogUtils;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import vazkii.patchouli.api.PatchouliAPI;

@Mod.EventBusSubscriber(modid = "kncraft", value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class JournalGuideLinks {
    @SubscribeEvent public static void setup(FMLClientSetupEvent event) {
        if (Compatibility.present("ftblibrary") && Compatibility.present("patchouli")) event.enqueueWork(JournalGuideLinks::install);
    }
    private static void install() {
        try {
            JournalGuideEvents.register(entry -> PatchouliAPI.get().openBookEntry(GuideDelivery.BOOK, entry, 0));
        } catch (ReflectiveOperationException ex) { LogUtils.getLogger().error("Could not connect FTB journal to the KNCraft Guide Book", ex); }
    }
    private JournalGuideLinks() {}
}
