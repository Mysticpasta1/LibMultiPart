/*
 * Copyright (c) 2019 AlexIIL
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package alexiil.mc.lib.multipart.impl;

import java.util.Objects;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.model.loading.v1.BlockStateResolver;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin.Context;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelResolver;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;

import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.util.Identifier;

import alexiil.mc.lib.multipart.impl.client.model.MultipartModel;
import alexiil.mc.lib.multipart.impl.client.render.MultipartBlockEntityRenderer;
import alexiil.mc.lib.multipart.impl.client.render.MultipartOutlineRenderer;

public class LibMultiPartClient implements ClientModInitializer {

    public static final Identifier MODEL_IDENTIFIER
        = Identifier.of(LibMultiPart.NAMESPACE, "container");

    private static MultipartModel.Unbaked unbaked = null;

    @Override
    public void onInitializeClient() {
        LibMultiPart.isWorldClientPredicate = w -> w != null && w == MinecraftClient.getInstance().world;
        RenderTickCounter renderTickCounter = MinecraftClient.getInstance().getRenderTickCounter();
        LibMultiPart.partialTickGetter = () -> renderTickCounter.getTickDelta(false);
        ModelLoadingPlugin.register(LibMultiPartClient::setupModelLoader);
        BlockRenderLayerMap.INSTANCE.putBlock(LibMultiPart.BLOCK, RenderLayer.getCutout());
        BlockEntityRendererFactories.register(LibMultiPart.BLOCK_ENTITY, MultipartBlockEntityRenderer::new);
        WorldRenderEvents.BLOCK_OUTLINE.register(MultipartOutlineRenderer.INSTANCE);
    }

    private static void setupModelLoader(Context pluginContext) {
        pluginContext.addModels(MODEL_IDENTIFIER);
        pluginContext.resolveModel().register(LibMultiPartClient::resolveModel);
        pluginContext.registerBlockStateResolver(LibMultiPart.BLOCK, LibMultiPartClient::addBlockStateModels);
    }

    private static UnbakedModel resolveModel(ModelResolver.Context context) {
        var model = Objects.requireNonNullElseGet(unbaked, () -> unbaked = new MultipartModel.Unbaked());
        return MODEL_IDENTIFIER.equals(context.id()) ? model : null;
    }

    private static void addBlockStateModels(BlockStateResolver.Context context) {
        var model = Objects.requireNonNullElseGet(unbaked, () -> unbaked = new MultipartModel.Unbaked());

        for (BlockState state : LibMultiPart.BLOCK.getStateManager().getStates()) {
            context.setModel(state, model);
        }
    }
}
