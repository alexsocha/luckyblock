package mod.lucky.drop.value;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.Random;

import mod.lucky.drop.func.DropProcessData;
import mod.lucky.util.LuckyFunction;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityTippedArrow;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagList;
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

public class CustomNBTTags {
    public static final NBTTagCompound protection = getEnchantment(0, 4);
    public static final NBTTagCompound fireProtection = getEnchantment(1, 4);
    public static final NBTTagCompound featherFalling = getEnchantment(2, 4);
    public static final NBTTagCompound blastProtection = getEnchantment(3, 4);
    public static final NBTTagCompound projectileProtection = getEnchantment(4, 4);
    public static final NBTTagCompound respiration = getEnchantment(5, 3);
    public static final NBTTagCompound aquaAffinity = getEnchantment(6, 1);
    public static final NBTTagCompound thorns = getEnchantment(7, 3);

    public static final NBTTagCompound sharpness = getEnchantment(16, 5);
    public static final NBTTagCompound smite = getEnchantment(17, 5);
    public static final NBTTagCompound baneOfArthroponds = getEnchantment(18, 5);
    public static final NBTTagCompound knockBack = getEnchantment(19, 2);
    public static final NBTTagCompound fireAspect = getEnchantment(20, 2);
    public static final NBTTagCompound looting = getEnchantment(21, 3);

    public static final NBTTagCompound efficiency = getEnchantment(32, 5);
    public static final NBTTagCompound silkTouch = getEnchantment(33, 1);
    public static final NBTTagCompound unbreaking = getEnchantment(34, 3);
    public static final NBTTagCompound fortune = getEnchantment(35, 3);

    public static final NBTTagCompound power = getEnchantment(48, 5);
    public static final NBTTagCompound punch = getEnchantment(49, 2);
    public static final NBTTagCompound flame = getEnchantment(50, 1);
    public static final NBTTagCompound infinity = getEnchantment(51, 1);

    public static final NBTTagCompound luckOfTheSea = getEnchantment(61, 3);
    public static final NBTTagCompound lure = getEnchantment(62, 3);

    public static final NBTTagCompound speed = getPotionEffect(1, 3, 9600);
    public static final NBTTagCompound slowness = getPotionEffect(2, 3, 9600);
    public static final NBTTagCompound haste = getPotionEffect(3, 3, 9600);
    public static final NBTTagCompound miningFatigue = getPotionEffect(4, 3, 9600);
    public static final NBTTagCompound strength = getPotionEffect(5, 3, 9600);
    public static final NBTTagCompound instantHealth = getPotionEffect(6, 3, 0);
    public static final NBTTagCompound instantDamage = getPotionEffect(7, 3, 0);
    public static final NBTTagCompound jumpBoost = getPotionEffect(8, 3, 9600);
    public static final NBTTagCompound nausea = getPotionEffect(9, 0, 9600);
    public static final NBTTagCompound regeneration = getPotionEffect(10, 3, 9600);
    public static final NBTTagCompound resistance = getPotionEffect(11, 3, 9600);
    public static final NBTTagCompound fireResistance = getPotionEffect(12, 0, 9600);
    public static final NBTTagCompound waterBreathing = getPotionEffect(13, 0, 9600);
    public static final NBTTagCompound invisibility = getPotionEffect(14, 0, 9600);
    public static final NBTTagCompound blindness = getPotionEffect(15, 0, 9600);
    public static final NBTTagCompound nightVision = getPotionEffect(16, 0, 9600);
    public static final NBTTagCompound hunger = getPotionEffect(17, 3, 9600);
    public static final NBTTagCompound weakness = getPotionEffect(18, 3, 9600);
    public static final NBTTagCompound poison = getPotionEffect(19, 3, 9600);
    public static final NBTTagCompound wither = getPotionEffect(20, 3, 9600);
    public static final NBTTagCompound healthBoost = getPotionEffect(21, 3, 9600);
    public static final NBTTagCompound absorbtion = getPotionEffect(22, 3, 9600);
    public static final NBTTagCompound saturation = getPotionEffect(23, 3, 9600);

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

    public static NBTTagCompound getEnchantment(int id, int maxLevel) {
        NBTTagCompound nbttag = new NBTTagCompound();

        nbttag.setShort("id", (short) id);
        nbttag.setShort("lvl", (short) maxLevel);

        return nbttag;
    }

