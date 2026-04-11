package com.breakinblocks.justtrialspawners.client;

import com.breakinblocks.justtrialspawners.JustTrialSpawners;
import com.breakinblocks.justtrialspawners.client.model.BreezeModel;
import com.breakinblocks.justtrialspawners.client.renderer.BoggedRenderer;
import com.breakinblocks.justtrialspawners.client.renderer.BreezeRenderer;
import com.breakinblocks.justtrialspawners.client.renderer.TrialSpawnerRenderer;
import com.breakinblocks.justtrialspawners.client.renderer.VaultRenderer;
import com.breakinblocks.justtrialspawners.client.renderer.WindChargeRenderer;
import com.breakinblocks.justtrialspawners.registry.ModBlockEntities;
import com.breakinblocks.justtrialspawners.registry.ModBlocks;
import com.breakinblocks.justtrialspawners.registry.ModEntities;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = JustTrialSpawners.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD,
        value = net.minecraftforge.api.distmarker.Dist.CLIENT)
public class JTSClient {

    public static void init(IEventBus modEventBus) {
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.TRIAL_SPAWNER.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.VAULT.get(), RenderType.cutout());
        });
    }

    @SubscribeEvent
    public static void onRegisterLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(BreezeModel.LAYER_LOCATION, BreezeModel::createMainLayer);
        event.registerLayerDefinition(BreezeModel.WIND_LAYER_LOCATION, BreezeModel::createWindLayer);
    }

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.BREEZE.get(), BreezeRenderer::new);
        event.registerEntityRenderer(ModEntities.BOGGED.get(), BoggedRenderer::new);
        event.registerEntityRenderer(ModEntities.WIND_CHARGE.get(), WindChargeRenderer::new);
        event.registerEntityRenderer(ModEntities.BREEZE_WIND_CHARGE.get(), WindChargeRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.TRIAL_SPAWNER.get(), TrialSpawnerRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.VAULT.get(), VaultRenderer::new);
    }
}
