package com.breakinblocks.justtrialspawners.common.item;

import com.breakinblocks.justtrialspawners.client.renderer.TrialSpawnerItemRenderer;
import com.breakinblocks.justtrialspawners.util.TrialSpawnerNbtHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;

public class TrialSpawnerBlockItem extends BlockItem {

    public TrialSpawnerBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private TrialSpawnerItemRenderer renderer;

            @Override
            public net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (renderer == null) {
                    renderer = new TrialSpawnerItemRenderer(
                            net.minecraft.client.Minecraft.getInstance().getItemRenderer().getBlockEntityRenderer());
                }
                return renderer;
            }
        });
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        String entityId = TrialSpawnerNbtHelper.getEntityIdFromItemStack(stack);
        if (entityId != null) {
            ResourceLocation rl = ResourceLocation.tryParse(entityId);
            if (rl != null) {
                String translationKey = "entity." + rl.getNamespace() + "." + rl.getPath();
                tooltip.add(Component.translatable(translationKey).withStyle(ChatFormatting.GRAY));
            }
        }
    }
}
