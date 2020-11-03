package mod.lucky.tileentity;

import mod.lucky.block.BlockLuckyBlock;
import mod.lucky.init.SetupCommon;
import mod.lucky.util.LuckyUtils;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants;

public class TileEntityLuckyBlock extends TileEntity implements ITickableTileEntity {
    private String[] drops = new String[0];
    private int luck = 0;

    public TileEntityLuckyBlock() {
        super(SetupCommon.TE_LUCKY_BLOCK);
    }

    @Override
    public CompoundNBT write(CompoundNBT nbttag) {
        super.write(nbttag);
        nbttag.put("Drops", LuckyUtils.tagListFromStrArray(this.drops));
        nbttag.putInt("Luck", this.luck);
        return nbttag;
    }

    @Override
    public void read(BlockState blockState, CompoundNBT nbttag) {
        super.read(blockState, nbttag);

        if (nbttag.contains("Drops"))
            this.drops = LuckyUtils.strArrayFromTagList(
                nbttag.getList("Drops", Constants.NBT.TAG_STRING));
        if (nbttag.contains("Luck"))
            this.luck = nbttag.getInt("Luck");
    }

    @Override
    public SUpdateTileEntityPacket getUpdatePacket() {
        CompoundNBT nbttag = new CompoundNBT();
        this.write(nbttag);
        return new SUpdateTileEntityPacket(
            new BlockPos(this.pos.getX(), this.pos.getY(), this.pos.getZ()), 1, nbttag);
    }

    @Override
    public void tick() {
        if (this.world != null && !this.world.isRemote && this.world.getGameTime() % 20L == 0L) {
            BlockState blockState = this.world .getBlockState(this.getPos());
            if (!(blockState.getBlock() instanceof BlockLuckyBlock)) {
                this.remove();
            }
        }
    }

    public void setLuck(int luck) { this.luck = luck; }
    public int getLuck() { return this.luck; }

    public void setDrops(String[] drops) { this.drops = drops; }
    public String[] getDrops() { return this.drops; }
}
