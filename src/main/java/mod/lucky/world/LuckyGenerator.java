package mod.lucky.world;

import java.util.ArrayList;
import java.util.Random;
import java.util.function.Function;

import com.mojang.datafixers.Dynamic;
import mod.lucky.Lucky;
import mod.lucky.block.BlockLuckyBlock;
import mod.lucky.drop.DropFull;
import mod.lucky.drop.func.DropProcessData;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.dimension.Dimension;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.gen.feature.*;
import net.minecraftforge.registries.ForgeRegistries;

public class LuckyGenerator extends Feature<NoFeatureConfig> {
    private BlockLuckyBlock block;
    private ArrayList<DropFull> surfaceDrops;
    private ArrayList<DropFull> netherDrops;
    private ArrayList<DropFull> endDrops;

    public LuckyGenerator(Function<Dynamic<?>, ? extends NoFeatureConfig> configFactory) {
        super(configFactory);
    }

    public void init(BlockLuckyBlock block) {
        this.block = block;
        this.surfaceDrops = new ArrayList<DropFull>();
        this.netherDrops = new ArrayList<DropFull>();
        this.endDrops = new ArrayList<DropFull>();
    }

    public static LuckyGenerator registerNew(BlockLuckyBlock block) {
        LuckyGenerator generator = new LuckyGenerator(NoFeatureConfig::deserialize);
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
        try {
            if (this.block.canPlaceAt(world, pos)) {
                Dimension dim = world.getDimension();

                if (dim.isNether()) this.generate(world, rand, pos, this.netherDrops);
                else if (dim.isSurfaceWorld()) this.generate(world, rand, pos, this.surfaceDrops);
                else this.generate(world, rand, pos, this.endDrops);

                return true;
            }
        } catch (Exception e) {
            Lucky.error(e, "Error during natural generation");
        }
        return false;
    }

    @Override
    public boolean place(IWorld world, ChunkGenerator<?> chunkGen,
        Random rand, BlockPos posIn, NoFeatureConfig config) {

        BlockPos pos = world.getHeight(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, posIn);
        return this.generate(world, rand, pos);
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
}
