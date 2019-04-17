package mod.lucky.drop.func;

import java.util.HashMap;
import mod.lucky.drop.DropProperties;
import net.minecraft.nbt.NBTTagCompound;

public abstract class DropFunction {
  public abstract void process(DropProcessData processData);

  public abstract String getType();

  public void registerProperties() {}

  public static void registerGlobalProperties() {
    DropProperties.setDefaultProperty("all", "type", String.class, "item");
    DropProperties.setDefaultProperty("all", "ID", String.class, "");
    DropProperties.setDefaultProperty("all", "damage", Integer.class, 0);
    DropProperties.setDefaultProperty("all", "amount", Integer.class, 1);
    DropProperties.setDefaultProperty("all", "reinitialize", Boolean.class, false);
    DropProperties.setDefaultProperty("all", "postDelayInit", Boolean.class, true);
    DropProperties.setDefaultProperty("all", "delay", Float.class, 0);
    DropProperties.setDefaultProperty("all", "posX", Float.class, 0);
    DropProperties.setDefaultProperty("all", "posY", Float.class, 0);
    DropProperties.setDefaultProperty("all", "posZ", Float.class, 0);
    DropProperties.setDefaultProperty("all", "pos", String.class, "(0,0,0)");
    DropProperties.setDefaultProperty("all", "pos2X", Float.class, 0);
    DropProperties.setDefaultProperty("all", "pos2Y", Float.class, 0);
    DropProperties.setDefaultProperty("all", "pos2Z", Float.class, 0);
    DropProperties.setDefaultProperty("all", "pos2", String.class, "(0,0,0)");
    DropProperties.setDefaultProperty("all", "posOffsetX", Float.class, 0);
    DropProperties.setDefaultProperty("all", "posOffsetY", Float.class, 0);
    DropProperties.setDefaultProperty("all", "posOffsetZ", Float.class, 0);
    DropProperties.setDefaultProperty("all", "posOffset", String.class, "(0,0,0)");
    DropProperties.setDefaultProperty("all", "centerX", Integer.class, 0);
    DropProperties.setDefaultProperty("all", "centerY", Integer.class, 0);
    DropProperties.setDefaultProperty("all", "centerZ", Integer.class, 0);
    DropProperties.setDefaultProperty("all", "rotation", Integer.class, 0);
    DropProperties.setDefaultProperty("all", "doUpdate", Boolean.class, false);
    DropProperties.setDefaultProperty("all", "blockMode", String.class, "replace");
    DropProperties.setDefaultProperty("all", "displayCommandOutput", Boolean.class, false);
    DropProperties.setDefaultProperty("all", "commandSender", String.class, "@");
    DropProperties.setDefaultProperty("all", "duration", Integer.class, 200);
    DropProperties.setDefaultProperty("all", "NBTTag", NBTTagCompound.class, null);
  }

  private static HashMap<String, DropFunction> dropFunctions = new HashMap<String, DropFunction>();

  public static void registerFunctions() {
    registerDropFunction(new DropFuncBlock());
    registerDropFunction(new DropFuncCommand());
    registerDropFunction(new DropFuncDifficulty());
    registerDropFunction(new DropFuncEffect());
    registerDropFunction(new DropFuncEntity());
    registerDropFunction(new DropFuncExplosion());
    registerDropFunction(new DropFuncFill());
    registerDropFunction(new DropFuncItem());
    registerDropFunction(new DropFuncMessage());
    registerDropFunction(new DropFuncParticle());
    registerDropFunction(new DropFuncSound());
    registerDropFunction(new DropFuncStructure());
    registerDropFunction(new DropFuncTime());
    registerDropFunction(new DropFuncNothing());
    registerGlobalProperties();
  }

  public static DropFunction getDropFunction(DropProperties dropProperties) {
    return getDropFunction(dropProperties.getPropertyString("type"));
  }

  public static DropFunction getDropFunction(String type) {
    return dropFunctions.get(type);
  }

  public static void registerDropFunction(DropFunction dropFunction) {
    dropFunctions.put(dropFunction.getType(), dropFunction);
    dropFunction.registerProperties();
  }
}
