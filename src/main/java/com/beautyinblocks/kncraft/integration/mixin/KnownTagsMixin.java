package com.beautyinblocks.kncraft.integration.mixin;

import com.beautyinblocks.kncraft.integration.supplies.KnownTags;
import java.util.List;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.TagLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = TagLoader.class, remap = false)
abstract class KnownTagsMixin {
    @Inject(method = "m_144495_", at = @At("RETURN"))
    private void kncraft$repairContributors(ResourceManager resources,
            CallbackInfoReturnable<Map<ResourceLocation, List<TagLoader.EntryWithSource>>> callback) {
        KnownTags.repair(callback.getReturnValue());
    }
}
