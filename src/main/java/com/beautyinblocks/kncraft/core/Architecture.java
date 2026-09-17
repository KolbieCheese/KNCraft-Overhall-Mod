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
        GuideBootstrap.install(FMLPaths.GAMEDIR.get());
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, ArchitectureConfig.SPEC, "kncraft-common.toml");
        FMLJavaModLoadingContext.get().getModEventBus().addListener(BundledPacks::register);
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(new Encounters());
        // These constructors are only resolved when their entire dependency set is present.
        if (Compatibility.exact("immersive_portals")) new com.beautyinblocks.portals.NativePortalLighting();
        if (Compatibility.tents()) new com.beautyinblocks.tents.TentPortals();
        if (Compatibility.exact("cold_sweat")) MinecraftForge.EVENT_BUS.register(new com.beautyinblocks.kncraft.integration.climate.ClimateIntegration());
    }
    @SubscribeEvent public void starting(ServerAboutToStartEvent event) {
        for (String id : event.getServer().getPackRepository().getSelectedIds()) {
            if (id.equals("file/kncraft-rules") || id.equals("file/kncraft-rules.zip")
                || id.contains("kncraft-aether-immersive-portals") || id.contains("kncraft-depth-immersive-portals"))
                throw new IllegalStateException("Remove replaced external datapack " + id + " before starting KNCraft Architecture. Keep a backup outside the world datapacks directory.");
        }
        // Also recognize a renamed old rules pack by its legacy scheduler resource.
        var scheduler = new net.minecraft.resources.ResourceLocation("kncraft:functions/encounter.mcfunction");
        if (event.getServer().getResourceManager().getResource(scheduler).isPresent())
            throw new IllegalStateException("An external pack still supplies kncraft:encounter. Remove the old kncraft-rules scheduler before starting the merged mod.");
    }
    @SubscribeEvent public void reload(net.minecraftforge.event.AddReloadListenerEvent event) {
        event.addListener(new LegacyPackGuard());
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
        if (Compatibility.present("waystones") && ArchitectureConfig.WAYSTONES.get()) lines.add(waystonePolicy());
        lines.add("Cohesion config: cotton=" + ArchitectureConfig.COTTON.get() + ", wildlife=" + ArchitectureConfig.WILDLIFE.get() + ", meals=" + ArchitectureConfig.MEALS.get() + ", fiber=" + ArchitectureConfig.FIBERS.get());
        lines.add("Guide: patchouli:kncraft_guide; " + GuideBootstrap.state + "; matching client installation required.");
        if (Compatibility.exact("cold_sweat")) lines.addAll(com.beautyinblocks.kncraft.integration.climate.ClimateIntegration.diagnostics());
        return lines;
    }
    private static String waystonePolicy() {
        var path = FMLPaths.CONFIGDIR.get().resolve("waystones-common.toml");
        if (!Files.isRegularFile(path)) return "WARNING: Waystones config absent; retain the natural-network policy from pack-overrides.";
        try (var config = com.electronwill.nightconfig.core.file.CommentedFileConfig.of(path)) {
            config.load();
            boolean creative = Boolean.TRUE.equals(config.get("restrictions.restrictToCreative"));
            boolean protectedStones = Boolean.TRUE.equals(config.get("restrictions.generatedWaystonesUnbreakable"));
            return creative && protectedStones ? "Waystones: creative-only editing and generated-stone protection configured"
                : "WARNING: Waystone protection is incomplete in upstream config; merge pack-overrides/waystones-policy.toml.fragment.";
        } catch (RuntimeException ex) { return "WARNING: Could not inspect Waystones policy; check config/waystones-common.toml."; }
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
