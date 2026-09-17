package com.beautyinblocks.test;
import com.beautyinblocks.portals.DepthBoundedForm;
import net.minecraft.commands.Commands;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.*;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.*;
import net.minecraft.world.level.block.*;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import qouteall.imm_ptl.core.portal.nether_portal.BlockPortalShape;
import java.util.*;
@Mod("depthheighttests") public class DepthHeightTests {
 public DepthHeightTests(){MinecraftForge.EVENT_BUS.register(this);}
 void check(boolean b,String s){if(!b)throw new IllegalStateException(s);}
 @SubscribeEvent public void commands(RegisterCommandsEvent e){e.getDispatcher().register(Commands.literal("depthheighttests").requires(s->s.getEntity()==null&&!s.getServer().usesAuthentication()).executes(c->{try{
 var server=c.getSource().getServer();var ow=server.overworld();var depth=server.getLevel(ResourceKey.create(Registries.DIMENSION,new ResourceLocation("callfromthedepth_:depth")));
 var block=ForgeRegistries.BLOCKS.getValue(new ResourceLocation("callfromthedepth_:depth_portal"));
 var form=new DepthBoundedForm(Blocks.REINFORCED_DEEPSLATE,block,Blocks.REINFORCED_DEEPSLATE,true);
 check(form.getReverse() instanceof DepthBoundedForm,"reverse loses bounded form");
 for(int x=38;x<=42;x++)for(int z=38;z<=42;z++)depth.getChunk(x,z);
 int count=0;
 for(var axis:List.of(Direction.Axis.X,Direction.Axis.Z))for(int y:List.of(-200,240)){
 Set<BlockPos> area=new HashSet<>();for(int a=0;a<20;a++)for(int b=0;b<6;b++)area.add(axis==Direction.Axis.X?new BlockPos(0,100+b,a):new BlockPos(a,100+b,0));
 var source=new BlockPortalShape(area,axis);
 check(form.testThisSideShape(ow,source),"large source rejected");
 // Clear a deterministic chamber in the isolated test world, preserving actual bedrock limits.
 for(BlockPos p:BlockPos.betweenClosed(620,0,620,660,127,660))depth.setBlock(p,(p.getY()<5||p.getY()>122?Blocks.BEDROCK:Blocks.AIR).defaultBlockState(),18);
 long start=System.nanoTime();var info=form.getNewPortalPlacement(depth,new BlockPos(640,y,640),ow,source,null);
 check(info!=null,"no bounded placement");var room=DepthBoundedForm.room(info.toShape.totalAreaBox,axis);
 check(DepthBoundedForm.inBounds(room),"unsafe landing chamber "+room);check(info.toShape.area.size()==120,"portal size changed");
 form.generateNewFrame(ow,source,depth,info.toShape);
 for(BlockPos p:BlockPos.betweenClosed(620,0,620,660,127,660))if(p.getY()<5||p.getY()>122)check(depth.getBlockState(p).is(Blocks.BEDROCK),"bedrock changed");
 for(int shift:List.of(-200,200)){var unsafe=source.getShapeWithMovedTotalAreaBox(source.totalAreaBox.getMoved(new BlockPos(0,shift,0)));check(!form.testThisSideShape(depth,unsafe),"unsafe source accepted");}
 System.out.println("DEPTH HEIGHT PASS axis="+axis+" mappedY="+y+" frame="+info.toShape.totalAreaBox+" chamber="+room+" ms="+(System.nanoTime()-start)/1000000.0);count++;
 }
 c.getSource().sendSuccess(()->Component.literal("DEPTH HEIGHT TESTS PASSED: four large-frame placements, both orientations, high/negative targets, bedrock intact, unsafe sources rejected"),false);return 1;
 }catch(Throwable ex){ex.printStackTrace();c.getSource().sendFailure(Component.literal("DEPTH HEIGHT FAILURE: "+ex));return 0;}}));}
}