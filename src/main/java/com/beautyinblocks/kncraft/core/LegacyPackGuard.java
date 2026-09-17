package com.beautyinblocks.kncraft.core;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

/** Reject renamed encounter schedulers and legacy pack names before the reload apply barrier. */
public final class LegacyPackGuard extends SimplePreparableReloadListener<Void> {
    @Override protected Void prepare(ResourceManager resources, ProfilerFiller profiler) {
        resources.listPacks().forEach(pack -> {
            String id = pack.packId();
            if (id.equals("file/kncraft-rules") || id.equals("file/kncraft-rules.zip")
                || id.contains("kncraft-aether-immersive-portals") || id.contains("kncraft-depth-immersive-portals"))
                throw new IllegalStateException("Remove replaced KNCraft datapack before reload: " + id);
        });
        if (resources.getResource(new ResourceLocation("kncraft:functions/encounter.mcfunction")).isPresent())
            throw new IllegalStateException("External kncraft:encounter scheduler conflicts with KNCraft Architecture; remove the old rules pack.");
        return null;
    }
    @Override protected void apply(Void ignored, ResourceManager resources, ProfilerFiller profiler) {}
}
