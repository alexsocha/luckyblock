package mod.lucky.drop.func;

import mod.lucky.Lucky;
import mod.lucky.drop.DropProperties;
import mod.lucky.structure.Structure;

public class DropFuncStructure extends DropFunction {
  @Override
  public void process(DropProcessData processData) {
    DropProperties drop = processData.getDropProperties();
    Structure structure = Lucky.getStructure(drop.getPropertyString("ID"));
    if (structure != null) structure.process(processData);
    else
      System.err.println(
          "Lucky Block: Structure with ID '" + drop.getPropertyString("ID") + "' does not exist");
  }

  @Override
  public void registerProperties() {
    DropProperties.setDefaultProperty(this.getType(), "rotation", Integer.class, 0);
    DropProperties.setDefaultProperty(this.getType(), "blockUpdate", Boolean.class, true);
  }

  @Override
  public String getType() {
    return "structure";
  }
}
