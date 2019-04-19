package mod.lucky.structure.rotation;

import net.minecraft.block.*;
import net.minecraft.block.BlockRailBase.EnumRailDirection;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagFloat;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntitySkull;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public final class Rotations {
  private static StateRotationHandler[] stateRotationHandlers = new StateRotationHandler[30000];
  private static TileEntityRotationHandler[] tileEntityRotationHandlers =
      new TileEntityRotationHandler[30000];

  public static Vec3d rotatePos(Vec3d pos, Vec3d centerPos, int rotation) {
    rotation %= 4;
    if (rotation < 0) rotation += 4;

    double posX = pos.x - centerPos.x;
    double posY = pos.y - centerPos.y;
    double posZ = centerPos.z - pos.z;

    for (int i = 0; i < rotation; ++i) {
      double x = posX;
      double z = posZ;
      posX = z;
      posZ = -x;
    }
    return new Vec3d(posX + centerPos.x, posY + centerPos.y, centerPos.z - posZ);
  }

  public static BlockPos rotatePos(BlockPos blockPos, BlockPos centerPos, int rotation) {
    Vec3d vec3 =
        rotatePos(
            new Vec3d(blockPos.getX(), blockPos.getY(), blockPos.getZ()),
            new Vec3d(centerPos.getX(), centerPos.getY(), centerPos.getZ()),
            rotation);
    return new BlockPos(vec3);
  }

  public static BlockPos rotatePos(BlockPos blockPos, Vec3d centerPos, int rotation) {
    Vec3d vec3 =
        rotatePos(
            new Vec3d(blockPos.getX(), blockPos.getY(), blockPos.getZ()), centerPos, rotation);
    return new BlockPos(vec3);
  }

  public static IBlockState rotateState(IBlockState state, int rotation) {
    rotation %= 4;
    if (rotation < 0) rotation += 4;
    StateRotationHandler stateRotationHandler =
        stateRotationHandlers[Block.getIdFromBlock(state.getBlock())];
    if (stateRotationHandler != null) return stateRotationHandler.rotate(state, rotation);
    else return state;
  }

  public static NBTTagCompound rotateEntity(
      NBTTagCompound entityTag, Vec3d centerPos, int rotation) {
    rotation %= 4;
    if (rotation < 0) rotation += 4;

    if (entityTag.hasKey("Pos")) {
      NBTTagList posList = entityTag.getTagList("Pos", 6);
      Vec3d entityPos =
          new Vec3d(posList.getDoubleAt(0), posList.getDoubleAt(1), posList.getDoubleAt(2));
      entityPos = rotatePos(entityPos, centerPos, rotation);
      posList = new NBTTagList();
      posList.appendTag(new NBTTagDouble(entityPos.x));
      posList.appendTag(new NBTTagDouble(entityPos.y));
      posList.appendTag(new NBTTagDouble(entityPos.z));
      entityTag.setTag("Pos", posList);
    }

    if (entityTag.hasKey("Motion")) {
      NBTTagList motionList = entityTag.getTagList("Motion", 6);
      Vec3d entityMotion =
          new Vec3d(
              motionList.getDoubleAt(0), motionList.getDoubleAt(1), motionList.getDoubleAt(2));
      entityMotion = rotatePos(entityMotion, new Vec3d(0, 0, 0), rotation);
      motionList = new NBTTagList();
      motionList.appendTag(new NBTTagDouble(entityMotion.x));
      motionList.appendTag(new NBTTagDouble(entityMotion.y));
      motionList.appendTag(new NBTTagDouble(entityMotion.z));
      entityTag.setTag("Motion", motionList);
    }

    if (entityTag.hasKey("Rotation")) {
      NBTTagList rotationList = entityTag.getTagList("Rotation", 5);
      float rotYaw = rotationList.getFloatAt(0);
      float rotPitch = rotationList.getFloatAt(1);
      rotYaw = (rotYaw + (rotation * 90.0F)) % 360.0F;
      rotationList = new NBTTagList();
      rotationList.appendTag(new NBTTagFloat(rotYaw));
      rotationList.appendTag(new NBTTagFloat(rotPitch));
      entityTag.setTag("Rotation", rotationList);
    }
    return entityTag;
  }

  public static Entity rotateEntity(Entity entity, int rotation) {
    rotation %= 4;
    if (rotation < 0) rotation += 4;
    Vec3d motion =
        rotatePos(
            new Vec3d(entity.motionX, entity.motionY, entity.motionZ),
            new Vec3d(0, 0, 0),
            rotation);
    entity.motionX = motion.x;
    entity.motionY = motion.y;
    entity.motionZ = motion.z;
    entity.rotationYaw = (entity.rotationYaw + (rotation * 90.0F)) % 360.0F;
    return entity;
  }

  public static TileEntity rotateTileEntity(TileEntity tileEntity, int rotation) {
    rotation %= 4;
    if (rotation < 0) rotation += 4;
    TileEntityRotationHandler tileEntityRotationHandler =
        tileEntityRotationHandlers[Block.getIdFromBlock(tileEntity.getBlockType())];
    if (tileEntityRotationHandler != null) tileEntityRotationHandler.rotate(tileEntity, rotation);
    return tileEntity;
  }

  public static void registerRotationHandlers() {
    EnumFacing enumFacing[] = {
      EnumFacing.NORTH, EnumFacing.EAST, EnumFacing.SOUTH, EnumFacing.WEST
    };
    Integer intFacing[] = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15};
    stateRotationHandlers[Block.getIdFromBlock(Blocks.LOG)] =
        new ArrayStateRotationHandler(BlockLog.LOG_AXIS, BlockLog.EnumAxis.Z, BlockLog.EnumAxis.X);
    stateRotationHandlers[Block.getIdFromBlock(Blocks.TORCH)] =
        new ArrayStateRotationHandler(BlockTorch.FACING, enumFacing);
    stateRotationHandlers[Block.getIdFromBlock(Blocks.UNLIT_REDSTONE_TORCH)] =
        new ArrayStateRotationHandler(BlockRedstoneTorch.FACING, enumFacing);
    stateRotationHandlers[Block.getIdFromBlock(Blocks.REDSTONE_TORCH)] =
        new ArrayStateRotationHandler(BlockRedstoneTorch.FACING, enumFacing);
    stateRotationHandlers[Block.getIdFromBlock(Blocks.BED)] =
        new ArrayStateRotationHandler(BlockBed.FACING, enumFacing);
    stateRotationHandlers[Block.getIdFromBlock(Blocks.PISTON)] =
        new ArrayStateRotationHandler(BlockPistonBase.FACING, enumFacing);
    stateRotationHandlers[Block.getIdFromBlock(Blocks.STICKY_PISTON)] =
        new ArrayStateRotationHandler(BlockPistonBase.FACING, enumFacing);
    stateRotationHandlers[Block.getIdFromBlock(Blocks.PISTON_HEAD)] =
        new ArrayStateRotationHandler(BlockPistonExtension.FACING, enumFacing);
    stateRotationHandlers[Block.getIdFromBlock(Blocks.PISTON_EXTENSION)] =
        new ArrayStateRotationHandler(BlockPistonMoving.FACING, enumFacing);
    stateRotationHandlers[Block.getIdFromBlock(Blocks.OAK_STAIRS)] =
        new ArrayStateRotationHandler(BlockStairs.FACING, enumFacing);
    stateRotationHandlers[Block.getIdFromBlock(Blocks.STONE_STAIRS)] =
        new ArrayStateRotationHandler(BlockStairs.FACING, enumFacing);
    stateRotationHandlers[Block.getIdFromBlock(Blocks.BRICK_STAIRS)] =
        new ArrayStateRotationHandler(BlockStairs.FACING, enumFacing);
    stateRotationHandlers[Block.getIdFromBlock(Blocks.STONE_BRICK_STAIRS)] =
        new ArrayStateRotationHandler(BlockStairs.FACING, enumFacing);
    stateRotationHandlers[Block.getIdFromBlock(Blocks.NETHER_BRICK_STAIRS)] =
        new ArrayStateRotationHandler(BlockStairs.FACING, enumFacing);
    stateRotationHandlers[Block.getIdFromBlock(Blocks.SANDSTONE_STAIRS)] =
        new ArrayStateRotationHandler(BlockStairs.FACING, enumFacing);
    stateRotationHandlers[Block.getIdFromBlock(Blocks.SPRUCE_STAIRS)] =
        new ArrayStateRotationHandler(BlockStairs.FACING, enumFacing);
    stateRotationHandlers[Block.getIdFromBlock(Blocks.BIRCH_STAIRS)] =
        new ArrayStateRotationHandler(BlockStairs.FACING, enumFacing);
    stateRotationHandlers[Block.getIdFromBlock(Blocks.JUNGLE_STAIRS)] =
        new ArrayStateRotationHandler(BlockStairs.FACING, enumFacing);
    stateRotationHandlers[Block.getIdFromBlock(Blocks.QUARTZ_STAIRS)] =
        new ArrayStateRotationHandler(BlockStairs.FACING, enumFacing);
    stateRotationHandlers[Block.getIdFromBlock(Blocks.ACACIA_STAIRS)] =
        new ArrayStateRotationHandler(BlockStairs.FACING, enumFacing);
    stateRotationHandlers[Block.getIdFromBlock(Blocks.DARK_OAK_STAIRS)] =
        new ArrayStateRotationHandler(BlockStairs.FACING, enumFacing);
    stateRotationHandlers[Block.getIdFromBlock(Blocks.RED_SANDSTONE_STAIRS)] =
        new ArrayStateRotationHandler(BlockStairs.FACING, enumFacing);
    stateRotationHandlers[Block.getIdFromBlock(Blocks.STANDING_BANNER)] =
        new ArrayStateRotationHandler(BlockBanner.ROTATION, intFacing);
    stateRotationHandlers[Block.getIdFromBlock(Blocks.WALL_BANNER)] =
        new ArrayStateRotationHandler(BlockStairs.FACING, enumFacing);
    stateRotationHandlers[Block.getIdFromBlock(Blocks.OAK_DOOR)] =
        new ArrayStateRotationHandler(BlockDoor.FACING, enumFacing);
    stateRotationHandlers[Block.getIdFromBlock(Blocks.IRON_DOOR)] =
        new ArrayStateRotationHandler(BlockDoor.FACING, enumFacing);
    stateRotationHandlers[Block.getIdFromBlock(Blocks.SPRUCE_DOOR)] =
        new ArrayStateRotationHandler(BlockDoor.FACING, enumFacing);
    stateRotationHandlers[Block.getIdFromBlock(Blocks.BIRCH_DOOR)] =
        new ArrayStateRotationHandler(BlockDoor.FACING, enumFacing);
    stateRotationHandlers[Block.getIdFromBlock(Blocks.JUNGLE_DOOR)] =
        new ArrayStateRotationHandler(BlockDoor.FACING, enumFacing);
    stateRotationHandlers[Block.getIdFromBlock(Blocks.ACACIA_DOOR)] =
        new ArrayStateRotationHandler(BlockDoor.FACING, enumFacing);
    stateRotationHandlers[Block.getIdFromBlock(Blocks.DARK_OAK_DOOR)] =
        new ArrayStateRotationHandler(BlockDoor.FACING, enumFacing);
    ArrayStateRotationHandler railHandler1 =
        new ArrayStateRotationHandler(
            BlockRail.SHAPE, EnumRailDirection.NORTH_SOUTH, EnumRailDirection.EAST_WEST);
    ArrayStateRotationHandler railHandler2 =
        new ArrayStateRotationHandler(
            BlockRail.SHAPE,
            EnumRailDirection.ASCENDING_NORTH,
            EnumRailDirection.ASCENDING_EAST,
            EnumRailDirection.ASCENDING_SOUTH,
            EnumRailDirection.ASCENDING_WEST);
    ArrayStateRotationHandler railHandler3 =
        new ArrayStateRotationHandler(
            BlockRail.SHAPE,
            EnumRailDirection.NORTH_EAST,
            EnumRailDirection.SOUTH_EAST,
            EnumRailDirection.SOUTH_WEST,
            EnumRailDirection.NORTH_WEST);
    stateRotationHandlers[Block.getIdFromBlock(Blocks.GOLDEN_RAIL)] =
        new MultipleStateRotationHandler(railHandler1, railHandler2, railHandler3);
    stateRotationHandlers[Block.getIdFromBlock(Blocks.DETECTOR_RAIL)] =
        new MultipleStateRotationHandler(railHandler1, railHandler2, railHandler3);
    stateRotationHandlers[Block.getIdFromBlock(Blocks.RAIL)] =
        new MultipleStateRotationHandler(railHandler1, railHandler2, railHandler3);
    stateRotationHandlers[Block.getIdFromBlock(Blocks.ACTIVATOR_RAIL)] =
        new MultipleStateRotationHandler(railHandler1, railHandler2, railHandler3);
    stateRotationHandlers[Block.getIdFromBlock(Blocks.LADDER)] =
        new ArrayStateRotationHandler(BlockLadder.FACING, enumFacing);
    stateRotationHandlers[Block.getIdFromBlock(Blocks.FURNACE)] =
        new ArrayStateRotationHandler(BlockFurnace.FACING, enumFacing);
    stateRotationHandlers[Block.getIdFromBlock(Blocks.LIT_FURNACE)] =
        new ArrayStateRotationHandler(BlockFurnace.FACING, enumFacing);
    stateRotationHandlers[Block.getIdFromBlock(Blocks.CHEST)] =
        new ArrayStateRotationHandler(BlockChest.FACING, enumFacing);
    stateRotationHandlers[Block.getIdFromBlock(Blocks.ENDER_CHEST)] =
        new ArrayStateRotationHandler(BlockEnderChest.FACING, enumFacing);
    stateRotationHandlers[Block.getIdFromBlock(Blocks.TRAPPED_CHEST)] =
        new ArrayStateRotationHandler(BlockChest.FACING, enumFacing);
    stateRotationHandlers[Block.getIdFromBlock(Blocks.STANDING_SIGN)] =
        new ArrayStateRotationHandler(BlockStandingSign.ROTATION, intFacing);
    stateRotationHandlers[Block.getIdFromBlock(Blocks.WALL_SIGN)] =
        new ArrayStateRotationHandler(BlockWallSign.FACING, enumFacing);
    stateRotationHandlers[Block.getIdFromBlock(Blocks.DISPENSER)] =
        new ArrayStateRotationHandler(BlockDispenser.FACING, enumFacing);
    stateRotationHandlers[Block.getIdFromBlock(Blocks.DROPPER)] =
        new ArrayStateRotationHandler(BlockDropper.FACING, enumFacing);
    stateRotationHandlers[Block.getIdFromBlock(Blocks.HOPPER)] =
        new ArrayStateRotationHandler(BlockHopper.FACING, enumFacing);
    ArrayStateRotationHandler leverHandler1 =
        new ArrayStateRotationHandler(
            BlockLever.FACING,
            BlockLever.EnumOrientation.DOWN_Z,
            BlockLever.EnumOrientation.DOWN_X);
    ArrayStateRotationHandler leverHandler2 =
        new ArrayStateRotationHandler(
            BlockLever.FACING,
            BlockLever.EnumOrientation.NORTH,
            BlockLever.EnumOrientation.EAST,
            BlockLever.EnumOrientation.SOUTH,
            BlockLever.EnumOrientation.WEST);
    ArrayStateRotationHandler leverHandler3 =
        new ArrayStateRotationHandler(
            BlockLever.FACING, BlockLever.EnumOrientation.UP_Z, BlockLever.EnumOrientation.UP_X);
    stateRotationHandlers[Block.getIdFromBlock(Blocks.LEVER)] =
        new MultipleStateRotationHandler(leverHandler1, leverHandler2, leverHandler3);
    stateRotationHandlers[Block.getIdFromBlock(Blocks.STONE_BUTTON)] =
        new ArrayStateRotationHandler(BlockButton.FACING, enumFacing);
    stateRotationHandlers[Block.getIdFromBlock(Blocks.WOODEN_BUTTON)] =
        new ArrayStateRotationHandler(BlockButton.FACING, enumFacing);
    stateRotationHandlers[Block.getIdFromBlock(Blocks.PUMPKIN)] =
        new ArrayStateRotationHandler(BlockPumpkin.FACING, enumFacing);
    stateRotationHandlers[Block.getIdFromBlock(Blocks.LIT_PUMPKIN)] =
        new ArrayStateRotationHandler(BlockPumpkin.FACING, enumFacing);
    stateRotationHandlers[Block.getIdFromBlock(Blocks.UNPOWERED_REPEATER)] =
        new ArrayStateRotationHandler(BlockRedstoneRepeater.FACING, enumFacing);
    stateRotationHandlers[Block.getIdFromBlock(Blocks.POWERED_REPEATER)] =
        new ArrayStateRotationHandler(BlockRedstoneRepeater.FACING, enumFacing);
    stateRotationHandlers[Block.getIdFromBlock(Blocks.UNPOWERED_COMPARATOR)] =
        new ArrayStateRotationHandler(BlockRedstoneComparator.FACING, enumFacing);
    stateRotationHandlers[Block.getIdFromBlock(Blocks.POWERED_COMPARATOR)] =
        new ArrayStateRotationHandler(BlockRedstoneComparator.FACING, enumFacing);
    stateRotationHandlers[Block.getIdFromBlock(Blocks.TRAPDOOR)] =
        new ArrayStateRotationHandler(BlockTrapDoor.FACING, enumFacing);
    stateRotationHandlers[Block.getIdFromBlock(Blocks.IRON_TRAPDOOR)] =
        new ArrayStateRotationHandler(BlockTrapDoor.FACING, enumFacing);
    ArrayStateRotationHandler mushroomHandler1 =
        new ArrayStateRotationHandler(
            BlockHugeMushroom.VARIANT,
            BlockHugeMushroom.EnumType.NORTH,
            BlockHugeMushroom.EnumType.EAST,
            BlockHugeMushroom.EnumType.SOUTH,
            BlockHugeMushroom.EnumType.WEST);
    ArrayStateRotationHandler mushroomHandler2 =
        new ArrayStateRotationHandler(
            BlockHugeMushroom.VARIANT,
            BlockHugeMushroom.EnumType.NORTH_EAST,
            BlockHugeMushroom.EnumType.SOUTH_EAST,
            BlockHugeMushroom.EnumType.SOUTH_WEST,
            BlockHugeMushroom.EnumType.NORTH_WEST);
    stateRotationHandlers[Block.getIdFromBlock(Blocks.RED_MUSHROOM_BLOCK)] =
        new MultipleStateRotationHandler(mushroomHandler1, mushroomHandler2);
    stateRotationHandlers[Block.getIdFromBlock(Blocks.BROWN_MUSHROOM_BLOCK)] =
        new MultipleStateRotationHandler(mushroomHandler1, mushroomHandler2);
    stateRotationHandlers[Block.getIdFromBlock(Blocks.VINE)] =
        new StateRotationHandler() {
          @Override
          public IBlockState rotate(IBlockState state, int rotation) {
            Boolean oldNorth = state.getValue(BlockVine.NORTH),
                oldEast = state.getValue(BlockVine.EAST),
                oldSouth = state.getValue(BlockVine.SOUTH),
                oldWest = state.getValue(BlockVine.WEST);
            Boolean newNorth, newEast, newSouth, newWest;

            for (int i = 0; i < rotation; i++) {
              newNorth = oldWest;
              newEast = oldNorth;
              newSouth = oldEast;
              newWest = oldSouth;
              oldNorth = newNorth;
              oldEast = newEast;
              oldSouth = newSouth;
              oldWest = newWest;
            }

            return state
                .withProperty(BlockVine.NORTH, oldNorth)
                .withProperty(BlockVine.EAST, oldEast)
                .withProperty(BlockVine.SOUTH, oldSouth)
                .withProperty(BlockVine.WEST, oldWest);
          }
        };
    stateRotationHandlers[Block.getIdFromBlock(Blocks.OAK_FENCE_GATE)] =
        new ArrayStateRotationHandler(BlockFenceGate.FACING, enumFacing);
    stateRotationHandlers[Block.getIdFromBlock(Blocks.SPRUCE_FENCE_GATE)] =
        new ArrayStateRotationHandler(BlockFenceGate.FACING, enumFacing);
    stateRotationHandlers[Block.getIdFromBlock(Blocks.BIRCH_FENCE_GATE)] =
        new ArrayStateRotationHandler(BlockFenceGate.FACING, enumFacing);
    stateRotationHandlers[Block.getIdFromBlock(Blocks.JUNGLE_FENCE_GATE)] =
        new ArrayStateRotationHandler(BlockFenceGate.FACING, enumFacing);
    stateRotationHandlers[Block.getIdFromBlock(Blocks.DARK_OAK_FENCE_GATE)] =
        new ArrayStateRotationHandler(BlockFenceGate.FACING, enumFacing);
    stateRotationHandlers[Block.getIdFromBlock(Blocks.ACACIA_FENCE_GATE)] =
        new ArrayStateRotationHandler(BlockFenceGate.FACING, enumFacing);
    stateRotationHandlers[Block.getIdFromBlock(Blocks.END_PORTAL_FRAME)] =
        new ArrayStateRotationHandler(BlockEndPortalFrame.FACING, enumFacing);
    stateRotationHandlers[Block.getIdFromBlock(Blocks.COCOA)] =
        new ArrayStateRotationHandler(BlockCocoa.FACING, enumFacing);
    stateRotationHandlers[Block.getIdFromBlock(Blocks.TRIPWIRE_HOOK)] =
        new ArrayStateRotationHandler(BlockTripWireHook.FACING, enumFacing);
    stateRotationHandlers[Block.getIdFromBlock(Blocks.SKULL)] =
        new ArrayStateRotationHandler(BlockSkull.FACING, enumFacing);
    tileEntityRotationHandlers[Block.getIdFromBlock(Blocks.SKULL)] =
        new TileEntityRotationHandler() {
          @Override
          public void rotate(TileEntity tileEntity, int rotation) {
            TileEntitySkull tileEntitySkull = (TileEntitySkull) tileEntity;
            tileEntitySkull.setSkullRotation(
                (tileEntitySkull.getSkullRotation() + (rotation * 4)) % 16);
          }
        };
    stateRotationHandlers[Block.getIdFromBlock(Blocks.ANVIL)] =
        new ArrayStateRotationHandler(BlockAnvil.FACING, enumFacing);
  }
}
