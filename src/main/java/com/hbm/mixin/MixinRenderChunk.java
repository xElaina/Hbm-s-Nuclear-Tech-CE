package com.hbm.mixin;

import com.hbm.main.client.StaticTesrBakedModels;
import com.hbm.render.chunk.IOversizedModelExtentsHolder;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.chunk.CompiledChunk;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.client.renderer.chunk.SetVisibility;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderChunk.class)
public abstract class MixinRenderChunk {

    @Unique
    private int hbm$negX;
    @Unique
    private int hbm$posX;
    @Unique
    private int hbm$negY;
    @Unique
    private int hbm$posY;
    @Unique
    private int hbm$negZ;
    @Unique
    private int hbm$posZ;

    @Shadow
    public AxisAlignedBB boundingBox;
    @Final
    @Shadow
    private BlockPos.MutableBlockPos position;

    @Inject(method = "rebuildChunk", at = @At("HEAD"))
    private void hbm$resetOversizedExtents(float x, float y, float z, net.minecraft.client.renderer.chunk.ChunkCompileTaskGenerator generator, CallbackInfo ci) {
        hbm$negX = 0;
        hbm$posX = 0;
        hbm$negY = 0;
        hbm$posY = 0;
        hbm$negZ = 0;
        hbm$posZ = 0;
    }

    @Redirect(method = "rebuildChunk", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/BlockRendererDispatcher;renderBlock(Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/world/IBlockAccess;Lnet/minecraft/client/renderer/BufferBuilder;)Z"))
    private boolean hbm$trackOversizedBlock(BlockRendererDispatcher dispatcher, IBlockState state, BlockPos pos,
                                            IBlockAccess world, BufferBuilder buffer) {
        boolean result = dispatcher.renderBlock(state, pos, world, buffer);
        if (result) {
            int[] extents = StaticTesrBakedModels.getManagedRenderExtents(state);
            if (extents != null) {
                int localX = pos.getX() - position.getX();
                int localY = pos.getY() - position.getY();
                int localZ = pos.getZ() - position.getZ();

                hbm$negX = Math.max(hbm$negX, extents[4] - localX);
                hbm$posX = Math.max(hbm$posX, localX + extents[5] - 15);
                hbm$negY = Math.max(hbm$negY, extents[1] - localY);
                hbm$posY = Math.max(hbm$posY, localY + extents[0] - 15);
                hbm$negZ = Math.max(hbm$negZ, extents[2] - localZ);
                hbm$posZ = Math.max(hbm$posZ, localZ + extents[3] - 15);
            }
        }
        return result;
    }

    @Redirect(method = "rebuildChunk", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/chunk/CompiledChunk;setVisibility(Lnet/minecraft/client/renderer/chunk/SetVisibility;)V"))
    private void hbm$publishOversizedExtents(CompiledChunk compiledChunk, SetVisibility visibility) {
        ((IOversizedModelExtentsHolder) compiledChunk).hbm$setOversizedModelExtents(hbm$negX, hbm$posX, hbm$negY, hbm$posY, hbm$negZ, hbm$posZ);
        compiledChunk.setVisibility(visibility);
    }

    @Inject(method = "setCompiledChunk", at = @At("RETURN"))
    private void hbm$applyExpansion(CompiledChunk compiledChunkIn, CallbackInfo ci) {
        double minX = position.getX();
        double minY = position.getY();
        double minZ = position.getZ();
        IOversizedModelExtentsHolder holder = (IOversizedModelExtentsHolder) compiledChunkIn;

        boundingBox = new AxisAlignedBB(
                minX - holder.hbm$getNegX(),
                minY - holder.hbm$getNegY(),
                minZ - holder.hbm$getNegZ(),
                minX + 16.0D + holder.hbm$getPosX(),
                minY + 16.0D + holder.hbm$getPosY(),
                minZ + 16.0D + holder.hbm$getPosZ());
    }
}
