package mod.lucky.world;

import com.mojang.serialization.Codec;
import mod.lucky.Lucky;
import mod.lucky.block.BlockLuckyBlock;
import mod.lucky.drop.DropFull;
import mod.lucky.drop.func.DropProcessData;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ISeedReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.NoFeatureConfig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class LuckyWorldFeature extends Feature<NoFeatureConfig> {
    private BlockLuckyBlock block;
    private HashMap<String, ArrayList<DropFull>> dimensionDrops;

    public LuckyWorldFeature(Codec<NoFeatureConfig> codec) {
        super(codec);
    }

    public void init(BlockLuckyBlock block) {
        this.block = block;
        this.dimensionDrops = new HashMap<>();
    }

    private void generate(IWorld world, Random rand, BlockPos pos, ArrayList<DropFull> drops) {
        int initIndex = rand.nextInt(drops.size());
        for (int i = 0; i < drops.size(); i++) {
            DropFull drop = drops.get((initIndex + i) % drops.size());
            if (rand.nextInt((int) drop.getChance()) == 0) {
                DropProcessData processData = new DropProcessData(world, null, pos);
                System.out.println("generating at: " + pos.toString());
                this.block.getDropProcessor().processRandomDrop(drops, processData, 0);
            }
        }
    }

    private boolean generate(ISeedReader world, Random rand, BlockPos pos) {
        ResourceLocation dimension = world.getWorld().getDimensionKey().getLocation();

        if (this.dimensionDrops.containsKey(dimension.toString())) {
            ArrayList<DropFull> drops = this.dimensionDrops.get(dimension.toString());
            this.generate(world, rand, pos, drops);
            return true;
        }
        return false;
    }

    @Override
    public boolean generate(ISeedReader world, ChunkGenerator chunkGenerator, Random random, BlockPos pos, NoFeatureConfig config) {
        try {
            pos = world.getHeight(Heightmap.Type.WORLD_SURFACE, pos);
            while (pos.getY() > 0) {
                BlockState soilState = world.getBlockState(
                    new BlockPos(pos.getX(), pos.getY() - 1, pos.getZ()));

                if (soilState.getBlock() == Blocks.BEDROCK || !this.block.canPlaceAt(world, pos)) {
                    pos = pos.down();
                } else {
                    return this.generate(world, random, pos);
                }
            }
            return false;

        } catch (Exception e) {
            Lucky.error(e, "Error during natural generation");
            return false;
        }
    }

    private DropFull initDrop(ResourceLocation dimension, DropFull drop) {
        if (!drop.wasChanceSet()) drop.setChance(300);
        return drop;
    }

    public void addDrop(ResourceLocation dimension, DropFull drop) {
        if (!this.dimensionDrops.containsKey(dimension.toString())) {
            this.dimensionDrops.put(dimension.toString(), new ArrayList<DropFull>());
        }
        this.dimensionDrops.get(dimension.toString()).add(this.initDrop(dimension, drop));
    }
}
