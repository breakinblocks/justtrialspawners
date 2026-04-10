package com.breakinblocks.justtrialspawners.events;

import com.breakinblocks.justtrialspawners.JustTrialSpawners;
import com.breakinblocks.justtrialspawners.common.entity.BoggedEntity;
import com.breakinblocks.justtrialspawners.common.entity.BreezeEntity;
import com.breakinblocks.justtrialspawners.common.entity.WindChargeProjectile;
import com.breakinblocks.justtrialspawners.registry.ModEnchantments;
import com.breakinblocks.justtrialspawners.registry.ModEntities;
import com.breakinblocks.justtrialspawners.registry.ModMobEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Silverfish;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

public class JTSEventHandler {

    @Mod.EventBusSubscriber(modid = JustTrialSpawners.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ModBusEvents {
        @SubscribeEvent
        public static void onEntityAttributeCreation(EntityAttributeCreationEvent event) {
            event.put(ModEntities.BREEZE.get(), BreezeEntity.createAttributes().build());
            event.put(ModEntities.BOGGED.get(), BoggedEntity.createAttributes().build());
        }
    }

    @Mod.EventBusSubscriber(modid = JustTrialSpawners.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class ForgeBusEvents {
        @SubscribeEvent
        public static void onLivingDeath(LivingDeathEvent event) {
            LivingEntity entity = event.getEntity();
            Level level = entity.level();
            if (level.isClientSide()) return;

            // Wind Charged: create wind burst on death
            if (entity.hasEffect(ModMobEffects.WIND_CHARGED.get())) {
                WindChargeProjectile.windBurst(level, entity.position(), 3.0F, 1.0F, entity);
            }

            // Oozing: spawn 2 small slimes on death
            if (entity.hasEffect(ModMobEffects.OOZING.get())) {
                for (int i = 0; i < 2; i++) {
                    Slime slime = EntityType.SLIME.create(level);
                    if (slime != null) {
                        slime.setSize(2, true);
                        slime.moveTo(entity.getX(), entity.getY(), entity.getZ(), level.getRandom().nextFloat() * 360.0F, 0.0F);
                        level.addFreshEntity(slime);
                    }
                }
            }

            // Weaving: place 2-3 cobwebs in random nearby air blocks
            if (entity.hasEffect(ModMobEffects.WEAVING.get())) {
                RandomSource random = level.getRandom();
                int count = 2 + random.nextInt(2);
                int placed = 0;
                for (int attempts = 0; attempts < 20 && placed < count; attempts++) {
                    BlockPos pos = entity.blockPosition().offset(
                            random.nextInt(7) - 3,
                            random.nextInt(7) - 3,
                            random.nextInt(7) - 3);
                    if (level.isEmptyBlock(pos)) {
                        level.setBlockAndUpdate(pos, Blocks.COBWEB.defaultBlockState());
                        placed++;
                    }
                }
            }
        }

        @SubscribeEvent
        public static void onLivingHurt(LivingHurtEvent event) {
            LivingEntity target = event.getEntity();
            Level level = target.level();
            if (level.isClientSide()) return;

            // Infested: 10% chance to spawn 1-2 silverfish when damaged
            if (target.hasEffect(ModMobEffects.INFESTED.get())) {
                RandomSource random = level.getRandom();
                if (random.nextFloat() < 0.1F) {
                    int count = 1 + random.nextInt(2);
                    for (int i = 0; i < count; i++) {
                        Silverfish silverfish = EntityType.SILVERFISH.create(level);
                        if (silverfish != null) {
                            silverfish.moveTo(target.getX(), target.getY(), target.getZ(), random.nextFloat() * 360.0F, 0.0F);
                            level.addFreshEntity(silverfish);
                        }
                    }
                }
            }

            // Enchantment effects require a living attacker
            if (!(event.getSource().getEntity() instanceof LivingEntity attacker)) return;
            ItemStack weapon = attacker.getMainHandItem();

            // Breach: multiply damage to simulate armor bypass
            int breachLevel = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.BREACH.get(), weapon);
            if (breachLevel > 0) {
                event.setAmount(event.getAmount() * (1.0F + 0.15F * breachLevel));
            }

            // Wind Burst: create wind burst at target on attack
            int windBurstLevel = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.WIND_BURST.get(), weapon);
            if (windBurstLevel > 0) {
                WindChargeProjectile.windBurst(level, target.position(), 1.5F, 0.5F * windBurstLevel, attacker);
            }
        }
    }
}
