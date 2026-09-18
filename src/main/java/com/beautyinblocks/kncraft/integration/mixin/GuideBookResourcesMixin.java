package com.beautyinblocks.kncraft.integration.mixin;

import com.google.gson.JsonObject;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import vazkii.patchouli.common.book.Book;
import vazkii.patchouli.xplat.XplatModContainer;

/** Patchouli 85 otherwise ignores use_resource_pack on books discovered in its external folder. */
@Mixin(value = Book.class, remap = false)
public abstract class GuideBookResourcesMixin {
    @Shadow @Final @Mutable public boolean isExternal;
    @Shadow @Final public ResourceLocation id;

    /** Publication edition is independent of Patchouli's internal content revision. */
    @Inject(method = "getSubtitle", at = @At("HEAD"), cancellable = true)
    private void kncraft$publicationEdition(CallbackInfoReturnable<MutableComponent> cir) {
        if (!isExternal && id.toString().equals("patchouli:kncraft_guide")) {
            cir.setReturnValue(Component.translatable("patchouli.gui.lexicon.edition_str", Component.literal("1st")));
        }
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void kncraft$useBundledPages(JsonObject root, XplatModContainer owner, ResourceLocation id,
                                       boolean external, CallbackInfo ci) {
        if (external && id.toString().equals("patchouli:kncraft_guide")
            && root.has("use_resource_pack") && root.get("use_resource_pack").getAsBoolean()) {
            isExternal = false;
        }
    }
}
