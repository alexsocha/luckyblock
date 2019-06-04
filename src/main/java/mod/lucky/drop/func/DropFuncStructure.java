package mod.lucky.drop.func;

import mod.lucky.Lucky;
import mod.lucky.drop.DropSingle;
import mod.lucky.structure.Structure;

public class DropFuncStructure extends DropFunction {
    @Override
    public void process(DropProcessData processData) {
        DropSingle drop = processData.getDropSingle();
        Structure structure = Lucky.getStructure(drop.getPropertyString("ID"));
        if (structure != null) structure.process(processData);
        else
            System.err.println(
                "Lucky Block: Structure with ID '" + drop.getPropertyString("ID") + "' does not exist");
    }

    @Override
    public void registerProperties() {
        DropSingle.setDefaultProperty(this.getType(), "rotation", Integer.class, 0);
        DropSingle.setDefaultProperty(this.getType(), "blockUpdate", Boolean.class, true);
    }

    @Override
    public String getType() {
        return "structure";
    }
}
