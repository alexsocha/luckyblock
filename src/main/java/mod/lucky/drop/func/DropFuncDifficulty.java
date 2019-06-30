package mod.lucky.drop.func;

import mod.lucky.drop.DropSingle;
import mod.lucky.drop.value.ValueParser;
import net.minecraft.world.EnumDifficulty;

public class DropFuncDifficulty extends DropFunc {
    @Override
    public void process(DropProcessData processData) {
        DropSingle drop = processData.getDropSingle();
        String id = drop.getPropertyString("ID");

        EnumDifficulty enumDifficulty;
        if (id.equalsIgnoreCase("peaceful") || id.equalsIgnoreCase("p"))
            enumDifficulty = EnumDifficulty.PEACEFUL;
        else if (id.equalsIgnoreCase("easy") || id.equalsIgnoreCase("e"))
            enumDifficulty = EnumDifficulty.EASY;
        else if (id.equalsIgnoreCase("normal") || id.equalsIgnoreCase("n"))
            enumDifficulty = EnumDifficulty.NORMAL;
        else if (id.equalsIgnoreCase("hard") || id.equalsIgnoreCase("h"))
            enumDifficulty = EnumDifficulty.HARD;
        else
            enumDifficulty = EnumDifficulty.byId(ValueParser.getInteger(id));

        processData.getWorld().getServer().setDifficultyForAllWorlds(enumDifficulty);
    }

    @Override
    public String getType() {
        return "difficulty";
    }
}
