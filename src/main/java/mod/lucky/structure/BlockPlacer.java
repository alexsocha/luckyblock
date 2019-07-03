package mod.lucky.structure;

import java.util.ArrayList;

import mod.lucky.drop.func.DropFuncBlock;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

public class BlockPlacer {
    private IWorld world;
    private ArrayList<BlockPos> updatePos;
    private ArrayList<IBlockState> updateState;


    public BlockPlacer(IWorld world) {
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
            if (world instanceof World) {
                World fullWorld = (World) this.world;
                fullWorld.markAndNotifyBlock(pos,
                    fullWorld.getChunk(pos),
                    fullWorld.getBlockState(pos),
                    this.updateState.get(i),
                    3);
            }
        }
    }

    public IWorld getWorld() { return this.world; }
}
