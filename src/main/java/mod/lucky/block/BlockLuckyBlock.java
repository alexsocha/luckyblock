package mod.lucky.block;

import java.util.ArrayList;
import java.util.Random;
import mod.lucky.command.LuckyCommandLogic;
import mod.lucky.crafting.LuckCrafting;
import mod.lucky.drop.DropContainer;
import mod.lucky.drop.func.DropProcessData;
import mod.lucky.drop.func.DropProcessor;
import mod.lucky.item.ItemLuckyBlock;
import mod.lucky.tileentity.TileEntityLuckyBlock;
import mod.lucky.util.LuckyFunction;
import mod.lucky.world.LuckyGenerator;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.command.CommandBase;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Enchantments;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.registries.IForgeRegistryEntry;

public class BlockLuckyBlock extends BlockContainer {
  private DropProcessor dropProcessor;
  private LuckyGenerator worldGenerator;
  private LuckCrafting crafting;
  private IForgeRegistryEntry.Impl<IRecipe> blockRecipe;
  private boolean creativeModeDrops = false;

  private final Random random = new Random();

  public BlockLuckyBlock(Material material) {
    super(material);
    this.setSoundType(SoundType.STONE);
    this.dropProcessor = new DropProcessor();
    this.worldGenerator = new LuckyGenerator(this);
    this.crafting = new LuckCrafting(this);
  }

  @Override
  public void onBlockAdded(World world, BlockPos pos, IBlockState state) {
    world.markAndNotifyBlock(pos, null, state, state, 3);
  }

  @Override
  public void neighborChanged(
      IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
    if (!worldIn.isRemote) {
      if (worldIn.isBlockPowered(pos)) {
        this.removeLuckyBlock(worldIn, null, pos, true);
      }
    }
  }

  @Override
  public boolean removedByPlayer(
      IBlockState state,
      World world,
      BlockPos harvestPos,
      EntityPlayer player,
      boolean willharvest) {
    return this.removeLuckyBlock(world, player, harvestPos, false);
  }

  public boolean removeLuckyBlock(
      World world, EntityPlayer player, BlockPos harvestPos, boolean removedByRedstone) {
    try {
      int luck = 0;
      String[] customDropsRaw = null;
      ArrayList<DropContainer> customDrops = null;

      TileEntityLuckyBlock tileEntityLuck = (TileEntityLuckyBlock) world.getTileEntity(harvestPos);
      if (tileEntityLuck != null) {
        luck = tileEntityLuck.getLuck();
        customDropsRaw = tileEntityLuck.getDrops();
        if (customDropsRaw != null && customDropsRaw.length != 0) {
          customDrops = new ArrayList<DropContainer>();
          for (String rawDrop : customDropsRaw) {
            DropContainer dropContainer = new DropContainer();
            dropContainer.readFromString(rawDrop);
            customDrops.add(dropContainer);
          }
        }
        world.removeTileEntity(harvestPos);
      }

      if (!world.setBlockToAir(harvestPos)) return false;

      if (!world.isRemote) {
        if (removedByRedstone) {
          LuckyCommandLogic luckyCommandLogic = new LuckyCommandLogic();
          luckyCommandLogic.setWorld(world);
          luckyCommandLogic.setPosition(harvestPos);
          player = CommandBase.getPlayer(world.getMinecraftServer(), luckyCommandLogic, "@p");
        }

        if (player.capabilities.isCreativeMode
            && this.creativeModeDrops == false
            && !removedByRedstone) {
          return true;
        } else if (EnchantmentHelper.getMaxEnchantmentLevel(Enchantments.SILK_TOUCH, player) > 0
            && !removedByRedstone) {
          ItemStack itemStack = new ItemStack(this);
          NBTTagCompound tagCompound = new NBTTagCompound();

          if (luck != 0) tagCompound.setInteger("Luck", luck);
          if (customDrops != null)
            tagCompound.setTag("Drops", LuckyFunction.getNBTTagListFromStringArray(customDropsRaw));
          if (tagCompound.hasKey("Luck") || customDrops != null)
            itemStack.setTagCompound(tagCompound);

          Block.spawnAsEntity(world, harvestPos, itemStack);
          return true;
        } else {
          if (customDrops != null)
            this.getDropProcessor()
                .processRandomDrop(
                    customDrops, new DropProcessData(world, player, harvestPos), luck);
          else
            this.getDropProcessor()
                .processRandomDrop(new DropProcessData(world, player, harvestPos), luck);
          return true;
        }
      }
    } catch (Exception e) {
      System.err.println(
          "The Lucky Block encountered and error while trying to perform a function. Error report below:");
      e.printStackTrace();
    }

    return true;
  }

