package com.breakinblocks.justtrialspawners.client.model;

import com.breakinblocks.justtrialspawners.JustTrialSpawners;
import com.breakinblocks.justtrialspawners.common.block.entity.AppearanceBlockEntity;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.ChunkRenderTypeSet;
import net.minecraftforge.client.model.BakedModelWrapper;
import net.minecraftforge.client.model.IQuadTransformer;
import net.minecraftforge.client.model.QuadTransformers;
import net.minecraftforge.client.model.data.ModelData;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Selects a baked resource-pack model using the individual block entity's model data. */
public class AppearanceModel extends BakedModelWrapper<BakedModel> {
    private final Map<ResourceLocation, BakedModel> appearances;
    private final String stateName;
    private final boolean ominous;
    private final int quarterTurns;
    private final IQuadTransformer rotation;

    public AppearanceModel(BakedModel original, Map<ResourceLocation, BakedModel> appearances,
                           String stateName, boolean ominous, int quarterTurns) {
        super(original);
        this.appearances = appearances;
        this.stateName = stateName;
        this.ominous = ominous;
        this.quarterTurns = quarterTurns;
        this.rotation = QuadTransformers.applying(BlockModelRotation.by(0, quarterTurns * 90)
                .getRotation().blockCenterToCorner());
    }

    public static void wrapModels(Map<ResourceLocation, BakedModel> registry, @Nullable BakedModel missing) {
        Map<ResourceLocation, BakedModel> appearances = new HashMap<>();
        registry.forEach((id, model) -> {
            if (id.getPath().startsWith("justtrialspawners/") && model != null && model != missing) {
                appearances.put(id, model);
            }
        });
        Map<ResourceLocation, BakedModel> models = Map.copyOf(appearances);
        registry.replaceAll((id, model) -> {
            if (!(id instanceof ModelResourceLocation location)
                    || !id.getNamespace().equals(JustTrialSpawners.MOD_ID)
                    || location.getVariant().equals("inventory")) return model;
            boolean vault = id.getPath().equals("vault");
            if (!vault && !id.getPath().equals("trial_spawner")) return model;

            Map<String, String> properties = new HashMap<>();
            for (String property : location.getVariant().split(",")) {
                String[] pair = property.split("=", 2);
                if (pair.length == 2) properties.put(pair[0], pair[1]);
            }
            String state = properties.getOrDefault(vault ? "vault_state" : "trial_spawner_state", "inactive");
            int turns = switch (properties.getOrDefault("facing", "north")) {
                case "east" -> 1;
                case "south" -> 2;
                case "west" -> 3;
                default -> 0;
            };
            return new AppearanceModel(model, models, state,
                    Boolean.parseBoolean(properties.get("ominous")), turns);
        });
    }

    private BakedModel resolve(ModelData data) {
        ResourceLocation id = data.get(AppearanceBlockEntity.APPEARANCE);
        if (id == null) return originalModel;
        if (ominous) {
            BakedModel model = appearances.get(id.withSuffix("/" + stateName + "_ominous"));
            if (model != null) return model;
            model = appearances.get(id.withSuffix("/ominous"));
            if (model != null) return model;
        }
        BakedModel model = appearances.get(id.withSuffix("/" + stateName));
        return model != null ? model : appearances.getOrDefault(id, originalModel);
    }

    @Nullable
    private static Direction rotate(@Nullable Direction direction, int turns) {
        if (direction == null || direction.getAxis().isVertical()) return direction;
        for (int i = 0; i < turns; i++) direction = direction.getClockWise();
        return direction;
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side,
                                    RandomSource random, ModelData data, @Nullable RenderType renderType) {
        BakedModel model = resolve(data);
        if (model == originalModel || quarterTurns == 0) {
            return model.getQuads(state, side, random, data, renderType);
        }
        List<BakedQuad> quads = model.getQuads(state, rotate(side, 4 - quarterTurns), random, data, renderType);
        List<BakedQuad> rotated = new ArrayList<>(quads.size());
        for (BakedQuad quad : quads) {
            BakedQuad copy = new BakedQuad(quad.getVertices().clone(), quad.getTintIndex(),
                    rotate(quad.getDirection(), quarterTurns), quad.getSprite(), quad.isShade(), quad.hasAmbientOcclusion());
            rotation.processInPlace(copy);
            rotated.add(copy);
        }
        return rotated;
    }

    @Override
    public ChunkRenderTypeSet getRenderTypes(BlockState state, RandomSource random, ModelData data) {
        return resolve(data).getRenderTypes(state, random, data);
    }

    @Override
    public TextureAtlasSprite getParticleIcon(ModelData data) {
        return resolve(data).getParticleIcon(data);
    }
}
