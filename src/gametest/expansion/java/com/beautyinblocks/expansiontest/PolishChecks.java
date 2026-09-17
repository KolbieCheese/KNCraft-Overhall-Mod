package com.beautyinblocks.expansiontest;

import static com.beautyinblocks.expansiontest.ExpansionChecks.*;
import com.beautyinblocks.kncraft.core.PolishConfig;
import com.beautyinblocks.kncraft.integration.equipment.EnchantmentAttributes;
import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.config.ConfigLoadingHandler;
import com.momosoftworks.coldsweat.config.spec.WorldSettingsConfig;
import com.momosoftworks.coldsweat.util.world.WorldHelper;
import com.mojang.authlib.GameProfile;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.*;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.registries.ForgeRegistries;
import net.p3pp3rf1y.sophisticatedcore.api.IStorageWrapper;
import net.p3pp3rf1y.sophisticatedcore.upgrades.magnet.MagnetUpgradeItem;

final class PolishChecks {
    static void run(MinecraftServer server) throws Exception {
        attributes(server);
        altar(server);
        backpack(server);
        climate(server);
    }
    static void procedures(ServerPlayer player) throws Exception {
        player.tickCount++;
        for (String name : List.of("FurorProc", "AgilityProc", "RangeProc", "ArmoringProc"))
            Class.forName("net.mcreator.moreenchantments.procedures." + name + "Procedure")
                .getMethod("execute", LevelAccessor.class, double.class, double.class, double.class, Entity.class)
                .invoke(null, player.level(), player.getX(), player.getY(), player.getZ(), player);
    }
    static void attributes(MinecraftServer server) throws Exception {
        var p = FakePlayerFactory.get(server.overworld(), new GameProfile(UUID.randomUUID(), "AttributeCheck"));
        var speed = p.getAttribute(Attributes.ATTACK_SPEED);
        speed.setBaseValue(5.25);
        var external = new AttributeModifier(UUID.randomUUID(), "administrator fixture", .75, AttributeModifier.Operation.ADDITION);
        speed.addTransientModifier(external);
        var sword = new ItemStack(Items.IRON_SWORD);
        var furor = ForgeRegistries.ENCHANTMENTS.getValue(new ResourceLocation("more_enchantments:furor"));
        check(furor != null, "Furor missing"); sword.enchant(furor, 2);
        p.setItemSlot(EquipmentSlot.MAINHAND, sword);
        procedures(p);
        double amount = speed.getModifiers().stream().filter(m -> m.getName().equals("KNCraft enchantment contribution")).mapToDouble(AttributeModifier::getAmount).sum();
        check(speed.getBaseValue() == 5.25 && amount > 0 && speed.getModifier(external.getId()) != null, "Custom attack base/modifier lost");
        int count = speed.getModifiers().size();
        for (int i = 0; i < 20; i++) procedures(p);
        check(speed.getModifiers().size() == count, "Enchantment modifiers accumulated");
        p.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY); procedures(p);
        check(speed.getBaseValue() == 5.25 && speed.getModifiers().stream().noneMatch(m -> m.getName().equals("KNCraft enchantment contribution")), "Unequip left enchantment bonus");
        var legacy = FakePlayerFactory.get(server.overworld(), new GameProfile(UUID.randomUUID(), "LegacyAttrCheck"));
        legacy.getAttribute(Attributes.ATTACK_SPEED).setBaseValue(4 + amount);
        legacy.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(.1);
        legacy.getAttribute(ForgeMod.BLOCK_REACH.get()).setBaseValue(4.5);
        legacy.getAttribute(Attributes.ARMOR).setBaseValue(0);
        legacy.setItemSlot(EquipmentSlot.MAINHAND, sword); procedures(legacy);
        check(legacy.getAttribute(Attributes.ATTACK_SPEED).getBaseValue() == 4 && EnchantmentAttributes.migration(legacy).getBoolean("migrated"), "Matching legacy migration failed");
        legacy.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY); procedures(legacy);
        check(legacy.getAttribute(Attributes.ATTACK_SPEED).getValue() == 4, "Legacy enchantment remained after unequip");
        var tag = new net.minecraft.nbt.CompoundTag(); legacy.saveWithoutId(tag);
        var reloaded = FakePlayerFactory.get(server.overworld(), new GameProfile(UUID.randomUUID(), "ReloadAttrCheck")); reloaded.load(tag); procedures(reloaded);
        check(reloaded.getAttribute(Attributes.ATTACK_SPEED).getBaseValue() == 4 && EnchantmentAttributes.migration(reloaded).getBoolean("migrated"), "Migration lost on player NBT reload");
        EnchantmentAttributes.restoreBackup(legacy);
        check(Math.abs(legacy.getAttribute(Attributes.ATTACK_SPEED).getBaseValue() - 4 - amount) < 1e-8, "Legacy backup restore failed");
        p.discard(); legacy.discard(); reloaded.discard();
        System.out.println("POLISH ATTRIBUTES: native procedures, external base/modifier, 20 repeated updates, unequip, matching migration, NBT reload and backup restore passed");
    }
    static void altar(MinecraftServer server) throws Exception {
        var level = server.overworld(); var pos = new BlockPos(2160, 155, 2160); level.getChunk(pos);
        var block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation("aether:altar"));
        var type = Class.forName("com.aetherteam.aether.blockentity.AbstractAetherFurnaceBlockEntity");
        var tick = type.getMethod("serverTick", Level.class, BlockPos.class, net.minecraft.world.level.block.state.BlockState.class, type);
        var gear = new ArrayList<String>();
        for (String material : List.of("goat_fur", "hoglin")) for (String slot : List.of("helmet", "chestplate", "leggings", "boots")) gear.add("cold_sweat:" + material + "_" + slot);
        gear.add("alexsmobs:frontier_cap");
        for (String id : gear) {
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3); level.setBlock(pos, block.defaultBlockState(), 3);
            var be = level.getBlockEntity(pos); var inventory = (Container)be;
            var input = item(id); check(input.isDamageableItem(), "Repair target has no durability: " + id);
            input.setDamageValue(10); input.setHoverName(net.minecraft.network.chat.Component.literal("Named expedition gear")); input.enchant(Enchantments.UNBREAKING, 2);
            input.getOrCreateTag().putString("KNCraftFixture", "preserve NBT");
            // Native insulation storage uses the item's serializable Forge capability.
            com.momosoftworks.coldsweat.common.capability.handler.ItemInsulationManager.getInsulationCap(input).ifPresent(cap -> cap.addInsulationItem(item("pamhc2crops:cottonitem")));
            var expected = input.copy(); expected.setDamageValue(0);
            inventory.setItem(0, input); inventory.setItem(1, item("aether:ambrosium_shard")); inventory.getItem(1).setCount(10);
            int ticks = id.contains("hoglin") ? 1000 : 700;
            for (int i = 0; i < ticks; i++) tick.invoke(null, level, pos, level.getBlockState(pos), be);
            var result = inventory.getItem(2);
            check(!result.isEmpty() && result.getDamageValue() == 0 && result.getCount() == 1, "Altar did not repair " + id);
            check(ItemStack.isSameItemSameTags(result, expected), "Altar lost item data: " + id + " result=" + result.save(new net.minecraft.nbt.CompoundTag()));
            check(inventory.getItem(0).isEmpty() && inventory.getItem(1).getCount() < 10, "Altar repair cost bypassed");
        }
        level.removeBlock(pos, false);
        System.out.println("POLISH ALTAR: all nine native fuelled repairs retained names/enchantments/insulation data and consumed input once");
    }
    static void backpack(MinecraftServer server) throws Exception {
        var level = server.overworld(); var pos = new BlockPos(2176, 155, 2176); level.getChunk(pos);
        for (var air : BlockPos.betweenClosed(pos.offset(-2, 1, -2), pos.offset(5, 5, 2))) level.setBlock(air, Blocks.AIR.defaultBlockState(), 2);
        level.setBlock(pos.below(), Blocks.STONE.defaultBlockState(), 3);
        var block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation("sophisticatedbackpacks:diamond_backpack")); level.setBlock(pos, block.defaultBlockState(), 3);
        var be = level.getBlockEntity(pos); be.getClass().getMethod("setBackpack", ItemStack.class).invoke(be, item("sophisticatedbackpacks:diamond_backpack"));
        var wrapper = (IStorageWrapper)be.getClass().getMethod("getBackpackWrapper").invoke(be);
        wrapper.getUpgradeHandler().setStackInSlot(0, item("sophisticatedbackpacks:tank_upgrade"));
        wrapper.getUpgradeHandler().setStackInSlot(1, item("sophisticatedbackpacks:magnet_upgrade"));
        var fluid = be.getCapability(ForgeCapabilities.FLUID_HANDLER, Direction.UP).orElseThrow(() -> new IllegalStateException("Backpack tank capability absent"));
        check(fluid.fill(new FluidStack(Fluids.WATER, 1000), IFluidHandler.FluidAction.EXECUTE) == 1000, "Tank did not fill");
        var p = FakePlayerFactory.get(level, new GameProfile(UUID.randomUUID(), "BackpackCheck")); p.setPos(pos.getX() + .5, pos.getY() + 1, pos.getZ() + .5); level.addNewPlayer(p);
        try {
            p.setShiftKeyDown(true); p.gameMode.changeGameModeForPlayer(GameType.SURVIVAL);
            var skin = item("cold_sweat:waterskin"); skin.setHoverName(net.minecraft.network.chat.Component.literal("Travel water")); p.setItemInHand(InteractionHand.MAIN_HAND, skin);
            var hit = new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false);
            p.gameMode.useItemOn(p, level, skin, InteractionHand.MAIN_HAND, hit);
            check(p.getMainHandItem().is(item("cold_sweat:filled_waterskin").getItem()) && fluid.getFluidInTank(0).getAmount() == 750, "Native waterskin interaction failed: " + p.getMainHandItem() + " tank=" + fluid.getFluidInTank(0));
            check(p.getMainHandItem().getHoverName().getString().equals("Travel water"), "Waterskin name lost");
            fluid.drain(600, IFluidHandler.FluidAction.EXECUTE); p.setItemInHand(InteractionHand.MAIN_HAND, item("cold_sweat:waterskin"));
            p.gameMode.useItemOn(p, level, p.getMainHandItem(), InteractionHand.MAIN_HAND, hit);
            check(p.getMainHandItem().is(item("cold_sweat:waterskin").getItem()) && fluid.getFluidInTank(0).getAmount() == 150, "Insufficient water created a filled waterskin");
            var magnet = wrapper.getUpgradeHandler().getTypeWrappers(MagnetUpgradeItem.TYPE).get(0);
            var crow = (Mob)ForgeRegistries.ENTITY_TYPES.getValue(new ResourceLocation("alexsmobs:crow")).create(level);
            crow.setPos(pos.getX() + 2, pos.getY() + 1, pos.getZ() + .5); level.addFreshEntity(crow);
            long time = level.getGameTime();
            try {
                var original = drop(p, item("pamhc2crops:blueberryitem"));
                PolishConfig.OFFERINGS.set(false); magnet.tick(null, level, pos);
                check(original.getItem().isEmpty(), "Baseline magnet did not collect offering"); original.discard();
                PolishConfig.OFFERINGS.set(true); server.getWorldData().overworldData().setGameTime(time + 50);
                var offered = drop(p, item("pamhc2crops:blueberryitem"));
                System.out.println("OFFERING FIXTURE owner=" + offered.getOwner() + " age=" + offered.getAge() + " accepts=" + crow.getClass().getMethod("canTargetItem", ItemStack.class).invoke(crow, offered.getItem())
                    + " visible=" + crow.hasLineOfSight(offered) + " nearby=" + level.getEntitiesOfClass(Mob.class, offered.getBoundingBox().inflate(8), m -> true).size());
                magnet.tick(null, level, pos);
                check(!offered.getItem().isEmpty(), "Protected offering was collected");
                server.getWorldData().overworldData().setGameTime(time + 100); magnet.tick(p, level, pos);
                check(!offered.getItem().isEmpty(), "Carried magnet bypassed offering protection");
                server.getWorldData().overworldData().setGameTime(time + 300); magnet.tick(null, level, pos);
                check(offered.getItem().isEmpty(), "Offering grace period did not expire"); offered.discard();
                server.getWorldData().overworldData().setGameTime(time + 350); var loot = new ItemEntity(level, pos.getX() + .5, pos.getY() + 1, pos.getZ() + .5, new ItemStack(Items.COBBLESTONE)); level.addFreshEntity(loot); magnet.tick(null, level, pos);
                check(loot.getItem().isEmpty(), "Ordinary loot was blocked"); loot.discard();
                crow.discard();
                var raccoon = (Mob)ForgeRegistries.ENTITY_TYPES.getValue(new ResourceLocation("alexsmobs:raccoon")).create(level);
                raccoon.setPos(pos.getX() + 2, pos.getY() + 1, pos.getZ() + .5); level.addFreshEntity(raccoon);
                try {
                    server.getWorldData().overworldData().setGameTime(time + 400); var berry = drop(p, item("pamhc2crops:strawberryitem")); magnet.tick(null, level, pos);
                    check(!berry.getItem().isEmpty(), "Raccoon offering was collected"); berry.discard();
                } finally { raccoon.discard(); }
            } finally { server.getWorldData().overworldData().setGameTime(time); crow.discard(); PolishConfig.OFFERINGS.set(true); }
        } finally { p.discard(); level.removeBlock(pos, false); }
        System.out.println("POLISH BACKPACK: actual sneak-use filled waterskin with 250 mB, rejected insufficient water; native magnet baseline/grace/expiry/ordinary loot passed");
    }
    static ItemEntity drop(ServerPlayer p, ItemStack item) {
        var dropped = new ItemEntity(p.level(), p.getX(), p.getY(), p.getZ(), item); dropped.setThrower(p.getUUID()); dropped.setPickUpDelay(40); p.level().addFreshEntity(dropped); return dropped;
    }
    static void climate(MinecraftServer server) throws Exception {
        var level = server.overworld(); var registry = level.registryAccess().registryOrThrow(Registries.BIOME);
        var overrides = WorldSettingsConfig.BIOME_TEMPERATURES.get();
        var ice = registry.getHolderOrThrow(ResourceKey.create(Registries.BIOME, new ResourceLocation("terralith:cave/ice_caves")));
        var originalRanges = new LinkedHashMap<String, String>();
        try {
            PolishConfig.REGIONAL_CLIMATE.set(false); ConfigLoadingHandler.loadConfigs(server.registryAccess());
            for (String id : List.of("biomesoplenty:auroral_garden", "biomesoplenty:cold_desert", "biomesoplenty:tropics", "terralith:emerald_peaks", "terralith:cave/ice_caves", "terralith:cave/thermal_caves"))
                originalRanges.put(id, WorldHelper.getBiomeTemperatureRange(level, registry.getHolderOrThrow(ResourceKey.create(Registries.BIOME, new ResourceLocation(id)))).toString());
        } finally { PolishConfig.REGIONAL_CLIMATE.set(true); ConfigLoadingHandler.loadConfigs(server.registryAccess()); }
        var expected = Temperature.convert(32, Temperature.Units.F, Temperature.Units.MC, true);
        check(Math.abs(WorldHelper.getBiomeTemperatureRange(level, ice).getFirst() - expected) < 1e-6, "Ice cave default missing");
        var custom = new ArrayList<List<?>>(); custom.addAll(overrides); custom.add(List.of("terralith:cave/ice_caves", 45, 45, "F"));
        try {
            WorldSettingsConfig.BIOME_TEMPERATURES.set(custom); ConfigLoadingHandler.loadConfigs(server.registryAccess());
            check(Math.abs(WorldHelper.getBiomeTemperatureRange(level, ice).getFirst() - Temperature.convert(45, Temperature.Units.F, Temperature.Units.MC, true)) < 1e-6, "Regional default replaced administrator temperature");
        } finally { WorldSettingsConfig.BIOME_TEMPERATURES.set(overrides); ConfigLoadingHandler.loadConfigs(server.registryAccess()); }
        var probe = WorldHelper.getDummyPlayer(level); long time = level.getDayTime();
        var pos = new BlockPos(2304, 128, 2304);
        try {
            for (String id : List.of("biomesoplenty:auroral_garden", "biomesoplenty:cold_desert", "biomesoplenty:tropics", "terralith:emerald_peaks", "terralith:cave/ice_caves", "terralith:cave/thermal_caves")) {
                var biome = registry.getHolderOrThrow(ResourceKey.create(Registries.BIOME, new ResourceLocation(id)));
                for (int x = (pos.getX()-48)>>4; x <= (pos.getX()+48)>>4; x++) for (int z = (pos.getZ()-48)>>4; z <= (pos.getZ()+48)>>4; z++)
                    level.getChunk(x,z).fillBiomesFromNoise((qx,qy,qz,sampler) -> biome, level.getChunkSource().randomState().sampler());
                var range = WorldHelper.getBiomeTemperatureRange(level, biome);
                var values = new ArrayList<Double>();
                for (int y : List.of(-32, 300)) for (long tod : List.of(6000L, 18000L)) {
                    level.setDayTime(tod); probe.setPos(pos.getX()+.5, y, pos.getZ()+.5);
                    double value = 0;
                    var stages = new LinkedHashMap<String, Double>();
                    for (var modifier : Temperature.getModifiers(probe, Temperature.Trait.WORLD))
                        if (Set.of("BiomeTempModifier","CaveBiomeTempModifier","ElevationTempModifier","ShadeTempModifier").contains(modifier.getClass().getSimpleName())) {
                            value = modifier.update(value, probe, Temperature.Trait.WORLD); stages.put(modifier.getClass().getSimpleName(), value);
                        }
                    check(level.getBiome(probe.blockPosition()).equals(biome), "Climate fixture biome mismatch");
                    if (!id.contains("cave/")) {
                        double nativeBiome = stages.get("BiomeTempModifier");
                        check(Math.abs(nativeBiome - (tod == 6000 ? range.getSecond() : range.getFirst())) < 1e-6, "Regional day/night contribution missing: " + id);
                    } else if (y == -32) {
                        check(Math.abs(value - range.getFirst()) < 1e-6, "Underground biome temperature did not reach cave sampler: " + id);
                    }
                    check(Double.isFinite(value), "Invalid sampled climate " + id); values.add(value);
                }
                if (!id.contains("cave/")) check(values.get(2) > values.get(3), "Above-ground day/night cycle was lost: " + id);
                System.out.println("REGIONAL SAMPLE " + id + " original biome MC=" + originalRanges.get(id) + " new biome MC=" + range + " ambient MC [underground day/night, elevated day/night]=" + values);
            }
        } finally { level.setDayTime(time); }
        System.out.println("POLISH CLIMATE: native biome/cave/elevation sampling at two heights/times and administrator reload preservation passed");
    }
}
