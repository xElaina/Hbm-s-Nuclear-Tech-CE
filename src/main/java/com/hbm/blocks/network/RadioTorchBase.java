package com.hbm.blocks.network;

import com.hbm.blocks.ILookOverlay;
import com.hbm.blocks.ITooltipProvider;
import com.hbm.main.MainRegistry;
import com.hbm.tileentity.IGUIProvider;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.internal.FMLNetworkHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

public abstract class RadioTorchBase extends BlockContainer
    implements IGUIProvider, ILookOverlay, ITooltipProvider {

  public static final PropertyDirection FACING = PropertyDirection.create("facing");
  public static final PropertyBool LIT = PropertyBool.create("lit");

  public RadioTorchBase() {
    super(Material.CIRCUITS);
    setSoundType(SoundType.WOOD);
    setDefaultState(
        this.blockState
            .getBaseState()
            .withProperty(FACING, EnumFacing.UP)
            .withProperty(LIT, false));
  }

  @Override
  public IBlockState getStateForPlacement(
      World worldIn,
      BlockPos pos,
      EnumFacing facing,
      float hitX,
      float hitY,
      float hitZ,
      int meta,
      EntityLivingBase placer) {

    return this.getDefaultState().withProperty(FACING, facing).withProperty(LIT, false);
  }

  @Override
  protected BlockStateContainer createBlockState() {
    return new BlockStateContainer(this, FACING, LIT);
  }

  @Override
  public IBlockState getStateFromMeta(int meta) {
    boolean lit = (meta & 1) == 1;
    EnumFacing facing = EnumFacing.byIndex((meta >> 1) & 7);
    return getDefaultState().withProperty(LIT, lit).withProperty(FACING, facing);
  }

  @Override
  public int getMetaFromState(IBlockState state) {
    int m = state.getValue(LIT) ? 1 : 0;
    m |= (state.getValue(FACING).getIndex() << 1);
    return m;
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
  @SideOnly(Side.CLIENT)
  public BlockRenderLayer getRenderLayer() {
    return BlockRenderLayer.CUTOUT;
  }

  @Override
  public boolean isOpaqueCube(IBlockState state) {
    return false;
  }

  @Override
  public boolean isFullCube(IBlockState state) {
    return false;
  }

  @Override
  @SideOnly(Side.CLIENT)
  public boolean shouldSideBeRendered(
      IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
    return true;
  }

  @Override
  public EnumBlockRenderType getRenderType(IBlockState state) {
    return EnumBlockRenderType.MODEL;
  }

  private static AxisAlignedBB makeAABBFor(EnumFacing dir) {
    double minX = dir.getXOffset() == 1 ? 0.0 : 0.375;
    double minY = dir.getYOffset() == 1 ? 0.0 : 0.375;
    double minZ = dir.getZOffset() == 1 ? 0.0 : 0.375;

    double maxX = dir.getXOffset() == -1 ? 1.0 : 0.625;
    double maxY = dir.getYOffset() == -1 ? 1.0 : 0.625;
    double maxZ = dir.getZOffset() == -1 ? 1.0 : 0.625;

    return new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ);
  }

  @Override
  public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
    return makeAABBFor(state.getValue(FACING));
  }

  @Override
  public AxisAlignedBB getSelectedBoundingBox(IBlockState state, World worldIn, BlockPos pos) {
    return getBoundingBox(state, worldIn, pos).offset(pos);
  }

  @Nullable
  @Override
  public AxisAlignedBB getCollisionBoundingBox(
      IBlockState blockState, IBlockAccess worldIn, BlockPos pos) {
    return NULL_AABB;
  }

  @Override
  public void neighborChanged(
      IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
    EnumFacing dir = state.getValue(FACING);
    if (!canBlockStay(worldIn, pos, dir)) {
      worldIn.destroyBlock(pos, true);
    }
  }

  @Override
  public BlockFaceShape getBlockFaceShape(
      IBlockAccess worldIn, IBlockState state, BlockPos pos, EnumFacing face) {
    return BlockFaceShape.UNDEFINED;
  }

  @Override
  public boolean canPlaceBlockOnSide(World worldIn, BlockPos pos, EnumFacing side) {
    if (!super.canPlaceBlockOnSide(worldIn, pos, side)) return false;
    return canBlockStay(worldIn, pos, side);
  }

  private boolean canBlockStay(World world, BlockPos pos, EnumFacing dir) {
    BlockPos supportPos = pos.offset(dir.getOpposite());
    IBlockState supportState = world.getBlockState(supportPos);
    Block b = supportState.getBlock();

    return b.isSideSolid(supportState, world, supportPos, dir)
        || b.hasComparatorInputOverride(supportState)
        || b.canProvidePower(supportState)
        || (b.isFullCube(supportState) && !b.isAir(supportState, world, supportPos));
  }

  @Override
  public boolean onBlockActivated(
      World worldIn,
      BlockPos pos,
      IBlockState state,
      EntityPlayer playerIn,
      EnumHand hand,
      EnumFacing facing,
      float hitX,
      float hitY,
      float hitZ) {
    if (worldIn.isRemote && !playerIn.isSneaking()) {
      FMLNetworkHandler.openGui(
          playerIn, MainRegistry.instance, 0, worldIn, pos.getX(), pos.getY(), pos.getZ());
      return true;
    } else {
      return !playerIn.isSneaking();
    }
  }

  @Override
  public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
    return null;
  }

  @Override
  public void addInformation(
      ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
    addStandardInfo(tooltip);
  }
}
