package com.hbm.mixin.mod.neonium;

import com.hbm.main.client.StaticTesrBakedModels;
import com.hbm.render.chunk.IOversizedModelExtentsHolder;
import me.jellysquid.mods.sodium.client.render.chunk.compile.ChunkBuildBuffers;
import me.jellysquid.mods.sodium.client.render.chunk.compile.ChunkBuildResult;
import me.jellysquid.mods.sodium.client.render.chunk.data.ChunkRenderBounds;
import me.jellysquid.mods.sodium.client.render.chunk.data.ChunkRenderData;
import me.jellysquid.mods.sodium.client.render.chunk.tasks.ChunkRenderRebuildTask;
import me.jellysquid.mods.sodium.client.render.pipeline.context.ChunkRenderCacheLocal;
import me.jellysquid.mods.sodium.client.util.task.CancellationSource;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.chunk.SetVisibility;
import net.minecraft.client.renderer.chunk.VisGraph;
import net.minecraft.util.EnumBlockRenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ChunkRenderRebuildTask.class, remap = false)
public abstract class MixinChunkRenderRebuildTask {

    @Unique
    private int hbm$negX, hbm$posX, hbm$negY, hbm$posY, hbm$negZ, hbm$posZ;
    @Unique
    private int hbm$currentNorth, hbm$currentSouth, hbm$currentWest, hbm$currentEast, hbm$currentUp, hbm$currentDown;
    @Unique
    private boolean hbm$shouldTrackCurrentBlock;

    @Inject(method = "performBuild", at = @At("HEAD"))
    private void hbm$resetOversizedExtents(ChunkRenderCacheLocal cache, ChunkBuildBuffers buffers,
                                           CancellationSource cancellationSource,
                                           CallbackInfoReturnable<ChunkBuildResult<?>> cir) {
        hbm$negX = 0;
        hbm$posX = 0;
        hbm$negY = 0;
        hbm$posY = 0;
        hbm$negZ = 0;
        hbm$posZ = 0;
        hbm$shouldTrackCurrentBlock = false;
    }

    @Redirect(method = "performBuild", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/state/IBlockState;getRenderType()Lnet/minecraft/util/EnumBlockRenderType;"), remap = true)
    private EnumBlockRenderType hbm$trackCurrentBlock(IBlockState state) {
        int[] extents = StaticTesrBakedModels.getManagedRenderExtents(state);
        if (extents == null) {
            hbm$shouldTrackCurrentBlock = false;
            return state.getRenderType();
        }

        hbm$currentNorth = extents[2];
        hbm$currentSouth = extents[3];
        hbm$currentWest = extents[4];
        hbm$currentEast = extents[5];
        hbm$currentUp = extents[0];
        hbm$currentDown = extents[1];
        hbm$shouldTrackCurrentBlock = true;
        return state.getRenderType();
    }

    @Redirect(method = "performBuild", at = @At(value = "INVOKE", target = "Lme/jellysquid/mods/sodium/client/render/chunk/data/ChunkRenderBounds$Builder;addBlock(III)V"), remap = false)
    private void hbm$trackRenderedBlock(ChunkRenderBounds.Builder bounds, int relX, int relY, int relZ) {
        if (hbm$shouldTrackCurrentBlock) {
            hbm$negX = Math.max(hbm$negX, hbm$currentWest - relX);
            hbm$posX = Math.max(hbm$posX, relX + hbm$currentEast - 15);
            hbm$negY = Math.max(hbm$negY, hbm$currentDown - relY);
            hbm$posY = Math.max(hbm$posY, relY + hbm$currentUp - 15);
            hbm$negZ = Math.max(hbm$negZ, hbm$currentNorth - relZ);
            hbm$posZ = Math.max(hbm$posZ, relZ + hbm$currentSouth - 15);
        }
        bounds.addBlock(relX, relY, relZ);
    }

    @Redirect(method = "performBuild", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/chunk/VisGraph;computeVisibility()Lnet/minecraft/client/renderer/chunk/SetVisibility;"), remap = true)
    private SetVisibility hbm$publishOversizedExtents(VisGraph occluder) {
        SetVisibility visibility = occluder.computeVisibility();
        ((IOversizedModelExtentsHolder) visibility).hbm$setOversizedModelExtents(hbm$negX, hbm$posX, hbm$negY, hbm$posY, hbm$negZ, hbm$posZ);
        return visibility;
    }

    @Redirect(method = "performBuild", at = @At(value = "INVOKE", target = "Lme/jellysquid/mods/sodium/client/render/chunk/data/ChunkRenderData$Builder;setBounds(Lme/jellysquid/mods/sodium/client/render/chunk/data/ChunkRenderBounds;)V"), remap = false)
    private void hbm$expandBounds(ChunkRenderData.Builder renderData, ChunkRenderBounds original) {
        if ((hbm$negX | hbm$posX | hbm$negY | hbm$posY | hbm$negZ | hbm$posZ) == 0) {
            renderData.setBounds(original);
            return;
        }

        renderData.setBounds(new ChunkRenderBounds(
                original.x1 - hbm$negX,
                original.y1 - hbm$negY,
                original.z1 - hbm$negZ,
                original.x2 + hbm$posX,
                original.y2 + hbm$posY,
                original.z2 + hbm$posZ
        ));
    }
}
