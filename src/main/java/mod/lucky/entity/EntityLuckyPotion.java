package mod.lucky.entity;

import mod.lucky.Lucky;
import mod.lucky.drop.DropContainer;
import mod.lucky.drop.func.DropProcessData;
import mod.lucky.drop.func.DropProcessor;
import mod.lucky.init.SetupCommon;
import mod.lucky.item.ItemLuckyPotion;
import mod.lucky.util.LuckyFunction;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class EntityLuckyPotion extends EntityThrowable {
    private static final DataParameter<ItemStack> ITEM_STACK =
        EntityDataManager.createKey(EntityLuckyPotion.class, DataSerializers.ITEM_STACK);

    private DropProcessor impactDropProcessor;
    private int luck = 0;
    private String[] customDrops = null;

    public EntityLuckyPotion(World world) {
        this(world, null, Lucky.luckyPotion, new DropProcessor(), 0, null);
    }

    public EntityLuckyPotion(World world,
        EntityLivingBase thrower,
        ItemLuckyPotion itemLuckyPotion,
        DropProcessor impactDropProcessor,
        int luck, String[] customDrops) {

        super(SetupCommon.luckyPotionType, thrower, world);

        this.setItemStack(new ItemStack(itemLuckyPotion, 1));
        this.impactDropProcessor = impactDropProcessor;
        this.luck = luck;
        this.customDrops = customDrops;
    }

    @Override
    protected void registerData() {
        this.getDataManager().register(ITEM_STACK, ItemStack.EMPTY);
    }

    public ItemStack getItemStack() {
        return this.getDataManager().get(ITEM_STACK);
    }

    private void setItemStack(ItemStack stack) {
        this.getDataManager().set(ITEM_STACK, stack);
    }

    @Override
    protected float getGravityVelocity() {
        return 0.05F;
    }

    private void luckyImpact(@Nullable Entity hitEntity) {
        try {
            if (this.impactDropProcessor != null
                && this.impactDropProcessor.getDrops().size() > 0) {

                Vec3d impactPos = hitEntity == null ? this.getPositionVector()
                    : hitEntity.getPositionVector();

                DropProcessData dropData = new DropProcessData(
                    this.getEntityWorld(), this.getThrower(), impactPos);
                if (hitEntity != null) dropData.setHitEntity(hitEntity);

                if (this.customDrops != null && this.customDrops.length != 0) {
                    this.impactDropProcessor.processRandomDrop(
                        LuckyFunction.dropsFromStrArray(this.customDrops),
                        dropData, this.luck);
                } else {
                    this.impactDropProcessor.processRandomDrop(dropData, this.luck);
                }
            }
        } catch (Exception e) {
            Lucky.LOGGER.error(DropProcessor.errorMessage());
            e.printStackTrace();
        }
    }

    @Override
    protected void onImpact(RayTraceResult rayTraceResult) {
        if (!this.world.isRemote) {
            this.luckyImpact(rayTraceResult.entity);
            this.remove();
        }
    }

    @Override
    public void writeAdditional(NBTTagCompound tagCompound) {
        super.writeAdditional(tagCompound);
        NBTTagList drops = new NBTTagList();
        for (int i = 0; i < this.impactDropProcessor.getDrops().size(); i++) {
            String dropString = this.impactDropProcessor.getDrops().get(i).toString();
            drops.add(new NBTTagString(dropString));
        }
        tagCompound.setTag("impact", drops);
        tagCompound.setTag("itemLuckyPotion",
            this.getItemStack().write(new NBTTagCompound()));
    }

    @Override
    public void readAdditional(NBTTagCompound tag) {
        super.readAdditional(tag);
        NBTTagList drops = tag.getList("impact", new NBTTagString().getId());
        for (int i = 0; i < drops.size(); i++) {
            DropContainer drop = new DropContainer();
            drop.readFromString(drops.getString(i));
            this.impactDropProcessor.registerDrop(drop);
        }
        if (tag.hasKey("itemLuckyPotion"))
            this.setItemStack(ItemStack.read(tag.getCompound("itemLuckyPotion")));
    }
}
