package com.beautyinblocks.kncraft.core;

import com.beautyinblocks.kncraft.integration.encounters.Encounters;
import com.mojang.logging.LogUtils;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;

@Mod("kncraft")
public final class Architecture {
    public Architecture() {
        Compatibility.validate();
        LegacyConfigImport.run(FMLPaths.CONFIGDIR.get());
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, ArchitectureConfig.SPEC, "kncraft-common.toml");
        FMLJavaModLoadingContext.get().getModEventBus().addListener(BundledPacks::register);
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(new Encounters());
        // These constructors are only resolved when their entire dependency set is present.
        if (Compatibility.exact("immersive_portals")) new com.beautyinblocks.portals.NativePortalLighting();
        if (Compatibility.tents()) new com.beautyinblocks.tents.TentPortals();
    }
    @SubscribeEvent public void starting(ServerAboutToStartEvent event) {
        for (String id : event.getServer().getPackRepository().getSelectedIds()) {
            if (id.equals("file/kncraft-rules") || id.equals("file/kncraft-rules.zip")
                || id.contains("kncraft-aether-immersive-portals") || id.contains("kncraft-depth-immersive-portals"))
                throw new IllegalStateException("Remove replaced external datapack " + id + " before starting KNCraft Architecture. Keep a backup outside the world datapacks directory.");
        }
    }
    public static List<String> report() {
        var lines = new ArrayList<String>();
        lines.add("KNCraft Architecture — Java 17 / Minecraft 1.20.1 / Forge 47.4.0");
        Compatibility.PINS.entrySet().stream().sorted(java.util.Map.Entry.comparingByKey())
            .forEach(pin -> lines.add(pin.getKey() + ": " + Compatibility.version(pin.getKey()) + " (audited " + pin.getValue() + ")"));
        lines.add("Config migration: " + LegacyConfigImport.state);
        lines.add("Native lighting=" + (Compatibility.exact("immersive_portals") && ArchitectureConfig.NATIVE_PORTALS.get()));
        lines.add("Tent synchronization=" + Compatibility.tents() + "; entrances=" + (Compatibility.tents() && ArchitectureConfig.TENTS.get()));
        lines.add("Performance: portal=" + (Compatibility.exact("witherstormmod") && ArchitectureConfig.PORTAL_SEARCH.get())
            + ", items=" + (Compatibility.exact("alexsmobs") && ArchitectureConfig.ITEM_SELECTION.get())
            + ", tornado=" + (Compatibility.exact("weather2") && ArchitectureConfig.TORNADO_QUERY.get()));
        lines.add("Waystone data=" + (Compatibility.present("waystones") && ArchitectureConfig.WAYSTONES.get()) + "; encounter scaling=" + (Compatibility.exact("witherstormmod") && ArchitectureConfig.ENCOUNTERS.get()));
        lines.add("Cohesion config: cotton=" + ArchitectureConfig.COTTON.get() + ", wildlife=" + ArchitectureConfig.WILDLIFE.get() + ", meals=" + ArchitectureConfig.MEALS.get() + ", fiber=" + ArchitectureConfig.FIBERS.get());
        lines.add("Guide: patchouli:kncraft_guide; matching client installation required. See docs/Validation.md for unverified checks.");
        return lines;
    }
    @SubscribeEvent public void started(ServerStartedEvent event) {
        String text = String.join("\n", report());
        LogUtils.getLogger().info("{}", text);
        try { Files.writeString(FMLPaths.CONFIGDIR.get().resolve("kncraft-status.txt"), text + "\n"); }
        catch (java.io.IOException ex) { LogUtils.getLogger().warn("Could not write kncraft-status.txt", ex); }
    }
    @SubscribeEvent public void commands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("kncraft").then(Commands.literal("status").executes(ctx -> {
            report().forEach(line -> ctx.getSource().sendSuccess(() -> Component.literal(line), false));
            return 1;
        })));
    }
}
