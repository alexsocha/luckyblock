package mod.lucky.world;

import java.util.ArrayList;
import java.util.Random;

import mod.lucky.Lucky;
import mod.lucky.block.BlockLuckyBlock;
import mod.lucky.drop.DropFull;
import mod.lucky.drop.func.DropProcessData;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.dimension.Dimension;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.IWorldGenerator;

public class LuckyGenerator implements IWorldGenerator {
    private BlockLuckyBlock block;
    private ArrayList<DropFull> surfaceDrops;
    private ArrayList<DropFull> netherDrops;
    private ArrayList<DropFull> endDrops;

    public LuckyGenerator(BlockLuckyBlock block) {
        this.block = block;
        this.surfaceDrops = new ArrayList<DropFull>();
        this.netherDrops = new ArrayList<DropFull>();
        this.endDrops = new ArrayList<DropFull>();
    }

    @Override
    public void generate(
        Random random,
        int chunkX,
        int chunkZ,
        World world,
        IChunkGenerator chunkGenerator,
        IChunkProvider chunkProvider) {
        try {
            Dimension dim = world.getDimension();
            if (dim.isNether()) {
                for (DropFull drop : this.netherDrops)
                    if (random.nextInt((int) drop.getChance()) == 0)
                        this.generateNether(world, random, chunkX * 16, chunkZ * 16, drop);

            } else if (dim.isSurfaceWorld()) {
                for (DropFull drop : this.surfaceDrops)
                    if (random.nextInt((int) drop.getChance()) == 0)
                        this.generateSurface(world, random, chunkX * 16, chunkZ * 16, drop);

            } else {
                for (DropFull drop : this.endDrops)
                    if (random.nextInt((int) drop.getChance()) == 0)
                        this.generateEnd(world, random, chunkX * 16, chunkZ * 16, drop);
            }
        } catch (Exception e) {
            Lucky.error(e, "Error during natural generation");
            Lucky.error(e, e.getMessage());
        }
    }

    private void generateNether(World world, Random random, int x, int z, DropFull drop) {
        x += (random.nextInt(16) + 8);
        z += (random.nextInt(16) + 8);
        this.generate(world, new BlockPos(x, 64, z), drop);
    }

    private void generateSurface(World world, Random random, int x, int z, DropFull drop) {
        x += (random.nextInt(16) + 8);
        z += (random.nextInt(16) + 8);
        this.generate(world, new BlockPos(x, 128, z), drop);
    }

    private void generateEnd(World world, Random random, int x, int z, DropFull drop) {
        x += (random.nextInt(16) + 8);
        z += (random.nextInt(16) + 8);
        this.generate(world, new BlockPos(x, 100, z), drop);
    }

    private void generate(World world, BlockPos pos, DropFull drop) {
        pos = this.getSurfacePos(world, pos);
        if (pos != null)
            this.block.getDropProcessor().processDrop(drop, new DropProcessData(world, null, pos));
    }

    public void addSurfacedDrop(DropFull drop) {
        this.addDrop(this.surfaceDrops, drop);
    }

    public void addNetherDrop(DropFull drop) {
        this.addDrop(this.netherDrops, drop);
    }

    public void addEndDrop(DropFull drop) {
        this.addDrop(this.endDrops, drop);
    }

    private void addDrop(ArrayList<DropFull> list, DropFull drop) {
        if (!drop.wasChanceSet()) drop.setChance(300);
        list.add(drop);
    }

    private BlockPos getSurfacePos(World world, BlockPos pos) {
        int newPosY = pos.getY();
        boolean canAdjust = false;
        do {
            BlockPos newPos = new BlockPos(pos.getX(), newPosY, pos.getZ());
            if (this.block.canPlaceAt(world, newPos)) {
                canAdjust = true;
                break;
            }
            newPosY--;
        } while (newPosY > 0);

        return canAdjust == false ? null : new BlockPos(pos.getX(), newPosY, pos.getZ());
    }
}
