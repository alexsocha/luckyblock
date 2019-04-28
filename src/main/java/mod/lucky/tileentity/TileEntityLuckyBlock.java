package mod.lucky.tileentity;

import mod.lucky.Lucky;
import mod.lucky.block.BlockLuckyBlock;
import mod.lucky.init.SetupCommon;
import mod.lucky.util.LuckyUtils;
import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;

public class TileEntityLuckyBlock extends TileEntity implements ITickable {
    private String[] drops = new String[0];
    private int luck = 0;

    public TileEntityLuckyBlock() {
        super(SetupCommon.LUCKY_BLOCK_TE_TYPE);
    }

    @Override
    public NBTTagCompound write(NBTTagCompound nbttag) {
        super.write(nbttag);
        nbttag.setTag("Drops", LuckyUtils.tagListFromStrArray(this.drops));
        nbttag.setInt("Luck", this.luck);
        return nbttag;
    }

    @Override
    public void read(NBTTagCompound nbttag) {
        super.read(nbttag);
        this.drops = LuckyUtils.strArrayFromTagList((NBTTagList) nbttag.getTag("Drops"));
        this.luck = nbttag.getInt("Luck");
    }

    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        NBTTagCompound nbttag = new NBTTagCompound();
        this.write(nbttag);
        return new SPacketUpdateTileEntity(
            new BlockPos(this.pos.getX(), this.pos.getY(), this.pos.getZ()), 1, nbttag);
    }

    @Override
    public void tick() {
        if (this.world != null && !this.world.isRemote && this.world.getGameTime() % 20L == 0L) {
            Block luckyBlock =
                this.world
                    .getBlockState(new BlockPos(this.pos.getX(), this.pos.getY(), this.pos.getZ()))
                    .getBlock();
            if (luckyBlock == Lucky.luckyBlock) {
                if (this.world.isBlockPowered(
                    new BlockPos(this.pos.getX(), this.pos.getY(), this.pos.getZ()))) {
                    ((BlockLuckyBlock) luckyBlock)
                        .removeLuckyBlock(
                            this.world,
                            null,
                            new BlockPos(this.pos.getX(), this.pos.getY(), this.pos.getZ()),
                            true);
                }
            }
        }
    }

    public void setLuck(int luck) { this.luck = luck; }
    public int getLuck() { return this.luck; }

    public void setDrops(String[] drops) { this.drops = drops; }
    public String[] getDrops() { return this.drops; }
}
