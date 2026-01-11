package com.hbm.render.tileentity;

import com.hbm.blocks.ModBlocks;
import com.hbm.interfaces.AutoRegister;
import com.hbm.main.MainRegistry;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.render.util.BeamPronter;
import com.hbm.tileentity.machine.storage.TileEntityBatteryREDD;
import com.hbm.util.BobMathUtil;
import com.hbm.util.Clock;
import com.hbm.util.RenderUtil;
import com.hbm.util.Vec3NT;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.Item;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Random;
@AutoRegister
public class RenderBatteryREDD extends TileEntitySpecialRenderer<TileEntityBatteryREDD> implements IItemRendererProvider {

    @Override
    public void render(TileEntityBatteryREDD tile, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(x + 0.5D, y, z + 0.5D);

        GlStateManager.enableLighting();
        GlStateManager.enableCull();

        switch (tile.getBlockMetadata() - 10) {
            case 2: GlStateManager.rotate(270, 0F, 1F, 0F); break;
            case 4: GlStateManager.rotate(0, 0F, 1F, 0F); break;
            case 3: GlStateManager.rotate(90, 0F, 1F, 0F); break;
            case 5: GlStateManager.rotate(180, 0F, 1F, 0F); break;
        }

        GlStateManager.shadeModel(GL11.GL_SMOOTH);

        bindTexture(ResourceManager.battery_redd_tex);
        ResourceManager.battery_redd.renderPart("Base");

        GlStateManager.pushMatrix();

        GlStateManager.translate(0D, 5.5D, 0D);
        float speed = tile.getSpeed();
        double rot = tile.prevRotation + (tile.rotation - tile.prevRotation) * (double) partialTicks;
        GlStateManager.rotate((float) rot, 1F, 0F, 0F);
        GlStateManager.translate(0D, -5.5D, 0D);

        ResourceManager.battery_redd.renderPart("Wheel");

        RenderArcFurnace.fullbright(true);
        ResourceManager.battery_redd.renderPart("Lights");
        RenderArcFurnace.fullbright(false);

        GlStateManager.pushMatrix();
        GlStateManager.translate(0D, 5.5D, 0D);

        //batteryAttribPushForAdditiveNoTex();
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240F, 240F);

        GlStateManager.disableCull();
        GlStateManager.disableTexture2D();

        GlStateManager.disableLighting();
        GlStateManager.pushAttrib();
        GlStateManager.disableAlpha();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
        GlStateManager.depthMask(false);

