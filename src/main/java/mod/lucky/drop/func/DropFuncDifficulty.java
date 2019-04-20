package mod.lucky.drop.func;

import mod.lucky.drop.DropProperties;
import mod.lucky.drop.value.ValueParser;
import net.minecraft.world.EnumDifficulty;

public class DropFuncDifficulty extends DropFunction {
    @Override
    public void process(DropProcessData processData) {
        DropProperties drop = processData.getDropProperties();
        String id = drop.getPropertyString("ID");
        EnumDifficulty enumdifficulty =
            !id.equalsIgnoreCase("peaceful") && !id.equalsIgnoreCase("p")
                ? (!id.equalsIgnoreCase("easy") && !id.equalsIgnoreCase("e")
                ? (!id.equalsIgnoreCase("normal") && !id.equalsIgnoreCase("n")
                ? (!id.equalsIgnoreCase("hard") && !id.equalsIgnoreCase("h")
                ? EnumDifficulty.getDifficultyEnum(ValueParser.getInteger(id))
                : EnumDifficulty.HARD)
                : EnumDifficulty.NORMAL)
                : EnumDifficulty.EASY)
                : EnumDifficulty.PEACEFUL;
        processData.getWorld().getMinecraftServer().setDifficultyForAllWorlds(enumdifficulty);
    }

    @Override
    public String getType() {
        return "difficulty";
    }
}
