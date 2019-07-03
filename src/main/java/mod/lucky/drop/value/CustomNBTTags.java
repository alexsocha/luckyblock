package mod.lucky.drop.value;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.Random;

import mod.lucky.Lucky;
import mod.lucky.drop.func.DropProcessData;
import mod.lucky.util.LuckyUtils;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityTippedArrow;
import net.minecraft.init.Enchantments;
import net.minecraft.init.MobEffects;
import net.minecraft.nbt.INBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.loot.*;
import net.minecraft.world.storage.loot.conditions.LootCondition;
import net.minecraft.world.storage.loot.conditions.LootConditionManager;
import net.minecraft.world.storage.loot.functions.LootFunction;
import net.minecraft.world.storage.loot.functions.LootFunctionManager;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.registries.ForgeRegistries;

public class CustomNBTTags {
    public static final NBTTagCompound protection = getEnchantment(Enchantments.PROTECTION, 4);
    public static final NBTTagCompound fireProtection = getEnchantment(Enchantments.FIRE_PROTECTION, 4);
    public static final NBTTagCompound featherFalling = getEnchantment(Enchantments.FEATHER_FALLING, 4);
    public static final NBTTagCompound blastProtection = getEnchantment(Enchantments.BLAST_PROTECTION, 4);
    public static final NBTTagCompound projectileProtection = getEnchantment(Enchantments.PROJECTILE_PROTECTION, 4);
    public static final NBTTagCompound respiration = getEnchantment(Enchantments.RESPIRATION, 3);
    public static final NBTTagCompound aquaAffinity = getEnchantment(Enchantments.AQUA_AFFINITY, 1);
    public static final NBTTagCompound thorns = getEnchantment(Enchantments.THORNS, 3);

    public static final NBTTagCompound sharpness = getEnchantment(Enchantments.SHARPNESS, 5);
    public static final NBTTagCompound smite = getEnchantment(Enchantments.SMITE, 5);
    public static final NBTTagCompound baneOfArthroponds = getEnchantment(Enchantments.BANE_OF_ARTHROPODS, 5);
    public static final NBTTagCompound knockBack = getEnchantment(Enchantments.KNOCKBACK, 2);
    public static final NBTTagCompound fireAspect = getEnchantment(Enchantments.FIRE_ASPECT, 2);
    public static final NBTTagCompound looting = getEnchantment(Enchantments.LOOTING, 3);

    public static final NBTTagCompound efficiency = getEnchantment(Enchantments.EFFICIENCY, 5);
    public static final NBTTagCompound silkTouch = getEnchantment(Enchantments.SILK_TOUCH, 1);
    public static final NBTTagCompound unbreaking = getEnchantment(Enchantments.UNBREAKING, 3);
    public static final NBTTagCompound fortune = getEnchantment(Enchantments.FORTUNE, 3);

    public static final NBTTagCompound power = getEnchantment(Enchantments.POWER, 5);
    public static final NBTTagCompound punch = getEnchantment(Enchantments.PUNCH, 2);
    public static final NBTTagCompound flame = getEnchantment(Enchantments.FLAME, 1);
    public static final NBTTagCompound infinity = getEnchantment(Enchantments.INFINITY, 1);
public static final NBTTagCompound luckOfTheSea = getEnchantment(Enchantments.LUCK_OF_THE_SEA, 3);
    public static final NBTTagCompound lure = getEnchantment(Enchantments.LURE, 3);

