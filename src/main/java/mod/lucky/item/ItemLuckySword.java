package mod.lucky.item;

import java.util.List;
import javax.annotation.Nullable;

import mod.lucky.crafting.LuckCrafting;
import mod.lucky.drop.func.DropProcessData;
import mod.lucky.drop.func.DropProcessor;
import mod.lucky.util.LuckyFunction;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemLuckySword extends ItemSword {
    private DropProcessor dropProcessor;
    private LuckCrafting crafting;

    public ItemLuckySword() {
        super(Item.ToolMaterial.IRON);
        this.setMaxDamage(3124);
        this.dropProcessor = new DropProcessor();
        this.crafting = new LuckCrafting(this);
    }

    @Override
    public int getMaxItemUseDuration(ItemStack stack) {
        return 7200;
    }

    @Override
    public boolean hitEntity(
        ItemStack itemStack, EntityLivingBase target, EntityLivingBase attacker) {
        try {
            int luck = ItemLuckyBlock.getLuck(itemStack);
            String[] drops = ItemLuckyBlock.getDrops(itemStack);

            if (drops != null && drops.length != 0)
                this.getDropProcessor()
                    .processRandomDrop(
                        LuckyFunction.getDropsFromStringArray(drops),
                        new DropProcessData(attacker.getEntityWorld(), attacker, target.getPositionVector())
                            .setHitEntity(target),
                        luck);
            else
                this.getDropProcessor()
                    .processRandomDrop(
                        new DropProcessData(attacker.getEntityWorld(), attacker, target.getPositionVector())
                            .setHitEntity(target),
                        luck);
        } catch (Exception e) {
            System.err.println(
                "The Lucky Sword encountered and error while trying to perform a function. Error report below:");
            e.printStackTrace();
        }

        return super.hitEntity(itemStack, target, attacker);
    }

    public DropProcessor getDropProcessor() {
        return this.dropProcessor;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean hasEffect(ItemStack stack) {
        return true;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(
        ItemStack stack, @Nullable World playerIn, List<String> tooltip, ITooltipFlag advanced) {
        int luck = ItemLuckyBlock.getLuck(stack);
        String[] drops = ItemLuckyBlock.getDrops(stack);
        tooltip.add(
            I18n.translateToLocal("item.luckyBlock.luck")
                + ": "
                + (luck == 0
                ? TextFormatting.GOLD
                : (luck < 0 ? TextFormatting.RED : TextFormatting.GREEN + "+"))
                + String.valueOf(luck));
        if (drops != null && drops.length != 0)
            tooltip.add(
                TextFormatting.GRAY
                    + ""
                    + TextFormatting.ITALIC
                    + I18n.translateToLocal("item.luckyBlock.customDrop"));
    }

    public LuckCrafting getCrafting() {
        return this.crafting;
    }
}
