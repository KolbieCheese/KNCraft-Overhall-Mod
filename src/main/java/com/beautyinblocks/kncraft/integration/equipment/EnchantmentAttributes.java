package com.beautyinblocks.kncraft.integration.equipment;

import com.beautyinblocks.kncraft.core.PolishConfig;
import com.mojang.logging.LogUtils;
import java.nio.charset.StandardCharsets;
import java.util.*;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/** Capture the upstream formulas, including configuration and wing-slot redirects, without dispatching commands. */
public final class EnchantmentAttributes {
    private static final String STATE = "KNCraftAttributeMigrationV1";
    private static final String[] NAMES = {"minecraft:generic.attack_speed", "minecraft:generic.movement_speed", "forge:block_reach", "minecraft:generic.armor"};
    private static final double[] BASES = {4, .1, 4.5, 0};
    private static final UUID[] IDS = Arrays.stream(NAMES).map(n -> UUID.nameUUIDFromBytes(("kncraft:enchantment/" + n).getBytes(StandardCharsets.UTF_8))).toArray(UUID[]::new);
    private static final Map<ServerPlayer, Pending> pending = new WeakHashMap<>();
    private static class Pending { final double[] values = new double[4]; int mask; int tick; }
    private static Attribute[] attributes() { return new Attribute[]{Attributes.ATTACK_SPEED, Attributes.MOVEMENT_SPEED, ForgeMod.BLOCK_REACH.get(), Attributes.ARMOR}; }

    public static int apply(Commands commands, CommandSourceStack source, String command, Entity entity) {
        if (!(entity instanceof ServerPlayer player) || !PolishConfig.ATTRIBUTES.get()) {
            if (entity instanceof ServerPlayer player) clear(player);
            return commands.performPrefixedCommand(source, command);
        }
        for (int i = 0; i < NAMES.length; i++) {
            String prefix = "attribute @p " + NAMES[i] + " base set ";
            if (!command.startsWith(prefix)) continue;
            double value = Double.parseDouble(command.substring(prefix.length()));
            if (!Double.isFinite(value)) return 0;
            var state = pending.computeIfAbsent(player, p -> new Pending());
            if (state.tick != player.tickCount) { state.tick = player.tickCount; state.mask = 0; }
            state.values[i] = value; state.mask |= 1 << i;
            if (state.mask == 15) reconcile(player, state.values);
            return 1;
        }
        throw new IllegalStateException("Unexpected More Enchantments attribute command: " + command);
    }

    public static void reconcile(ServerPlayer player, double[] desired) {
        var attrs = attributes();
        var data = player.getPersistentData();
        if (!data.contains(STATE)) {
            var backup = new CompoundTag();
            boolean matches = true;
            for (int i = 0; i < attrs.length; i++) {
                var attr = player.getAttribute(attrs[i]);
                if (attr == null) { matches = false; continue; }
                backup.putDouble(NAMES[i], attr.getBaseValue());
                matches &= Math.abs(attr.getBaseValue() - desired[i]) < 1e-7;
            }
            boolean migrate = matches && PolishConfig.MIGRATE_ATTRIBUTES.get();
            backup.putBoolean("migrated", migrate);
            data.put(STATE, backup);
            if (migrate) for (int i = 0; i < attrs.length; i++) player.getAttribute(attrs[i]).setBaseValue(BASES[i]);
            else if (!matches) LogUtils.getLogger().info("KNCraft preserved nonmatching attribute bases for {}; review /kncraft attributes if upgrading legacy enchanted equipment", player.getGameProfile().getName());
        }
        for (int i = 0; i < attrs.length; i++) {
            var attr = player.getAttribute(attrs[i]);
            if (attr == null) continue;
            double amount = desired[i] - BASES[i];
            var previous = attr.getModifier(IDS[i]);
            if (previous != null && Math.abs(previous.getAmount() - amount) < 1e-9) continue;
            if (previous != null) attr.removeModifier(IDS[i]);
            if (Math.abs(amount) > 1e-9) attr.addTransientModifier(new AttributeModifier(IDS[i], "KNCraft enchantment contribution", amount, AttributeModifier.Operation.ADDITION));
        }
    }
    public static void clear(ServerPlayer player) {
        var attrs = attributes();
        for (int i = 0; i < attrs.length; i++) if (player.getAttribute(attrs[i]) != null) player.getAttribute(attrs[i]).removeModifier(IDS[i]);
        pending.remove(player);
    }
    public static CompoundTag migration(ServerPlayer player) { return player.getPersistentData().getCompound(STATE).copy(); }
    public static void restoreBackup(ServerPlayer player) {
        var saved = migration(player); var attrs = attributes();
        for (int i = 0; i < attrs.length; i++) if (saved.contains(NAMES[i]) && player.getAttribute(attrs[i]) != null) player.getAttribute(attrs[i]).setBaseValue(saved.getDouble(NAMES[i]));
        saved.putBoolean("restored", true); player.getPersistentData().put(STATE, saved);
    }
    @SubscribeEvent public void clone(PlayerEvent.Clone event) {
        if (event.getOriginal().getPersistentData().contains(STATE)) event.getEntity().getPersistentData().put(STATE, event.getOriginal().getPersistentData().getCompound(STATE).copy());
    }
    @SubscribeEvent public void logout(PlayerEvent.PlayerLoggedOutEvent event) { if (event.getEntity() instanceof ServerPlayer p) pending.remove(p); }
}
