package com.beautyinblocks.tents;

import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.protocol.game.ClientboundPingPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import nomadictents.NTSavedData;
import nomadictents.dimension.DimensionFactory;
import nomadictents.dimension.DynamicDimensionHelper;
import nomadictents.structure.TentPlacer;
import nomadictents.tileentity.TentDoorBlockEntity;
import nomadictents.util.Tent;
import qouteall.imm_ptl.core.portal.Portal;
import qouteall.imm_ptl.core.portal.PortalManipulation;
import qouteall.q_misc_util.dimension.DimensionIdManagement;
import qouteall.q_misc_util.dimension.DimensionIdRecord;
import qouteall.q_misc_util.forge.networking.Dim_Sync;
import qouteall.q_misc_util.forge.networking.Message;
import qouteall.q_misc_util.my_util.DQuaternion;
import org.slf4j.Logger;
import java.util.*;

@Mod("kncrafttentportals")
public class TentPortals {
    public static final String DATA = "KNCraftTentLink";
    private static final Logger LOG = LogUtils.getLogger();
    private static boolean ready;
    private static int ticks;
    private static int nextPing = -1464000000;
    private static final Map<UUID, Integer> pendingSync = new HashMap<>();
    private static final Set<ResourceKey<Level>> awaitingDimensions = new HashSet<>();
    private static final ForgeConfigSpec.Builder CONFIG = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec.BooleanValue ENABLED = CONFIG.comment("Enable immersive tent doorways. Dimension synchronization remains active when disabled.").define("immersiveTentEntrances", true);
    public TentPortals() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, CONFIG.build(), "kncraft-tent-portals.toml");
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent public void started(ServerStartedEvent e) { ready = true; }
    @SubscribeEvent public void stopped(ServerStoppedEvent e) {
        ready = false; ticks = 0; pendingSync.clear(); awaitingDimensions.clear();
    }

    // Infiniverse fires Load after inserting the level, before returning to Nomadic Tents.
    // Synchronize here, before either mod can send dimension-dependent packets.
    @SubscribeEvent public void loaded(LevelEvent.Load e) {
        if (ready && e.getLevel() instanceof ServerLevel level && isTentLevel(level)) {
            awaitingDimensions.add(level.dimension());
            syncDimensions(level.getServer());
        }
    }
    public static void syncDimensions(MinecraftServer server) {
        if (DimensionIdRecord.serverRecord == null) return;
        DimensionIdManagement.updateAndSaveServerDimIdRecord();
        Dim_Sync packet = new Dim_Sync();
        pendingSync.clear();
        for (var player : server.getPlayerList().getPlayers()) {
            int id = nextPing++;
            pendingSync.put(player.getUUID(), id);
            Message.sendToPlayer(packet, player);
            // Vanilla handles Ping on the client main thread, after the preceding
            // Dim_Sync work. Its Pong proves the integer registry has been applied.
            player.connection.send(new ClientboundPingPacket(id));
        }
        finishSync(server);
        LOG.info("Sent dimension registry ({} dimensions); awaiting {} client acknowledgements", server.levelKeys().size(), pendingSync.size());
    }
    public static void acknowledge(ServerPlayer player, int id) {
        if (pendingSync.remove(player.getUUID(), id)) finishSync(player.getServer());
    }
    private static void finishSync(MinecraftServer server) {
        pendingSync.keySet().removeIf(uuid -> server.getPlayerList().getPlayer(uuid) == null);
        if (!pendingSync.isEmpty() || awaitingDimensions.isEmpty()) return;
        awaitingDimensions.clear();
        // Global portal synchronization can itself encode the newly allocated ID.
        // Defer it, as well as portal creation, until the client has acknowledged.
        MinecraftForge.EVENT_BUS.post(new qouteall.q_misc_util.forge.events.ServerDimensionDynamicUpdateEvent(server.levelKeys()));
        LOG.info("Clients acknowledged new tent dimensions; entrances can now open");
    }
    private static boolean isTentLevel(Level level) {
        return level.dimension().location().getNamespace().equals("nomadictents");
    }
    public static boolean linked(TentDoorBlockEntity door) {
        return door.getPersistentData().contains(DATA);
    }
    public static boolean complete(TentDoorBlockEntity door) {
        var tent = door.getTent();
        return TentPlacer.getInstance().isTent(door.getLevel(), door.getBlockPos(), tent.getType(),
            TentPlacer.getOverworldSize(tent.getSize()), door.getDirection());
    }

    /** Called only after Nomadic Tents' own canEnter check succeeds. */
    public static boolean open(TentDoorBlockEntity door, Entity visitor) {
        if (!(door.getLevel() instanceof ServerLevel source)) return false;
        if (!ENABLED.get()) {
            if (isTentLevel(source) || visitor.isOnPortalCooldown() || !door.canEnter(visitor).isAllow()) return false;
            var key = NTSavedData.get(source.getServer()).getOrCreateKey(source.getServer(), door.getTent().getId());
            DynamicDimensionHelper.getOrCreateWorld(source.getServer(), key, DimensionFactory::createDimension);
            // Native click-to-enter also waits for acknowledgement when the visual
            // portal feature is disabled. Clicking again then uses the original door.
            return awaitingDimensions.contains(key);
        }
        if (isTentLevel(source)) return hasLivePortal(door);
        if (!door.canEnter(visitor).isAllow()) return false;
        if (hasLivePortal(door)) return true;
        try {
            create(door);
            return true;
        } catch (Exception ex) {
            LOG.error("Could not create immersive tent entrance at {} {}", source.dimension().location(), door.getBlockPos(), ex);
            // Dimension synchronization is independent: preserve the native exit/entry fallback.
            return false;
        }
    }
    public static boolean hasLivePortal(TentDoorBlockEntity door) {
        if (!(door.getLevel() instanceof ServerLevel level) || !linked(door)) return false;
        CompoundTag tag = door.getPersistentData().getCompound(DATA);
        if (!tag.hasUUID("portal")) return false;
        Entity e = level.getEntity(tag.getUUID("portal"));
        return e instanceof Portal p && !p.isRemoved() && valid(p, true);
    }
    public static Portal create(TentDoorBlockEntity outside) {
        ServerLevel source = (ServerLevel) outside.getLevel();
        MinecraftServer server = source.getServer();
        Tent tent = outside.getTent();
        var key = NTSavedData.get(server).getOrCreateKey(server, tent.getId());
        ServerLevel target = DynamicDimensionHelper.getOrCreateWorld(server, key, DimensionFactory::createDimension);
        if (target == null) throw new IllegalStateException("Tent dimension was not created");
        if (awaitingDimensions.contains(key)) return null;
        BlockPos insidePos = Tent.calculatePos(tent.getId());
        target.getChunk(insidePos);
        Direction inward = outside.getDirection();
        Vec3 exit = Vec3.atBottomCenterOf(outside.getBlockPos().relative(inward.getOpposite()));
        TentPlacer.getInstance().placeOrUpgradeTent(target, insidePos, tent, source, exit, inward.toYRot());
        if (!(target.getBlockEntity(insidePos) instanceof TentDoorBlockEntity inside))
            throw new IllegalStateException("Interior door missing");

        // Retire old links before reconnecting a tent that has moved or been upgraded.
        discardOld(outside); discardOld(inside);
        UUID token = UUID.randomUUID();
        Vec3 direction = Vec3.atLowerCornerOf(inward.getNormal());
        Vec3 normal = direction.scale(-1);
        Portal forward = qouteall.imm_ptl.core.platform_specific.IPRegistry.PORTAL.get().create(source);
        if (forward == null) throw new IllegalStateException("Portal entity unavailable");
        forward.setOriginPos(Vec3.atCenterOf(outside.getBlockPos()).add(0, .5, 0).subtract(direction.scale(.26)));
        forward.setOrientationAndSize(new Vec3(normal.z, 0, -normal.x), new Vec3(0, 1, 0), 1, 2);
        forward.setDestinationDimension(target.dimension());
        forward.setDestination(Vec3.atCenterOf(insidePos).add(.26, .5, 0));
        forward.setRotationTransformation(DQuaternion.rotationByDegrees(new Vec3(0, 1, 0),
            Math.toDegrees(Math.atan2(direction.z, direction.x))));
        forward.setInteractable(false); // The physical door must remain reachable with a mallet.
        forward.portalTag = "kncraft_tent";
        forward.addTag("kncraft_tent");
        Portal reverse = PortalManipulation.createReversePortal(forward, qouteall.imm_ptl.core.platform_specific.IPRegistry.PORTAL.get());
        reverse.portalTag = "kncraft_tent";
        reverse.addTag("kncraft_tent");
        reverse.setInteractable(false);
        CompoundTag link = new CompoundTag();
        link.putUUID("token", token);
        link.putInt("tent", tent.getId());
        link.putString("outsideDim", source.dimension().location().toString());
        link.putLong("outsidePos", outside.getBlockPos().asLong());
        link.putString("insideDim", target.dimension().location().toString());
        link.putLong("insidePos", insidePos.asLong());
        forward.getPersistentData().put(DATA, link.copy());
        reverse.getPersistentData().put(DATA, link.copy());
        putDoorLink(outside, link, forward.getUUID());
        putDoorLink(inside, link, reverse.getUUID());
        forward.updateCache(); reverse.updateCache();
        if (!source.addFreshEntity(forward) || !target.addFreshEntity(reverse)) {
            forward.discard(); reverse.discard();
            throw new IllegalStateException("Unable to spawn paired portal entities");
        }
        LOG.info("Linked tent {}: {} {} -> {} {}", tent.getId(), source.dimension().location(), outside.getBlockPos(), key.location(), insidePos);
        return forward;
    }
    private static void putDoorLink(TentDoorBlockEntity door, CompoundTag link, UUID portal) {
        CompoundTag tag = link.copy(); tag.putUUID("portal", portal);
        door.getPersistentData().put(DATA, tag); door.setChanged();
    }
    private static void discardOld(TentDoorBlockEntity door) {
        CompoundTag tag = door.getPersistentData().getCompound(DATA);
        if (tag.hasUUID("portal") && door.getLevel() instanceof ServerLevel level) {
            Entity old = level.getEntity(tag.getUUID("portal"));
            if (old instanceof Portal) old.discard();
        }
    }
    private static TentDoorBlockEntity anchor(MinecraftServer server, CompoundTag tag, String prefix, boolean load) {
        var key = ResourceKey.create(Registries.DIMENSION, new ResourceLocation(tag.getString(prefix + "Dim")));
        ServerLevel level = server.getLevel(key);
        if (level == null) return null;
        BlockPos pos = BlockPos.of(tag.getLong(prefix + "Pos"));
        if (load) level.getChunk(pos);
        if (!level.hasChunkAt(pos)) return null;
        return level.getBlockEntity(pos) instanceof TentDoorBlockEntity d ? d : null;
    }
    public static boolean managed(Portal portal) { return portal.getPersistentData().contains(DATA); }
    public static boolean valid(Portal portal, boolean load) {
        if (!ENABLED.get()) return false;
        if (!(portal.level() instanceof ServerLevel level)) return true;
        CompoundTag tag = portal.getPersistentData().getCompound(DATA);
        if (!tag.hasUUID("token")) return false;
        var outside = anchor(level.getServer(), tag, "outside", load);
        var inside = anchor(level.getServer(), tag, "inside", load);
        return matches(outside, tag) && matches(inside, tag) && complete(outside);
    }
    private static boolean matches(TentDoorBlockEntity door, CompoundTag tag) {
        if (door == null || door.getTent().getId() != tag.getInt("tent")) return false;
        CompoundTag stored = door.getPersistentData().getCompound(DATA);
        return stored.hasUUID("token") && stored.getUUID("token").equals(tag.getUUID("token"));
    }
    private static boolean stale(Portal portal) {
        if (!ENABLED.get()) return true;
        var server = portal.getServer();
        CompoundTag tag = portal.getPersistentData().getCompound(DATA);
        if (!tag.hasUUID("token")) return true;
        // Cleanup must not keep remote chunks loaded after all players leave.
        // Unknown unloaded anchors are checked when loaded or before actual travel.
        for (String prefix : List.of("outside", "inside")) {
            var key = ResourceKey.create(Registries.DIMENSION, new ResourceLocation(tag.getString(prefix + "Dim")));
            var level = server.getLevel(key);
            if (level == null) return true;
            if (!level.hasChunkAt(BlockPos.of(tag.getLong(prefix + "Pos")))) continue;
            var door = anchor(server, tag, prefix, false);
            if (!matches(door, tag) || (prefix.equals("outside") && !complete(door))) return true;
        }
        return false;
    }
    public static boolean allows(Portal portal, Entity entity) {
        if (!(portal.level() instanceof ServerLevel level)) return true;
        // Immersive Portals predicts crossings on the client. A server-only native
        // door cooldown rejects otherwise valid rapid return trips after prediction.
        if (!valid(portal, true)) return false;
        CompoundTag tag = portal.getPersistentData().getCompound(DATA);
        // Exiting retains Nomadic Tents' unconditional exit permission.
        if (isTentLevel(level)) return true;
        var door = anchor(level.getServer(), tag, "outside", true);
        return door != null && door.canEnter(entity).isAllow();
    }
    @SubscribeEvent public void tick(TickEvent.ServerTickEvent e) {
        if (e.phase != TickEvent.Phase.END || !ready) return;
        MinecraftServer server = e.getServer();
        finishSync(server);
        if (++ticks % 20 != 0) return;
        // Scan only already-loaded chunks immediately around online players.
        Set<TentDoorBlockEntity> seen = new HashSet<>();
        for (var player : server.getPlayerList().getPlayers()) {
            ServerLevel level = player.serverLevel();
            if (!ENABLED.get() || isTentLevel(level)) continue;
            int cx = player.chunkPosition().x, cz = player.chunkPosition().z;
            for (int x = cx - 1; x <= cx + 1; x++) for (int z = cz - 1; z <= cz + 1; z++) {
                var chunk = level.getChunkSource().getChunkNow(x, z);
                if (chunk == null) continue;
                for (BlockEntity be : List.copyOf(chunk.getBlockEntities().values())) {
                    if (be instanceof TentDoorBlockEntity door && !seen.contains(door)
                        && door.getBlockPos().distSqr(player.blockPosition()) <= 32 * 32
                        && !hasLivePortal(door) && door.canEnter(player).isAllow() && seen.add(door)) open(door, player);
                }
            }
        }
        List<Portal> stale = new ArrayList<>();
        for (ServerLevel level : server.getAllLevels()) for (Entity entity : level.getAllEntities()) {
            if (entity instanceof Portal p && managed(p) && stale(p)) stale.add(p);
        }
        stale.forEach(Entity::discard);
    }
}
