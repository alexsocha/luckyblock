package mod.lucky.util;

import java.util.ArrayList;
import java.util.Random;

import mod.lucky.drop.DropContainer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;

public class LuckyUtils {
    public static int[] potionEffectListGood = {1, 5, 6, 8, 10, 12, 13, 14, 16, 14};
    public static int[] potionEffectListBad = {2, 7, 18, 19};

    private static Random random = new Random();

    private static ArrayList<String> potionNames = new ArrayList<>();

    static {
        for (ResourceLocation resource : ForgeRegistries.POTION_TYPES.getKeys()) {
            String potion = resource.getPath();
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
            colors[a] = EnumDyeColor.values()[random.nextInt(
                EnumDyeColor.values().length)].getId();
        }
        explosionTag.setIntArray("Colors", colors);

        // set explosion list
        explosionList.add(explosionTag);

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

    @Nullable
    public static Entity getEntity(World world, int id, String name) {
        ResourceLocation rl = new ResourceLocation(name);
        if (ForgeRegistries.ENTITIES.containsKey(rl))
            return ForgeRegistries.ENTITIES.getValue(rl).create(world);
        else return null;
    }

    @Deprecated
    public static int getRandomStatusEffect() {
        return 0;
        // return Potion.REGISTRY.getIDForObject(Potion.REGISTRY.getRandomObject(random));
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

    @Deprecated
    public static int getRandomMobEggID() {
        return 0;
        /*
        Object[] values = EntityList.ENTITY_EGGS.values().toArray();
        EntityList.EntityEggInfo egg = (EntityList.EntityEggInfo) values[random.nextInt(values.length)];
        return EntityList.getID(EntityList.getClass(egg.spawnedID));
        */
    }

    @Deprecated
    public static String getRandomMobEggName() {
        return 0;
        /*
        Object[] values = EntityList.ENTITY_EGGS.values().toArray();
        EntityList.EntityEggInfo egg = (EntityList.EntityEggInfo) values[random.nextInt(values.length)];
        return egg.spawnedID.toString();
        */
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
            nbttagList.add(new NBTTagString(element));
        }
        return nbttagList;
    }

    public static String[] strArrayFromTagList(NBTTagList nbttagList) {
        String[] array = new String[nbttagList == null ? 0 : nbttagList.size()];
        for (int a = 0; a < array.length; a++) {
            array[a] = nbttagList.getString(a);
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
