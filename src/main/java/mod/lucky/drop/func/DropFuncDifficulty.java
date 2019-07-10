package mod.lucky.drop.func;

import mod.lucky.drop.DropSingle;
import mod.lucky.drop.value.ValueParser;
import net.minecraft.world.Difficulty;

public class DropFuncDifficulty extends DropFunc {
    @Override
    public void process(DropProcessData processData) {
        DropSingle drop = processData.getDropSingle();
        String id = drop.getPropertyString("ID");

        Difficulty enumDifficulty;
        if (id.equalsIgnoreCase("peaceful") || id.equalsIgnoreCase("p"))
            enumDifficulty = Difficulty.PEACEFUL;
        else if (id.equalsIgnoreCase("easy") || id.equalsIgnoreCase("e"))
            enumDifficulty = Difficulty.EASY;
        else if (id.equalsIgnoreCase("normal") || id.equalsIgnoreCase("n"))
            enumDifficulty = Difficulty.NORMAL;
        else if (id.equalsIgnoreCase("hard") || id.equalsIgnoreCase("h"))
            enumDifficulty = Difficulty.HARD;
        else
            enumDifficulty = Difficulty.byId(ValueParser.getInteger(id));

        processData.getWorld().getServer().setDifficultyForAllWorlds(
            enumDifficulty, false /* don't force */);
    }

    @Override
    public String getType() {
        return "difficulty";
    }
}
