package mod.lucky.drop.value;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Random;

import com.google.gson.JsonParser;
import mod.lucky.Lucky;
import mod.lucky.drop.func.DropProcessData;
import mod.lucky.util.LuckyUtils;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.nbt.*;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.tileentity.ChestTileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.ServerWorld;
import net.minecraft.world.storage.loot.*;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.registries.ForgeRegistries;

public class CustomNBTTags {
    public static final CompoundNBT protection = getEnchantment(Enchantments.PROTECTION, 4);
    public static final CompoundNBT fireProtection = getEnchantment(Enchantments.FIRE_PROTECTION, 4);
    public static final CompoundNBT featherFalling = getEnchantment(Enchantments.FEATHER_FALLING, 4);
    public static final CompoundNBT blastProtection = getEnchantment(Enchantments.BLAST_PROTECTION, 4);
    public static final CompoundNBT projectileProtection = getEnchantment(Enchantments.PROJECTILE_PROTECTION, 4);
    public static final CompoundNBT respiration = getEnchantment(Enchantments.RESPIRATION, 3);
    public static final CompoundNBT aquaAffinity = getEnchantment(Enchantments.AQUA_AFFINITY, 1);
    public static final CompoundNBT thorns = getEnchantment(Enchantments.THORNS, 3);

    public static final CompoundNBT sharpness = getEnchantment(Enchantments.SHARPNESS, 5);
    public static final CompoundNBT smite = getEnchantment(Enchantments.SMITE, 5);
    public static final CompoundNBT baneOfArthroponds = getEnchantment(Enchantments.BANE_OF_ARTHROPODS, 5);
    public static final CompoundNBT knockBack = getEnchantment(Enchantments.KNOCKBACK, 2);
    public static final CompoundNBT fireAspect = getEnchantment(Enchantments.FIRE_ASPECT, 2);
    public static final CompoundNBT looting = getEnchantment(Enchantments.LOOTING, 3);

    public static final CompoundNBT efficiency = getEnchantment(Enchantments.EFFICIENCY, 5);
    public static final CompoundNBT silkTouch = getEnchantment(Enchantments.SILK_TOUCH, 1);
    public static final CompoundNBT unbreaking = getEnchantment(Enchantments.UNBREAKING, 3);
    public static final CompoundNBT fortune = getEnchantment(Enchantments.FORTUNE, 3);

    public static final CompoundNBT power = getEnchantment(Enchantments.POWER, 5);
    public static final CompoundNBT punch = getEnchantment(Enchantments.PUNCH, 2);
    public static final CompoundNBT flame = getEnchantment(Enchantments.FLAME, 1);
    public static final CompoundNBT infinity = getEnchantment(Enchantments.INFINITY, 1);
public static final CompoundNBT luckOfTheSea = getEnchantment(Enchantments.LUCK_OF_THE_SEA, 3);
    public static final CompoundNBT lure = getEnchantment(Enchantments.LURE, 3);

