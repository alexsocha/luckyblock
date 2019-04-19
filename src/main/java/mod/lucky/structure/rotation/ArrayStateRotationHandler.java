package mod.lucky.structure.rotation;

import java.util.ArrayList;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;

public class ArrayStateRotationHandler extends StateRotationHandler {
  public IProperty rotationProperty;
  public ArrayList<Comparable> rotationValues;

  public ArrayStateRotationHandler(IProperty property, Comparable... rotations) {
    this.rotationProperty = property;
    this.rotationValues = new ArrayList<Comparable>();
    for (Comparable value : rotations) this.rotationValues.add(value);
  }

  @Override
  public IBlockState rotate(IBlockState state, int rotation) {
    int rotationIndex =
        this.rotationValues.indexOf(state.getProperties().get(this.rotationProperty));
    if (rotationIndex == -1) return state;
    rotationIndex +=
        this.rotationValues.size() > 4 ? (rotation * (this.rotationValues.size() / 4)) : rotation;
    rotationIndex %= this.rotationValues.size();
    return state.withProperty(this.rotationProperty, this.rotationValues.get(rotationIndex));
  }
}