        Vec3NT vec = new Vec3NT(0, 0, 4);

        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();
        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);

        double len = 4.25D;
        double width = 0.125D;
        double span = speed * 0.75D;

        if (span > 0D) {
            for (int j = -1; j <= 1; j += 2) {
                for (int i = 0; i < 8; i++) {
                    double xOffset = 0.8125D * j;

                    vec.set(0, 1, 0);
                    vec.rotateAroundXDeg(i * 45D);

                    double y1 = vec.y * len - vec.y * width;
                    double z1 = vec.z * len - vec.z * width;
                    double y2 = vec.y * len + vec.y * width;
                    double z2 = vec.z * len + vec.z * width;
                    buf.pos(xOffset, y1, z1).color(1F, 1F, 0F, 0.75F).endVertex();
                    buf.pos(xOffset, y2, z2).color(1F, 1F, 0F, 0.75F).endVertex();
                    vec.rotateAroundXDeg(span);
                    buf.pos(xOffset, y2, z2).color(1F, 1F, 0F, 0.5F).endVertex();
                    buf.pos(xOffset, y1, z1).color(1F, 1F, 0F, 0.5F).endVertex();

                    buf.pos(xOffset, y1, z1).color(1F, 1F, 0F, 0.5F).endVertex();
                    buf.pos(xOffset, y2, z2).color(1F, 1F, 0F, 0.5F).endVertex();
                    vec.rotateAroundXDeg(span);
                    buf.pos(xOffset, y2, z2).color(1F, 1F, 0F, 0.25F).endVertex();
                    buf.pos(xOffset, y1, z1).color(1F, 1F, 0F, 0.25F).endVertex();

                    buf.pos(xOffset, y1, z1).color(1F, 1F, 0F, 0.25F).endVertex();
                    buf.pos(xOffset, y2, z2).color(1F, 1F, 0F, 0.25F).endVertex();
                    vec.rotateAroundXDeg(span);
                    buf.pos(xOffset, y2, z2).color(1F, 1F, 0F, 0F).endVertex();
                    buf.pos(xOffset, y1, z1).color(1F, 1F, 0F, 0F).endVertex();
                }
            }
        }

        tess.draw();

        /*batteryAttribPop();
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 0F, 0F);*/

        GlStateManager.enableLighting();
        GlStateManager.enableAlpha();
        GlStateManager.disableBlend();
        GlStateManager.depthMask(true);
        GlStateManager.popAttrib();

        GlStateManager.enableCull();
        GlStateManager.enableTexture2D();

        GlStateManager.popMatrix();

        renderSparkle(tile);

        GlStateManager.popMatrix();

        if (speed > 0F) {
            renderZaps(tile);
        }

        GlStateManager.shadeModel(GL11.GL_FLAT);

        GlStateManager.popMatrix();
    }

    protected void renderSparkle(TileEntityBatteryREDD tile) {
        long time = Clock.get_ms();
        float a = 0.45F + (float) (Math.sin(time / 1000D) * 0.15D);
        float alphaMult = tile.getSpeed() / 15F;
        float r = 1.0F;
        float g = 0.25F;
        float b = 0.75F;

        double mainOsc = BobMathUtil.sps(time / 1000D) % 1D;
        double sparkleSpin = (time / 250D * -1D) % 1D;
        double sparkleOsc = (Math.sin(time / 1000D) * 0.5D) % 1D;

        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240F, 240F);
        GlStateManager.disableCull();

        GlStateManager.disableLighting();
        GlStateManager.pushAttrib();
        GlStateManager.disableAlpha();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
        GlStateManager.depthMask(false);

        GlStateManager.color(r, g, b, a * alphaMult);

        GlStateManager.matrixMode(GL11.GL_TEXTURE);
        GlStateManager.loadIdentity();
        bindTexture(ResourceManager.fusion_plasma_tex);
        GlStateManager.translate(0D, mainOsc, 0D);
        ResourceManager.battery_redd.renderPart("Plasma");
        GlStateManager.matrixMode(GL11.GL_TEXTURE);
        GlStateManager.loadIdentity();
        GlStateManager.matrixMode(GL11.GL_MODELVIEW);

        if (MainRegistry.proxy.me().getDistanceSq(tile.getPos().getX() + 0.5D, tile.getPos().getY() + 2.5D, tile.getPos().getZ() + 0.5D) < 100D * 100D) {
            GlStateManager.color(r * 2F, g * 2F, b * 2F, 0.75F * alphaMult);

            GlStateManager.matrixMode(GL11.GL_TEXTURE);
            GlStateManager.loadIdentity();
            bindTexture(ResourceManager.fusion_plasma_sparkle_tex);
            GlStateManager.translate(sparkleSpin, sparkleOsc, 0D);
            ResourceManager.battery_redd.renderPart("Plasma");
            GlStateManager.matrixMode(GL11.GL_TEXTURE);
            GlStateManager.loadIdentity();
            GlStateManager.matrixMode(GL11.GL_MODELVIEW);
        }

        GlStateManager.enableLighting();
        GlStateManager.enableAlpha();
        GlStateManager.disableBlend();
        GlStateManager.depthMask(true);
        GlStateManager.popAttrib();

        GlStateManager.enableCull();
    }

    protected void renderZaps(TileEntityBatteryREDD tile) {
        Random rand = new Random(tile.getWorld().getTotalWorldTime() / 5L);
        rand.nextBoolean();

        if (rand.nextBoolean()) {
            GlStateManager.pushMatrix();
            GlStateManager.translate(3.125D, 5.5D, 0D);
            BeamPronter.prontBeam(new Vec3d(-1.375D, -2.625D, 3.75D), BeamPronter.EnumWaveType.RANDOM, BeamPronter.EnumBeamType.SOLID, 0x404040, 0x002040,
                    (int) (System.currentTimeMillis() % 1000L) / 50, 15, 0.25F, 3, 0.0625F);
            BeamPronter.prontBeam(new Vec3d(-1.375D, -2.625D, 3.75D), BeamPronter.EnumWaveType.RANDOM, BeamPronter.EnumBeamType.SOLID, 0x404040, 0x002040,
                    (int) (System.currentTimeMillis() % 1000L) / 50, 1, 0F, 3, 0.0625F);
            GlStateManager.popMatrix();
        }

        if (rand.nextBoolean()) {
            GlStateManager.pushMatrix();
            GlStateManager.translate(-3.125D, 5.5D, 0D);
            BeamPronter.prontBeam(new Vec3d(1.375D, -2.625D, 3.75D), BeamPronter.EnumWaveType.RANDOM, BeamPronter.EnumBeamType.SOLID, 0x404040, 0x002040,
                    (int) (System.currentTimeMillis() % 1000L) / 50, 15, 0.25F, 3, 0.0625F);
            BeamPronter.prontBeam(new Vec3d(1.375D, -2.625D, 3.75D), BeamPronter.EnumWaveType.RANDOM, BeamPronter.EnumBeamType.SOLID, 0x404040, 0x002040,
                    (int) (System.currentTimeMillis() % 1000L) / 50, 1, 0F, 3, 0.0625F);
            GlStateManager.popMatrix();
        }

        if (rand.nextBoolean()) {
            GlStateManager.pushMatrix();
            GlStateManager.translate(3.125D, 5.5D, 0D);
            BeamPronter.prontBeam(new Vec3d(-1.375D, -2.625D, -3.75D), BeamPronter.EnumWaveType.RANDOM, BeamPronter.EnumBeamType.SOLID, 0x404040, 0x002040,
                    (int) (System.currentTimeMillis() % 1000L) / 50, 15, 0.25F, 3, 0.0625F);
            BeamPronter.prontBeam(new Vec3d(-1.375D, -2.625D, -3.75D), BeamPronter.EnumWaveType.RANDOM, BeamPronter.EnumBeamType.SOLID, 0x404040, 0x002040,
                    (int) (System.currentTimeMillis() % 1000L) / 50, 1, 0F, 3, 0.0625F);
            GlStateManager.popMatrix();
        }

        if (rand.nextBoolean()) {
            GlStateManager.pushMatrix();
            GlStateManager.translate(-3.125D, 5.5D, 0D);
            BeamPronter.prontBeam(new Vec3d(1.375D, -2.625D, -3.75D), BeamPronter.EnumWaveType.RANDOM, BeamPronter.EnumBeamType.SOLID, 0x404040, 0x002040,
                    (int) (System.currentTimeMillis() % 1000L) / 50, 15, 0.25F, 3, 0.0625F);
            BeamPronter.prontBeam(new Vec3d(1.375D, -2.625D, -3.75D), BeamPronter.EnumWaveType.RANDOM, BeamPronter.EnumBeamType.SOLID, 0x404040, 0x002040,
                    (int) (System.currentTimeMillis() % 1000L) / 50, 1, 0F, 3, 0.0625F);
            GlStateManager.popMatrix();
        }
    }
    // TODO
    // note: I did that based on ItemRenderWeaponBase, don't know if that's correct
    // for now I'll comment usages as it doesn't fix the issue - would love some advice here, mov
    private static final ThreadLocal<Deque<BatteryAttribState>> BATTERY_ATTRIB_STACK =
            ThreadLocal.withInitial(ArrayDeque::new);

    private static final class BatteryAttribState {
        boolean alpha;
        boolean blend;
        boolean cull;
        boolean lighting;
        boolean texture2d;
        boolean depthMask;

        int srcRGB, dstRGB, srcA, dstA;

        float r, g, b, a;
        int shadeModel;
        int matrixMode;

        int alphaFunc;
        float alphaRef;
        float lightX, lightY;
    }

    private static void batteryAttribPushForAdditiveNoTex() {
        final BatteryAttribState s = new BatteryAttribState();

        s.alpha    = RenderUtil.isAlphaEnabled();
        s.blend    = RenderUtil.isBlendEnabled();
        s.cull     = RenderUtil.isCullEnabled();
        s.lighting = RenderUtil.isLightingEnabled();
        s.texture2d = RenderUtil.isTexture2DEnabled();
        s.depthMask = RenderUtil.isDepthMaskEnabled();

        s.srcRGB = RenderUtil.getBlendSrcFactor();
        s.dstRGB = RenderUtil.getBlendDstFactor();
        s.srcA   = RenderUtil.getBlendSrcAlphaFactor();
        s.dstA   = RenderUtil.getBlendDstAlphaFactor();

        s.r = RenderUtil.getCurrentColorRed();
        s.g = RenderUtil.getCurrentColorGreen();
        s.b = RenderUtil.getCurrentColorBlue();
        s.a = RenderUtil.getCurrentColorAlpha();

        s.shadeModel = RenderUtil.getShadeModel();
        s.matrixMode = GL11.glGetInteger(GL11.GL_MATRIX_MODE);

        s.alphaFunc = RenderUtil.getAlphaFunc();
        s.alphaRef  = RenderUtil.getAlphaRef();
        s.lightX    = OpenGlHelper.lastBrightnessX;
        s.lightY    = OpenGlHelper.lastBrightnessY;

        BATTERY_ATTRIB_STACK.get().push(s);

        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.0F);
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240F, 240F);

        if (s.cull) GlStateManager.disableCull();
        if (s.texture2d) GlStateManager.disableTexture2D();
        if (s.lighting) GlStateManager.disableLighting();

        if (s.alpha) GlStateManager.disableAlpha();

        if (!s.blend) GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA.factor, GlStateManager.DestFactor.ONE.factor,
                GlStateManager.SourceFactor.ONE.factor, GlStateManager.DestFactor.ZERO.factor
        );

        GlStateManager.depthMask(false);
    }

    private static void batteryAttribPop() {
        final BatteryAttribState s = BATTERY_ATTRIB_STACK.get().poll();
        if (s == null) return;

        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, s.lightX, s.lightY);
        GlStateManager.alphaFunc(s.alphaFunc, s.alphaRef);

        GlStateManager.matrixMode(s.matrixMode);
        GlStateManager.shadeModel(s.shadeModel);

        GlStateManager.color(s.r, s.g, s.b, s.a);

        GlStateManager.tryBlendFuncSeparate(s.srcRGB, s.dstRGB, s.srcA, s.dstA);
        if (s.blend) GlStateManager.enableBlend();
        else GlStateManager.disableBlend();

        if (s.alpha) GlStateManager.enableAlpha();
        else GlStateManager.disableAlpha();

        GlStateManager.depthMask(s.depthMask);

        if (s.lighting) GlStateManager.enableLighting();
        else GlStateManager.disableLighting();

        if (s.texture2d) GlStateManager.enableTexture2D();
        else GlStateManager.disableTexture2D();

        if (s.cull) GlStateManager.enableCull();
        else GlStateManager.disableCull();
    }

    @Override
    public Item getItemForRenderer() {
        return Item.getItemFromBlock(ModBlocks.machine_battery_redd);
    }

    @Override
    public ItemRenderBase getRenderer(Item item) {
        return new ItemRenderBase() {
            @Override
            public void renderInventory() {
                GlStateManager.translate(0D, -3D, 0D);
                GlStateManager.scale(2.5D, 2.5D, 2.5D);
            }

            @Override
            public void renderCommon() {
                GlStateManager.rotate(-90F, 0F, 1F, 0F);
                GlStateManager.scale(0.5D, 0.5D, 0.5D);

                GlStateManager.shadeModel(GL11.GL_SMOOTH);
                bindTexture(ResourceManager.battery_redd_tex);
                ResourceManager.battery_redd.renderPart("Base");
                ResourceManager.battery_redd.renderPart("Wheel");

                GlStateManager.disableLighting();
                ResourceManager.battery_redd.renderPart("Lights");
                GlStateManager.enableLighting();

                GlStateManager.shadeModel(GL11.GL_FLAT);
            }
        };
    }

}
