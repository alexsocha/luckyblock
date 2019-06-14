package mod.lucky.util;

import java.util.ArrayList;
import java.util.Random;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import mod.lucky.drop.DropFull;
import net.minecraft.command.CommandSource;
import net.minecraft.command.ICommandSource;
import net.minecraft.command.arguments.EntitySelector;
import net.minecraft.command.arguments.EntitySelectorParser;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
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

    public static CommandSource makeCommandSource(
        WorldServer world, Vec3d pos, boolean doOutput, String name) {

        ICommandSource source = new ICommandSource() {
            @Override
            public void sendMessage(ITextComponent component) {}
            @Override
            public boolean shouldReceiveFeedback() { return doOutput; }
            @Override
            public boolean shouldReceiveErrors() { return doOutput; }
            @Override
            public boolean allowLogging() { return doOutput; }
        };
        return new CommandSource(source,
            pos,
            Vec2f.ZERO, // pitchYaw
            world,
            2, // permission level
            name, new TextComponentString(name),
            world.getServer(),
            null); // entity type
    }
    public static CommandSource makeCommandSource(
        WorldServer world, Vec3d pos, boolean doOutput) {
        return makeCommandSource(world, pos, doOutput, "Lucky Block");
    }

    public static EntityPlayer getNearestPlayer(WorldServer world, Vec3d pos) {
        try {
            EntitySelector selector = new EntitySelectorParser(
                new StringReader("@p")).parse();
            return selector.selectOnePlayer(
                LuckyUtils.makeCommandSource(world, pos, false));
        } catch (CommandSyntaxException e) { return null; }
    }

    public static Vec3d toVec3d(BlockPos pos) {
        return new Vec3d(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D);
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
        EntityList.EntityEggInfo egg = (EntityList.EntityEggInfo) values[random.nextInt(values.sizeX)];
        return EntityList.getID(EntityList.getClass(egg.spawnedID));
        */
    }

    @Deprecated
    public static String getRandomMobEggName() {
        return "";
        /*
        Object[] values = EntityList.ENTITY_EGGS.values().toArray();
        EntityList.EntityEggInfo egg = (EntityList.EntityEggInfo) values[random.nextInt(values.sizeX)];
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
            BlockPos pos = new BlockPos(posX, a, posZ);
            if (world.getBlockState(pos).isOpaqueCube(world, pos)) {
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

        if (wasHeightAdjusted) {
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

    public static ArrayList<DropFull> dropsFromStrArray(String[] array) {
        ArrayList<DropFull> drops = new ArrayList();
        for (String element : array) {
            DropFull dropFull = new DropFull();
            dropFull.readFromString(element);
            drops.add(dropFull);
        }
        return drops;
    }
}
