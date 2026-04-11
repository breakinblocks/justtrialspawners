package com.breakinblocks.justtrialspawners.registry;

import com.breakinblocks.justtrialspawners.JustTrialSpawners;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(
            ForgeRegistries.SOUND_EVENTS, JustTrialSpawners.MOD_ID);

    public static final RegistryObject<SoundEvent> TRIAL_SPAWNER_SPAWN_MOB = register("trial_spawner.spawn_mob");
    public static final RegistryObject<SoundEvent> TRIAL_SPAWNER_DETECT_PLAYER = register("trial_spawner.detect_player");
    public static final RegistryObject<SoundEvent> TRIAL_SPAWNER_OPEN_SHUTTER = register("trial_spawner.open_shutter");
    public static final RegistryObject<SoundEvent> TRIAL_SPAWNER_CLOSE_SHUTTER = register("trial_spawner.close_shutter");
    public static final RegistryObject<SoundEvent> TRIAL_SPAWNER_EJECT_ITEM = register("trial_spawner.eject_item");
    public static final RegistryObject<SoundEvent> TRIAL_SPAWNER_AMBIENT = register("trial_spawner.ambient");
    public static final RegistryObject<SoundEvent> TRIAL_SPAWNER_AMBIENT_OMINOUS = register("trial_spawner.ambient_ominous");

    public static final RegistryObject<SoundEvent> BREEZE_IDLE_GROUND = register("breeze.idle_ground");
    public static final RegistryObject<SoundEvent> BREEZE_IDLE_AIR = register("breeze.idle_air");
    public static final RegistryObject<SoundEvent> BREEZE_CHARGE = register("breeze.charge");
    public static final RegistryObject<SoundEvent> BREEZE_SHOOT = register("breeze.shoot");
    public static final RegistryObject<SoundEvent> BREEZE_INHALE = register("breeze.inhale");
    public static final RegistryObject<SoundEvent> BREEZE_JUMP = register("breeze.jump");
    public static final RegistryObject<SoundEvent> BREEZE_LAND = register("breeze.land");
    public static final RegistryObject<SoundEvent> BREEZE_SLIDE = register("breeze.slide");
    public static final RegistryObject<SoundEvent> BREEZE_DEATH = register("breeze.death");
    public static final RegistryObject<SoundEvent> BREEZE_HURT = register("breeze.hurt");
    public static final RegistryObject<SoundEvent> BREEZE_WHIRL = register("breeze.whirl");
    public static final RegistryObject<SoundEvent> BREEZE_DEFLECT = register("breeze.deflect");

    public static final RegistryObject<SoundEvent> BOGGED_AMBIENT = register("bogged.ambient");
    public static final RegistryObject<SoundEvent> BOGGED_DEATH = register("bogged.death");
    public static final RegistryObject<SoundEvent> BOGGED_HURT = register("bogged.hurt");
    public static final RegistryObject<SoundEvent> BOGGED_STEP = register("bogged.step");
    public static final RegistryObject<SoundEvent> BOGGED_SHEAR = register("bogged.shear");

    public static final RegistryObject<SoundEvent> WIND_CHARGE_THROW = register("wind_charge.throw");
    public static final RegistryObject<SoundEvent> WIND_CHARGE_BURST = register("wind_charge.burst");
    public static final RegistryObject<SoundEvent> BREEZE_WIND_CHARGE_BURST = register("breeze_wind_charge.burst");

    public static final RegistryObject<SoundEvent> VAULT_ACTIVATE = register("vault.activate");
    public static final RegistryObject<SoundEvent> VAULT_DEACTIVATE = register("vault.deactivate");
    public static final RegistryObject<SoundEvent> VAULT_AMBIENT = register("vault.ambient");
    public static final RegistryObject<SoundEvent> VAULT_OPEN_SHUTTER = register("vault.open_shutter");
    public static final RegistryObject<SoundEvent> VAULT_INSERT = register("vault.insert");
    public static final RegistryObject<SoundEvent> VAULT_INSERT_FAIL = register("vault.insert_fail");
    public static final RegistryObject<SoundEvent> VAULT_REJECT_REWARDED_PLAYER = register("vault.reject_rewarded_player");
    public static final RegistryObject<SoundEvent> VAULT_EJECT = register("vault.eject");

    private static RegistryObject<SoundEvent> register(String name) {
        return SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(
                new ResourceLocation(JustTrialSpawners.MOD_ID, name)));
    }

    public static void register(IEventBus modEventBus) {
        SOUNDS.register(modEventBus);
    }
}
