package mod.lucky.entity;

import mod.lucky.Lucky;
import mod.lucky.drop.DropFull;
import mod.lucky.drop.func.DropProcessData;
import mod.lucky.drop.func.DropProcessor;
import mod.lucky.init.SetupCommon;
import mod.lucky.util.ObfHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.math.*;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class EntityLuckyProjectile extends ArrowEntity {
    private static final DataParameter<ItemStack> ITEM_STACK =
        EntityDataManager.createKey(
            EntityLuckyProjectile.class, DataSerializers.ITEMSTACK);

    private ItemEntity entityItem;
    private boolean hasTrail = false;
    private float trailFrequency = 1;
    private boolean hasImpact = false;
    private DropProcessor dropProcessorTrail;
    private DropProcessor dropProcessorImpact;

    public EntityLuckyProjectile(EntityType<? extends EntityLuckyProjectile> entityType, World world) {
        super(entityType, world);
    }

    public EntityLuckyProjectile(World world) {
        this(SetupCommon.ENTITY_LUCKY_PROJECTILE, world);
    }

    @Override
    protected void registerData() {
        super.registerData();
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

    private void luckyTick() {
        Vec3d pos = this.getPositionVector();
        try {
            if (this.entityItem == null && this.getEntityWorld().isRemote)
                this.entityItem = new ItemEntity(
                    this.getEntityWorld(),
                    pos.x, pos.y, pos.z,
                    this.dataManager.get(ITEM_STACK));
        } catch (Exception e) {}

        if (this.entityItem != null) this.entityItem.tick();

        if (!this.getEntityWorld().isRemote && this.hasTrail && this.ticksExisted >= 2) {
            try {
                if (this.trailFrequency < 1.0 && this.trailFrequency > 0) {
                    int amount = (int) (1.0 / this.trailFrequency);
                    for (int i = 0; i < amount; i++) {
                        Vec3d entityMotion = this.getMotion();
                        this.dropProcessorTrail.processRandomDrop(
                            new DropProcessData(
                                this.getEntityWorld(),
                                this.getShooter(),
                                new Vec3d(
                                    pos.x + entityMotion.x * i / amount,
                                    pos.y + entityMotion.y * i / amount,
                                    pos.z + entityMotion.z * i / amount)),
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
                if (hitEntity != null) dropData.setHitEntity(hitEntity);

                this.dropProcessorImpact.processRandomDrop(dropData, 0);
            }
        } catch (Exception e) {
            Lucky.error(e, DropProcessor.errorMessage());
        }
        this.remove();
    }

    @Override
    public void tick() {
        try {
            super.tick();
            luckyTick();
        } catch (Exception e) {
            Lucky.error(e, "Error in lucky projectile tick");
        }
    }

    @Override
    protected void onHit(RayTraceResult rayTrace) {
        super.onHit(rayTrace);
        if (rayTrace.getType() != RayTraceResult.Type.MISS) {
            if (!this.world.isRemote) {
                Entity hitEntity = rayTrace.getType() == RayTraceResult.Type.ENTITY
                    ? ((EntityRayTraceResult) rayTrace).getEntity() : null;
                this.luckyHit(hitEntity);
            }
            this.remove();
        }
    }

    @Override
    public void writeAdditional(CompoundNBT tag) {
        super.writeAdditional(tag);
        if (this.entityItem != null)
            tag.put("item", this.entityItem.getItem().write(new CompoundNBT()));

        if (this.hasTrail) {
            CompoundNBT trailTag = new CompoundNBT();
            trailTag.putFloat("frequency", this.trailFrequency);

            ListNBT drops = new ListNBT();
            for (int i = 0; i < this.dropProcessorTrail.getDrops().size(); i++) {
                String dropString = this.dropProcessorTrail.getDrops().get(i).toString();
                drops.add(ObfHelper.createStringNBT(dropString));
            }
            trailTag.put("drops", drops);
            tag.put("trail", trailTag);
        }

        if (this.hasImpact) {
            ListNBT drops = new ListNBT();
            for (int i = 0; i < this.dropProcessorImpact.getDrops().size(); i++) {
                String dropString = this.dropProcessorImpact.getDrops().get(i).toString();
                drops.add(ObfHelper.createStringNBT(dropString));
            }
            tag.put("impact", drops);
        }
    }


    @Override
    public void readAdditional(CompoundNBT tag) {
        ItemStack stack = null;
        if (tag.contains("item")) stack = ItemStack.read(tag.getCompound("item"));
        else stack = new ItemStack(Items.STICK);
        stack.setCount(1);

        Vec3d pos = this.getPositionVector();
        this.entityItem = new ItemEntity(
            this.getEntityWorld(),
            pos.x, pos.y, pos.z,
            stack);

        this.getDataManager().set(ITEM_STACK, stack);

        if (tag.contains("trail")) {
            CompoundNBT trailTag = tag.getCompound("trail");
            if (trailTag.contains("frequency"))
                this.trailFrequency = trailTag.getFloat("frequency");
            if (trailTag.contains("drops")) {
                ListNBT drops = trailTag.getList("drops", ObfHelper.createStringNBT("").getId());
                for (int i = 0; i < drops.size(); i++) {
                    DropFull drop = new DropFull();
                    drop.readFromString(drops.getString(i));
                    this.dropProcessorTrail.registerDrop(drop);
                }
            }
            this.hasTrail = true;
        }

        if (tag.contains("impact")) {
            ListNBT drops = tag.getList("impact", ObfHelper.createStringNBT("").getId());
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

    public ItemEntity getItemEntity() { return this.entityItem; }
}