    public static final CompoundNBT speed = getEffectInstance(Effects.SPEED, 3, 9600);
    public static final CompoundNBT slowness = getEffectInstance(Effects.SLOWNESS, 3, 9600);
    public static final CompoundNBT haste = getEffectInstance(Effects.HASTE, 3, 9600);
    public static final CompoundNBT miningFatigue = getEffectInstance(Effects.MINING_FATIGUE, 3, 9600);
    public static final CompoundNBT strength = getEffectInstance(Effects.STRENGTH, 3, 9600);
    public static final CompoundNBT instantHealth = getEffectInstance(Effects.INSTANT_HEALTH, 3, 0);
    public static final CompoundNBT instantDamage = getEffectInstance(Effects.INSTANT_DAMAGE, 3, 0);
    public static final CompoundNBT jumpBoost = getEffectInstance(Effects.JUMP_BOOST, 3, 9600);
    public static final CompoundNBT nausea = getEffectInstance(Effects.NAUSEA, 0, 9600);
    public static final CompoundNBT regeneration = getEffectInstance(Effects.REGENERATION, 3, 9600);
    public static final CompoundNBT resistance = getEffectInstance(Effects.RESISTANCE, 3, 9600);
    public static final CompoundNBT fireResistance = getEffectInstance(Effects.FIRE_RESISTANCE, 0, 9600);
    public static final CompoundNBT waterBreathing = getEffectInstance(Effects.WATER_BREATHING, 0, 9600);
    public static final CompoundNBT invisibility = getEffectInstance(Effects.INVISIBILITY, 0, 9600);
    public static final CompoundNBT blindness = getEffectInstance(Effects.BLINDNESS, 0, 9600);
    public static final CompoundNBT nightVision = getEffectInstance(Effects.NIGHT_VISION, 0, 9600);
    public static final CompoundNBT hunger = getEffectInstance(Effects.HUNGER, 3, 9600);
    public static final CompoundNBT weakness = getEffectInstance(Effects.WEAKNESS, 3, 9600);
    public static final CompoundNBT poison = getEffectInstance(Effects.POISON, 3, 9600);
    public static final CompoundNBT wither = getEffectInstance(Effects.WITHER, 3, 9600);
    public static final CompoundNBT healthBoost = getEffectInstance(Effects.HEALTH_BOOST, 3, 9600);
    public static final CompoundNBT absorbtion = getEffectInstance(Effects.ABSORPTION, 3, 9600);
    public static final CompoundNBT saturation = getEffectInstance(Effects.SATURATION, 3, 9600);

    // make sure that this exists
    private static final Class lootManagerCls = LootTableManager.class;
    private static final Gson GSON_INSTANCE = ObfuscationReflectionHelper.getPrivateValue(
        LootTableManager.class, new LootTableManager(), "GSON_INSTANCE");
    public static Random random = new Random();

    public static CompoundNBT getEnchantment(Enchantment enchantment, int maxLevel) {
        CompoundNBT nbttag = new CompoundNBT();

        nbttag.putString("id", ForgeRegistries.ENCHANTMENTS.getKey(enchantment).toString());
        nbttag.putShort("lvl", (short) maxLevel);

        return nbttag;
    }

    public static CompoundNBT getEffectInstance(Effect potion, int amplifier, int duration) {
        CompoundNBT nbttag = new CompoundNBT();
        EffectInstance effect = new EffectInstance(potion, duration, amplifier);
        return effect.write(nbttag);
    }

    public static ArrayList<CompoundNBT> getRandomList(
        int minAmount, int maxAmount, CompoundNBT... elements) {
        int amountToRemove =
            elements.length - (random.nextInt((maxAmount + 1) - minAmount) + minAmount);

        ArrayList<CompoundNBT> chosenElementList = new ArrayList<CompoundNBT>(elements.length);
        for (CompoundNBT element : elements) {
            chosenElementList.add(element.copy());
        }

        for (int a = 0; a < amountToRemove; a++) {
            int index = random.nextInt(chosenElementList.size());
            chosenElementList.remove(index);
        }

        return chosenElementList;
    }

    public static ListNBT getRandomEnchantmentList(
        int minAmount, int maxAmount, CompoundNBT... enchantments) {
        ArrayList<CompoundNBT> chosenEnchantments =
            getRandomList(minAmount, maxAmount, enchantments);

        ListNBT nbttaglist = new ListNBT();
        for (CompoundNBT enchantment : chosenEnchantments) {
            enchantment.putShort("lvl", (short) (random.nextInt(enchantment.getShort("lvl")) + 1));
            nbttaglist.add(enchantment);
        }

        return nbttaglist;
    }

