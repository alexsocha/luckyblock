package mod.lucky.world;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

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
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.gen.feature.*;
import net.minecraft.world.gen.feature.structure.StructureManager;
import net.minecraftforge.registries.ForgeRegistries;

public class LuckyGenerator extends Feature<NoFeatureConfig> {
    private BlockLuckyBlock block;
    private HashMap<String, ArrayList<DropFull>> dimensionDrops;

    public LuckyGenerator(Codec<NoFeatureConfig> codec) {
        super(codec);
    }

    public void init(BlockLuckyBlock block) {
        this.block = block;
        this.dimensionDrops = new HashMap<>();
    }

    public static LuckyGenerator registerNew(BlockLuckyBlock block) {
        LuckyGenerator generator = new LuckyGenerator(NoFeatureConfig.field_236558_a_);
        generator.init(block);

        ConfiguredFeature<?, ?> configuredGenerator
            = new ConfiguredFeature<NoFeatureConfig, LuckyGenerator>(
                generator, new NoFeatureConfig());

        ForgeRegistries.BIOMES.forEach(biome ->
            biome.addFeature(
                GenerationStage.Decoration.SURFACE_STRUCTURES,
                configuredGenerator));

        return generator;
    }

    private void generate(IWorld world, Random rand, BlockPos pos, ArrayList<DropFull> drops) {
        int initIndex = rand.nextInt(drops.size());
        for (int i = 0; i < drops.size(); i++) {
            DropFull drop = drops.get((initIndex + i) % drops.size());
            if (rand.nextInt((int) drop.getChance()) == 0) {
                DropProcessData processData = new DropProcessData(world, null, pos);
                this.block.getDropProcessor().processRandomDrop(drops, processData, 0);
            }
        }
    }

    private boolean generate(IWorld world, Random rand, BlockPos pos) {
        ResourceLocation dimension = world.getWorld().func_234923_W_().func_240901_a_();

        if (this.dimensionDrops.containsKey(dimension.toString())) {
            ArrayList<DropFull> drops = this.dimensionDrops.get(dimension.toString());
            this.generate(world, rand, pos, drops);
            return true;
        }
        return false;
    }

    @Override
    public boolean func_230362_a_(ISeedReader world, StructureManager structureManager, ChunkGenerator chunkGenerator, Random random, BlockPos pos, NoFeatureConfig config) {
        try {
            pos = world.getHeight(Heightmap.Type.WORLD_SURFACE, pos);
            while (pos.getY() > 0) {
                BlockState soilState = world.getBlockState(
                    new BlockPos(pos.getX(), pos.getY() - 1, pos.getZ()));

                if (soilState.getBlock() == Blocks.BEDROCK || !this.block.canPlaceAt(world, pos)) pos = pos.down();
                else return this.generate(world, random, pos);
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
