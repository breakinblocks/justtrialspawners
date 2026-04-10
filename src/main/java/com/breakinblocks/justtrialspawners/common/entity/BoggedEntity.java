package com.breakinblocks.justtrialspawners.common.entity;

import com.breakinblocks.justtrialspawners.JustTrialSpawners;
import com.breakinblocks.justtrialspawners.registry.ModSounds;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraftforge.common.IForgeShearable;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

/**
 * Bogged - a skeleton variant that shoots poison arrows and can be sheared for mushrooms.
 */
public class BoggedEntity extends AbstractSkeleton implements IForgeShearable {
    private static final int HARD_ATTACK_INTERVAL = 50;
    private static final int NORMAL_ATTACK_INTERVAL = 70;
    private static final EntityDataAccessor<Boolean> DATA_SHEARED =
            SynchedEntityData.defineId(BoggedEntity.class, EntityDataSerializers.BOOLEAN);
    public static final ResourceLocation SHEARING_LOOT = new ResourceLocation(JustTrialSpawners.MOD_ID, "shearing/bogged");

    public BoggedEntity(EntityType<? extends BoggedEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return AbstractSkeleton.createAttributes()
                .add(Attributes.MAX_HEALTH, 16.0);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_SHEARED, false);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("sheared", this.isSheared());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.setSheared(tag.getBoolean("sheared"));
    }

    public boolean isSheared() {
        return this.entityData.get(DATA_SHEARED);
    }

    public void setSheared(boolean sheared) {
        this.entityData.set(DATA_SHEARED, sheared);
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        if (itemstack.is(Items.SHEARS) && this.isShearable(itemstack, this.level(), this.blockPosition())) {
            this.level().playSound(null, this, ModSounds.BOGGED_SHEAR.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
            this.spawnShearedMushrooms();
            this.setSheared(true);
            this.gameEvent(GameEvent.SHEAR, player);
            if (!this.level().isClientSide) {
                itemstack.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(hand));
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }
        return super.mobInteract(player, hand);
    }

    private void spawnShearedMushrooms() {
        if (!this.level().isClientSide && this.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            net.minecraft.world.level.storage.loot.LootTable lootTable =
                    serverLevel.getServer().getLootData().getLootTable(SHEARING_LOOT);
            net.minecraft.world.level.storage.loot.LootParams lootParams =
                    new net.minecraft.world.level.storage.loot.LootParams.Builder(serverLevel)
                            .withParameter(net.minecraft.world.level.storage.loot.parameters.LootContextParams.ORIGIN, this.position())
                            .withParameter(net.minecraft.world.level.storage.loot.parameters.LootContextParams.THIS_ENTITY, this)
                            .create(net.minecraft.world.level.storage.loot.parameters.LootContextParamSets.SELECTOR);
            for (ItemStack drop : lootTable.getRandomItems(lootParams)) {
                this.spawnAtLocation(drop, this.getBbHeight());
            }
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.BOGGED_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.BOGGED_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.BOGGED_DEATH.get();
    }

    @Override
    protected SoundEvent getStepSound() {
        return ModSounds.BOGGED_STEP.get();
    }

    @Override
    protected AbstractArrow getArrow(ItemStack arrowStack, float velocity) {
        AbstractArrow arrow = super.getArrow(arrowStack, velocity);
        if (arrow instanceof Arrow typedArrow) {
            typedArrow.addEffect(new MobEffectInstance(MobEffects.POISON, 100));
        }
        return arrow;
    }

    // IForgeShearable implementation
    @Override
    public boolean isShearable(ItemStack item, Level level, net.minecraft.core.BlockPos pos) {
        return !this.isSheared() && this.isAlive();
    }

    @Override
    public List<ItemStack> onSheared(@Nullable Player player, ItemStack item, Level level, net.minecraft.core.BlockPos pos, int fortune) {
        // Shearing is handled in mobInteract for proper sound/event handling
        return Collections.emptyList();
    }
}
