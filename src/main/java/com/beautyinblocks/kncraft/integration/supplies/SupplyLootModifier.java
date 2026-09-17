package com.beautyinblocks.kncraft.integration.supplies;

import com.beautyinblocks.kncraft.core.ExpansionConfig;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;
import net.minecraftforge.registries.ForgeRegistries;

/** One small addition per selected chest; original pools, luck and treasure remain intact. */
public final class SupplyLootModifier extends LootModifier {
    public static final Codec<SupplyLootModifier> CODEC = RecordCodecBuilder.create(instance -> codecStart(instance)
        .and(Codec.STRING.fieldOf("profile").forGetter(modifier -> modifier.profile)).apply(instance, SupplyLootModifier::new));
    public static final Map<String, List<String>> PROFILES = Map.of(
        "pantry", List.of("pamhc2foodcore:stewitem", "pamhc2foodcore:carrotsoupitem", "pamhc2foodcore:applejuiceitem"),
        "farm", List.of("pamhc2crops:cottonseeditem", "pamhc2crops:riceseeditem", "pamhc2crops:tealeafseeditem"),
        "camp", List.of("pamhc2crops:cottonitem", "ropebridge:rope", "pamhc2crops:hotteaitem"),
        "rail", List.of("minecraft:rail", "betterminecarts:bio_diesel_fuel", "minecraft:torch"));
    private final String profile;
    public SupplyLootModifier(LootItemCondition[] conditions, String profile) { super(conditions); this.profile = profile; }
    @Override protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generated, LootContext context) {
        if (!ExpansionConfig.THEMED_LOOT.get() || !PROFILES.containsKey(profile)) return generated;
        var choices = PROFILES.get(profile).stream().map(ResourceLocation::new).filter(ForgeRegistries.ITEMS::containsKey).toList();
        if (!choices.isEmpty() && context.getRandom().nextFloat() < .35f) {
            var id = choices.get(context.getRandom().nextInt(choices.size()));
            generated.add(new ItemStack(ForgeRegistries.ITEMS.getValue(id), profile.equals("rail") && id.getNamespace().equals("minecraft") ? 4 : 1));
        }
        return generated;
    }
    @Override public Codec<? extends IGlobalLootModifier> codec() { return CODEC; }
}
