package mod.lucky.block;

import java.util.ArrayList;
import java.util.Random;

import mod.lucky.Lucky;
import mod.lucky.drop.DropFull;
import mod.lucky.drop.func.DropProcessData;
import mod.lucky.drop.func.DropProcessor;
import mod.lucky.item.LuckyItem;
import mod.lucky.tileentity.TileEntityLuckyBlock;
import mod.lucky.util.LuckyUtils;
import mod.lucky.world.LuckyGenerator;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.registries.IForgeRegistryEntry;

public class BlockLuckyBlock extends BlockContainer {
    private DropProcessor dropProcessor;
    private LuckyGenerator worldGenerator;
    private IForgeRegistryEntry<IRecipe> blockRecipe;
    private boolean doCreativeDrops = false;

    public BlockLuckyBlock() {
        super(Block.Properties.create(Material.WOOD, MaterialColor.YELLOW)
            .sound(SoundType.STONE)
            .hardnessAndResistance(0.2f, 6000000.0f));


        this.dropProcessor = new DropProcessor();
        this.worldGenerator = new LuckyGenerator(this);
    }

    public boolean removeLuckyBlock(
        World world, EntityPlayer player, BlockPos harvestPos, boolean removedByRedstone) {

        try {
            int luck = 0;
            String[] customDropsRaw = null;
            ArrayList<DropFull> customDrops = null;

            TileEntityLuckyBlock tileEntityLuck =
                (TileEntityLuckyBlock) world.getTileEntity(harvestPos);

            if (tileEntityLuck != null) {
                luck = tileEntityLuck.getLuck();
                customDropsRaw = tileEntityLuck.getDrops();
                if (customDropsRaw != null && customDropsRaw.length != 0) {
                    customDrops = new ArrayList<DropFull>();
                    for (String rawDrop : customDropsRaw) {
                        DropFull dropFull = new DropFull();
                        dropFull.readFromString(rawDrop);
                        customDrops.add(dropFull);
                    }
                }
                world.removeTileEntity(harvestPos);
            }

            if (!world.removeBlock(harvestPos)) return false;

            if (!world.isRemote) {
                if (removedByRedstone) {
                    player = LuckyUtils.getNearestPlayer(
                        (WorldServer) world, LuckyUtils.toVec3d(harvestPos));
                }

                if (player.isCreative() && !this.doCreativeDrops && !removedByRedstone) {
                    return true;
                } else if (EnchantmentHelper.getMaxEnchantmentLevel(
                    Enchantments.SILK_TOUCH, player) > 0
                    && !removedByRedstone) {

                    ItemStack strack = new ItemStack(this);
                    NBTTagCompound tag = new NBTTagCompound();

                    if (luck != 0) tag.setInt("Luck", luck);
                    if (customDrops != null)
                        tag.setTag("Drops",
                            LuckyUtils.tagListFromStrArray(customDropsRaw));
                    if (tag.hasKey("Luck") || customDrops != null)
                        strack.setTag(tag);

                    Block.spawnAsEntity(world, harvestPos, strack);
                    return true;
                } else {
                    DropProcessData dropData = new DropProcessData(world, player, harvestPos);
                    if (customDrops != null)
                        this.getDropProcessor()
                            .processRandomDrop(customDrops, dropData, luck);
                    else
                        this.getDropProcessor().processRandomDrop(dropData, luck);
                    return true;
                }
            }
        } catch (Exception e) {
            Lucky.LOGGER.error(DropProcessor.errorMessage());
            e.printStackTrace();
        }

        return true;
    }

    @Override
    public void onBlockAdded(IBlockState state, World world, BlockPos pos, IBlockState oldState) {
        world.markAndNotifyBlock(pos, null, state, state, 3);
    }

    @Override
    public void neighborChanged(IBlockState state, World world,
        BlockPos pos, Block block, BlockPos fromPos) {

        if (!world.isRemote) {
            if (world.isBlockPowered(pos)) {
                this.removeLuckyBlock(world, null, pos, true);
            }
        }
    }

    @Override
    public void harvestBlock(World world, EntityPlayer player, BlockPos harvestPos,
        IBlockState state, TileEntity tileEntity, ItemStack stack) {
        this.removeLuckyBlock(world, player, harvestPos, false);
    }

    public int getItemsToDropCount(IBlockState state, int fortune, World worldIn,
        BlockPos pos, Random random) {
        return 0;
    }

    private boolean canPlaceOnBlock(Block soil) {
        return soil == Blocks.GRASS_PATH
            || soil == Blocks.DIRT
            || soil == Blocks.SAND
            || soil == Blocks.STONE
            || soil == Blocks.GRAVEL
            || soil == Blocks.NETHERRACK
            || soil == Blocks.SOUL_SAND
            || soil == Blocks.NETHER_BRICKS
            || soil == Blocks.END_STONE;
    }

    public boolean canPlaceAt(World world, BlockPos pos) {
        IBlockState curState = world.getBlockState(pos);
        IBlockState soilState = world.getBlockState(
            new BlockPos(pos.getX(), pos.getY() - 1, pos.getZ()));

        return (curState.getMaterial().isReplaceable()
            && !curState.getMaterial().isLiquid()
            && (world.getLight(pos) >= 8 || world.canBlockSeeSky(pos))
            && (this.canPlaceOnBlock(soilState.getBlock())));
    }

    @Override
    public ToolType getHarvestTool(IBlockState state) {
        return ToolType.PICKAXE;
    }

    @Override
    public int getHarvestLevel(IBlockState state) {
        return 0;
    }

    @Override
    public TileEntity createNewTileEntity(IBlockReader reader) {
        return new TileEntityLuckyBlock();
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos,
        IBlockState state, EntityLivingBase player, ItemStack itemStack) {

        TileEntityLuckyBlock tileEntityLuck = (TileEntityLuckyBlock) world.getTileEntity(pos);
        if (tileEntityLuck == null) return;

        int luck = LuckyItem.getLuck(itemStack);
        String[] drops = LuckyItem.getRawDrops(itemStack);

        tileEntityLuck.setLuck(luck);
        if (drops != null && drops.length != 0) tileEntityLuck.setDrops(drops);

        tileEntityLuck.markDirty();
        world.markAndNotifyBlock(pos, null, state, state, 3);

        if (world.isBlockPowered(pos) && !world.isRemote)
            this.removeLuckyBlock(world, null, pos, true);
    }

    public DropProcessor getDropProcessor() { return this.dropProcessor; }
    public LuckyGenerator getWorldGenerator() { return this.worldGenerator; }

    public void setDoCreativeDrops(boolean doCreativeDrops) {
        this.doCreativeDrops = doCreativeDrops;
    }
    public BlockLuckyBlock setBlockRecipe(IForgeRegistryEntry<IRecipe> recipe) {
        this.blockRecipe = recipe;
        return this;
    }
    public IForgeRegistryEntry<IRecipe> getBlockRecipe() { return this.blockRecipe; }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.MODEL;
    }
}
