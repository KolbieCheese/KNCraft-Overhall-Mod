package com.beautyinblocks.tenttests;
import com.beautyinblocks.tents.TentPortals;

import com.mojang.authlib.GameProfile;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import nomadictents.NTConfig;
import nomadictents.structure.TentPlacer;
import nomadictents.tileentity.TentDoorBlockEntity;
import nomadictents.util.Tent;
import nomadictents.util.TentType;
import nomadictents.util.TentSize;
import qouteall.imm_ptl.core.portal.Portal;
import qouteall.q_misc_util.dimension.DimensionIdRecord;
import java.util.UUID;

@Mod("kncrafttenttests")
public class TentTests {
    private static UUID traveler;
    private static net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> destination;
    public TentTests() { MinecraftForge.EVENT_BUS.register(this); }
    static void check(boolean yes, String message) { if (!yes) throw new IllegalStateException(message); }
    @SubscribeEvent public void commands(RegisterCommandsEvent e) {
        e.getDispatcher().register(Commands.literal("kncrafttentacktest").requires(s -> s.hasPermission(4)).executes(c -> {
            var server=c.getSource().getServer();
            var level=server.overworld();
            var player=FakePlayerFactory.get(level,new GameProfile(UUID.randomUUID(),"TentSyncTest"));
            java.util.Map<UUID,net.minecraft.server.level.ServerPlayer> byId=net.minecraftforge.fml.util.ObfuscationReflectionHelper.getPrivateValue(net.minecraft.server.players.PlayerList.class,server.getPlayerList(),"f_11197_");
            java.util.List<net.minecraft.server.level.ServerPlayer> players=net.minecraftforge.fml.util.ObfuscationReflectionHelper.getPrivateValue(net.minecraft.server.players.PlayerList.class,server.getPlayerList(),"f_11196_");
            var packets=new java.util.ArrayList<net.minecraft.network.protocol.Packet<?>>();
            var testChannel=new io.netty.channel.embedded.EmbeddedChannel();
            var testConnection=new net.minecraft.network.Connection(net.minecraft.network.protocol.PacketFlow.SERVERBOUND) {
                @Override public io.netty.channel.Channel channel() {return testChannel;}
                @Override public void send(net.minecraft.network.protocol.Packet<?> packet) {packets.add(packet);}
                @Override public void send(net.minecraft.network.protocol.Packet<?> packet, net.minecraft.network.PacketSendListener listener) {packets.add(packet);}
            };
            player.connection=new net.minecraft.server.network.ServerGamePacketListenerImpl(server,testConnection,player) {
                @Override public void send(net.minecraft.network.protocol.Packet<?> packet) {packets.add(packet);}
            };
            players.add(player);byId.put(player.getUUID(),player);
            try {
                var door=place(level,new BlockPos(1200,150,1100),new Tent(2000000+Math.floorMod(UUID.randomUUID().hashCode(),1000000),TentType.YURT,TentSize.TINY),Direction.EAST);
                check(TentPortals.create(door)==null,"New portal exposed before client acknowledgement");
                check(!TentPortals.linked(door),"Pending dimension created a door link");
                int ping=packets.stream().filter(p -> p instanceof net.minecraft.network.protocol.game.ClientboundPingPacket).mapToInt(p -> ((net.minecraft.network.protocol.game.ClientboundPingPacket)p).getId()).reduce((a,b)->b).orElseThrow();
                check(packets.size()>=2,"Dimension sync was not sent before ping");
                player.connection.handlePong(new net.minecraft.network.protocol.game.ServerboundPongPacket(ping+1));
                check(TentPortals.create(door)==null,"Incorrect pong released dimension gate");
                TentPortals.syncDimensions(server);
                int latestPing=packets.stream().filter(p -> p instanceof net.minecraft.network.protocol.game.ClientboundPingPacket).mapToInt(p -> ((net.minecraft.network.protocol.game.ClientboundPingPacket)p).getId()).reduce((a,b)->b).orElseThrow();
                player.connection.handlePong(new net.minecraft.network.protocol.game.ServerboundPongPacket(ping));
                check(TentPortals.create(door)==null,"Stale pong released newer registry gate");
                player.connection.handlePong(new net.minecraft.network.protocol.game.ServerboundPongPacket(latestPing));
                check(TentPortals.create(door)!=null,"Correct client pong failed to release gate");
                c.getSource().sendSuccess(()->Component.literal("CLIENT ACKNOWLEDGEMENT GATE PASSED"),false);return 1;
            } catch(Throwable ex) {ex.printStackTrace();c.getSource().sendFailure(Component.literal("ACK TEST FAILED: "+ex));return 0;}
            finally {players.remove(player);byId.remove(player.getUUID());testChannel.finishAndReleaseAll();}
        }));
        e.getDispatcher().register(Commands.literal("kncrafttenttraverse").requires(s -> s.hasPermission(4)).executes(c -> {
            try {
                var level = c.getSource().getServer().overworld();
                var door = (TentDoorBlockEntity)level.getBlockEntity(new BlockPos(1000,150,1100));
                var portal = (Portal)level.getEntity(door.getPersistentData().getCompound(TentPortals.DATA).getUUID("portal"));
                var cow = net.minecraft.world.entity.EntityType.COW.create(level);
                cow.setNoAi(true); cow.setNoGravity(true); cow.setInvulnerable(true);
                var destLevel=level.getServer().getLevel(portal.dimensionTo);
                for(int x=-1;x<=1;x++) for(int z=-1;z<=1;z++) destLevel.setChunkForced(x,z,true);
                Vec3 start = portal.getOriginPos().add(portal.getNormal().scale(.3)).add(0,-.8,0);
                cow.setPos(start); cow.xo=start.x; cow.yo=start.y; cow.zo=start.z; cow.xOld=start.x; cow.yOld=start.y; cow.zOld=start.z;
                check(level.addFreshEntity(cow),"Failed to spawn test cow");
                cow.setPos(start.subtract(portal.getNormal().scale(.4)));
                check(qouteall.imm_ptl.core.teleportation.ServerTeleportationManager.shouldEntityTeleport(portal,cow),"Crossing was not recognized");
                traveler=cow.getUUID();destination=portal.dimensionTo;
                qouteall.imm_ptl.core.IPGlobal.serverTeleportationManager.startTeleportingRegularEntity(portal,cow);
                c.getSource().sendSuccess(()->Component.literal("TRAVERSAL STARTED"),false);return 1;
            } catch(Throwable ex){ex.printStackTrace();c.getSource().sendFailure(Component.literal("TRAVERSAL FAILED: "+ex));return 0;}
        }));
        e.getDispatcher().register(Commands.literal("kncrafttentreturn").requires(s -> s.hasPermission(4)).executes(c -> {
            try {
                var level=c.getSource().getServer().getLevel(destination);
                var cow=level.getEntity(traveler);
                if(cow==null) for(var diagnosticLevel:level.getServer().getAllLevels()) for(var entity:diagnosticLevel.getAllEntities()) if(entity instanceof net.minecraft.world.entity.animal.Cow) System.out.println("COW LOCATION "+entity.getUUID()+" "+diagnosticLevel.dimension()+" "+entity.position());
                check(cow!=null,"Traveler never reached tent dimension: "+traveler);
                var door=(TentDoorBlockEntity)level.getBlockEntity(new BlockPos(0,64,0));
                var portal=(Portal)level.getEntity(door.getPersistentData().getCompound(TentPortals.DATA).getUUID("portal"));
                check(!cow.isOnPortalCooldown(),"Crossing incorrectly applied native cooldown");
                Vec3 start=portal.getOriginPos().add(portal.getNormal().scale(.3)).add(0,-.8,0);
                cow.setPos(start);cow.xo=start.x;cow.yo=start.y;cow.zo=start.z;cow.xOld=start.x;cow.yOld=start.y;cow.zOld=start.z;
                cow.setPos(start.subtract(portal.getNormal().scale(.4)));
                check(qouteall.imm_ptl.core.teleportation.ServerTeleportationManager.shouldEntityTeleport(portal,cow),"Exit crossing not recognized");
                qouteall.imm_ptl.core.IPGlobal.serverTeleportationManager.startTeleportingRegularEntity(portal,cow);
                c.getSource().sendSuccess(()->Component.literal("EXIT TRAVERSAL STARTED"),false);return 1;
            } catch(Throwable ex){ex.printStackTrace();c.getSource().sendFailure(Component.literal("EXIT TRAVERSAL FAILED: "+ex));return 0;}
        }));
        e.getDispatcher().register(Commands.literal("kncrafttentverify").requires(s -> s.hasPermission(4)).executes(c -> {
            var cow=c.getSource().getServer().overworld().getEntity(traveler);
            check(cow!=null,"Traveler failed to return to overworld");cow.discard();
            c.getSource().sendSuccess(()->Component.literal("ROUND-TRIP ENTITY TRAVERSAL PASSED"),false);return 1;
        }));
        e.getDispatcher().register(Commands.literal("kncrafttenttest").requires(s -> s.hasPermission(4))
            .executes(c -> {
                try { run(c.getSource().getServer().overworld()); c.getSource().sendSuccess(() -> Component.literal("TENT TESTS PASSED"), false); return 1; }
                catch (Throwable ex) { ex.printStackTrace(); c.getSource().sendFailure(Component.literal("TENT TESTS FAILED: " + ex)); return 0; }
            }));
        e.getDispatcher().register(Commands.literal("kncrafttentpersist").requires(s -> s.hasPermission(4))
            .executes(c -> {
                try {
                    ServerLevel level = c.getSource().getServer().overworld();
                    for (int i=0;i<4;i++) {
                        BlockPos pos = new BlockPos(1000+i*40,150,1100);
                        level.getChunk(pos);
                        var door = (TentDoorBlockEntity)level.getBlockEntity(pos);
                        check(door != null, "Saved exterior door missing");
                        // Entity chunks need one tick to finish loading; test after forceload command.
                        check(TentPortals.hasLivePortal(door), "Saved paired portal missing for " + i);
                    }
                    c.getSource().sendSuccess(() -> Component.literal("PERSISTENCE TEST PASSED"),false); return 1;
                } catch(Throwable ex) {ex.printStackTrace();c.getSource().sendFailure(Component.literal("PERSISTENCE TEST FAILED: "+ex));return 0;}
            }));
    }
    static TentDoorBlockEntity place(ServerLevel level, BlockPos pos, Tent tent, Direction direction) {
        level.getChunk(pos);
        check(TentPlacer.getInstance().placeTent(level,pos,tent.getType(),TentPlacer.getOverworldSize(tent.getSize()),direction,tent.getColor()), "Template placement failed");
        var door=(TentDoorBlockEntity)level.getBlockEntity(pos);
        check(door!=null,"Exterior door missing");
        door.setTent(tent);door.setDirection(direction);
        check(TentPortals.complete(door),"Exterior not complete");
        return door;
    }
    static void run(ServerLevel level) {
        Direction[] dirs={Direction.EAST,Direction.SOUTH,Direction.WEST,Direction.NORTH};
        for(int i=0;i<4;i++) {
            // Different IDs per test invocation force genuine runtime dimension creation.
            int id=100000+Math.floorMod(UUID.randomUUID().hashCode(),1000000);
            Tent tent=new Tent(id,TentType.YURT,TentSize.TINY);
            BlockPos pos=new BlockPos(1000+i*40,150,1000);
            var door=place(level,pos,tent,dirs[i]);
            Portal portal=TentPortals.create(door);
            check(DimensionIdRecord.serverRecord.getIntId(portal.dimensionTo)>=0,"Fresh dimension has no IP integer ID");
            var buf=new net.minecraft.network.FriendlyByteBuf(io.netty.buffer.Unpooled.buffer());
            try {
                qouteall.q_misc_util.dimension.DimId.writeWorldId(buf,portal.dimensionTo,false);
                check(qouteall.q_misc_util.dimension.DimId.readWorldId(buf,false).equals(portal.dimensionTo),"Dimension packet failed round trip");
                buf.clear();new qouteall.q_misc_util.forge.networking.Dim_Sync().toBytes(buf);
                check(buf.readableBytes()>0,"Dimension sync packet was empty");
            } finally {buf.release();}
            check(TentPortals.valid(portal,true),"New portal is invalid");
            check(portal.getNormal().dot(Vec3.atLowerCornerOf(dirs[i].getNormal())) < -.999,"Entrance normal incorrect");
            check(portal.transformLocalVec(Vec3.atLowerCornerOf(dirs[i].getNormal())).distanceTo(new Vec3(1,0,0))<.001,"Entrance rotation incorrect");
            var insideLevel=level.getServer().getLevel(portal.dimensionTo);
            var insidePos=Tent.calculatePos(id);
            var inside=(TentDoorBlockEntity)insideLevel.getBlockEntity(insidePos);
            var reverse=(Portal)insideLevel.getEntity(inside.getPersistentData().getCompound(TentPortals.DATA).getUUID("portal"));
            check(reverse!=null && reverse.getNormal().distanceTo(new Vec3(1,0,0))<.001,"Exit normal incorrect");
            check(reverse.transformPoint(portal.transformPoint(portal.getOriginPos().add(.1,.2,.3))).distanceTo(portal.getOriginPos().add(.1,.2,.3))<.001,"Round-trip transformation failed");
            var player=FakePlayerFactory.get(level,new GameProfile(UUID.randomUUID(),"TentTest"));
            player.setGameMode(GameType.SURVIVAL);player.setPos(pos.getX(),pos.getY(),pos.getZ());
            boolean ownership=NTConfig.CONFIG.OWNER_ONLY_ENTER.get();
            boolean safe=NTConfig.CONFIG.ENTER_WHEN_SAFE.get();
            try {
                NTConfig.CONFIG.OWNER_ONLY_ENTER.set(true);NTConfig.CONFIG.ENTER_WHEN_SAFE.set(false);
                door.setOwner(UUID.randomUUID());
                check(!portal.canTeleportEntity(player),"Nonowner bypassed portal permission");
                door.setOwner(player.getUUID());
                check(portal.canTeleportEntity(player),"Owner could not enter portal");
                portal.onEntityTeleportedOnServer(player);
                check(!player.isOnPortalCooldown(),"Seamless crossing applied an unsynchronized cooldown");
                net.minecraftforge.fml.util.ObfuscationReflectionHelper.setPrivateValue(net.minecraft.world.entity.Entity.class,player,60,"f_19839_");
                check(portal.canTeleportEntity(player),"Native cooldown still rejects seamless return trips");
                net.minecraftforge.fml.util.ObfuscationReflectionHelper.setPrivateValue(net.minecraft.world.entity.Entity.class,player,0,"f_19839_");
                NTConfig.CONFIG.ENTER_WHEN_SAFE.set(true);
                var zombie=net.minecraft.world.entity.EntityType.ZOMBIE.create(level);
                zombie.setPos(Vec3.atBottomCenterOf(pos.relative(dirs[i].getOpposite(),2)));level.addFreshEntity(zombie);
                check(!portal.canTeleportEntity(player),"Nearby monster restriction was bypassed");
                zombie.discard();NTConfig.CONFIG.ENTER_WHEN_SAFE.set(false);
                // The door mixin must leave native onEnter callers in their original world.
                door.onEnter(player);
                check(player.level()==level,"Native teleport was not intercepted");
            } finally {NTConfig.CONFIG.OWNER_ONLY_ENTER.set(ownership);NTConfig.CONFIG.ENTER_WHEN_SAFE.set(safe);}
            // Preserve a content marker when the same tent is packed and rebuilt elsewhere.
            BlockPos marker=insidePos.relative(Direction.EAST,2).above();
            insideLevel.setBlockAndUpdate(marker,Blocks.DIAMOND_BLOCK.defaultBlockState());
            TentPlacer.getInstance().removeTent(level,pos,tent.getType(),TentPlacer.getOverworldSize(tent.getSize()),dirs[i]);
            check(!TentPortals.valid(portal,true),"Packed tent portal remained valid");
            check(!portal.canTeleportEntity(player),"Packed tent still usable");
            var moved=place(level,new BlockPos(1000+i*40,150,1100),tent,dirs[i]);
            Portal replacement=TentPortals.create(moved);
            check(replacement.dimensionTo.equals(portal.dimensionTo),"Moved tent lost its dimension");
            check(insideLevel.getBlockState(marker).is(Blocks.DIAMOND_BLOCK),"Moving tent destroyed its contents");
            check(!TentPortals.valid(portal,true),"Old entrance remained linked after move");
            check(TentPortals.valid(replacement,true),"Moved tent portal invalid");
            System.out.println("TENT TEST PASSED: "+dirs[i]+" dimension="+replacement.dimensionTo.location());
        }
    }
}
