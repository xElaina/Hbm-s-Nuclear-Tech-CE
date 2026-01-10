package com.hbm.render.tileentity;

import com.hbm.blocks.ModBlocks;
import com.hbm.interfaces.AutoRegister;
import com.hbm.items.machine.ItemBatteryPack;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.tileentity.machine.storage.TileEntityBatterySocket;
import com.hbm.util.EnumUtil;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.Item;
import org.lwjgl.opengl.GL11;
@AutoRegister
public class RenderBatterySocket extends TileEntitySpecialRenderer<TileEntityBatterySocket> implements IItemRendererProvider {

    @Override
    public void render(TileEntityBatterySocket tile, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(x + 0.5D, y, z + 0.5D);

        GlStateManager.enableLighting();
        GlStateManager.enableCull();

        switch(tile.getBlockMetadata() - 10) {
            case 2: GL11.glRotatef(90, 0F, 1F, 0F); break;
            case 4: GL11.glRotatef(180, 0F, 1F, 0F); break;
            case 3: GL11.glRotatef(270, 0F, 1F, 0F); break;
            case 5: GL11.glRotatef(0, 0F, 1F, 0F); break;
        }

        GlStateManager.translate(-0.5D, 0D, 0.5D);

        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        bindTexture(ResourceManager.battery_socket_tex);
        ResourceManager.battery_socket.renderPart("Socket");

        if (tile.renderPack >= 0) {
            ItemBatteryPack.EnumBatteryPack pack = EnumUtil.grabEnumSafely(ItemBatteryPack.EnumBatteryPack.class, tile.renderPack);
            if (pack != null) {
                bindTexture(pack.texture);
                ResourceManager.battery_socket.renderPart(pack.isCapacitor() ? "Capacitor" : "Battery");
            }
        }

        GlStateManager.shadeModel(GL11.GL_FLAT);

        GlStateManager.popMatrix();
    }

    @Override
    public Item getItemForRenderer() {
        return Item.getItemFromBlock(ModBlocks.machine_battery_socket);
    }

    @Override
    public ItemRenderBase getRenderer(Item item) {
        return new ItemRenderBase() {
            @Override
            public void renderInventory() {
                GlStateManager.translate(0D, -2D, 0D);
                GlStateManager.scale(5D, 5D, 5D);
            }

            @Override
            public void renderCommon() {
                GlStateManager.shadeModel(GL11.GL_SMOOTH);
                bindTexture(ResourceManager.battery_socket_tex);
                ResourceManager.battery_socket.renderPart("Socket");
                GlStateManager.shadeModel(GL11.GL_FLAT);
            }
        };
    }
}
