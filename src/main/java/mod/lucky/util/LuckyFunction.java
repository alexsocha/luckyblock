package mod.lucky.util;

import java.util.ArrayList;
import java.util.Random;

import mod.lucky.drop.DropContainer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.entity.monster.*;
import net.minecraft.entity.passive.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemDye;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class LuckyFunction {
    public static int[] potionEffectList = {1, 2, 3, 4, 5, 6, 8, 9, 10, 12, 14};

    public static int[] potionEffectListGood = {1, 5, 6, 8, 10, 12, 13, 14, 16, 14};
    public static int[] potionEffectListBad = {2, 7, 18, 19};

    public static int[] mobEggList = {
        50, 51, 52, 54, 55, 56, 57, 58, 59, 60, 61, 62, 65, 66, 90, 91, 92, 93, 94, 95, 96, 98, 100, 120
    };
    public static int[] mobIDList = {
        50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64, 65, 66, 90, 91, 92, 93, 94, 95, 96,
        97, 98, 99, 100, 120
    };
    public static String[] mobNameList = {
        "creeper",
        "skeleton",
        "spider",
        "giant zombie",
        "zombie",
        "slime",
        "ghast",
        "zombie pigman",
        "enderman",
        "cave spider",
        "silverfish",
        "blaze",
        "magma cube",
        "ender dragon",
        "wither",
        "witch",
        "bat",
        "pig",
        "sheep",
        "cow",
        "chicken",
        "squid",
        "wolf",
        "mooshroom",
        "snow golem",
        "ocelot",
        "iron golem",
        "horse",
        "villager"
    };

    private static Random random = new Random();

    private static ArrayList<String> potionNames = new ArrayList<String>();

    static {
        for (ResourceLocation resource : PotionType.REGISTRY.getKeys()) {
            String potion = resource.getResourcePath();
            if (!potion.equals("empty")
                && !potion.equals("water")
                && !potion.equals("mundane")
                && !potion.equals("thick")
                && !potion.equals("awkward")) potionNames.add(potion);
        }
    }

    public static NBTTagCompound getRandomFireworksRocket() {
        Random random = new Random();

        NBTTagCompound mainTag = new NBTTagCompound();
        NBTTagCompound fireworksTag = new NBTTagCompound();
        NBTTagCompound explosionTag = new NBTTagCompound();
        NBTTagList explosionList = new NBTTagList();

        // set explosion properties
        explosionTag.setByte("Type", (byte) random.nextInt(5));
        explosionTag.setBoolean("Flicker", random.nextBoolean());
        explosionTag.setBoolean("Trail", random.nextBoolean());
        int colorAmount = random.nextInt(4) + 1;
        int[] colors = new int[colorAmount];
        for (int a = 0; a < colorAmount; a++) {
            colors[a] = Integer.valueOf(ItemDye.DYE_COLORS[random.nextInt(14)]);
        }
        explosionTag.setIntArray("Colors", colors);

        // set explosion list
        explosionList.appendTag(explosionTag);

        // set fireworks rocket properties
        fireworksTag.setTag("Explosions", explosionList);
        fireworksTag.setByte("Flight", (byte) (random.nextInt(2) + 1));

        // set main properties
        mainTag.setTag("Fireworks", fireworksTag);
        return mainTag;
    }

    public static int getRandomPotionDamage() {
        return calculatePotionDamage(getRandomPotionEffect(), 0);
    }

    public static int calculatePotionDamage(int effect, int isSplash) {
        // determines whether to leave the tier and duration, increase the
        // duration by 8/3, or change the tier (halving the duration)
        int tier = 0;
        int extended = 0;
        int i = random.nextInt(3);
        if (i == 1) tier = 32;
        if (i == 2) extended = 64;

        // determines if this is a splash potion
        int splash = 0;
        if (isSplash == 0) {
            if (random.nextInt(2) == 0) splash = 16384;
        } else if (isSplash == 1) splash = 0;
        else if (isSplash == 2) splash = 16384;

        return effect + tier + extended + splash;
    }

    public static String getRandomPotionName() {
        return potionNames.get(random.nextInt(potionNames.size()));
    }

    @Deprecated
    public static EntityLiving getMobByNameOrId(World world, String name, int id) {
        // hostile
        if (name.equals(mobNameList[0]) || id == mobIDList[0]) return new EntityCreeper(world);
        if (name.equals(mobNameList[1]) || id == mobIDList[1]) return new EntitySkeleton(world);
        if (name.equals(mobNameList[2]) || id == mobIDList[2]) return new EntitySpider(world);
        if (name.equals(mobNameList[3]) || id == mobIDList[3]) return new EntityGiantZombie(world);
        if (name.equals(mobNameList[4]) || id == mobIDList[4]) return new EntityZombie(world);
        if (name.equals(mobNameList[5]) || id == mobIDList[5]) return new EntitySlime(world);
        if (name.equals(mobNameList[6]) || id == mobIDList[6]) return new EntityGhast(world);
        if (name.equals(mobNameList[7]) || id == mobIDList[7]) return new EntityPigZombie(world);
        if (name.equals(mobNameList[8]) || id == mobIDList[8]) return new EntityEnderman(world);
        if (name.equals(mobNameList[9]) || id == mobIDList[9]) return new EntityCaveSpider(world);
        if (name.equals(mobNameList[10]) || id == mobIDList[10]) return new EntitySilverfish(world);
        if (name.equals(mobNameList[11]) || id == mobIDList[11]) return new EntityBlaze(world);
        if (name.equals(mobNameList[12]) || id == mobIDList[12]) return new EntityMagmaCube(world);
        if (name.equals(mobNameList[13]) || id == mobIDList[13]) return new EntityDragon(world);
        if (name.equals(mobNameList[14]) || id == mobIDList[14]) return new EntityWither(world);
        if (name.equals(mobNameList[15]) || id == mobIDList[15]) return new EntityWitch(world);

        // passive
        if (name.equals(mobNameList[16]) || id == mobIDList[16]) return new EntityBat(world);
        if (name.equals(mobNameList[17]) || id == mobIDList[17]) return new EntityPig(world);
        if (name.equals(mobNameList[18]) || id == mobIDList[18]) return new EntitySheep(world);
        if (name.equals(mobNameList[19]) || id == mobIDList[19]) return new EntityCow(world);
        if (name.equals(mobNameList[20]) || id == mobIDList[20]) return new EntityChicken(world);
        if (name.equals(mobNameList[21]) || id == mobIDList[21]) return new EntitySquid(world);
        if (name.equals(mobNameList[22]) || id == mobIDList[22]) return new EntityWolf(world);
        if (name.equals(mobNameList[23]) || id == mobIDList[23]) return new EntityMooshroom(world);
        if (name.equals(mobNameList[24]) || id == mobIDList[24]) return new EntitySnowman(world);
        if (name.equals(mobNameList[25]) || id == mobIDList[25]) return new EntityOcelot(world);
        if (name.equals(mobNameList[26]) || id == mobIDList[26]) return new EntityIronGolem(world);
        if (name.equals(mobNameList[27]) || id == mobIDList[27]) return new EntityHorse(world);
        if (name.equals(mobNameList[28]) || id == mobIDList[28]) return new EntityVillager(world);

        return null;
    }

    public static Entity getEntity(World world, int id, String name) {
        if (!EntityList.ENTITY_EGGS.containsKey(Integer.valueOf(id))) {
            // return EntityList.createEntityByIDFromName(name, world);
            return null;
        } else {
            return EntityList.createEntityByID(id, world);
        }
    }

    public static int getRandomStatusEffect() {
        return Potion.REGISTRY.getIDForObject(Potion.REGISTRY.getRandomObject(random));
    }

    public static int getRandomPotionEffect() {
        return random.nextInt(14) + 1;
    }

    public static int getRandomPotionEffectGood() {
        return potionEffectListGood[random.nextInt(potionEffectListGood.length)];
    }

    public static int getRandomPotionEffectBad() {
        return potionEffectListBad[random.nextInt(potionEffectListBad.length)];
    }

    public static int getRandomMobEggID() {
        Object[] values = EntityList.ENTITY_EGGS.values().toArray();
        EntityList.EntityEggInfo egg = (EntityList.EntityEggInfo) values[random.nextInt(values.length)];
        return EntityList.getID(EntityList.getClass(egg.spawnedID));
    }

    public static String getRandomMobEggName() {
        Object[] values = EntityList.ENTITY_EGGS.values().toArray();
        EntityList.EntityEggInfo egg = (EntityList.EntityEggInfo) values[random.nextInt(values.length)];
        return egg.spawnedID.toString();
    }

    public static int getPlayerDirection(EntityPlayer player, int accuracy) {
        int yaw = (int) player.rotationYaw;
        int angle = 360 / accuracy;
        if (yaw < 0) yaw += 360;
        yaw += (angle / 2);
        yaw %= 360;
        return (yaw / angle);
    }

    public static int adjustHeight(World world, int height, int posX, int posY, int posZ) {
        boolean wasHeightAdjusted = false;
        int newPosY = posY;
        int airCount = 0;
        for (int a = posY; a < posY + 16; a++) {
            if (world.getBlockState(new BlockPos(posX, a, posZ)).isOpaqueCube()) {
                airCount = 0;
                newPosY = a + 1;
            } else {
                airCount++;
            }

            if (airCount == height) {
                wasHeightAdjusted = true;
                break;
            }
        }

        if (wasHeightAdjusted == true) {
            return newPosY;
        } else {
            return -1;
        }
    }

    public static NBTTagList tagListFromStrArray(String[] array) {
        NBTTagList nbttagList = new NBTTagList();
        for (String element : array) {
            nbttagList.appendTag(new NBTTagString(element));
        }
        return nbttagList;
    }

    public static String[] strArrayFromTagList(NBTTagList nbttagList) {
        String[] array = new String[nbttagList == null ? 0 : nbttagList.tagCount()];
        for (int a = 0; a < array.length; a++) {
            array[a] = nbttagList.getStringTagAt(a);
        }
        return array;
    }

    public static ArrayList<DropContainer> dropsFromStrArray(String[] array) {
        ArrayList<DropContainer> drops = new ArrayList();
        for (String element : array) {
            DropContainer dropContainer = new DropContainer();
            dropContainer.readFromString(element);
            drops.add(dropContainer);
        }
        return drops;
    }
}
