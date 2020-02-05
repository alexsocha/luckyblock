package mod.lucky.entity;

import mod.lucky.Lucky;
import mod.lucky.drop.DropFull;
import mod.lucky.drop.func.DropProcessData;
import mod.lucky.drop.func.DropProcessor;
import mod.lucky.init.SetupCommon;
import mod.lucky.item.ItemLuckyPotion;
import mod.lucky.util.LuckyUtils;
import mod.lucky.util.ObfHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.IRendersAsItem;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.ThrowableEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;

@OnlyIn(value = Dist.CLIENT, _interface = IRendersAsItem.class)
public class EntityLuckyPotion extends ThrowableEntity implements IRendersAsItem {
    private static final DataParameter<ItemStack> ITEM_STACK =
        EntityDataManager.createKey(EntityLuckyPotion.class, DataSerializers.ITEMSTACK);

    private DropProcessor impactDropProcessor;
    private int luck = 0;
    private String[] customDrops = null;

    public void init(ItemLuckyPotion itemLuckyPotion,
        DropProcessor impactDropProcessor,
        int luck, String[] customDrops) {

        this.setItemStack(new ItemStack(itemLuckyPotion, 1));
        this.impactDropProcessor = impactDropProcessor;
        this.luck = luck;
        this.customDrops = customDrops;
    }

    public EntityLuckyPotion(EntityType<? extends EntityLuckyPotion> entityType, World world) {
        super(entityType, world);
        this.init(Lucky.luckyPotion, new DropProcessor(), 0, null);
    }

    public EntityLuckyPotion(World world) {
        this(SetupCommon.ENTITY_LUCKY_POTION, world);
    }

    public EntityLuckyPotion(World world,
        LivingEntity thrower,
        ItemLuckyPotion itemLuckyPotion,
        DropProcessor impactDropProcessor,
        int luck, String[] customDrops) {

        super(SetupCommon.ENTITY_LUCKY_POTION, thrower, world);
        this.init(itemLuckyPotion, impactDropProcessor, luck, customDrops);
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
                        LuckyUtils.dropsFromStrArray(this.customDrops),
                        dropData, this.luck);
                } else {
                    this.impactDropProcessor.processRandomDrop(dropData, this.luck);
                }
            }
        } catch (Exception e) {
            Lucky.error(e, DropProcessor.errorMessage());
        }
    }

    @Override
    protected void onImpact(RayTraceResult rayTrace) {
        if (!this.world.isRemote) {
            Entity hitEntity = rayTrace.getType() == RayTraceResult.Type.ENTITY
                ? ((EntityRayTraceResult) rayTrace).getEntity() : null;
            this.luckyImpact(hitEntity);
            this.remove();
        }
    }

    @Override
    public void writeAdditional(CompoundNBT tagCompound) {
        super.writeAdditional(tagCompound);
        ListNBT drops = new ListNBT();
        for (int i = 0; i < this.impactDropProcessor.getDrops().size(); i++) {
            String dropString = this.impactDropProcessor.getDrops().get(i).toString();
            drops.add(ObfHelper.createStringNBT(dropString));
        }
        tagCompound.put("impact", drops);
        tagCompound.put("itemLuckyPotion",
            this.getItemStack().write(new CompoundNBT()));
    }

    @Override
    public void readAdditional(CompoundNBT tag) {
        super.readAdditional(tag);
        ListNBT drops = tag.getList("impact", ObfHelper.createStringNBT("").getId());
        for (int i = 0; i < drops.size(); i++) {
            DropFull drop = new DropFull();
            drop.readFromString(drops.getString(i));
            this.impactDropProcessor.registerDrop(drop);
        }
        if (tag.contains("itemLuckyPotion"))
            this.setItemStack(ItemStack.read(tag.getCompound("itemLuckyPotion")));
    }

    @Override
    public ItemStack getItem() {
        return this.getItemStack();
    }
}
