package com.beautyinblocks.kncraft.core;

import java.util.List;
import java.util.Set;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

public final class IntegrationMixinPlugin implements IMixinConfigPlugin {
    public boolean shouldApplyMixin(String target, String mixin) {
        if (mixin.contains(".tents.")) return Compatibility.tents();
        if (mixin.endsWith("AvoidStormMixin")) return Compatibility.exact("witherstormmod");
        if (mixin.endsWith("ItemTargetMixin")) return Compatibility.exact("alexsmobs");
        if (mixin.endsWith("TornadoMixin")) return Compatibility.exact("weather2");
        return false;
    }
    public void onLoad(String pkg) {}
    public String getRefMapperConfig() { return null; }
    public void acceptTargets(Set<String> mine, Set<String> others) {}
    public List<String> getMixins() { return null; }
    public void preApply(String target, ClassNode node, String mixin, IMixinInfo info) {}
    public void postApply(String target, ClassNode node, String mixin, IMixinInfo info) {}
}
