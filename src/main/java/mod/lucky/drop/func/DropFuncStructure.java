package mod.lucky.drop.func;

import mod.lucky.Lucky;
import mod.lucky.drop.DropSingle;
import mod.lucky.init.SetupCommon;
import mod.lucky.structure.Structure;

public class DropFuncStructure extends DropFunc {
    @Override
    public void process(DropProcessData processData) {
        DropSingle drop = processData.getDropSingle();

        Structure structure = SetupCommon.getStructure(drop.getPropertyString("ID"));
        if (structure != null) structure.process(processData);
        else
            Lucky.LOGGER.error("Structure with ID '"
                + drop.getPropertyString("ID") + "' does not exist");
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