    public static ListNBT getRandomEffectInstanceList(
        int minAmount, int maxAmount, CompoundNBT... potionEffects) {
        ArrayList<CompoundNBT> chosenEffectInstances =
            getRandomList(minAmount, maxAmount, potionEffects);

        ListNBT nbttaglist = new ListNBT();
        for (CompoundNBT potionEffect : chosenEffectInstances) {
            potionEffect.putByte(
                "Amplifier", (byte) (random.nextInt(potionEffect.getByte("Amplifier") + 1)));
            int minDuration = (int) (potionEffect.getInt("Duration") / 3F);
            potionEffect.putInt(
                "Duration",
                random.nextInt((potionEffect.getInt("Duration") + 1) - minDuration) + minDuration);
            nbttaglist.add(potionEffect);
        }

        return nbttaglist;
    }

    public static String[] nbtHashVariables = {
        "#luckySwordEnchantments",
        "#luckyAxeEnchantments",
        "#luckyToolEnchantments",
        "#luckyHelmetEnchantments",
        "#luckyLeggingsEnchantments",
        "#luckyBootsEnchantments",
        "#luckyBowEnchantments",
        "#luckyFishingRodEnchantments",
        "#randEnchantment",
        "#luckyPotionEffects",
        "#unluckyPotionEffects",
        "#randFireworksRocket",
        "#randLaunchMotion",
        "#motionFromDirection",
        "#bowMotion",
        "#chestLootTable",
        "#customChestLootTable"
    };

