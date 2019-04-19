package mod.lucky.drop.func;

import java.util.ArrayList;
import java.util.Random;
import mod.lucky.Lucky;
import mod.lucky.drop.DropBase;
import mod.lucky.drop.DropContainer;
import mod.lucky.drop.DropGroup;
import mod.lucky.drop.DropProperties;

public class DropProcessor {
  private ArrayList<DropContainer> drops;
  private int debugDropIndex = 0;
  private int debugIndexMin = 0;
  private int debugIndexMax = 1000;

  public DropProcessor() {
    this.drops = new ArrayList<DropContainer>();
  }

  public void processDrop(DropBase drop, DropProcessData processData) {
    DropBase processDrop = drop.initialize(processData);

    if (processDrop instanceof DropContainer)
      this.processDrop(((DropContainer) processDrop).getDrop(), processData);
    if (processDrop instanceof DropGroup) {
      DropGroup group = ((DropGroup) processDrop);
      for (int i = 0; i < group.getAmount(); i++) {
        this.processDrop(group.getDrops().get(i), processData);
      }
    }
    if (processDrop instanceof DropProperties) {
      DropProperties originalDrop = (DropProperties) drop;
      DropProperties properties = (DropProperties) processDrop;
      DropFunction dropFunction = DropFunction.getDropFunction(properties);

      if (dropFunction == null)
        System.err.println(
            "Lucky Block: Error processing drop type '"
                + properties.getPropertyString("type")
                + "'. Drop type does not exist.");
      else {
        int amount = properties.getPropertyInt("amount");
        boolean reinitialize = properties.getPropertyBoolean("reinitialize");
        boolean postInit = properties.getPropertyBoolean("postDelayInit");

        DropProcessData dropData = processData.copy();
        dropData.setDropProperties(properties);

        if (properties.hasProperty("delay")) {
          if (reinitialize) {
            for (int i = 0; i < amount; i++) {
              float delay = dropData.getDropProperties().getPropertyFloat("delay");
              if (postInit) {
                dropData.setDropProperties(originalDrop);
                Lucky.getInstance().getTickHandler().addDelayDrop(this, dropData.copy(), delay);
              } else Lucky.getInstance().getTickHandler().addDelayDrop(this, dropData, delay);
              if (i < amount - 1) dropData.setDropProperties(originalDrop.initialize(dropData));
            }
          } else {
            if (postInit) dropData.setDropProperties(originalDrop);
            Lucky.getInstance()
                .getTickHandler()
                .addDelayDrop(
                    this, dropData, dropData.getDropProperties().getPropertyFloat("delay"));
          }
          return;
        }

        for (int i = 0; i < amount; i++) {
          dropFunction.process(dropData);
          if (reinitialize && i < amount - 1)
            dropData.setDropProperties(originalDrop.initialize(dropData));
        }
      }
    }
  }

  public void processDelayDrop(DropProcessData processData) {
    DropProperties originalDrop = processData.getDropProperties();
    DropProperties properties = processData.getDropProperties();
    DropFunction dropFunction = DropFunction.getDropFunction(properties);
    if (dropFunction == null)
      System.err.println(
          "Lucky Block: Error processing drop type '"
              + properties.getPropertyString("type")
              + "'. Drop type does not exist.");
    else {
      boolean postInit = properties.getPropertyBoolean("postDelayInit");
      if (postInit) properties = originalDrop.initialize(processData);
      int amount = properties.getPropertyInt("amount");
      boolean reinitialize = properties.getPropertyBoolean("reinitialize");

      DropProcessData dropData = processData.copy();
      dropData.setDropProperties(properties);

      if (reinitialize) dropFunction.process(dropData);
      else {
        for (int i = 0; i < amount; i++) dropFunction.process(dropData);
      }
    }
  }

  public void processRandomDrop(DropProcessData processData, int luck) {
    this.processRandomDrop(processData, luck, true);
  }

  public void processRandomDrop(DropProcessData processData, int luck, boolean output) {
    this.processRandomDrop(processData, luck, true, false);
  }

  public void processRandomDrop(
      DropProcessData processData, int luck, boolean output, boolean debug) {
    DropContainer drop = this.selectRandomDrop(this.drops, luck);
    if (debug) {
      if (this.debugDropIndex >= this.drops.size() || this.debugDropIndex > this.debugIndexMax)
        this.debugDropIndex = this.debugIndexMin;
      drop = this.drops.get(this.debugDropIndex);
      this.debugDropIndex++;
    }
    if (drop == null) return;
    if (output) System.out.println("Chosen Lucky Block Drop: " + drop);
    this.processDrop(drop, processData);
  }

  public void processRandomDrop(
      ArrayList<DropContainer> drops, DropProcessData processData, int luck) {
    this.processRandomDrop(drops, processData, luck, true);
  }

  public void processRandomDrop(
      ArrayList<DropContainer> drops, DropProcessData processData, int luck, boolean output) {
    DropContainer drop = this.selectRandomDrop(drops, luck);
    if (drop == null) return;
    if (output) System.out.println("Chosen Lucky Block Drop: " + drop);
    this.processDrop(drop, processData);
  }

  public DropContainer selectRandomDrop(ArrayList<DropContainer> drops, int luck) {
    if (drops.size() == 0) return null;

    int lowestLuck = 0;
    int heighestLuck = 0;
    for (int i = 0; i < drops.size(); i++) {
      if (drops.get(i).getLuck() < lowestLuck) lowestLuck = drops.get(i).getLuck();
      if (drops.get(i).getLuck() > heighestLuck) heighestLuck = drops.get(i).getLuck();
    }
    heighestLuck += (lowestLuck * -1) + 1;

    float levelIncrease = 1.0F / (1.0F - (((luck < 0 ? luck * -1 : luck) * 0.77F) / 100F));

    float weightTotal = 0;
    ArrayList<Float> weightPoints = new ArrayList<Float>();
    weightPoints.add(0.0F);
    for (DropContainer drop : drops) {
      int dropLuck = drop.getLuck() + (lowestLuck * -1) + 1;
      float newLuck = 0.0F;
      if (luck >= 0) newLuck = (float) Math.pow(levelIncrease, dropLuck);
      else newLuck = (float) Math.pow(levelIncrease, heighestLuck + 1 - dropLuck);
      float newChance = drop.getChance() * newLuck * 100;
      weightTotal += newChance;
      weightPoints.add(weightTotal);
    }

    Random random = new Random();
    float randomIndex = random.nextFloat() * weightTotal;
    DropContainer chosenDrop = drops.get(this.getDropIndexByWeight(weightPoints, randomIndex));

    return chosenDrop;
  }

  private int getDropIndexByWeight(ArrayList<Float> weightPoints, float randomIndex) {
    for (int i = 0; i < weightPoints.size(); i++) {
      if (randomIndex >= weightPoints.get(i) && randomIndex < weightPoints.get(i + 1)) return i;
    }
    return 0;
  }

  public void registerDrop(DropContainer drop) {
    this.drops.add(drop);
  }

  public ArrayList<DropContainer> getDrops() {
    return this.drops;
  }
}
