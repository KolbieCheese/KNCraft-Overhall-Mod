package com.beautyinblocks.perftests;
import com.beautyinblocks.performance.*;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.*;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import weather2.weathersystem.storm.*;
import weather2.config.ConfigTornado;
import java.util.*;
import java.lang.reflect.*;
@Mod("kncraftperformancetests")
public class PerformanceTests {
    public PerformanceTests(){if (!Boolean.getBoolean("kncraft.isolatedTests") || !java.nio.file.Files.isRegularFile(net.minecraftforge.fml.loading.FMLPaths.GAMEDIR.get().resolve("KNCraft-ISOLATED-TEST-WORLD"))) throw new IllegalStateException("Test harness requires an explicitly marked isolated server and -Dkncraft.isolatedTests=true"); MinecraftForge.EVENT_BUS.register(this);}
    static void check(boolean ok,String message){if(!ok)throw new IllegalStateException(message);}
    @SubscribeEvent public void commands(RegisterCommandsEvent event){
        event.getDispatcher().register(Commands.literal("kncraftperfprepare").requires(s->s.hasPermission(4)).executes(c->{
            var l=c.getSource().getServer().overworld();
            for(int x=7;x<=14;x++)for(int z=7;z<=14;z++){l.setChunkForced(x,z,true);l.getChunk(x,z);}
            c.getSource().sendSuccess(()->Component.literal("PERF FIXTURES PREPARED; wait for chunk visibility before running tests"),false);return 1;
        }));
        event.getDispatcher().register(Commands.literal("kncraftperftest").requires(s->s.hasPermission(4)).executes(c->{
            try {
                var level=c.getSource().getServer().overworld();
                minimumTests();portalTests(level);itemGoalTests(level);tornadoTests(level);
                c.getSource().sendSuccess(()->Component.literal("KNCRAFT PERFORMANCE: ALL REGRESSIONS PASSED"),false);return 1;
            }catch(Throwable t){t.printStackTrace();c.getSource().sendFailure(Component.literal("PERFORMANCE TEST FAILED: "+t));return 0;}
            finally{Performance.PORTAL_SEARCH.set(true);Performance.ITEM_SELECTION.set(true);Performance.TORNADO_QUERY.set(true);}
        }));
    }
    record Candidate(int order,double distance){}
    static void minimumTests(){
        Random r=new Random(20260916);var comp=Comparator.comparingDouble(Candidate::distance);
        for(int i=0;i<10000;i++){
            List<Candidate> a=new ArrayList<>();int size=r.nextInt(250);
            for(int j=0;j<size;j++)a.add(new Candidate(j,r.nextInt(20)));
            if(size>3 && i%7==0){a.set(0,new Candidate(0,Double.NaN));a.set(1,new Candidate(1,-0.0));a.set(2,new Candidate(2,0.0));}
            List<Candidate> sorted=new ArrayList<>(a);sorted.sort(comp);NearestSelection.moveMinimumFirst(a,comp);
            check(a.size()==sorted.size() && (a.isEmpty() || a.get(0)==sorted.get(0)),"Stable minimum differs");
        }
        System.out.println("PERF TEST: 10000 minimum-selection cases match stable sorting (ties, empty lists, NaN).");
    }
    static Object goal(ServerLevel level) throws Exception {
        return Class.forName("nonamecrackers2.witherstormmod.common.entity.goal.AvoidWitherStormGoal")
            .getConstructor(PathfinderMob.class,float.class,double.class,double.class)
            .newInstance(EntityType.COW.create(level),128f,1d,1.2d);
    }
    static BlockPos search(Object goal,Vec3 p) throws Exception {
        return (BlockPos)goal.getClass().getMethod("getNearestLoadedBlockPos",Vec3.class,int.class,net.minecraft.world.level.block.Block.class).invoke(goal,p,16,Blocks.NETHER_PORTAL);
    }
    static void portalTests(ServerLevel level) throws Exception {
        Object goal=goal(level);Random r=new Random(93423);int cases=0;
        for(Vec3 p:List.of(new Vec3(64.8,240.9,64.2),new Vec3(-64.8,239.1,-64.2),new Vec3(-.8,240,-.2))){
            for(int cx=(((int)p.x-16)>>4);cx<=(((int)p.x+16)>>4);cx++)for(int cz=(((int)p.z-16)>>4);cz<=(((int)p.z+16)>>4);cz++)level.getChunk(cx,cz);
            for(BlockPos q:BlockPos.betweenClosed((int)p.x-16,(int)p.y-4,(int)p.z-16,(int)p.x+16,(int)p.y+4,(int)p.z+16))level.setBlock(q,Blocks.AIR.defaultBlockState(),18);
            for(int pass=0;pass<45;pass++){
                List<BlockPos> placed=new ArrayList<>();
                for(int j=0;j<pass%9;j++){
                    BlockPos q=new BlockPos((int)p.x+r.nextInt(33)-16,(int)p.y+r.nextInt(9)-4,(int)p.z+r.nextInt(33)-16);
                    level.setBlock(q,Blocks.NETHER_PORTAL.defaultBlockState(),18);placed.add(q);
                }
                Performance.PORTAL_SEARCH.set(false);BlockPos expected=search(goal,p);
                Performance.PORTAL_SEARCH.set(true);BlockPos actual=search(goal,p);
                check(Objects.equals(expected,actual),"Portal order mismatch "+p+" "+expected+" vs "+actual);
                cases++;
                for(BlockPos q:placed)level.setBlock(q,Blocks.AIR.defaultBlockState(),18);
                check(search(goal,p)==null,"Removed portal remained cached");
            }
        }
        check(!PortalSearch.find(level,new Vec3(20000000,240,20000000),16,Blocks.NETHER_PORTAL).handled(),"Unloaded area did not fall back");
        check(!PortalSearch.find(level,new Vec3(0,-63,0),16,Blocks.NETHER_PORTAL).handled(),"Height edge did not fall back");
        check(!PortalSearch.find(level,new Vec3(0,240,0),16,Blocks.AIR).handled(),"Nonportal search did not fall back");
        Vec3 p=new Vec3(512.8,280.9,512.2);
        for(int cx=31;cx<=33;cx++)for(int cz=31;cz<=33;cz++)level.getChunk(cx,cz);
        check(PortalSearch.find(level,p,16,Blocks.NETHER_PORTAL).handled(),"Pristine no-portal case did not optimize");
        for(int i=0;i<100;i++){Performance.PORTAL_SEARCH.set(false);search(goal,p);Performance.PORTAL_SEARCH.set(true);search(goal,p);}
        long start=System.nanoTime();
        Performance.PORTAL_SEARCH.set(false);for(int i=0;i<300;i++)search(goal,p);long original=System.nanoTime()-start;
        Performance.PORTAL_SEARCH.set(true);start=System.nanoTime();for(int i=0;i<300;i++)search(goal,p);long optimized=System.nanoTime()-start;
        var result=PortalSearch.find(level,p,16,Blocks.NETHER_PORTAL);
        System.out.println("PERF TEST: "+cases+" live portal comparisons + immediate removal + fallbacks passed. 300 empty searches original="+original/1e6+"ms optimized="+optimized/1e6+"ms; blockChecks="+result.blockChecks()+" sectionChecks="+result.sectionChecks());
    }
    static Field field(Class<?> c,String name) throws Exception {Field f=c.getDeclaredField(name);f.setAccessible(true);return f;}
    static void itemGoalTests(ServerLevel level) throws Exception {
        var seagull=(PathfinderMob)ForgeRegistries.ENTITY_TYPES.getValue(new ResourceLocation("alexsmobs","seagull")).create(level);
        seagull.setPos(128,245,128);seagull.setNoAi(true);
        level.getChunk(8,8);List<Entity> spawned=new ArrayList<>();
        try{
            for(int i=0;i<80;i++){
                ItemEntity item=new ItemEntity(level,128+(i%9-4),245,128+(i/9-4),new ItemStack(Items.BREAD));item.tickCount=100;
                level.addFreshEntity(item);spawned.add(item);
            }
            Class<?> cls=Class.forName("com.github.alexthe666.alexsmobs.entity.ai.CreatureAITargetItems");
            Object goal=cls.getConstructor(PathfinderMob.class,boolean.class).newInstance(seagull,false);
            Performance.ITEM_SELECTION.set(false);field(cls,"mustUpdate").setBoolean(goal,true);
            check(((Goal)goal).canUse(),"Original item goal failed");Object expected=field(cls,"targetEntity").get(goal);
            Performance.ITEM_SELECTION.set(true);field(cls,"mustUpdate").setBoolean(goal,true);
            check(((Goal)goal).canUse(),"Optimized item goal failed");check(field(cls,"targetEntity").get(goal)==expected,"Live item target changed");
            ((Entity)expected).discard();
            Performance.ITEM_SELECTION.set(false);field(cls,"mustUpdate").setBoolean(goal,true);check(((Goal)goal).canUse(),"Original second target failed");expected=field(cls,"targetEntity").get(goal);
            Performance.ITEM_SELECTION.set(true);field(cls,"mustUpdate").setBoolean(goal,true);check(((Goal)goal).canUse(),"Optimized second target failed");check(field(cls,"targetEntity").get(goal)==expected,"Removed-item retarget changed");
            System.out.println("PERF TEST: actual Alex's Mobs item goal matches original with 80 candidates, equal-distance ties and target removal.");
        }finally{for(Entity e:spawned)e.discard();}
    }
    static class TestStorm extends StormObject {
        final List<Integer> affected=new ArrayList<>();
        TestStorm(ServerLevel level){super(weather2.ServerTickHandler.getWeatherManagerFor(level));}
        @Override public void spinEntityv2(Entity e){affected.add(e.getId());}
    }
    static void tornadoTests(ServerLevel level) throws Exception {
        for(int x=10;x<=14;x++)for(int z=10;z<=14;z++)level.getChunk(x,z);
        var storm=new TestStorm(level);storm.pos=new Vec3(192,250,192);storm.posGround=storm.pos;storm.posBaseFormationPos=storm.pos;storm.currentTopYBlock=250;storm.maxHeight=60;
        var helper=new TornadoHelper(storm);helper.grabDist=20;List<Entity> spawned=new ArrayList<>();
        boolean animals=ConfigTornado.Storm_Tornado_grabAnimals,mobs=ConfigTornado.Storm_Tornado_grabMobs,villagers=ConfigTornado.Storm_Tornado_grabVillagers,only=ConfigTornado.Storm_Tornado_grabPlayersOnly,items=ConfigTornado.Storm_Tornado_grabItems;
        try {
            for(int i=0;i<200;i++){ItemEntity e=new ItemEntity(level,191+i%3,250,191+i%3,new ItemStack(Items.STONE));level.addFreshEntity(e);spawned.add(e);}
            for(EntityType<?> type:List.of(EntityType.COW,EntityType.ZOMBIE,EntityType.VILLAGER,EntityType.ARMOR_STAND,EntityType.ARROW,ForgeRegistries.ENTITY_TYPES.getValue(new ResourceLocation("alexsmobs","seagull")))){
                Entity e=type.create(level);e.setPos(192,250,192);if(e instanceof Mob mob)mob.setNoAi(true);level.addFreshEntity(e);spawned.add(e);
            }
            int cases=0;
            for(int flags=0;flags<64;flags++){
                storm.setPet((flags&1)!=0);storm.setPetGrabsItems((flags&2)!=0);
                ConfigTornado.Storm_Tornado_grabAnimals=(flags&4)!=0;ConfigTornado.Storm_Tornado_grabMobs=(flags&8)!=0;
                ConfigTornado.Storm_Tornado_grabVillagers=(flags&16)!=0;ConfigTornado.Storm_Tornado_grabPlayersOnly=(flags&32)!=0;
                ConfigTornado.Storm_Tornado_grabItems=(flags&4)!=0;
                Performance.TORNADO_QUERY.set(false);storm.affected.clear();boolean expected=helper.forceRotate(level,false);List<Integer> ids=new ArrayList<>(storm.affected);
                if(flags==28) check(ids.size()>=4,"Normal tornado fixture failed to affect living entities");
                if(flags==3) check(ids.size()==200,"Pet tornado fixture failed to affect 200 items");
                Performance.TORNADO_QUERY.set(true);storm.affected.clear();boolean actual=helper.forceRotate(level,false);
                check(expected==actual && ids.equals(storm.affected),"Tornado affected entities/order changed flags="+flags);cases++;
            }
            var box=new AABB(190,248,190,195,255,195);
            System.out.println("PERF TEST: "+cases+" actual tornado forceRotate comparisons passed (normal/pet, grab settings, items/mobs). Broad candidates="+level.getEntitiesOfClass(Entity.class,box).size()+", living="+level.getEntitiesOfClass(LivingEntity.class,box).size());
        }finally{
            for(Entity e:spawned)e.discard();
            ConfigTornado.Storm_Tornado_grabAnimals=animals;ConfigTornado.Storm_Tornado_grabMobs=mobs;ConfigTornado.Storm_Tornado_grabVillagers=villagers;ConfigTornado.Storm_Tornado_grabPlayersOnly=only;ConfigTornado.Storm_Tornado_grabItems=items;
        }
    }
}
