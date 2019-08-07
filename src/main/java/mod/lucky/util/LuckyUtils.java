package mod.lucky.util;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import mod.lucky.drop.DropFull;
import net.minecraft.command.CommandSource;
import net.minecraft.command.ICommandSource;
import net.minecraft.command.arguments.EntitySelector;
import net.minecraft.command.arguments.EntitySelectorParser;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.DyeColor;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;

public class LuckyUtils {
    private static Random random = new Random();

    private static List<String> potionIds;
    private static List<String> spawnEggIds;
    private static List<String> colorNames = Arrays.asList("black", "blue", "brown", "cyan", "gray", "green", "green", "light_blue", "light_gray", "lime", "magenta", "orange", "pink", "purple", "red", "white", "yellow");

    static {
        potionIds = ForgeRegistries.POTIONS.getKeys().stream()
            .filter(k -> k.getNamespace().equals("minecraft")
                && !k.getPath().equals("empty")
                && !k.getPath().equals("water")
                && !k.getPath().equals("mundane")
                && !k.getPath().equals("thick")
                && !k.getPath().equals("awkward"))
            .map(k -> k.toString()).collect(Collectors.toList());

        spawnEggIds =  ForgeRegistries.ITEMS.getKeys().stream()
            .filter(k -> k.getNamespace().equals("minecraft")
                && k.getPath().endsWith("_spawn_egg"))
            .map(k -> k.toString()).collect(Collectors.toList());

    }

    public static CommandSource makeCommandSource(
        ServerWorld world, Vec3d pos, boolean doOutput, String name) {

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
            name, new StringTextComponent(name),
            world.getServer(),
            null); // entity type
    }
    public static CommandSource makeCommandSource(
        ServerWorld world, Vec3d pos, boolean doOutput) {
        return makeCommandSource(world, pos, doOutput, "Lucky Block");
    }

    public static PlayerEntity getNearestPlayer(ServerWorld world, Vec3d pos) {
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

    public static CompoundNBT getRandomFireworksRocket() {
        Random random = new Random();

        CompoundNBT mainTag = new CompoundNBT();
        CompoundNBT fireworksTag = new CompoundNBT();
        CompoundNBT explosionTag = new CompoundNBT();
        ListNBT explosionList = new ListNBT();

        // set explosion properties
        explosionTag.putByte("Type", (byte) random.nextInt(5));
        explosionTag.putBoolean("Flicker", random.nextBoolean());
        explosionTag.putBoolean("Trail", random.nextBoolean());
        int colorAmount = random.nextInt(4) + 1;
        int[] colors = new int[colorAmount];
        for (int a = 0; a < colorAmount; a++) {
            float[] c = DyeColor.values()[
                random.nextInt(DyeColor.values().length)]
                .getColorComponentValues();
            Color color = new Color(c[0], c[1], c[2]);
            colors[a] = color.getRGB();
        }
        explosionTag.putIntArray("Colors", colors);

        // set explosion list
        explosionList.add(explosionTag);

        // set fireworks rocket properties
        fireworksTag.put("Explosions", explosionList);
        fireworksTag.putByte("Flight", (byte) (random.nextInt(2) + 1));

        // set main properties
        mainTag.put("Fireworks", fireworksTag);
        return mainTag;
    }

    public static String getRandomPotionId() {
        return potionIds.get(random.nextInt(potionIds.size()));
    }

    public static String getRandomSpawnEggId() {
        return spawnEggIds.get(random.nextInt(spawnEggIds.size()));
    }

    public static String getRandomColor() {
        return colorNames.get(random.nextInt(colorNames.size()));
    }

    @Nullable
    public static Entity getEntity(World world, int id, String name) {
        ResourceLocation rl = new ResourceLocation(name);
        if (ForgeRegistries.ENTITIES.containsKey(rl))
            return ForgeRegistries.ENTITIES.getValue(rl).create(world);
        else return null;
    }

    public static int getPlayerDirection(PlayerEntity player, int accuracy) {
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

    public static ListNBT tagListFromStrArray(String[] array) {
        ListNBT nbttagList = new ListNBT();
        for (String element : array) {
            nbttagList.add(new StringNBT(element));
        }
        return nbttagList;
    }

    public static String[] strArrayFromTagList(ListNBT nbttagList) {
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
