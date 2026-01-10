package com.hbm.blocks.network;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ITooltipProvider;
import com.hbm.lib.ForgeDirection;
import com.hbm.tileentity.TileEntityProxyCombo;
import com.hbm.tileentity.machine.storage.TileEntityBatterySocket;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MachineBatterySocket extends BlockDummyable implements ITooltipProvider {

    public MachineBatterySocket(String s) {
        super(Material.IRON, s);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        if (meta >= 12) return new TileEntityBatterySocket();
        if (meta >= 6) return new TileEntityProxyCombo().inventory().power().conductor();
        return null;
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        return super.standardOpenBehavior(world, pos.getX(), pos.getY(), pos.getZ(), player, 0);
    }

    @Override
    public int[] getDimensions() {
        return new int[]{1, 0, 1, 0, 1, 0};
    }

    @Override
    public int getOffset() {
        return 0;
    }

    @Override
    protected void fillSpace(World world, int x, int y, int z, ForgeDirection dir, int o) {
        super.fillSpace(world, x, y, z, dir, o);

        ForgeDirection rot = dir.getRotation(ForgeDirection.UP);
        this.makeExtra(world, x - dir.offsetX, y, z - dir.offsetZ);
        this.makeExtra(world, x + rot.offsetX, y, z + rot.offsetZ);
        this.makeExtra(world, x - dir.offsetX + rot.offsetX, y, z - dir.offsetZ + rot.offsetZ);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        addStandardInfo(tooltip);
    }
}
