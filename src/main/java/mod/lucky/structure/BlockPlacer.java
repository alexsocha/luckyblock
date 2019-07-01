package mod.lucky.structure;

import java.util.ArrayList;

import mod.lucky.drop.func.DropFuncBlock;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockPlacer {
    private World world;
    private ArrayList<BlockPos> updatePos;
    private ArrayList<IBlockState> updateState;


    public BlockPlacer(World world) {
        this.world = world;
        this.updatePos = new ArrayList<BlockPos>();
        this.updateState = new ArrayList<IBlockState>();
    }

    public void add(IBlockState blockState, BlockPos blockPos) {
        if (blockPos.getY() <= 0) return;
        DropFuncBlock.setBlock(this.world, blockState, blockPos, false);
        this.updatePos.add(blockPos);
        this.updateState.add(blockState);
    }

    public void update() {
        for (int i = 0; i < this.updatePos.size(); i++) {
            BlockPos pos = this.updatePos.get(i);
            this.world.markAndNotifyBlock(pos,
                this.world.getChunk(pos),
                this.world.getBlockState(pos),
                this.updateState.get(i),
                3);
        }
    }

    public World getWorld() { return this.world; }
}