    public static INBT getNBTTagFromString(String name, DropProcessData processData) {
        if (name.equals("#luckySwordEnchantments"))
            return getRandomEnchantmentList(4, 6,
                sharpness, smite, baneOfArthroponds, knockBack, fireAspect, looting, unbreaking);
        if (name.equals("#luckyAxeEnchantments"))
            return getRandomEnchantmentList(4, 6,
                sharpness, smite, baneOfArthroponds, efficiency, unbreaking, fortune);
        if (name.equals("#luckyToolEnchantments"))
            return getRandomEnchantmentList(2, 3, efficiency, unbreaking, fortune);

        if (name.equals("#luckyHelmetEnchantments"))
            return getRandomEnchantmentList(4, 6,
                protection,
                fireProtection,
                blastProtection,
                projectileProtection,
                respiration,
                aquaAffinity,
                unbreaking);
        if (name.equals("#luckyChestplateEnchantments"))
            return getRandomEnchantmentList(4, 6,
                protection,
                fireProtection,
                blastProtection,
                projectileProtection,
                thorns,
                unbreaking);
        if (name.equals("#luckyLeggingsEnchantments"))
            return getRandomEnchantmentList(4, 6,
                protection,
                fireProtection,
                blastProtection,
                projectileProtection,
                thorns,
                unbreaking);
        if (name.equals("#luckyBootsEnchantments"))
            return getRandomEnchantmentList(4, 6,
                protection,
                fireProtection,
                featherFalling,
                blastProtection,
                projectileProtection,
                thorns,
                unbreaking);

        if (name.equals("#luckyBowEnchantments"))
            return getRandomEnchantmentList(3, 5, unbreaking, power, punch, flame, infinity);
        if (name.equals("#luckyFishingRodEnchantments"))
            return getRandomEnchantmentList(2, 3, unbreaking, luckOfTheSea, lure);

        if (name.equals("#randEnchantment"))
            return getRandomEnchantmentList(1, 1,
                protection,
                fireProtection,
                featherFalling,
                blastProtection,
                projectileProtection,
                thorns,
                sharpness,
                smite,
                baneOfArthroponds,
                knockBack,
                fireAspect,
                looting,
                efficiency,
                silkTouch,
                unbreaking,
                fortune,
                power,
                punch,
                flame,
                infinity,
                luckOfTheSea,
                lure);

        if (name.equals("#luckyPotionEffects"))
            return getRandomEffectInstanceList(7, 10,
                speed,
                haste,
                strength,
                instantHealth,
                jumpBoost,
                regeneration,
                resistance,
                fireResistance,
                waterBreathing,
                invisibility,
                nightVision,
                healthBoost,
                absorbtion,
                saturation);

        if (name.equals("#unluckyPotionEffects"))
            return getRandomEffectInstanceList(5, 7,
                slowness,
                miningFatigue,
                instantDamage,
                nausea,
                blindness,
                hunger,
                weakness,
                poison,
                wither);

        if (name.equals("#randFireworksRocket")) return LuckyUtils.getRandomFireworksRocket();

        if (name.startsWith("#randLaunchMotion")) {
            try {
                float launchPower = 0.9F;
                int launchAngle = 15;
                if (name.startsWith("#randLaunchMotion(")) {
                    String contents = name.substring(name.indexOf('(') + 1, name.lastIndexOf(')'));
                    String[] splitValue = DropStringUtils.splitBracketString(contents, ',');
                    splitValue[0] = DropStringUtils.removeNumSuffix(splitValue[0]);
                    launchPower = ValueParser.getFloat(splitValue[0], processData);
                    launchAngle = ValueParser.getInteger(splitValue[1], processData);
                }

                float launchYaw = MathHelper.wrapDegrees(random.nextFloat() * 360.0F);
                float launchPitch = -90.0F + (random.nextInt(launchAngle * 2) - launchAngle);
                float launchMotionX =
                    -MathHelper.sin(launchYaw / 180.0F * (float) Math.PI)
                        * MathHelper.cos(launchPitch / 180.0F * (float) Math.PI)
                        * launchPower;
                float launchMotionZ =
                    MathHelper.cos(launchYaw / 180.0F * (float) Math.PI)
                        * MathHelper.cos(launchPitch / 180.0F * (float) Math.PI)
                        * launchPower;
                float launchMotionY = -MathHelper.sin(launchPitch / 180.0F * (float) Math.PI) * launchPower;

                ListNBT motionList = new ListNBT();
                motionList.add(new DoubleNBT(launchMotionX));
                motionList.add(new DoubleNBT(launchMotionY));
                motionList.add(new DoubleNBT(launchMotionZ));
                return motionList;
            } catch (Exception e) {
            }
        }

        if (name.startsWith("#motionFromDirection(")) {
            try {
                String contents = name.substring(name.indexOf('(') + 1, name.lastIndexOf(')'));
                String[] splitValue = DropStringUtils.splitBracketString(contents, ',');
                splitValue[2] = DropStringUtils.removeNumSuffix(splitValue[2]);

                int launchYaw = ValueParser.getInteger(splitValue[0], processData);
                int launchPitch = ValueParser.getInteger(splitValue[1], processData);
                float launchPower = ValueParser.getFloat(splitValue[2], processData);
                float launchMotionX =
                    -MathHelper.sin(launchYaw / 180.0F * (float) Math.PI)
                        * MathHelper.cos(launchPitch / 180.0F * (float) Math.PI)
                        * launchPower;
                float launchMotionZ =
                    MathHelper.cos(launchYaw / 180.0F * (float) Math.PI)
                        * MathHelper.cos(launchPitch / 180.0F * (float) Math.PI)
                        * launchPower;
                float launchMotionY = -MathHelper.sin(launchPitch / 180.0F * (float) Math.PI) * launchPower;

                ListNBT motionList = new ListNBT();
                motionList.add(new DoubleNBT(launchMotionX));
                motionList.add(new DoubleNBT(launchMotionY));
                motionList.add(new DoubleNBT(launchMotionZ));
                return motionList;
            } catch (Exception e) {
            }
        }

        if (name.startsWith("#bowMotion")) {
            try {
                float bowPowerMod = 1.0F;
                float randAngle = 0;
                if (name.startsWith("#bowMotion(")) {
                    String contents = name.substring(name.indexOf('(') + 1, name.lastIndexOf(')'));
                    String[] splitValue = DropStringUtils.splitBracketString(contents, ',');
                    splitValue[0] = DropStringUtils.removeNumSuffix(splitValue[0]);
                    if (splitValue.length > 1) splitValue[1] = DropStringUtils.removeNumSuffix(splitValue[1]);
                    bowPowerMod = ValueParser.getFloat(splitValue[0], processData);
                    if (splitValue.length > 1) randAngle = ValueParser.getFloat(splitValue[1], processData);
                }

                Entity shooter = processData.getPlayer();
                ArrowEntity entityArrow;
                if (shooter instanceof LivingEntity)
                    entityArrow = new ArrowEntity(processData.getWorld(), (LivingEntity) shooter);
                else
                    entityArrow =
                        new ArrowEntity(
                            processData.getWorld(), shooter.posX, shooter.posY, shooter.posZ);
                entityArrow.shoot(
                    shooter,
                    shooter.rotationPitch
                        + HashVariables.randomFloatClamp(
                        processData.getWorld().rand, -randAngle, randAngle),
                    shooter.rotationYaw
                        + HashVariables.randomFloatClamp(
                        processData.getWorld().rand, -randAngle, randAngle),
                    0.0F,
                    processData.getBowPower(),
                    1.0F);

                ListNBT motionList = new ListNBT();
                Vec3d arrowMotion = entityArrow.getMotion();
                motionList.add(new DoubleNBT(arrowMotion.x));
                motionList.add(new DoubleNBT(arrowMotion.y));
                motionList.add(new DoubleNBT(arrowMotion.z));
                return motionList;
            } catch (Exception e) {}
        }

        name =
            name.replace(
                "#chestVillageArmorer",
                "#chestLootTable(" + LootTables.CHESTS_VILLAGE_VILLAGE_ARMORER.getPath() + ")");
        name =
            name.replace(
                "#chestBonusChest",
                "#chestLootTable(" + LootTables.CHESTS_SPAWN_BONUS_CHEST.getPath() + ")");
        name =
            name.replace(
                "#chestDungeonChest",
                "#chestLootTable(" + LootTables.CHESTS_SIMPLE_DUNGEON.getPath() + ")");

        if (name.startsWith("#customChestLootTable(")) {
            try {
                String contents =
                    ValueParser.getString(name.substring(name.indexOf('(') + 1, name.lastIndexOf(')')));
                LootTable.Serializer serializer = new LootTable.Serializer();
                LootTable lootTable =
                    ForgeHooks.loadLootTable(
                        GSON_INSTANCE,
                        LootTables.CHESTS_SPAWN_BONUS_CHEST,
                        (new JsonParser()).parse(contents).getAsJsonObject(),
                        true,
                        processData.getWorld().getServer().getLootTableManager());

                ChestTileEntity tileEntityChest = new ChestTileEntity();
                LootContext.Builder contextBuilder =
                    new LootContext.Builder((ServerWorld) processData.getWorld());
                if (processData.getPlayer() != null && processData.getPlayer() instanceof PlayerEntity)
                    contextBuilder.withLuck(((PlayerEntity) processData.getPlayer()).getLuck());

                lootTable.fillInventory(tileEntityChest,
                    contextBuilder.build(new LootParameterSet.Builder().build()));

                CompoundNBT tagCompound = new CompoundNBT();
                tileEntityChest.write(tagCompound);
                return tagCompound.getList("Items", Constants.NBT.TAG_COMPOUND);

            } catch (Exception e) {
                Lucky.error(e, "Error creating chest from .json loot table");
            }
        }

        if (name.startsWith("#chestLootTable(")) {
            ChestTileEntity tileEntityChest = new ChestTileEntity();
            String lootId =
                ValueParser.getString(name.substring(name.indexOf('(') + 1, name.lastIndexOf(')')));
            tileEntityChest.setWorld(processData.getWorld());
            tileEntityChest.setLootTable(new ResourceLocation("minecraft", lootId), random.nextLong());
            tileEntityChest.getStackInSlot(0); // For fillWithLoot()

            CompoundNBT tagCompound = new CompoundNBT();
            tileEntityChest.write(tagCompound);
            return tagCompound.getList("Items", Constants.NBT.TAG_COMPOUND);
        }

        return null;
    }
}
