package com.beautyinblocks.portals;

import com.google.common.collect.Multimap;
import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;
import java.lang.reflect.*;
import java.util.*;


public final class NativePortalLighting {
 private static final Logger LOG=LogUtils.getLogger();
 private static final TagKey<Item> AETHER_ACTIVATORS=TagKey.create(Registries.ITEM,new ResourceLocation("aether:aether_portal_activation_items"));
 private record Pending(ServerLevel world,BlockPos pos,String portal,String generator,long expires){}
 private final Map<String,Pending> pending=new LinkedHashMap<>();
 private Field generators,identifier;private Method perform;private boolean adapterFailed;
 public NativePortalLighting(){MinecraftForge.EVENT_BUS.register(this);
  net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
 }
 private void setup(net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent event){
  event.enqueueWork(()->net.minecraft.core.Registry.register(qouteall.imm_ptl.core.portal.custom_portal_gen.form.PortalGenForm.codecRegistry,new ResourceLocation("kncraft:bounded_depth"),DepthBoundedForm.CODEC));
 }
 // Observe before Aether's NORMAL-priority listener fills the portal and cancels the event.
 // We never cancel an event, consume an item, place a native block, or light an invalid frame.
 @SubscribeEvent(priority=EventPriority.HIGHEST)
 public void onUse(PlayerInteractEvent.RightClickBlock event){
  if(!com.beautyinblocks.kncraft.core.ArchitectureConfig.NATIVE_PORTALS.get()||!(event.getLevel() instanceof ServerLevel world)||event.getFace()==null||adapterFailed)return;
  String dimension=world.dimension().location().toString();String portal,generator;
  var stack=event.getItemStack();String item=String.valueOf(ForgeRegistries.ITEMS.getKey(stack.getItem()));
  if((stack.is(Items.WATER_BUCKET)||stack.is(AETHER_ACTIVATORS))&&(dimension.equals("minecraft:overworld")||dimension.equals("aether:the_aether"))){portal="aether:aether_portal";generator="kncraft_aether:aether";}
  else if(item.equals("callfromthedepth_:depth")&&(dimension.equals("minecraft:overworld")||dimension.equals("callfromthedepth_:depth"))){portal="callfromthedepth_:depth_portal";generator="kncraft_depth:depth";}
  else return;
  BlockPos pos=event.getPos().relative(event.getFace()).immutable();
  if(!world.hasChunkAt(pos)||isPortal(world,pos,portal))return; // Only a newly lit portal, not a cancelled click on an old one.
  if(pending.size()>=256)return;
  pending.put(dimension+":"+pos, new Pending(world,pos,portal,generator,world.getGameTime()+10));
 }
 private boolean isPortal(ServerLevel world,BlockPos pos,String id){return id.equals(String.valueOf(ForgeRegistries.BLOCKS.getKey(world.getBlockState(pos).getBlock())));}
 @SubscribeEvent public void tick(TickEvent.ServerTickEvent event){
  if(event.phase!=TickEvent.Phase.END||pending.isEmpty())return;
  var it=pending.values().iterator();
  while(it.hasNext()){
   var p=it.next();if(p.world.getGameTime()>p.expires||!p.world.hasChunkAt(p.pos)){it.remove();continue;}
   if(!isPortal(p.world,p.pos,p.portal))continue;
   it.remove();
   try{
    if(start(p))LOG.info("Native portal lighting accepted for {} in {} at {}",p.generator,p.world.dimension().location(),p.pos);
    else LOG.warn("No matching immersive generation accepted {} at {}; native portal preserved, stick fallback remains available",p.generator,p.pos);
   }catch(ReflectiveOperationException|RuntimeException ex){adapterFailed=true;LOG.error("Native portal integration disabled after API failure; native portals and stick activation remain available",ex);pending.clear();break;}
  }
 }
 private boolean start(Pending p)throws ReflectiveOperationException{
  if(generators==null){
   Class<?> manager=Class.forName("qouteall.imm_ptl.core.portal.custom_portal_gen.CustomPortalGenManagement");
   Class<?> generation=Class.forName("qouteall.imm_ptl.core.portal.custom_portal_gen.CustomPortalGeneration");
   generators=manager.getDeclaredField("useItemGen");generators.setAccessible(true);
   identifier=generation.getField("identifier");perform=generation.getMethod("perform",ServerLevel.class,BlockPos.class,Entity.class);
  }
  // Read the live registry each time: /reload replaces definitions. Do not cache a stale generator.
  Multimap<?,?> registry=(Multimap<?,?>)generators.get(null);
  for(Object gen:registry.values())if(p.generator.equals(String.valueOf(identifier.get(gen)))&&Boolean.TRUE.equals(perform.invoke(gen,p.world,p.pos,null)))return true;
  return false;
 }
 @SubscribeEvent public void stopped(ServerStoppedEvent e){pending.clear();adapterFailed=false;}
}