  @Override
  public Item getItemDropped(IBlockState state, Random rand, int fortune) {
    return null;
  }

  private boolean canStayOnBlock(Block soil) {
    return soil == Blocks.GRASS_PATH
        || soil == Blocks.DIRT
        || soil == Blocks.SAND
        || soil == Blocks.STONE
        || soil == Blocks.GRAVEL
        || soil == Blocks.NETHERRACK
        || soil == Blocks.SOUL_SAND
        || soil == Blocks.NETHER_BRICK
        || soil == Blocks.END_STONE;
  }

  public boolean canBlockStay(World world, BlockPos pos) {
    Block curBlock = world.getBlockState(pos).getBlock();
    Block soil =
        world.getBlockState(new BlockPos(pos.getX(), pos.getY() - 1, pos.getZ())).getBlock();
    return (curBlock.isReplaceable(world, pos)
            && curBlock != Blocks.WATER
            && curBlock != Blocks.FLOWING_WATER
            && curBlock != Blocks.LAVA
            && curBlock != Blocks.FLOWING_LAVA)
        && (world.getBlockLightOpacity(pos) >= 8 || world.canBlockSeeSky(pos))
        && (soil != null && this.canStayOnBlock(soil));
  }

  @Override
  public TileEntity createNewTileEntity(World world, int metadata) {
    return new TileEntityLuckyBlock();
  }

  @Override
  public void onBlockPlacedBy(
      World world, BlockPos pos, IBlockState state, EntityLivingBase player, ItemStack itemStack) {
    TileEntityLuckyBlock tileEntityLuck = (TileEntityLuckyBlock) world.getTileEntity(pos);
    if (tileEntityLuck == null) return;

    int luck = ItemLuckyBlock.getLuck(itemStack);
    String[] drops = ItemLuckyBlock.getDrops(itemStack);

    tileEntityLuck.setLuck(luck);
    if (drops != null && drops.length != 0) tileEntityLuck.setDrops(drops);

    tileEntityLuck.markDirty();
    world.markAndNotifyBlock(pos, null, state, state, 3);

    if (world.isBlockPowered(pos) && !world.isRemote) this.removeLuckyBlock(world, null, pos, true);
  }

  public DropProcessor getDropProcessor() {
    return this.dropProcessor;
  }

  public LuckyGenerator getWorldGenerator() {
    return this.worldGenerator;
  }

  public LuckCrafting getCrafting() {
    return this.crafting;
  }

  public BlockLuckyBlock setBlockRecipe(IForgeRegistryEntry.Impl<IRecipe> recipe) {
    this.blockRecipe = recipe;
    return this;
  }

  public IForgeRegistryEntry.Impl<IRecipe> getBlockRecipe() {
    return this.blockRecipe;
  }

  public boolean getCreativeModeDrops() {
    return this.creativeModeDrops;
  }

  public void setCreativeModeDrops(boolean creativeModeDrops) {
    this.creativeModeDrops = creativeModeDrops;
  }

  @Override
  public EnumBlockRenderType getRenderType(IBlockState state) {
    return EnumBlockRenderType.MODEL;
  }
}
