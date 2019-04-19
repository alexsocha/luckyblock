package mod.lucky.entity;

import mod.lucky.Lucky;
import mod.lucky.drop.DropContainer;
import mod.lucky.drop.func.DropProcessData;
import mod.lucky.drop.func.DropProcessor;
import mod.lucky.item.ItemLuckyPotion;
import mod.lucky.util.LuckyFunction;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.init.Items;
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

public class EntityLuckyPotion extends EntityThrowable {
  private static final DataParameter<ItemStack> POTION_ITEM =
      EntityDataManager.<ItemStack>createKey(EntityLuckyPotion.class, DataSerializers.ITEM_STACK);
  private ItemStack itemLuckyPotion = null;
  private DropProcessor impactDropProcessor;
  private int luck = 0;
  private String[] customDrops = null;

  public EntityLuckyPotion(World world) {
    super(world);
  }

  public EntityLuckyPotion(World world, EntityLivingBase thrower) {
    this(world, thrower, Lucky.lucky_potion, new DropProcessor(), 0, null);
  }

  public EntityLuckyPotion(
      World world,
      EntityLivingBase thrower,
      ItemLuckyPotion itemLuckyPotion,
      DropProcessor impactDropProcessor,
      int luck,
      String[] customDrops) {
    super(world, thrower);
    this.itemLuckyPotion = new ItemStack(itemLuckyPotion, 1);
    this.dataManager.set(POTION_ITEM, this.itemLuckyPotion);
    this.impactDropProcessor = impactDropProcessor;
    this.luck = luck;
    this.customDrops = customDrops;
  }

  public EntityLuckyPotion(World world, double posX, double posY, double posZ) {
    super(world, posX, posY, posZ);
  }

  @Override
  protected void entityInit() {
    super.entityInit();
    if (this.impactDropProcessor == null) this.impactDropProcessor = new DropProcessor();
    this.dataManager.register(POTION_ITEM, new ItemStack(Items.STICK));
  }

  public ItemStack getItemLuckyPotion() {
    return this.dataManager.get(POTION_ITEM);
  }

  @Override
  protected float getGravityVelocity() {
    return 0.05F;
  }

  private void luckyImpact(Entity hitEntity) {
    try {
      if (this.impactDropProcessor != null && this.impactDropProcessor.getDrops().size() > 0) {
        Vec3d impactPos =
            hitEntity == null ? this.getPositionVector() : hitEntity.getPositionVector();
        if (this.customDrops != null && this.customDrops.length != 0)
          this.impactDropProcessor.processRandomDrop(
              LuckyFunction.getDropsFromStringArray(this.customDrops),
              new DropProcessData(this.getEntityWorld(), this.getThrower(), impactPos)
                  .setHitEntity(hitEntity),
              this.luck);
        else
          this.impactDropProcessor.processRandomDrop(
              new DropProcessData(this.getEntityWorld(), this.getThrower(), impactPos)
                  .setHitEntity(hitEntity),
              this.luck);
      }
    } catch (Exception e) {
      System.err.println(
          "The Lucky Potion encountered and error while trying to perform a function. Error report below:");
      e.printStackTrace();
    }
  }

  @Override
  public void onUpdate() {
    try {
      if (this.itemLuckyPotion == null && this.getEntityWorld().isRemote)
        this.itemLuckyPotion = this.dataManager.get(POTION_ITEM);
    } catch (Exception e) {
    }
    this.onEntityUpdate();
    super.onUpdate();
  }

  @Override
  protected void onImpact(RayTraceResult rayTraceResult) {
    if (!this.world.isRemote) {
      this.luckyImpact(rayTraceResult.entityHit);
      this.setDead();
    }
  }

  @Override
  public void writeEntityToNBT(NBTTagCompound tagCompound) {
    super.writeEntityToNBT(tagCompound);
    NBTTagList drops = new NBTTagList();
    for (int i = 0; i < this.impactDropProcessor.getDrops().size(); i++) {
      drops.appendTag(new NBTTagString(this.impactDropProcessor.getDrops().get(i).toString()));
    }
    tagCompound.setTag("impact", drops);
    tagCompound.setTag(
        "itemLuckyPotion", this.getItemLuckyPotion().writeToNBT(new NBTTagCompound()));
  }

  @Override
  public void readEntityFromNBT(NBTTagCompound tagCompound) {
    super.readEntityFromNBT(tagCompound);
    NBTTagList drops = tagCompound.getTagList("impact", new NBTTagString().getId());
    for (int i = 0; i < drops.tagCount(); i++) {
      DropContainer drop = new DropContainer();
      drop.readFromString(drops.getStringTagAt(i));
      this.impactDropProcessor.registerDrop(drop);
    }
    if (tagCompound.hasKey("itemLuckyPotion"))
      this.itemLuckyPotion = new ItemStack(tagCompound.getCompoundTag("itemLuckyPotion"));
  }
}
