package me.jellysquid.mods.sodium.client.render.chunk.data;

import net.minecraft.client.renderer.chunk.SetVisibility;

/** Stub for compilation only — provided at runtime by Neonium. */
public class ChunkRenderData {

    public static class Builder {
        private SetVisibility occlusionData;
        private ChunkRenderBounds bounds;

        public void setOcclusionData(SetVisibility data) {
            this.occlusionData = data;
        }

        public void setBounds(ChunkRenderBounds bounds) {
            this.bounds = bounds;
        }
    }
}
