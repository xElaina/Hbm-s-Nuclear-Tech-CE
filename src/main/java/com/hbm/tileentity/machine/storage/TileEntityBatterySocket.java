package com.hbm.tileentity.machine.storage;

import com.hbm.api.energymk2.IBatteryItem;
import com.hbm.interfaces.AutoRegister;
import com.hbm.inventory.container.ContainerBatterySocket;
import com.hbm.inventory.gui.GUIBatterySocket;
import com.hbm.items.ModItems;
import com.hbm.lib.DirPos;
import com.hbm.lib.ForgeDirection;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@AutoRegister
public class TileEntityBatterySocket extends TileEntityBatteryBase {

    public long[] log = new long[20];
    public long delta = 0;

    public int renderPack = -1;

    public TileEntityBatterySocket() {
        super(1);
    }

    @Override
    public String getDefaultName() {
        return "container.batterySocket";
    }

    @Override
    public void update() {
        long prevPower = this.getPower();

        super.update();

        if (!world.isRemote) {

            long avg = (this.getPower() + prevPower) / 2;
            this.delta = avg - this.log[0];

            for (int i = 1; i < this.log.length; i++) {
                this.log[i - 1] = this.log[i];
            }

            this.log[19] = avg;
        }
    }

    @Override
    public void serialize(ByteBuf buf) {
        super.serialize(buf);

        int renderPack = -1;
        if (!inventory.getStackInSlot(0).isEmpty() && inventory.getStackInSlot(0).getItem() == ModItems.battery_pack) {
            renderPack = inventory.getStackInSlot(0).getItemDamage();
        }
        buf.writeInt(renderPack);
        buf.writeLong(delta);
    }

    @Override
    public void deserialize(ByteBuf buf) {
        super.deserialize(buf);
        renderPack = buf.readInt();
        delta = buf.readLong();
    }

    @Override
    public boolean canExtractItem(int i, ItemStack stack, int j) {
        if (stack.getItem() instanceof IBatteryItem) {
            if (i == mode_input && ((IBatteryItem) stack.getItem()).getCharge(stack) == 0) return true;
            return i == mode_output && ((IBatteryItem) stack.getItem()).getCharge(stack) == ((IBatteryItem) stack.getItem()).getMaxCharge(stack);
        }
        return false;
    }

    @Override
    public int[] getAccessibleSlotsFromSide(EnumFacing side) {
        return new int[]{0};
    }

    @Override
    public long getPower() {
        if (inventory.getStackInSlot(0).isEmpty() || !(inventory.getStackInSlot(0).getItem() instanceof IBatteryItem))
            return 0;
        return ((IBatteryItem) inventory.getStackInSlot(0).getItem()).getCharge(inventory.getStackInSlot(0));
    }

    @Override
    public void setPower(long power) {
        if (inventory.getStackInSlot(0).isEmpty() || !(inventory.getStackInSlot(0).getItem() instanceof IBatteryItem))
            return;
        ((IBatteryItem) inventory.getStackInSlot(0).getItem()).setCharge(inventory.getStackInSlot(0), power);
    }

    @Override
    public long getMaxPower() {
        if (inventory.getStackInSlot(0).isEmpty() || !(inventory.getStackInSlot(0).getItem() instanceof IBatteryItem))
            return 0;
        return ((IBatteryItem) inventory.getStackInSlot(0).getItem()).getMaxCharge(inventory.getStackInSlot(0));
    }

    @Override
    public long getProviderSpeed() {
        if (inventory.getStackInSlot(0).isEmpty() || !(inventory.getStackInSlot(0).getItem() instanceof IBatteryItem))
            return 0;
        int mode = this.getRelevantMode(true);
        return mode == mode_output || mode == mode_buffer ? ((IBatteryItem) inventory.getStackInSlot(0).getItem()).getDischargeRate(inventory.getStackInSlot(0)) : 0;
    }

    @Override
    public long getReceiverSpeed() {
        if (inventory.getStackInSlot(0).isEmpty() || !(inventory.getStackInSlot(0).getItem() instanceof IBatteryItem))
            return 0;
        int mode = this.getRelevantMode(true);
        return mode == mode_input || mode == mode_buffer ? ((IBatteryItem) inventory.getStackInSlot(0).getItem()).getChargeRate(inventory.getStackInSlot(0)) : 0;
    }

    @Override
    public BlockPos[] getPortPos() {
        ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - 10);
        ForgeDirection rot = dir.getRotation(ForgeDirection.UP);
        return new BlockPos[]{
                new BlockPos(pos.getX(), pos.getY(), pos.getZ()),
                new BlockPos(pos.getX() - dir.offsetX, pos.getY(), pos.getZ() - dir.offsetZ),
                new BlockPos(pos.getX() + rot.offsetX, pos.getY(), pos.getZ() + rot.offsetZ),
                new BlockPos(pos.getX() - dir.offsetX + rot.offsetX, pos.getY(), pos.getZ() - dir.offsetZ + rot.offsetZ)
        };
    }

    @Override
    public DirPos[] getConPos() {
        ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - 10);
        ForgeDirection rot = dir.getRotation(ForgeDirection.UP);
        return new DirPos[]{
                new DirPos(pos.getX() + dir.offsetX, pos.getY(), pos.getZ() + dir.offsetZ, dir),
                new DirPos(pos.getX() + dir.offsetX + rot.offsetX, pos.getY(), pos.getZ() + dir.offsetZ + rot.offsetZ, dir),

                new DirPos(pos.getX() - dir.offsetX * 2, pos.getY(), pos.getZ() - dir.offsetZ * 2, dir.getOpposite()),
                new DirPos(pos.getX() - dir.offsetX * 2 + rot.offsetX, pos.getY(), pos.getZ() - dir.offsetZ * 2 + rot.offsetZ, dir.getOpposite()),

                new DirPos(pos.getX() + rot.offsetX * 2, pos.getY(), pos.getZ() + rot.offsetZ * 2, rot),
                new DirPos(pos.getX() + rot.offsetX * 2 - dir.offsetX, pos.getY(), pos.getZ() + rot.offsetZ * 2 - dir.offsetZ, rot),

                new DirPos(pos.getX() - rot.offsetX, pos.getY(), pos.getZ() - rot.offsetZ, rot.getOpposite()),
                new DirPos(pos.getX() - rot.offsetX - dir.offsetX, pos.getY(), pos.getZ() - rot.offsetZ - dir.offsetZ, rot.getOpposite())
        };
    }

    @Override
    public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new ContainerBatterySocket(player.inventory, this);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new GUIBatterySocket(player.inventory, this);
    }

    AxisAlignedBB bb = null;

    @Override
    public AxisAlignedBB getRenderBoundingBox() {

        if (bb == null) {
            bb = new AxisAlignedBB(
                    pos.getX() - 1,
                    pos.getY(),
                    pos.getZ() - 1,
                    pos.getX() + 2,
                    pos.getY() + 2,
                    pos.getZ() + 2
            );
        }

        return bb;
    }
}
