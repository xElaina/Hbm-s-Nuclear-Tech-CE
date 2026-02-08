package com.hbm.blocks.network;

import com.hbm.blocks.ModBlocks;
import com.hbm.main.MainRegistry;
import com.hbm.tileentity.network.TileEntityRadioTorchReceiver;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.internal.FMLNetworkHandler;

public class RadioTorchReceiver extends RadioTorchRWBase {
  public RadioTorchReceiver(String regName) {
    super();
    this.setTranslationKey(regName);
    this.setRegistryName(regName);

    ModBlocks.ALL_BLOCKS.add(this);
  }

  @Override
  public TileEntity createNewTileEntity(World worldIn, int meta) {
    return new TileEntityRadioTorchReceiver();
  }

  @Override
  public boolean onBlockActivated(
      World world,
      BlockPos pos,
      IBlockState state,
      EntityPlayer player,
      EnumHand hand,
      EnumFacing facing,
      float hitX,
      float hitY,
      float hitZ) {
    if (world.isRemote) {
      return true;
    } else if (!player.isSneaking()) {
      FMLNetworkHandler.openGui(
          player, MainRegistry.instance, 0, world, pos.getX(), pos.getY(), pos.getZ());
      return true;
    } else {
      return false;
    }
  }

  @Override
  public void onBlockPlacedBy(
      World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
    worldIn.setBlockState(
        pos, state.withProperty(FACING, EnumFacing.getDirectionFromEntityLiving(pos, placer)), 2);
  }

  @Override
  public IBlockState withRotation(IBlockState state, Rotation rot) {
    return state.withProperty(FACING, rot.rotate(state.getValue(FACING)));
  }

  @Override
  public IBlockState withMirror(IBlockState state, Mirror mirrorIn) {
    return state.withRotation(mirrorIn.toRotation(state.getValue(FACING)));
  }

  @Override
  public boolean canProvidePower(IBlockState state) {
    return true;
  }

  @Override
  public boolean getWeakChanges(IBlockAccess world, BlockPos pos) {
    return false;
  }

  @Override
  public int getWeakPower(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {
    TileEntity tile = world.getTileEntity(pos);

    if (tile instanceof TileEntityRadioTorchReceiver) {
      int value = ((TileEntityRadioTorchReceiver) tile).lastState;
      return value;
    }

    return 0;
  }

  @Override
  public int getStrongPower(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {
    if (side == state.getValue(FACING).getOpposite()) return getWeakPower(state, world, pos, side);
    return 0;
  }
}
