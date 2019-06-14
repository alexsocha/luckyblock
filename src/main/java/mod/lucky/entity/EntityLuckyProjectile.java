package mod.lucky.entity;

import mod.lucky.Lucky;
import mod.lucky.drop.DropFull;
import mod.lucky.drop.func.DropProcessData;
import mod.lucky.drop.func.DropProcessor;
import mod.lucky.init.SetupCommon;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import javax.annotation.Nullable;

public class EntityLuckyProjectile extends EntityArrow {
    private static final DataParameter<ItemStack> ITEM_STACK =
        EntityDataManager.createKey(
            EntityLuckyProjectile.class, DataSerializers.ITEM_STACK);

    private EntityItem entityItem;
    private boolean hasTrail = false;
    private float trailFrequency = 1;
    private boolean hasImpact = false;
    private DropProcessor dropProcessorTrail;
    private DropProcessor dropProcessorImpact;

    public EntityLuckyProjectile(World world) {
        super(SetupCommon.LUCKY_PROJECTILE_TYPE, world);
    }

    @Override
    protected void registerData() {
        this.getDataManager().register(ITEM_STACK, ItemStack.EMPTY);
        this.dropProcessorTrail = new DropProcessor();
        this.dropProcessorImpact = new DropProcessor();
    }

    public ItemStack getItemStack() {
        return this.getDataManager().get(ITEM_STACK);
    }

    private void setItemStack(ItemStack stack) {
        this.getDataManager().set(ITEM_STACK, stack);
    }

    @Nullable
    private Entity getShooter() {
        return this.shootingEntity != null && this.world instanceof WorldServer
            ? ((WorldServer) this.world).getEntityFromUuid(this.shootingEntity)
            : null;
    }

    private void luckyTick() {
        try {
            if (this.entityItem == null && this.getEntityWorld().isRemote)
                this.entityItem = new EntityItem(
                    this.getEntityWorld(),
                    this.posX, this.posY, this.posZ,
                    this.dataManager.get(ITEM_STACK));
        } catch (Exception e) {}

        if (this.entityItem != null) this.entityItem.tick();

        if (!this.getEntityWorld().isRemote && this.hasTrail && this.ticksExisted >= 2) {
            try {
                if (this.trailFrequency < 1.0 && this.trailFrequency > 0) {
                    int amount = (int) (1.0 / this.trailFrequency);
                    for (int i = 0; i < amount; i++) {
                        this.dropProcessorTrail.processRandomDrop(
                            new DropProcessData(
                                this.getEntityWorld(),
                                this.getShooter(),
                                new Vec3d(
                                    this.posX + this.motionX * i / amount,
                                    this.posY + this.motionY * i / amount,
                                    this.posZ + this.motionZ * i / amount)),
                            0, false);
                    }
                } else if ((this.ticksExisted - 2) % ((int) this.trailFrequency) == 0)
                    this.dropProcessorTrail.processRandomDrop(
                        new DropProcessData(
                            this.getEntityWorld(),
                            this.getShooter(),
                            this.getPositionVector()),
                        0, false);
            } catch (Exception e) {
                Lucky.error(e, DropProcessor.errorMessage());
            }
        }
    }

    private void luckyHit(@Nullable Entity hitEntity) {
        try {
            if (this.hasImpact) {
                DropProcessData dropData = new DropProcessData(
                    this.getEntityWorld(),
                    this.getShooter(),
                    hitEntity != null
                        ? hitEntity.getPositionVector()
                        : this.getPositionVector());

                this.dropProcessorImpact.processRandomDrop(dropData, 0);
            }
        } catch (Exception e) {
            Lucky.error(e, DropProcessor.errorMessage());
        }
        this.remove();
    }

    @Override
    public void tick() {
        super.tick();
        luckyTick();
    }

    @Override
    protected void onHit(RayTraceResult rayTraceResult) {
        super.onHit(rayTraceResult);
        this.luckyHit(rayTraceResult.entity);
    }

    @Override
    public void writeAdditional(NBTTagCompound tag) {
        super.writeAdditional(tag);
        if (this.entityItem != null)
            tag.setTag("item", this.entityItem.getItem().write(new NBTTagCompound()));

        if (this.hasTrail) {
            NBTTagCompound trailTag = new NBTTagCompound();
            trailTag.setFloat("frequency", this.trailFrequency);

            NBTTagList drops = new NBTTagList();
            for (int i = 0; i < this.dropProcessorTrail.getDrops().size(); i++) {
                String dropString = this.dropProcessorTrail.getDrops().get(i).toString();
                drops.add(new NBTTagString(dropString));
            }
            trailTag.setTag("drops", drops);
            tag.setTag("trail", trailTag);
        }

        if (this.hasImpact) {
            NBTTagList drops = new NBTTagList();
            for (int i = 0; i < this.dropProcessorImpact.getDrops().size(); i++) {
                String dropString = this.dropProcessorTrail.getDrops().get(i).toString();
                drops.add(new NBTTagString(dropString));
            }
            tag.setTag("impact", drops);
        }
    }


    @Override
    public void readAdditional(NBTTagCompound tag) {
        ItemStack stack = null;
        if (tag.hasKey("item")) stack = ItemStack.read(tag.getCompound("item"));
        else stack = new ItemStack(Items.STICK);
        stack.setCount(1);

        this.entityItem = new EntityItem(
            this.getEntityWorld(),
            this.posX, this.posY, this.posZ,
            stack);

        this.getDataManager().set(ITEM_STACK, stack);

        if (tag.hasKey("trail")) {
            NBTTagCompound trailTag = tag.getCompound("trail");
            if (trailTag.hasKey("frequency"))
                this.trailFrequency = trailTag.getFloat("frequency");
            if (trailTag.hasKey("drops")) {
                NBTTagList drops = trailTag.getList("drops", new NBTTagString().getId());
                for (int i = 0; i < drops.size(); i++) {
                    DropFull drop = new DropFull();
                    drop.readFromString(drops.getString(i));
                    this.dropProcessorTrail.registerDrop(drop);
                }
            }
            this.hasTrail = true;
        }

        if (tag.hasKey("impact")) {
            NBTTagList drops = tag.getList("impact", new NBTTagString().getId());
            for (int i = 0; i < drops.size(); i++) {
                DropFull drop = new DropFull();
                drop.readFromString(drops.getString(i));
                this.dropProcessorImpact.registerDrop(drop);
            }
            this.hasImpact = true;
        }
    }

    @Override
    protected ItemStack getArrowStack() { return ItemStack.EMPTY; }

    public EntityItem getEntityItem() { return this.entityItem; }
}
