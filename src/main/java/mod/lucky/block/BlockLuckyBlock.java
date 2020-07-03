package mod.lucky.block;

import java.util.ArrayList;

import mod.lucky.Lucky;
import mod.lucky.drop.DropFull;
import mod.lucky.drop.func.DropProcessData;
import mod.lucky.drop.func.DropProcessor;
import mod.lucky.item.LuckyItem;
import mod.lucky.tileentity.TileEntityLuckyBlock;
import mod.lucky.util.LuckyUtils;
import mod.lucky.world.LuckyGenerator;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.ToolType;

public class BlockLuckyBlock extends ContainerBlock {
    private DropProcessor dropProcessor;
    private LuckyGenerator worldGenerator;
    private boolean doCreativeDrops = false;

    public BlockLuckyBlock() {
        super(Block.Properties.create(Material.WOOD, MaterialColor.YELLOW)
            .sound(SoundType.STONE)
            .hardnessAndResistance(0.2f, 6000000.0f));

        this.dropProcessor = new DropProcessor();
    }

    public boolean removeLuckyBlock(World world, PlayerEntity player,
        BlockPos harvestPos, boolean removedByRedstone) {

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

            if (!world.removeBlock(harvestPos, false)) return false;

            if (!world.isRemote) {
                if (removedByRedstone) {
                    player = LuckyUtils.getNearestPlayer(
                        (ServerWorld) world, LuckyUtils.toVector3d(harvestPos));
                }

                if (player.isCreative() && !this.doCreativeDrops && !removedByRedstone) {
                    return true;
                } else if (EnchantmentHelper.getMaxEnchantmentLevel(
                    Enchantments.SILK_TOUCH, player) > 0
                    && !removedByRedstone) {

                    ItemStack stack = new ItemStack(this);
                    CompoundNBT tag = new CompoundNBT();

                    if (luck != 0) tag.putInt("Luck", luck);
                    if (customDrops != null)
                        tag.put("Drops",
                            LuckyUtils.tagListFromStrArray(customDropsRaw));
                    if (tag.contains("Luck") || customDrops != null)
                        stack.setTag(tag);

                    Block.spawnAsEntity(world, harvestPos, stack);
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
            Lucky.error(e, DropProcessor.errorMessage());
        }

        return true;
    }

    @Override
    public void neighborChanged(BlockState state, World world,
        BlockPos pos, Block block, BlockPos fromPos, boolean flag) {

        if (!world.isRemote) {
            if (world.isBlockPowered(pos)) {
                this.removeLuckyBlock(world, null, pos, true);
            }
        }
    }

    @Override
    public void harvestBlock(World world, PlayerEntity player, BlockPos harvestPos,
        BlockState state, TileEntity tileEntity, ItemStack stack) {
        this.removeLuckyBlock(world, player, harvestPos, false);
    }

    @Override
    public boolean removedByPlayer(BlockState state, World world,
        BlockPos pos, PlayerEntity player, boolean willHarvest, FluidState fluidState) {

        super.removedByPlayer(state, world, pos, player, willHarvest, fluidState);

        if (player.isCreative() && this.doCreativeDrops)
            this.removeLuckyBlock(world, player, pos, false);

        return true;
    }

    private boolean canPlaceOnBlock(BlockState soil) {
        return soil.isSolid();
    }

    public boolean canPlaceAt(IWorld world, BlockPos pos) {
        BlockState curState = world.getBlockState(pos);
        BlockState soilState = world.getBlockState(
            new BlockPos(pos.getX(), pos.getY() - 1, pos.getZ()));

        return (curState.getMaterial().isReplaceable()
            && !curState.getMaterial().isLiquid()
            && (this.canPlaceOnBlock(soilState)));
    }

    @Override
    public ToolType getHarvestTool(BlockState state) {
        return ToolType.PICKAXE;
    }

    @Override
    public int getHarvestLevel(BlockState state) {
        return 0;
    }

    @Override
    public TileEntity createNewTileEntity(IBlockReader reader) {
        return new TileEntityLuckyBlock();
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos,
        BlockState state, LivingEntity player, ItemStack itemStack) {

        TileEntityLuckyBlock tileEntityLuck = (TileEntityLuckyBlock) world.getTileEntity(pos);
        if (tileEntityLuck == null) return;

        int luck = LuckyItem.getLuck(itemStack);
        String[] drops = LuckyItem.getRawDrops(itemStack);

        tileEntityLuck.setLuck(luck);
        if (drops != null && drops.length != 0) tileEntityLuck.setDrops(drops);
        tileEntityLuck.markDirty();

        if (world.isBlockPowered(pos) && !world.isRemote)
            this.removeLuckyBlock(world, null, pos, true);
    }

    public DropProcessor getDropProcessor() { return this.dropProcessor; }
    public LuckyGenerator getWorldGenerator() { return this.worldGenerator; }
    public void setWorldGenerator(LuckyGenerator gen) { this.worldGenerator = gen; }

    public void setDoCreativeDrops(boolean doDrops) { this.doCreativeDrops = doDrops; }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }
}
