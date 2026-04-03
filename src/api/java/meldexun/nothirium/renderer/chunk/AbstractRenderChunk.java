package meldexun.nothirium.renderer.chunk;

import meldexun.nothirium.util.VisibilitySet;
import meldexun.renderlib.util.Frustum;

/** Stub for compilation only — provided at runtime by Nothirium. */
public abstract class AbstractRenderChunk {
    private VisibilitySet visibilitySet = new VisibilitySet();

    public int getX() { return 0; }
    public int getY() { return 0; }
    public int getZ() { return 0; }
    public boolean isFogCulled(double cameraX, double cameraY, double cameraZ, double fogEndSqr) { return false; }
    public boolean isFrustumCulled(Frustum frustum) { return false; }
    public void setVisibility(VisibilitySet visibilitySet) {
        this.visibilitySet = visibilitySet;
    }
}
