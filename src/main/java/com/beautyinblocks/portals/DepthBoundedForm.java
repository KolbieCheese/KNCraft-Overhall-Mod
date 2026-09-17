package com.beautyinblocks.portals;

import com.mojang.serialization.Codec;
import net.minecraft.core.*;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.*;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.registries.ForgeRegistries;
import qouteall.imm_ptl.core.portal.custom_portal_gen.PortalGenInfo;
import qouteall.imm_ptl.core.portal.custom_portal_gen.form.*;
import qouteall.imm_ptl.core.portal.nether_portal.BlockPortalShape;
import qouteall.q_misc_util.my_util.IntBox;
import java.util.*;
import java.util.function.Function;

/** Bounded arrival placement for the audited Call From The Depth 1.22.1 terrain (0..127).
 * Bedrock can occupy 0..4 and 123..127. Every edited block remains in 5..122.
 * Deliberately registered as a distinct datapack form: other portal generators are unaffected.
 */
public final class DepthBoundedForm extends ClassicalForm {
 public static final int MIN=5, MAX=122;
 public static final Codec<DepthBoundedForm> CODEC=com.mojang.serialization.codecs.RecordCodecBuilder.create(i->i.group(
  net.minecraft.core.registries.BuiltInRegistries.BLOCK.byNameCodec().fieldOf("from_frame_block").forGetter((DepthBoundedForm f)->f.fromFrameBlock),
  net.minecraft.core.registries.BuiltInRegistries.BLOCK.byNameCodec().fieldOf("area_block").forGetter((DepthBoundedForm f)->f.areaBlock),
  net.minecraft.core.registries.BuiltInRegistries.BLOCK.byNameCodec().fieldOf("to_frame_block").forGetter((DepthBoundedForm f)->f.toFrameBlock),
  Codec.BOOL.fieldOf("generate_frame_if_not_found").forGetter((DepthBoundedForm f)->f.generateFrameIfNotFound)
 ).apply(i,DepthBoundedForm::new));
 private static final Set<String> TERRAIN=Set.of("minecraft:stone","minecraft:deepslate","minecraft:tuff","minecraft:sculk","minecraft:gravel","minecraft:dirt","minecraft:coarse_dirt","minecraft:rooted_dirt",
  "callfromthedepth_:deepstone","callfromthedepth_:heatedstone","callfromthedepth_:sporedheatedstone","callfromthedepth_:rotteddepthstone","callfromthedepth_:lostsoulssoil","callfromthedepth_:deepgrass");
 public DepthBoundedForm(Block from,Block area,Block to,boolean generate){super(from,area,to,generate);}
 @Override public Codec<? extends PortalGenForm> getCodec(){return CODEC;}
 @Override public PortalGenForm getReverse(){return new DepthBoundedForm(toFrameBlock,areaBlock,fromFrameBlock,generateFrameIfNotFound);}
 public static boolean isDepth(ServerLevel level){return level.dimension().location().toString().equals("callfromthedepth_:depth");}
 public static boolean inBounds(IntBox box){return box.l.getY()>=MIN&&box.h.getY()<=MAX;}
 public static IntBox room(IntBox frame,Direction.Axis axis){return axis==Direction.Axis.X?frame.getAdjusted(-2,-1,0,2,1,0):frame.getAdjusted(0,-1,-2,0,1,2);}
 @Override public boolean testThisSideShape(ServerLevel world,BlockPortalShape shape){
  // Vertical frames only; keep full frame plus landing floor and headroom inside safe band.
  return shape.axis!=Direction.Axis.Y && shape.totalAreaBox.getSize().getY()+2<=MAX-MIN+1
   && shape.totalAreaBox.getSize().getX()<=64 && shape.totalAreaBox.getSize().getZ()<=64
   && (!isDepth(world)||inBounds(room(shape.totalAreaBox,shape.axis)));
 }
 @Override public Function<WorldGenRegion,Function<BlockPos.MutableBlockPos,PortalGenInfo>> getFrameMatchingFunc(ServerLevel from,ServerLevel to,BlockPortalShape source){
  var original=super.getFrameMatchingFunc(from,to,source);
  return region->{var match=original.apply(region);return pos->{
   var info=match.apply(pos);
   // Do not reuse the broken legacy frames above the roof or below the floor.
   return info!=null&&isDepth(to)&&!inBounds(room(info.toShape.totalAreaBox,info.toShape.axis))?null:info;
  };};
 }
 @Override public PortalGenInfo getNewPortalPlacement(ServerLevel to,BlockPos mapped,ServerLevel from,BlockPortalShape source,Entity entity){
  if(!isDepth(to))return super.getNewPortalPlacement(to,mapped,from,source,entity);
  if(!to.getServer().isSameThread())throw new IllegalStateException("Depth placement must run on server thread");
  if(!testThisSideShape(from,source))return fail(from,source,entity);
  BlockPos size=source.totalAreaBox.getSize();
  int minBase=MIN+1,maxBase=MAX-size.getY();
  if(maxBase<minBase)return fail(from,source,entity);
  int targetY=Mth.clamp(mapped.getY(),Math.min(34,maxBase),maxBase);
  List<Integer> heights=new ArrayList<>();for(int y=minBase;y<=maxBase;y++)heights.add(y);
  heights.sort(Comparator.comparingInt(y->Math.abs(y-targetY)));
  List<BlockPos> offsets=new ArrayList<>();for(int x=-16;x<=16;x++)for(int z=-16;z<=16;z++)offsets.add(new BlockPos(x,0,z));
  offsets.sort(Comparator.comparingInt(p->p.getX()*p.getX()+p.getZ()*p.getZ()));
  IntBox carveCandidate=null;
  for(BlockPos offset:offsets)for(int y:heights){
   BlockPos base=new BlockPos(mapped.getX()-size.getX()/2+offset.getX(),y,mapped.getZ()-size.getZ()/2+offset.getZ());
   IntBox frame=IntBox.fromBasePointAndSize(base,size), chamber=room(frame,source.axis);
   if(!inBounds(chamber)||!loaded(to,chamber))continue;
   // Corner tests cheaply reject solid regions before a volume walk.
   boolean air=true;for(BlockPos p:frame.getEightVertices())if(!to.isEmptyBlock(p)){air=false;break;}
   if(air&&clearRoom(to,chamber))return place(to,from,source,frame,chamber);
   // Retain the nearest natural-terrain fallback, while still preferring an existing opening.
   if(carveCandidate==null&&Math.abs(offset.getX())<=8&&Math.abs(offset.getZ())<=8&&canExcavate(to,chamber))carveCandidate=frame;
  }
  if(carveCandidate!=null)return place(to,from,source,carveCandidate,room(carveCandidate,source.axis));
  return fail(from,source,entity);
 }
 private static boolean loaded(ServerLevel level,IntBox box){
  for(int x=box.l.getX()>>4;x<=box.h.getX()>>4;x++)for(int z=box.l.getZ()>>4;z<=box.h.getZ()>>4;z++)if(level.getChunkSource().getChunkNow(x,z)==null)return false;
  return true;
 }
 private static boolean editable(ServerLevel level,BlockPos p){
  var s=level.getBlockState(p);
  return !s.hasBlockEntity()&&s.getFluidState().isEmpty()&&(s.isAir()||TERRAIN.contains(String.valueOf(ForgeRegistries.BLOCKS.getKey(s.getBlock()))));
 }
 private static boolean unoccupied(ServerLevel level,IntBox box){return level.getEntitiesOfClass(Entity.class,new AABB(box.l,box.h.offset(1,1,1))).isEmpty();}
 private static boolean clearRoom(ServerLevel level,IntBox box){
  if(!unoccupied(level,box))return false;
  for(BlockPos p:BlockPos.betweenClosed(box.l,box.h)){
   if(p.getY()==box.l.getY()){if(!editable(level,p))return false;}
   else if(!level.isEmptyBlock(p))return false;
  }return true;
 }
 private static boolean canExcavate(ServerLevel level,IntBox box){
  if(!unoccupied(level,box))return false;
  for(BlockPos p:BlockPos.betweenClosed(box.l,box.h))if(!editable(level,p))return false;
  return true;
 }
 private PortalGenInfo place(ServerLevel to,ServerLevel from,BlockPortalShape source,IntBox frame,IntBox chamber){
  // Revalidate all edits immediately; never overwrite bedrock, fluids, block entities or an existing frame.
  if(!inBounds(chamber)||!canExcavate(to,chamber))return null;
  for(BlockPos p:BlockPos.betweenClosed(chamber.l,chamber.h))to.setBlock(p,p.getY()==chamber.l.getY()?Blocks.DEEPSLATE.defaultBlockState():Blocks.AIR.defaultBlockState(),18);
  var target=source.getShapeWithMovedTotalAreaBox(frame);
  com.mojang.logging.LogUtils.getLogger().info("Bounded Depth portal frame {} to {}; landing chamber {} to {}",frame.l,frame.h,chamber.l,chamber.h);
  return new PortalGenInfo(from.dimension(),to.dimension(),source,target);
 }
 private PortalGenInfo fail(ServerLevel from,BlockPortalShape shape,Entity trigger){
  // IP clears native interior before the asynchronous search. Restore it on a safe failure.
  if(shape.isFrameIntact(p->from.getBlockState(p).getBlock()==fromFrameBlock)){
   var state=areaBlock.defaultBlockState();
   if(state.hasProperty(BlockStateProperties.HORIZONTAL_AXIS))state=state.setValue(BlockStateProperties.HORIZONTAL_AXIS,shape.axis==Direction.Axis.X?Direction.Axis.Z:Direction.Axis.X);
   for(BlockPos p:shape.area)if(from.isEmptyBlock(p))from.setBlock(p,state,18);
  }
  if(trigger instanceof ServerPlayer player)player.sendSystemMessage(Component.literal("No safe room for this Depth portal was found below the bedrock roof. Native portal restored."));
  return null;
 }
}
