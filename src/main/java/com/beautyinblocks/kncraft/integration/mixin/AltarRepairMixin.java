package com.beautyinblocks.kncraft.integration.mixin;

import com.beautyinblocks.kncraft.core.PolishConfig;
import java.util.Set;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Repair output must retain serialized capabilities, including sewn insulation. */
@Mixin(targets = "com.aetherteam.aether.recipe.recipes.item.AltarRepairRecipe", remap = false)
public abstract class AltarRepairMixin {
    private static final Set<String> KNCRAFT_REPAIRS = Set.of("goat_fur_helmet", "goat_fur_chestplate", "goat_fur_leggings", "goat_fur_boots",
        "hoglin_helmet", "hoglin_chestplate", "hoglin_leggings", "hoglin_boots", "frontier_cap");
    @Inject(method = "m_5874_", at = @At("HEAD"), cancellable = true)
    private void kncraft$keepCapabilities(Container container, RegistryAccess registry, CallbackInfoReturnable<ItemStack> result) {
        if (!PolishConfig.ALTAR.get()) return;
        var id = ((Recipe<?>)(Object)this).getId();
        if (!id.getNamespace().equals("kncraft") || !id.getPath().endsWith("_altar_repair")
            || !KNCRAFT_REPAIRS.contains(id.getPath().replace("_altar_repair", ""))) return;
        var copy = container.getItem(0).copy();
        copy.setCount(1); copy.setDamageValue(0);
        result.setReturnValue(copy);
    }
}
