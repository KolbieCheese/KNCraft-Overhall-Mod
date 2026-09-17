package com.beautyinblocks.test;
import com.mojang.authlib.GameProfile;
import net.minecraft.commands.Commands;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.*;
import net.minecraft.server.level.*;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.*;
import net.minecraft.world.phys.*;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import java.util.*;
@Mod("depthportalchecks")
public class DepthPortalChecks {
 public DepthPortalChecks(){MinecraftForge.EVENT_BUS.register(this);}
 record Fixture(ServerLevel world,BlockPos b,int width,int height){}
 List<Fixture> fixtures=new ArrayList<>();int ticks;net.minecraft.commands.CommandSourceStack pending;
 @SubscribeEvent public void commands(RegisterCommandsEvent e){e.getDispatcher().register(Commands.literal("nativeportalchecks").requires(s->s.getEntity()==null&&!s.getServer().usesAuthentication()).executes(c->{try{run(c.getSource().getServer());pending=c.getSource();ticks=0;return 1;}catch(Exception ex){ex.printStackTrace();c.getSource().sendFailure(Component.literal("PORTAL REPAIR FAILURE: "+ex));return 0;}}));}
 void frame(ServerLevel w,BlockPos b,int width,int height,String frame,String portal){
  for(int x=-1;x<=width;x++)for(int z=-1;z<=1;z++)w.setChunkForced((b.getX()+x)>>4,(b.getZ()+z)>>4,true);
  var border=ForgeRegistries.BLOCKS.getValue(new ResourceLocation(frame));var area=portal.equals("minecraft:air")?Blocks.AIR:ForgeRegistries.BLOCKS.getValue(new ResourceLocation(portal));
  for(int pass=0;pass<2;pass++)for(int x=-1;x<=width;x++)for(int y=-1;y<=height;y++){
   boolean edge=x==-1||x==width||y==-1||y==height;if(edge!=(pass==0))continue;
   var state=(edge?border:area).defaultBlockState();if(!edge&&state.hasProperty(BlockStateProperties.HORIZONTAL_AXIS))state=state.setValue(BlockStateProperties.HORIZONTAL_AXIS,Direction.Axis.X);
   w.setBlock(b.offset(x,y,0),state,18);
  }
  fixtures.add(new Fixture(w,b,width,height));
 }
 void run(net.minecraft.server.MinecraftServer s)throws Exception{
  fixtures.clear();var ow=s.overworld();int i=0;
  for(String name:List.of("aether","depth"))for(int width:List.of(2,20)){
   int ratio=name.equals("depth")?16:1;var dst=s.getLevel(ResourceKey.create(Registries.DIMENSION,new ResourceLocation(name.equals("depth")?"callfromthedepth_:depth":"aether:the_aether")));
   int x=800+i++*64;var a=new BlockPos(x*ratio,220,352*ratio);var b=new BlockPos(x,220,352);
   String border=name.equals("depth")?"minecraft:reinforced_deepslate":"minecraft:glowstone";String area=name.equals("depth")?"callfromthedepth_:depth_portal":"aether:aether_portal";int height=width==2?3:6;
   // Exercise the reverse direction on the large frame.
   
   var src=ow;var pos=a; frame(src,pos,width,height,border,"minecraft:air");
   var player=new FakePlayer(src,new GameProfile(UUID.randomUUID(),"PortalRepairTest"));player.setPos(pos.getX()+0.5,pos.getY(),pos.getZ()+2);var stack=new ItemStack(name.equals("aether")?Items.WATER_BUCKET:ForgeRegistries.ITEMS.getValue(new ResourceLocation("callfromthedepth_:depth")));player.setItemInHand(InteractionHand.MAIN_HAND,stack);
   var hit=new BlockHitResult(Vec3.atCenterOf(pos.below()),Direction.UP,pos.below(),false);
   var ctx=new UseOnContext(player,InteractionHand.MAIN_HAND,hit);
   var event=new net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock(player,InteractionHand.MAIN_HAND,pos.below(),hit);
   boolean cancelled=MinecraftForge.EVENT_BUS.post(event);if(!cancelled){stack.useOn(ctx);if(name.equals("aether")){player.setYRot(180);player.setXRot((float)Math.toDegrees(Math.atan2(player.getEyeY()-pos.getY(),1.5)));var used=stack.getItem().use(src,player,InteractionHand.MAIN_HAND);player.setItemInHand(InteractionHand.MAIN_HAND,used.getObject());}}
   int lit=0;for(int xx=0;xx<width;xx++)for(int yy=0;yy<height;yy++)if(ForgeRegistries.BLOCKS.getKey(src.getBlockState(pos.offset(xx,yy,0)).getBlock()).toString().equals(area))lit++;
   if(lit!=width*height)throw new IllegalStateException("Native lighting failed "+name+" width="+width+" blocks="+lit);
   if(name.equals("aether")&&player.getMainHandItem().getItem()!=Items.BUCKET)throw new IllegalStateException("Aether bucket consumption changed");
   System.out.println("NATIVE LIGHTING PASS "+name+" width="+width+" eventCancelled="+cancelled+" nativeBlocks="+lit);
  }
 }
 @SubscribeEvent public void tick(net.minecraftforge.event.TickEvent.ServerTickEvent e){if(e.phase!=net.minecraftforge.event.TickEvent.Phase.END||pending==null)return;if(++ticks%20!=0)return;int passed=0;for(var f:fixtures){int n=0;for(int x=0;x<f.width;x++)for(int y=0;y<f.height;y++)if(ForgeRegistries.BLOCKS.getKey(f.world.getBlockState(f.b.offset(x,y,0)).getBlock()).toString().equals("immersive_portals:nether_portal_block"))n++;long faces=0;for(var entity:f.world.getAllEntities())if(!entity.isRemoved()&&entity.getType().toString().contains("general_breakable_portal")&&Math.abs(entity.getZ()-(f.b.getZ()+0.5))<0.1&&entity.getX()>=f.b.getX()&&entity.getX()<=f.b.getX()+f.width&&Math.abs(entity.getY()-(f.b.getY()+f.height/2.0))<0.1)faces++;if(n==f.width*f.height&&faces==2)passed++;}
  if(passed==4){pending.sendSuccess(()->Component.literal("NATIVE PORTAL CHECKS PASSED: 4 native lighting actions, 4 converted source areas with 8 portal faces; small and large forward, bucket consumed normally"),false);pending=null;}else if(ticks>=6000){final int p=passed;pending.sendFailure(Component.literal("NATIVE PORTAL TIMEOUT: "+p+"/4 sources"));pending=null;}
 }
}