    public static NBTTagCompound getPotionEffect(int id, int amplifier, int duration) {
        NBTTagCompound nbttag = new NBTTagCompound();

        nbttag.setByte("Id", (byte) id);
        nbttag.setByte("Amplifier", (byte) amplifier);
        nbttag.setInteger("Duration", duration);

        return nbttag;
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
            nbttaglist.appendTag(enchantment);
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
            int minDuration = (int) (potionEffect.getInteger("Duration") / 3F);
            potionEffect.setInteger(
                "Duration",
                random.nextInt((potionEffect.getInteger("Duration") + 1) - minDuration) + minDuration);
            nbttaglist.appendTag(potionEffect);
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

    public static NBTBase getNBTTagFromString(String name, DropProcessData processData) {
        if (name.equals("#luckySwordEnchantments"))
            return getRandomEnchantmentList(
                4, 6, sharpness, smite, baneOfArthroponds, knockBack, fireAspect, looting, unbreaking);
        if (name.equals("#luckyAxeEnchantments"))
            return getRandomEnchantmentList(
                4, 6, sharpness, smite, baneOfArthroponds, efficiency, unbreaking, fortune);
        if (name.equals("#luckyToolEnchantments"))
            return getRandomEnchantmentList(2, 3, efficiency, unbreaking, fortune);

        if (name.equals("#luckyHelmetEnchantments"))
            return getRandomEnchantmentList(
                4,
                6,
                protection,
                fireProtection,
                blastProtection,
                projectileProtection,
                respiration,
                aquaAffinity,
                unbreaking);
        if (name.equals("#luckyChestplateEnchantments"))
            return getRandomEnchantmentList(
                4,
                6,
                protection,
                fireProtection,
                blastProtection,
                projectileProtection,
                thorns,
                unbreaking);
        if (name.equals("#luckyLeggingsEnchantments"))
            return getRandomEnchantmentList(
                4,
                6,
                protection,
                fireProtection,
                blastProtection,
                projectileProtection,
                thorns,
                unbreaking);
        if (name.equals("#luckyBootsEnchantments"))
            return getRandomEnchantmentList(
                4,
                6,
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
            return getRandomEnchantmentList(
                1,
                1,
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
            return getRandomPotionEffectList(
                7,
                10,
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
            return getRandomPotionEffectList(
                5,
                7,
                slowness,
                miningFatigue,
                instantDamage,
                nausea,
                blindness,
                hunger,
                weakness,
                poison,
                wither);

        if (name.equals("#randFireworksRocket")) return LuckyFunction.getRandomFireworksRocket();

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
                motionList.appendTag(new NBTTagDouble(launchMotionX));
                motionList.appendTag(new NBTTagDouble(launchMotionY));
                motionList.appendTag(new NBTTagDouble(launchMotionZ));
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
                motionList.appendTag(new NBTTagDouble(launchMotionX));
                motionList.appendTag(new NBTTagDouble(launchMotionY));
                motionList.appendTag(new NBTTagDouble(launchMotionZ));
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
                motionList.appendTag(new NBTTagDouble(entityArrow.motionX));
                motionList.appendTag(new NBTTagDouble(entityArrow.motionY));
                motionList.appendTag(new NBTTagDouble(entityArrow.motionZ));
                return motionList;
            } catch (Exception e) {
            }
        }

        name =
            name.replace(
                "#chestVillageBlacksmith",
                "#chestLootTable(" + LootTableList.CHESTS_VILLAGE_BLACKSMITH.getResourcePath() + ")");
        name =
            name.replace(
                "#chestBonusChest",
                "#chestLootTable(" + LootTableList.CHESTS_SPAWN_BONUS_CHEST.getResourcePath() + ")");
        name =
            name.replace(
                "#chestDungeonChest",
                "#chestLootTable(" + LootTableList.CHESTS_SIMPLE_DUNGEON.getResourcePath() + ")");

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
                        processData.getWorld().getLootTableManager());

                TileEntityChest tileEntityChest = new TileEntityChest();
                LootContext.Builder contextBuilder =
                    new LootContext.Builder((WorldServer) processData.getWorld());
                if (processData.getPlayer() != null && processData.getPlayer() instanceof EntityPlayer)
                    contextBuilder.withLuck(((EntityPlayer) processData.getPlayer()).getLuck());

                lootTable.fillInventory(tileEntityChest, random, contextBuilder.build());

                NBTTagCompound tagCompound = new NBTTagCompound();
                tileEntityChest.writeToNBT(tagCompound);
                return tagCompound.getTagList("Items", 10);

            } catch (Exception e) {
                System.err.println("Lucky Block: Error creating chest from .json loot table");
                e.printStackTrace();
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
            tileEntityChest.writeToNBT(tagCompound);
            return tagCompound.getTagList("Items", 10);
        }

        return null;
    }
}
