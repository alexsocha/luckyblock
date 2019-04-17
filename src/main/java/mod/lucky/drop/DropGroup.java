package mod.lucky.drop;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;
import mod.lucky.drop.func.DropProcessData;
import mod.lucky.drop.value.DropStringUtils;
import mod.lucky.drop.value.DropValue;
import net.minecraft.nbt.NBTTagCompound;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

public class DropGroup extends DropBase {
  private ArrayList<DropBase> drops;
  private DropValue amount;
  private boolean shuffle;

  public DropGroup() {
    super();
    this.drops = new ArrayList<DropBase>();
  }

  @Override
  public DropGroup initialize(DropProcessData processData) {
    DropGroup dropGroup = this.copy();
    dropGroup.amount.initialize(processData);
    if (dropGroup.shuffle) Collections.shuffle(dropGroup.drops);
    return dropGroup;
  }

  @Override
  public void readFromString(String string) {
    String[] colonSections = DropStringUtils.splitBracketString(string, ':');
    String lineOfContents = colonSections[colonSections.length - 1];
    String[] commaSections = DropStringUtils.splitBracketString(lineOfContents, ',');
    lineOfContents = commaSections[0];

    commaSections = ArrayUtils.remove(commaSections, 0);
    String groupProperties = StringUtils.join(commaSections, ',');

    if (!groupProperties.equals("")) groupProperties = "," + groupProperties;
    if (lineOfContents.toLowerCase(Locale.ENGLISH).startsWith("group"))
      lineOfContents = lineOfContents.substring("group".length() + 1, lineOfContents.length() - 1);
    else lineOfContents = lineOfContents.substring(1, lineOfContents.length() - 1);
    String[] groupContents = DropStringUtils.splitBracketString(lineOfContents, ';');

    if (colonSections.length >= 3) {
      this.amount = new DropValue(colonSections[1], Integer.class);
      this.shuffle = true;
    } else {
      this.amount = new DropValue(groupContents.length);
      this.shuffle = false;
    }

    for (String drop : groupContents) {
      drop += groupProperties;
      if (drop.toLowerCase(Locale.ENGLISH).startsWith("group")) {
        DropGroup group = new DropGroup();
        group.readFromString(drop);
        this.drops.add(group);
      } else {
        DropProperties properties = new DropProperties();
        properties.readFromString(drop);
        this.drops.add(properties);
      }
    }
  }

  public ArrayList<DropBase> getDrops() {
    return this.drops;
  }

  public int getAmount() {
    return this.amount.getValueInt();
  }

  @Override
  public String writeToString() {
    return null;
  }

  @Override
  public void readFromNBT(NBTTagCompound tagCompound) {}

  @Override
  public NBTTagCompound writeToNBT() {
    return null;
  }

  public DropGroup copy() {
    DropGroup dropGroup = new DropGroup();
    dropGroup.amount = this.amount.copy();
    dropGroup.shuffle = this.shuffle;
    // don't need to copy
    dropGroup.drops = this.drops;
    return dropGroup;
  }
}
