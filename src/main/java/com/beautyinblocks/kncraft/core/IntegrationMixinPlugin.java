package com.beautyinblocks.kncraft.core;

import java.util.List;
import java.util.Set;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

public final class IntegrationMixinPlugin implements IMixinConfigPlugin {
    public boolean shouldApplyMixin(String target, String mixin) {
        if (mixin.endsWith("GuideBookResourcesMixin") || mixin.endsWith("GuideEntryListMixin")) return Compatibility.supported("patchouli");
        if (mixin.endsWith("ReservedFoodMixin")) return Compatibility.supported("sophisticatedcore");
        if (mixin.endsWith("AltarRepairMixin")) return Compatibility.supported("aether");
        if (mixin.endsWith("EnchantmentAttributesMixin")) return Compatibility.supported("more_enchantments");
        if (mixin.endsWith("KnownTagsMixin")) return Compatibility.present("betterminecarts") || Compatibility.present("callfromthedepth_");
        if (mixin.endsWith("TentPlacementClimateMixin") || mixin.endsWith("TentEnclosureMixin"))
            return Compatibility.supported("nomadictents") && Compatibility.supported("cold_sweat");
        if (mixin.endsWith("FlightEquipmentMixin") || mixin.endsWith("WingArmorMixin"))
            return Compatibility.supported("more_enchantments") && Compatibility.supported("elytraslot");
        if (mixin.endsWith("FeedingClimateMixin"))
            return Compatibility.supported("sophisticatedcore") && Compatibility.supported("cold_sweat");
        if (mixin.endsWith("CompanionCleaveMixin")) return Compatibility.supported("bettercombat");
        if (mixin.contains(".tents.")) return Compatibility.tents();
        if (mixin.endsWith("AvoidStormMixin")) return Compatibility.supported("witherstormmod");
        if (mixin.endsWith("ItemTargetMixin")) return Compatibility.supported("alexsmobs");
        if (mixin.endsWith("TornadoMixin")) return Compatibility.supported("weather2");
        return false;
    }
    public void onLoad(String pkg) {}
    public String getRefMapperConfig() { return null; }
    public void acceptTargets(Set<String> mine, Set<String> others) {}
    public List<String> getMixins() { return null; }
    public void preApply(String target, ClassNode node, String mixin, IMixinInfo info) {}
    public void postApply(String target, ClassNode node, String mixin, IMixinInfo info) {}
}
