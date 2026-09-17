package com.beautyinblocks.kncraft.integration.mixin;

import com.beautyinblocks.kncraft.integration.equipment.EnchantmentAttributes;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(targets = {"net.mcreator.moreenchantments.procedures.FurorProcProcedure", "net.mcreator.moreenchantments.procedures.AgilityProcProcedure", "net.mcreator.moreenchantments.procedures.RangeProcProcedure", "net.mcreator.moreenchantments.procedures.ArmoringProcProcedure"}, remap = false)
public abstract class EnchantmentAttributesMixin {
    @Redirect(method = "execute", at = @At(value = "INVOKE", target = "Lnet/minecraft/commands/Commands;m_230957_(Lnet/minecraft/commands/CommandSourceStack;Ljava/lang/String;)I"))
    private static int kncraft$attributes(Commands commands, CommandSourceStack source, String command,
                                        LevelAccessor world, double x, double y, double z, Entity entity) {
        return EnchantmentAttributes.apply(commands, source, command, entity);
    }
}
