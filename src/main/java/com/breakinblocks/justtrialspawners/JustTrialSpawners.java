package com.breakinblocks.justtrialspawners;

import com.breakinblocks.justtrialspawners.client.JTSClient;
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
        event.enqueueWork(JTSNetworking::register);
    }
}
