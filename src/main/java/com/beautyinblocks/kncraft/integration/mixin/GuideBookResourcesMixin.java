package com.beautyinblocks.kncraft.integration.mixin;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import vazkii.patchouli.common.book.Book;
import vazkii.patchouli.xplat.XplatModContainer;

/** Patchouli 85 otherwise ignores use_resource_pack on books discovered in its external folder. */
@Mixin(value = Book.class, remap = false)
public abstract class GuideBookResourcesMixin {
    @Shadow @Final @Mutable public boolean isExternal;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void kncraft$useBundledPages(JsonObject root, XplatModContainer owner, ResourceLocation id,
                                       boolean external, CallbackInfo ci) {
        if (external && id.toString().equals("patchouli:kncraft_guide")
            && root.has("use_resource_pack") && root.get("use_resource_pack").getAsBoolean()) {
            isExternal = false;
        }
    }
}
