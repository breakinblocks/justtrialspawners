package com.breakinblocks.justtrialspawners;

import com.breakinblocks.justtrialspawners.client.JTSClient;
import com.breakinblocks.justtrialspawners.common.entity.WindChargeProjectile;
import com.breakinblocks.justtrialspawners.config.JTSConfig;
import com.breakinblocks.justtrialspawners.common.command.TrialSpawnerCommand;
import com.breakinblocks.justtrialspawners.events.JTSEventHandler;
import com.breakinblocks.justtrialspawners.migration.TrialsMigrationHandler;
import com.breakinblocks.justtrialspawners.net.JTSNetworking;
import com.breakinblocks.justtrialspawners.registry.ModBlockEntities;
import com.breakinblocks.justtrialspawners.registry.ModBlocks;
import com.breakinblocks.justtrialspawners.registry.ModCreativeTabs;
import com.breakinblocks.justtrialspawners.registry.ModEntities;
import com.breakinblocks.justtrialspawners.registry.ModItems;
import com.breakinblocks.justtrialspawners.registry.ModEnchantments;
import com.breakinblocks.justtrialspawners.registry.ModMobEffects;
import com.breakinblocks.justtrialspawners.registry.ModSounds;
import net.minecraft.core.Position;
import net.minecraft.core.dispenser.AbstractProjectileDispenseBehavior;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(JustTrialSpawners.MOD_ID)
public class JustTrialSpawners {
    public static final String MOD_ID = "justtrialspawners";
    public static final Logger LOGGER = LogManager.getLogger();

    public static JustTrialSpawners instance;

    public JustTrialSpawners() {
        instance = this;

        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, JTSConfig.SERVER_SPEC);

        ModBlocks.register(modEventBus);
        ModItems.register(modEventBus);
        ModBlockEntities.register(modEventBus);
        ModEntities.register(modEventBus);
        ModSounds.register(modEventBus);
        ModMobEffects.register(modEventBus);
        ModEnchantments.register(modEventBus);
        ModCreativeTabs.register(modEventBus);

        modEventBus.addListener(this::commonSetup);

        // JTSEventHandler uses @Mod.EventBusSubscriber on inner classes (auto-registered)
        MinecraftForge.EVENT_BUS.register(TrialSpawnerCommand.class);
        MinecraftForge.EVENT_BUS.register(TrialsMigrationHandler.class);

        if (FMLEnvironment.dist == Dist.CLIENT) {
            JTSClient.init(modEventBus);
        }
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            JTSNetworking.register();
            registerDispenseBehaviors();
        });
    }

    private static void registerDispenseBehaviors() {
        DispenserBlock.registerBehavior(ModItems.WIND_CHARGE.get(), new AbstractProjectileDispenseBehavior() {
            @Override
            protected Projectile getProjectile(Level level, Position position, ItemStack stack) {
                WindChargeProjectile projectile = new WindChargeProjectile(ModEntities.WIND_CHARGE.get(), level);
                projectile.setPos(position.x(), position.y(), position.z());
                return projectile;
            }

            @Override
            protected float getUncertainty() {
                return 1.0F;
            }

            @Override
            protected float getPower() {
                return 1.0F;
            }
        });
    }
}