    public static final NBTTagCompound speed = getPotionEffect(MobEffects.SPEED, 3, 9600);
    public static final NBTTagCompound slowness = getPotionEffect(MobEffects.SLOWNESS, 3, 9600);
    public static final NBTTagCompound haste = getPotionEffect(MobEffects.HASTE, 3, 9600);
    public static final NBTTagCompound miningFatigue = getPotionEffect(MobEffects.MINING_FATIGUE, 3, 9600);
    public static final NBTTagCompound strength = getPotionEffect(MobEffects.STRENGTH, 3, 9600);
    public static final NBTTagCompound instantHealth = getPotionEffect(MobEffects.INSTANT_HEALTH, 3, 0);
    public static final NBTTagCompound instantDamage = getPotionEffect(MobEffects.INSTANT_DAMAGE, 3, 0);
    public static final NBTTagCompound jumpBoost = getPotionEffect(MobEffects.JUMP_BOOST, 3, 9600);
    public static final NBTTagCompound nausea = getPotionEffect(MobEffects.NAUSEA, 0, 9600);
    public static final NBTTagCompound regeneration = getPotionEffect(MobEffects.REGENERATION, 3, 9600);
    public static final NBTTagCompound resistance = getPotionEffect(MobEffects.RESISTANCE, 3, 9600);
    public static final NBTTagCompound fireResistance = getPotionEffect(MobEffects.FIRE_RESISTANCE, 0, 9600);
    public static final NBTTagCompound waterBreathing = getPotionEffect(MobEffects.WATER_BREATHING, 0, 9600);
    public static final NBTTagCompound invisibility = getPotionEffect(MobEffects.INVISIBILITY, 0, 9600);
    public static final NBTTagCompound blindness = getPotionEffect(MobEffects.BLINDNESS, 0, 9600);
    public static final NBTTagCompound nightVision = getPotionEffect(MobEffects.NIGHT_VISION, 0, 9600);
    public static final NBTTagCompound hunger = getPotionEffect(MobEffects.HUNGER, 3, 9600);
    public static final NBTTagCompound weakness = getPotionEffect(MobEffects.WEAKNESS, 3, 9600);
    public static final NBTTagCompound poison = getPotionEffect(MobEffects.POISON, 3, 9600);
    public static final NBTTagCompound wither = getPotionEffect(MobEffects.WITHER, 3, 9600);
    public static final NBTTagCompound healthBoost = getPotionEffect(MobEffects.HEALTH_BOOST, 3, 9600);
    public static final NBTTagCompound absorbtion = getPotionEffect(MobEffects.ABSORPTION, 3, 9600);
    public static final NBTTagCompound saturation = getPotionEffect(MobEffects.SATURATION, 3, 9600);

    private static final Gson GSON_INSTANCE =
        (new GsonBuilder())
            .registerTypeAdapter(RandomValueRange.class, new RandomValueRange.Serializer())
            .registerTypeAdapter(LootPool.class, new LootPool.Serializer())
            .registerTypeAdapter(LootTable.class, new LootTable.Serializer())
            .registerTypeHierarchyAdapter(LootEntry.class, new LootEntry.Serializer())
            .registerTypeHierarchyAdapter(LootFunction.class, new LootFunctionManager.Serializer())
            .registerTypeHierarchyAdapter(LootCondition.class, new LootConditionManager.Serializer())
            .registerTypeHierarchyAdapter(
                LootContext.EntityTarget.class, new LootContext.EntityTarget.Serializer())
            .create();
    public static Random random = new Random();

    public static NBTTagCompound getEnchantment(Enchantment enchantment, int maxLevel) {
        NBTTagCompound nbttag = new NBTTagCompound();

        nbttag.setString("id", ForgeRegistries.ENCHANTMENTS.getKey(enchantment).toString());
        nbttag.setShort("lvl", (short) maxLevel);

        return nbttag;
    }

    public static NBTTagCompound getPotionEffect(Potion potion, int amplifier, int duration) {
        NBTTagCompound nbttag = new NBTTagCompound();
        PotionEffect effect = new PotionEffect(potion, duration, amplifier);
        return effect.write(nbttag);
    }

    public static ArrayList<NBTTagCompound> getRandomList(
        int minAmount, int maxAmount, NBTTagCompound... elements) {
        int amountToRemove =
            elements.length - (random.nextInt((maxAmount + 1) - minAmount) + minAmount);

        ArrayList<NBTTagCompound> chosenElementList = new ArrayList<NBTTagCompound>(elements.length);
        for (NBTTagCompound element : elements) {
            chosenElementList.add(element.copy());
        }

        for (int a = 0; a < amountToRemove; a++) {
            int index = random.nextInt(chosenElementList.size());
            chosenElementList.remove(index);
        }

        return chosenElementList;
    }

    public static NBTTagList getRandomEnchantmentList(
        int minAmount, int maxAmount, NBTTagCompound... enchantments) {
        ArrayList<NBTTagCompound> chosenEnchantments =
            getRandomList(minAmount, maxAmount, enchantments);

        NBTTagList nbttaglist = new NBTTagList();
        for (NBTTagCompound enchantment : chosenEnchantments) {
            enchantment.setShort("lvl", (short) (random.nextInt(enchantment.getShort("lvl")) + 1));
            nbttaglist.add(enchantment);
        }

        return nbttaglist;
    }

