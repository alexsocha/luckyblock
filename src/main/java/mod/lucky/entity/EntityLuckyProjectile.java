package mod.lucky.entity;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import java.util.List;
import javax.annotation.Nullable;
import mod.lucky.drop.DropContainer;
import mod.lucky.drop.func.DropProcessData;
import mod.lucky.drop.func.DropProcessor;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IProjectile;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.network.play.server.SPacketChangeGameState;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class EntityLuckyProjectile extends Entity implements IProjectile {
  private static final Predicate<Entity> ARROW_TARGETS =
      Predicates.and(
          EntitySelectors.NOT_SPECTATING,
          EntitySelectors.IS_ALIVE,
          new Predicate<Entity>() {
            public boolean apply(@Nullable Entity p_apply_1_) {
              return p_apply_1_.canBeCollidedWith();
            }
          });
  private static final DataParameter<Byte> CRITICAL =
      EntityDataManager.<Byte>createKey(EntityLuckyProjectile.class, DataSerializers.BYTE);
  private int xTile;
  private int yTile;
  private int zTile;
  private Block inTile;
  private int inData;
  protected boolean inGround;
  protected int field_184552_b;
  /** 1 if the player can pick up the arrow */
  public EntityArrow.PickupStatus canBePickedUp;
  /** Seems to be some sort of timer for animating an arrow. */
  public int arrowShake;
  /** The owner of this arrow. */
  public Entity shootingEntity;

  private int ticksInGround;
  private int ticksInAir;
  private double damage;
  /** The amount of knockback an arrow applies when it hits a mob. */
  private int knockbackStrength;

  // CUSTOM
  private static final DataParameter<ItemStack> PROJECTILE_ITEM =
      EntityDataManager.<ItemStack>createKey(
          EntityLuckyProjectile.class, DataSerializers.ITEM_STACK);
  private EntityItem item;
  private boolean hasTrail = false;
  private float trailFrequency = 1;
  private boolean hasImpact = false;
  private DropProcessor dropProcessorTrail;
  private DropProcessor dropProcessorImpact;

  public EntityLuckyProjectile(World worldIn) {
    super(worldIn);
    this.xTile = -1;
    this.yTile = -1;
    this.zTile = -1;
    this.canBePickedUp = EntityArrow.PickupStatus.DISALLOWED;
    this.damage = 2.0D;
    this.setSize(0.5F, 0.5F);
  }

  public EntityLuckyProjectile(World worldIn, double x, double y, double z) {
    this(worldIn);
    this.setPosition(x, y, z);
  }

  public EntityLuckyProjectile(World worldIn, EntityLivingBase shooter) {
    this(
        worldIn,
        shooter.posX,
        shooter.posY + shooter.getEyeHeight() - 0.10000000149011612D,
        shooter.posZ);
    this.shootingEntity = shooter;

    if (shooter instanceof EntityPlayer) {
      this.canBePickedUp = EntityArrow.PickupStatus.ALLOWED;
    }
  }

  private void luckyImpact(Entity entityHit) {
    try {
      if (this.hasImpact) {
        if (entityHit == null)
          this.dropProcessorImpact.processRandomDrop(
              new DropProcessData(
                  this.getEntityWorld(), this.shootingEntity, this.getPositionVector()),
              0);
        else
          this.dropProcessorImpact.processRandomDrop(
              new DropProcessData(
                      this.getEntityWorld(), this.shootingEntity, entityHit.getPositionVector())
                  .setHitEntity(entityHit),
              0);
      }

      if (!this.getEntityWorld().isRemote) this.setDead();
    } catch (Exception e) {
      System.err.println(
          "The Lucky Bow encountered and error while trying to perform an impact function. Error report below:");
      e.printStackTrace();
    }
    this.setDead();
  }

  private void luckyUpdate() {
    try {
      if (this.item == null && this.getEntityWorld().isRemote)
        this.item =
            new EntityItem(
                this.getEntityWorld(),
                this.posX,
                this.posY,
                this.posZ,
                this.dataManager.get(PROJECTILE_ITEM));
    } catch (Exception e) {
    }

    if (this.item != null) this.item.onUpdate();

    if (!this.getEntityWorld().isRemote && this.hasTrail && this.ticksInAir >= 2) {
      try {
        if (this.trailFrequency < 1.0 && this.trailFrequency > 0) {
          int amount = (int) (1.0 / this.trailFrequency);
          for (int i = 0; i < amount; i++) {
            this.dropProcessorTrail.processRandomDrop(
                new DropProcessData(
                    this.getEntityWorld(),
                    this.shootingEntity,
                    new Vec3d(
                        this.posX + this.motionX * i / amount,
                        this.posY + this.motionY * i / amount,
                        this.posZ + this.motionZ * i / amount)),
                0,
                false);
          }
        } else if ((this.ticksInAir - 2) % ((int) this.trailFrequency) == 0)
          this.dropProcessorTrail.processRandomDrop(
              new DropProcessData(
                  this.getEntityWorld(), this.shootingEntity, this.getPositionVector()),
              0,
              false);
      } catch (Exception e) {
        System.err.println(
            "The Lucky Bow encountered and error while trying to perform a trail function. Error report below:");
        e.printStackTrace();
      }
    }
  }

  /**
   * Checks if the entity is in range to render by using the past in distance and comparing it to
   * its average edge length * 64 * renderDistanceWeight Args: distance
   */
  @Override
  @SideOnly(Side.CLIENT)
  public boolean isInRangeToRenderDist(double distance) {
    double d0 = this.getEntityBoundingBox().getAverageEdgeLength() * 10.0D;

    if (Double.isNaN(d0)) {
      d0 = 1.0D;
    }

    d0 = d0 * 64.0D * getRenderDistanceWeight();
    return distance < d0 * d0;
  }

  @Override
  protected void entityInit() {
    this.dataManager.register(PROJECTILE_ITEM, new ItemStack(Items.STICK));
    this.dataManager.register(CRITICAL, Byte.valueOf((byte) 0));
    this.dropProcessorTrail = new DropProcessor();
    this.dropProcessorImpact = new DropProcessor();
  }

  public void func_184547_a(
      Entity p_184547_1_,
      float p_184547_2_,
      float p_184547_3_,
      float p_184547_4_,
      float p_184547_5_,
      float p_184547_6_) {
    float f =
        -MathHelper.sin(p_184547_3_ * 0.017453292F) * MathHelper.cos(p_184547_2_ * 0.017453292F);
    float f1 = -MathHelper.sin(p_184547_2_ * 0.017453292F);
    float f2 =
        MathHelper.cos(p_184547_3_ * 0.017453292F) * MathHelper.cos(p_184547_2_ * 0.017453292F);
    this.shoot(f, f1, f2, p_184547_5_, p_184547_6_);
    this.motionX += p_184547_1_.motionX;
    this.motionZ += p_184547_1_.motionZ;

    if (!p_184547_1_.onGround) {
      this.motionY += p_184547_1_.motionY;
    }
  }

  /** Similar to setArrowHeading, it's point the throwable entity to a x, y, z direction. */
  @Override
  public void shoot(double x, double y, double z, float velocity, float inaccuracy) {
    float f = MathHelper.sqrt(x * x + y * y + z * z);
    x = x / f;
    y = y / f;
    z = z / f;
    x = x + this.rand.nextGaussian() * 0.007499999832361937D * inaccuracy;
    y = y + this.rand.nextGaussian() * 0.007499999832361937D * inaccuracy;
    z = z + this.rand.nextGaussian() * 0.007499999832361937D * inaccuracy;
    x = x * velocity;
    y = y * velocity;
    z = z * velocity;
    this.motionX = x;
    this.motionY = y;
    this.motionZ = z;
    float f1 = MathHelper.sqrt(x * x + z * z);
    this.prevRotationYaw = this.rotationYaw = (float) (MathHelper.atan2(x, z) * (180D / Math.PI));
    this.prevRotationPitch =
        this.rotationPitch = (float) (MathHelper.atan2(y, f1) * (180D / Math.PI));
    this.ticksInGround = 0;
  }

  @Override
  @SideOnly(Side.CLIENT)
  public void setPositionAndRotationDirect(
      double x,
      double y,
      double z,
      float yaw,
      float pitch,
      int posRotationIncrements,
      boolean p_180426_10_) {
    this.setPosition(x, y, z);
    this.setRotation(yaw, pitch);
  }

  /** Sets the velocity to the args. Args: x, y, z */
  @Override
  @SideOnly(Side.CLIENT)
  public void setVelocity(double x, double y, double z) {
    this.motionX = x;
    this.motionY = y;
    this.motionZ = z;

    if (this.prevRotationPitch == 0.0F && this.prevRotationYaw == 0.0F) {
      float f = MathHelper.sqrt(x * x + z * z);
      this.prevRotationYaw = this.rotationYaw = (float) (MathHelper.atan2(x, z) * (180D / Math.PI));
      this.prevRotationPitch =
          this.rotationPitch = (float) (MathHelper.atan2(y, f) * (180D / Math.PI));
      this.prevRotationPitch = this.rotationPitch;
      this.prevRotationYaw = this.rotationYaw;
      this.setLocationAndAngles(
          this.posX, this.posY, this.posZ, this.rotationYaw, this.rotationPitch);
      this.ticksInGround = 0;
    }
  }

  /** Called to update the entity's position/logic. */
  @Override
  public void onUpdate() {
    super.onUpdate();

    if (this.prevRotationPitch == 0.0F && this.prevRotationYaw == 0.0F) {
      float f = MathHelper.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
      this.prevRotationYaw =
          this.rotationYaw =
              (float) (MathHelper.atan2(this.motionX, this.motionZ) * (180D / Math.PI));
      this.prevRotationPitch =
          this.rotationPitch = (float) (MathHelper.atan2(this.motionY, f) * (180D / Math.PI));
    }

    BlockPos blockpos = new BlockPos(this.xTile, this.yTile, this.zTile);
    IBlockState iblockstate = this.world.getBlockState(blockpos);
    Block block = iblockstate.getBlock();

    if (iblockstate.getMaterial() != Material.AIR) {
      AxisAlignedBB axisalignedbb = iblockstate.getCollisionBoundingBox(this.world, blockpos);

      if (axisalignedbb != Block.NULL_AABB
          && axisalignedbb.offset(blockpos).contains(new Vec3d(this.posX, this.posY, this.posZ))) {
        this.inGround = true;
      }
    }

    if (this.arrowShake > 0) {
      --this.arrowShake;
    }

    if (this.inGround) {
      int j = block.getMetaFromState(iblockstate);

      if (block == this.inTile && j == this.inData) {
        ++this.ticksInGround;

        if (this.ticksInGround >= 1200) {
          this.setDead();
        }
      } else {
        this.inGround = false;
        this.motionX *= this.rand.nextFloat() * 0.2F;
        this.motionY *= this.rand.nextFloat() * 0.2F;
        this.motionZ *= this.rand.nextFloat() * 0.2F;
        this.ticksInGround = 0;
        this.ticksInAir = 0;
      }

      ++this.field_184552_b;

      this.luckyImpact(null);
      this.luckyUpdate();
    } else {
      this.field_184552_b = 0;
      ++this.ticksInAir;
      Vec3d vec3d1 = new Vec3d(this.posX, this.posY, this.posZ);
      Vec3d vec3d =
          new Vec3d(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ);
      RayTraceResult raytraceresult = this.world.rayTraceBlocks(vec3d1, vec3d, false, true, false);
      vec3d1 = new Vec3d(this.posX, this.posY, this.posZ);
      vec3d =
          new Vec3d(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ);

      if (raytraceresult != null) {
        vec3d =
            new Vec3d(raytraceresult.hitVec.x, raytraceresult.hitVec.y, raytraceresult.hitVec.z);
      }

      Entity entity = this.func_184551_a(vec3d1, vec3d);

      if (entity != null) {
        raytraceresult = new RayTraceResult(entity);
      }

      if (raytraceresult != null
          && raytraceresult.entityHit != null
          && raytraceresult.entityHit instanceof EntityPlayer) {
        EntityPlayer entityplayer = (EntityPlayer) raytraceresult.entityHit;

        if (this.shootingEntity instanceof EntityPlayer
            && !((EntityPlayer) this.shootingEntity).canAttackPlayer(entityplayer)) {
          raytraceresult = null;
        }
      }

      if (raytraceresult != null
          && (this.ticksInAir > 5
              || (raytraceresult.entityHit != this.shootingEntity
                  && this.shootingEntity != null))) {
        this.onHit(raytraceresult);
        this.luckyImpact(raytraceresult.entityHit);
      }

      if (this.getIsCritical()) {
        for (int k = 0; k < 4; ++k) {
          this.world.spawnParticle(
              EnumParticleTypes.CRIT,
              this.posX + this.motionX * k / 4.0D,
              this.posY + this.motionY * k / 4.0D,
              this.posZ + this.motionZ * k / 4.0D,
              -this.motionX,
              -this.motionY + 0.2D,
              -this.motionZ,
              new int[0]);
        }
      }

      this.luckyUpdate();

      this.posX += this.motionX;
      this.posY += this.motionY;
      this.posZ += this.motionZ;
      float f4 = MathHelper.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
      this.rotationYaw = (float) (MathHelper.atan2(this.motionX, this.motionZ) * (180D / Math.PI));

      for (this.rotationPitch = (float) (MathHelper.atan2(this.motionY, f4) * (180D / Math.PI));
          this.rotationPitch - this.prevRotationPitch < -180.0F;
          this.prevRotationPitch -= 360.0F) {;
      }

      while (this.rotationPitch - this.prevRotationPitch >= 180.0F) {
        this.prevRotationPitch += 360.0F;
      }

      while (this.rotationYaw - this.prevRotationYaw < -180.0F) {
        this.prevRotationYaw -= 360.0F;
      }

      while (this.rotationYaw - this.prevRotationYaw >= 180.0F) {
        this.prevRotationYaw += 360.0F;
      }

      this.rotationPitch =
          this.prevRotationPitch + (this.rotationPitch - this.prevRotationPitch) * 0.2F;
      this.rotationYaw = this.prevRotationYaw + (this.rotationYaw - this.prevRotationYaw) * 0.2F;
      float f1 = 0.99F;
      float f2 = 0.05F;

      if (this.isInWater()) {
        for (int i = 0; i < 4; ++i) {
          float f3 = 0.25F;
          this.world.spawnParticle(
              EnumParticleTypes.WATER_BUBBLE,
              this.posX - this.motionX * f3,
              this.posY - this.motionY * f3,
              this.posZ - this.motionZ * f3,
              this.motionX,
              this.motionY,
              this.motionZ,
              new int[0]);
        }

        f1 = 0.6F;
      }

      if (this.isWet()) {
        this.extinguish();
      }

      this.motionX *= f1;
      this.motionY *= f1;
      this.motionZ *= f1;
      this.motionY -= f2;
      this.setPosition(this.posX, this.posY, this.posZ);
      this.doBlockCollisions();
    }
  }

  protected void onHit(RayTraceResult p_184549_1_) {
    Entity entity = p_184549_1_.entityHit;

    if (entity != null) {
      float f =
          MathHelper.sqrt(
              this.motionX * this.motionX
                  + this.motionY * this.motionY
                  + this.motionZ * this.motionZ);
      int i = MathHelper.ceil(f * this.damage);

      if (this.getIsCritical()) {
        i += this.rand.nextInt(i / 2 + 2);
      }

      DamageSource damagesource = null;

      if (this.shootingEntity == null) {
        damagesource = DamageSource.causeIndirectDamage(this, null);
      } else {
        if (this.shootingEntity instanceof EntityLivingBase)
          damagesource =
              DamageSource.causeIndirectDamage(this, (EntityLivingBase) this.shootingEntity);
      }

      if (this.isBurning() && !(entity instanceof EntityEnderman)) {
        entity.setFire(5);
      }

      if (entity.attackEntityFrom(damagesource, i)) {
        if (entity instanceof EntityLivingBase) {
          EntityLivingBase entitylivingbase = (EntityLivingBase) entity;

          if (!this.world.isRemote) {
            entitylivingbase.setArrowCountInEntity(entitylivingbase.getArrowCountInEntity() + 1);
          }

          if (this.knockbackStrength > 0) {
            float f1 = MathHelper.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);

            if (f1 > 0.0F) {
              entitylivingbase.addVelocity(
                  this.motionX * this.knockbackStrength * 0.6000000238418579D / f1,
                  0.1D,
                  this.motionZ * this.knockbackStrength * 0.6000000238418579D / f1);
            }
          }

          if (this.shootingEntity instanceof EntityLivingBase) {
            EnchantmentHelper.applyThornEnchantments(entitylivingbase, this.shootingEntity);
            EnchantmentHelper.applyArthropodEnchantments(
                (EntityLivingBase) this.shootingEntity, entitylivingbase);
          }

          this.arrowHit(entitylivingbase);

          if (this.shootingEntity != null
              && entitylivingbase != this.shootingEntity
              && entitylivingbase instanceof EntityPlayer
              && this.shootingEntity instanceof EntityPlayerMP) {
            ((EntityPlayerMP) this.shootingEntity)
                .connection.sendPacket(new SPacketChangeGameState(6, 0.0F));
          }
        }

        this.playSound(
            SoundEvents.ENTITY_ARROW_HIT, 1.0F, 1.2F / (this.rand.nextFloat() * 0.2F + 0.9F));

        if (!(entity instanceof EntityEnderman)) {
          this.setDead();
        }
      } else {
        this.motionX *= -0.10000000149011612D;
        this.motionY *= -0.10000000149011612D;
        this.motionZ *= -0.10000000149011612D;
        this.rotationYaw += 180.0F;
        this.prevRotationYaw += 180.0F;
        this.ticksInAir = 0;

        if (!this.world.isRemote
            && this.motionX * this.motionX
                    + this.motionY * this.motionY
                    + this.motionZ * this.motionZ
                < 0.0010000000474974513D) {
          if (this.canBePickedUp == EntityArrow.PickupStatus.ALLOWED) {
            this.entityDropItem(this.getArrowStack(), 0.1F);
          }

          this.setDead();
        }
      }
    } else {
      BlockPos blockpos = p_184549_1_.getBlockPos();
      this.xTile = blockpos.getX();
      this.yTile = blockpos.getY();
      this.zTile = blockpos.getZ();
      IBlockState iblockstate = this.world.getBlockState(blockpos);
      this.inTile = iblockstate.getBlock();
      this.inData = this.inTile.getMetaFromState(iblockstate);
      this.motionX = ((float) (p_184549_1_.hitVec.x - this.posX));
      this.motionY = ((float) (p_184549_1_.hitVec.y - this.posY));
      this.motionZ = ((float) (p_184549_1_.hitVec.z - this.posZ));
      float f2 =
          MathHelper.sqrt(
              this.motionX * this.motionX
                  + this.motionY * this.motionY
                  + this.motionZ * this.motionZ);
      this.posX -= this.motionX / f2 * 0.05000000074505806D;
      this.posY -= this.motionY / f2 * 0.05000000074505806D;
      this.posZ -= this.motionZ / f2 * 0.05000000074505806D;
      this.playSound(
          SoundEvents.ENTITY_ARROW_HIT, 1.0F, 1.2F / (this.rand.nextFloat() * 0.2F + 0.9F));
      this.inGround = true;
      this.arrowShake = 7;
      this.setIsCritical(false);

      if (iblockstate.getMaterial() != Material.AIR) {
        this.inTile.onEntityCollidedWithBlock(this.world, blockpos, iblockstate, this);
      }
    }
  }

  protected void arrowHit(EntityLivingBase living) {
    this.setDead();
  }

  protected Entity func_184551_a(Vec3d p_184551_1_, Vec3d p_184551_2_) {
    Entity entity = null;
    List<Entity> list =
        this.world.getEntitiesInAABBexcluding(
            this,
            this.getEntityBoundingBox().expand(this.motionX, this.motionY, this.motionZ).grow(1.0D),
            ARROW_TARGETS);
    double d0 = 0.0D;

    for (int i = 0; i < list.size(); ++i) {
      Entity entity1 = list.get(i);

      if (entity1 != this.shootingEntity || this.ticksInAir >= 5) {
        AxisAlignedBB axisalignedbb = entity1.getEntityBoundingBox().grow(0.30000001192092896D);
        RayTraceResult raytraceresult = axisalignedbb.calculateIntercept(p_184551_1_, p_184551_2_);

        if (raytraceresult != null) {
          double d1 = p_184551_1_.squareDistanceTo(raytraceresult.hitVec);

          if (d1 < d0 || d0 == 0.0D) {
            entity = entity1;
            d0 = d1;
          }
        }
      }
    }

    return entity;
  }

  /** (abstract) Protected helper method to write subclass entity data to NBT. */
  @Override
  public void writeEntityToNBT(NBTTagCompound tagCompound) {
    tagCompound.setInteger("xTile", this.xTile);
    tagCompound.setInteger("yTile", this.yTile);
    tagCompound.setInteger("zTile", this.zTile);
    tagCompound.setShort("life", (short) this.ticksInGround);
    ResourceLocation resourcelocation = Block.REGISTRY.getNameForObject(this.inTile);
    tagCompound.setString("inTile", resourcelocation == null ? "" : resourcelocation.toString());
    tagCompound.setByte("inData", (byte) this.inData);
    tagCompound.setByte("shake", (byte) this.arrowShake);
    tagCompound.setByte("inGround", (byte) (this.inGround ? 1 : 0));
    tagCompound.setByte("pickup", (byte) this.canBePickedUp.ordinal());
    tagCompound.setDouble("damage", this.damage);

    this.customWriteEntityToNBT(tagCompound);
  }

  private void customWriteEntityToNBT(NBTTagCompound tagCompound) {
    if (this.item != null)
      tagCompound.setTag("item", this.item.getItem().writeToNBT(new NBTTagCompound()));

    if (this.hasTrail) {
      NBTTagCompound trailTag = new NBTTagCompound();
      trailTag.setFloat("frequency", this.trailFrequency);

      NBTTagList drops = new NBTTagList();
      for (int i = 0; i < this.dropProcessorTrail.getDrops().size(); i++) {
        drops.appendTag(new NBTTagString(this.dropProcessorTrail.getDrops().get(i).toString()));
      }
      trailTag.setTag("drops", drops);
      tagCompound.setTag("trail", trailTag);
    }

    if (this.hasImpact) {
      NBTTagList drops = new NBTTagList();
      for (int i = 0; i < this.dropProcessorImpact.getDrops().size(); i++) {
        drops.appendTag(new NBTTagString(this.dropProcessorImpact.getDrops().get(i).toString()));
      }
      tagCompound.setTag("impact", drops);
    }
  }

  /** (abstract) Protected helper method to read subclass entity data from NBT. */
  @Override
  public void readEntityFromNBT(NBTTagCompound tagCompund) {
    this.xTile = tagCompund.getInteger("xTile");
    this.yTile = tagCompund.getInteger("yTile");
    this.zTile = tagCompund.getInteger("zTile");
    this.ticksInGround = tagCompund.getShort("life");

    if (tagCompund.hasKey("inTile", 8)) {
      this.inTile = Block.getBlockFromName(tagCompund.getString("inTile"));
    } else {
      this.inTile = Block.getBlockById(tagCompund.getByte("inTile") & 255);
    }

    this.inData = tagCompund.getByte("inData") & 255;
    this.arrowShake = tagCompund.getByte("shake") & 255;
    this.inGround = tagCompund.getByte("inGround") == 1;

    if (tagCompund.hasKey("damage", 99)) {
      this.damage = tagCompund.getDouble("damage");
    }

    if (tagCompund.hasKey("pickup", 99)) {
      this.canBePickedUp = EntityArrow.PickupStatus.getByOrdinal(tagCompund.getByte("pickup"));
    } else if (tagCompund.hasKey("player", 99)) {
      this.canBePickedUp =
          tagCompund.getBoolean("player")
              ? EntityArrow.PickupStatus.ALLOWED
              : EntityArrow.PickupStatus.DISALLOWED;
    }

    this.customReadEntityFromNBT(tagCompund);
  }

  private void customReadEntityFromNBT(NBTTagCompound tagCompound) {
    if (tagCompound.hasKey("item")) {
      tagCompound.getCompoundTag("item").setTag("Count", new NBTTagInt(1));
      this.item =
          new EntityItem(
              this.getEntityWorld(),
              this.posX,
              this.posY,
              this.posZ,
              new ItemStack(tagCompound.getCompoundTag("item")));
    } else
      this.item =
          new EntityItem(
              this.getEntityWorld(), this.posX, this.posY, this.posZ, new ItemStack(Items.STICK));

    this.dataManager.set(PROJECTILE_ITEM, this.item.getItem());

    if (tagCompound.hasKey("trail")) {
      NBTTagCompound trailTag = tagCompound.getCompoundTag("trail");
      if (trailTag.hasKey("frequency")) this.trailFrequency = trailTag.getFloat("frequency");
      if (trailTag.hasKey("drops")) {
        NBTTagList drops = trailTag.getTagList("drops", new NBTTagString().getId());
        for (int i = 0; i < drops.tagCount(); i++) {
          DropContainer drop = new DropContainer();
          drop.readFromString(drops.getStringTagAt(i));
          this.dropProcessorTrail.registerDrop(drop);
        }
      }
      this.hasTrail = true;
    }

    if (tagCompound.hasKey("impact")) {
      NBTTagList drops = tagCompound.getTagList("impact", new NBTTagString().getId());
      for (int i = 0; i < drops.tagCount(); i++) {
        DropContainer drop = new DropContainer();
        drop.readFromString(drops.getStringTagAt(i));
        this.dropProcessorImpact.registerDrop(drop);
      }
      this.hasImpact = true;
    }
  }

  /** Called by a player entity when they collide with an entity */
  @Override
  public void onCollideWithPlayer(EntityPlayer entityIn) {
    if (!this.world.isRemote && this.inGround && this.arrowShake <= 0) {
      boolean flag =
          this.canBePickedUp == EntityArrow.PickupStatus.ALLOWED
              || this.canBePickedUp == EntityArrow.PickupStatus.CREATIVE_ONLY
                  && entityIn.capabilities.isCreativeMode;

      if (this.canBePickedUp == EntityArrow.PickupStatus.ALLOWED
          && !entityIn.inventory.addItemStackToInventory(this.getArrowStack())) {
        flag = false;
      }

      if (flag) {
        this.playSound(
            SoundEvents.ENTITY_ITEM_PICKUP,
            0.2F,
            ((this.rand.nextFloat() - this.rand.nextFloat()) * 0.7F + 1.0F) * 2.0F);
        entityIn.onItemPickup(this, 1);
        this.setDead();
      }
    }
  }

  protected ItemStack getArrowStack() {
    return new ItemStack(Items.ARROW);
  }

  /**
   * returns if this entity triggers Block.onEntityWalking on the blocks they walk on. used for
   * spiders and wolves to prevent them from trampling crops
   */
  @Override
  protected boolean canTriggerWalking() {
    return false;
  }

  public void setDamage(double damageIn) {
    this.damage = damageIn;
  }

  public double getDamage() {
    return this.damage;
  }

  /** Sets the amount of knockback the arrow applies when it hits a mob. */
  public void setKnockbackStrength(int knockbackStrengthIn) {
    this.knockbackStrength = knockbackStrengthIn;
  }

  /** If returns false, the item will not inflict any damage against entities. */
  @Override
  public boolean canBeAttackedWithItem() {
    return false;
  }

  @Override
  public float getEyeHeight() {
    return 0.0F;
  }

  /** Whether the arrow has a stream of critical hit particles flying behind it. */
  public void setIsCritical(boolean critical) {
    byte b0 = this.dataManager.get(CRITICAL).byteValue();

    if (critical) {
      this.dataManager.set(CRITICAL, Byte.valueOf((byte) (b0 | 1)));
    } else {
      this.dataManager.set(CRITICAL, Byte.valueOf((byte) (b0 & -2)));
    }
  }

  /** Whether the arrow has a stream of critical hit particles flying behind it. */
  public boolean getIsCritical() {
    byte b0 = this.dataManager.get(CRITICAL).byteValue();
    return (b0 & 1) != 0;
  }

  public EntityItem getItem() {
    return this.item;
  }

  public static enum PickupStatus {
    DISALLOWED,
    ALLOWED,
    CREATIVE_ONLY;

    public static EntityLuckyProjectile.PickupStatus func_188795_a(int p_188795_0_) {
      if (p_188795_0_ < 0 || p_188795_0_ > values().length) {
        p_188795_0_ = 0;
      }

      return values()[p_188795_0_];
    }
  }
}
