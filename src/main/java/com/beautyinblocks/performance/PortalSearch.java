package com.beautyinblocks.performance;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.Vec3;
public final class PortalSearch {
    public record Result(boolean handled, BlockPos position, int blockChecks, int sectionChecks) {}
    private static final Result FALLBACK=new Result(false,null,0,0);
    private PortalSearch() {}
    public static Result find(ServerLevel level, Vec3 position, int radius, Block block) {
        // Restrict the optimization to its audited production use. Air/out-of-height semantics differ.
        if(block!=Blocks.NETHER_PORTAL || radius<0 || radius>32 || level.isDebug() || !level.getServer().isSameThread()) return FALLBACK;
        if(!Double.isFinite(position.x) || !Double.isFinite(position.y) || !Double.isFinite(position.z)
            || Math.abs(position.x)>29999900 || Math.abs(position.z)>29999900
            || position.y<level.getMinBuildHeight()+4 || position.y>=level.getMaxBuildHeight()-4) return FALLBACK;
        // Int casts are intentional: upstream truncates toward zero, including negative coordinates.
        int minX=(int)position.x-radius, maxX=(int)position.x+radius;
        int minY=(int)position.y-4, maxY=(int)position.y+4;
        int minZ=(int)position.z-radius, maxZ=(int)position.z+radius;
        int cx0=minX>>4, cz0=minZ>>4, nx=(maxX>>4)-cx0+1, nz=(maxZ>>4)-cz0+1;
        int sy0=minY>>4, ny=(maxY>>4)-sy0+1;
        LevelChunk[] chunks=new LevelChunk[nx*nz];
        // Never change original chunk-loading behavior: if any required chunk is absent, fall back.
        for(int z=0;z<nz;z++) for(int x=0;x<nx;x++) {
            LevelChunk c=level.getChunkSource().getChunkNow(cx0+x,cz0+z);
            if(c==null) return FALLBACK;
            chunks[z*nx+x]=c;
        }
        boolean[] candidate=new boolean[chunks.length*ny];
        int sectionChecks=0, blockChecks=0, candidates=0;
        for(int i=0;i<chunks.length;i++) for(int y=0;y<ny;y++) {
            var section=chunks[i].getSection(level.getSectionIndex((sy0+y)<<4));
            candidate[i*ny+y]=section.maybeHas(state->state.getBlock()==block);
            sectionChecks++;
            if(candidate[i*ny+y]) candidates++;
        }
        // Palettes can retain removed portal states. Avoid the optimized loop when pruning is weak.
        if(candidates*2>=candidate.length) return FALLBACK;
        BlockPos.MutableBlockPos cursor=new BlockPos.MutableBlockPos();
        // Exactly BlockPos.betweenClosed order: X fastest, then Y, then Z. No persistent cache.
        for(int z=minZ;z<=maxZ;z++) for(int y=minY;y<=maxY;y++) {
            for(int x=minX;x<=maxX;) {
                int next=Math.min(maxX,((x>>4)<<4)+15);
                int ci=((z>>4)-cz0)*nx+(x>>4)-cx0;
                if(candidate[ci*ny+(y>>4)-sy0]) {
                    for(;x<=next;x++) {
                        cursor.set(x,y,z);blockChecks++;
                        if(level.getBlockState(cursor).getBlock()==block) return new Result(true,cursor.immutable(),blockChecks,sectionChecks);
                    }
                } else x=next+1;
            }
        }
        return new Result(true,null,blockChecks,sectionChecks);
    }
}