    public static NBTTagList getRandomPotionEffectList(
        int minAmount, int maxAmount, NBTTagCompound... potionEffects) {
        ArrayList<NBTTagCompound> chosenPotionEffects =
            getRandomList(minAmount, maxAmount, potionEffects);

        NBTTagList nbttaglist = new NBTTagList();
        for (NBTTagCompound potionEffect : chosenPotionEffects) {
            potionEffect.setByte(
                "Amplifier", (byte) (random.nextInt(potionEffect.getByte("Amplifier") + 1)));
            int minDuration = (int) (potionEffect.getInt("Duration") / 3F);
            potionEffect.setInt(
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

    public static INBTBase getNBTTagFromString(String name, DropProcessData processData) {
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
            return getRandomPotionEffectList(7, 10,
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
            return getRandomPotionEffectList(5, 7,
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

                NBTTagList motionList = new NBTTagList();
                motionList.add(new NBTTagDouble(launchMotionX));
                motionList.add(new NBTTagDouble(launchMotionY));
                motionList.add(new NBTTagDouble(launchMotionZ));
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

                NBTTagList motionList = new NBTTagList();
                motionList.add(new NBTTagDouble(launchMotionX));
                motionList.add(new NBTTagDouble(launchMotionY));
                motionList.add(new NBTTagDouble(launchMotionZ));
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
                EntityArrow entityArrow;
                if (shooter instanceof EntityLivingBase)
                    entityArrow = new EntityTippedArrow(processData.getWorld(), (EntityLivingBase) shooter);
                else
                    entityArrow =
                        new EntityTippedArrow(
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

                NBTTagList motionList = new NBTTagList();
                motionList.add(new NBTTagDouble(entityArrow.motionX));
                motionList.add(new NBTTagDouble(entityArrow.motionY));
                motionList.add(new NBTTagDouble(entityArrow.motionZ));
                return motionList;
            } catch (Exception e) {
            }
        }

        name =
            name.replace(
                "#chestVillageBlacksmith",
                "#chestLootTable(" + LootTableList.CHESTS_VILLAGE_BLACKSMITH.getPath() + ")");
        name =
            name.replace(
                "#chestBonusChest",
                "#chestLootTable(" + LootTableList.CHESTS_SPAWN_BONUS_CHEST.getPath() + ")");
        name =
            name.replace(
                "#chestDungeonChest",
                "#chestLootTable(" + LootTableList.CHESTS_SIMPLE_DUNGEON.getPath() + ")");

        if (name.startsWith("#customChestLootTable(")) {
            try {
                String contents =
                    ValueParser.getString(name.substring(name.indexOf('(') + 1, name.lastIndexOf(')')));
                LootTable.Serializer serializer = new LootTable.Serializer();
                LootTable lootTable =
                    ForgeHooks.loadLootTable(
                        GSON_INSTANCE,
                        LootTableList.CHESTS_SPAWN_BONUS_CHEST,
                        contents,
                        true,
                        processData.getWorld().getServer().getLootTableManager());

                TileEntityChest tileEntityChest = new TileEntityChest();
                LootContext.Builder contextBuilder =
                    new LootContext.Builder((WorldServer) processData.getWorld());
                if (processData.getPlayer() != null && processData.getPlayer() instanceof EntityPlayer)
                    contextBuilder.withLuck(((EntityPlayer) processData.getPlayer()).getLuck());

                lootTable.fillInventory(tileEntityChest, random, contextBuilder.build());

                NBTTagCompound tagCompound = new NBTTagCompound();
                tileEntityChest.write(tagCompound);
                return tagCompound.getList("Items", Constants.NBT.TAG_COMPOUND);

            } catch (Exception e) {
                Lucky.error(e, "Error creating chest from .json loot table");
            }
        }

        if (name.startsWith("#chestLootTable(")) {
            TileEntityChest tileEntityChest = new TileEntityChest();
            String lootId =
                ValueParser.getString(name.substring(name.indexOf('(') + 1, name.lastIndexOf(')')));
            tileEntityChest.setWorld(processData.getWorld());
            tileEntityChest.setLootTable(new ResourceLocation("minecraft", lootId), random.nextLong());
            tileEntityChest.getStackInSlot(0); // For fillWithLoot()

            NBTTagCompound tagCompound = new NBTTagCompound();
            tileEntityChest.write(tagCompound);
            return tagCompound.getList("Items", Constants.NBT.TAG_COMPOUND);
        }

        return null;
    }
}
