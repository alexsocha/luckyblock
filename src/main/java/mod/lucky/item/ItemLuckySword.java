package mod.lucky.item;

import mod.lucky.Lucky;
import mod.lucky.drop.func.DropProcessData;
import mod.lucky.drop.func.DropProcessor;
import mod.lucky.util.LuckyUtils;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.*;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;

public class ItemLuckySword extends SwordItem implements ILuckyItemContainer {
    private LuckyItem luckyItem = new LuckyItem(this);

    public ItemLuckySword() {
        super(ItemTier.IRON, 3, -2.4f, new Item.Properties()
            .defaultMaxDamage(3124)
            .group(ItemGroup.COMBAT));
    }

    @Override
    public LuckyItem getLuckyItem() { return this.luckyItem; }

    @Override
    public int getUseDuration(ItemStack stack) { return 7200; }

    @Override
    public boolean hitEntity(
        ItemStack stack, LivingEntity target, LivingEntity attacker) {

        try {
            int luck = LuckyItem.getLuck(stack);
            String[] drops = LuckyItem.getRawDrops(stack);

            DropProcessData dropData = new DropProcessData(
                attacker.getEntityWorld(),
                attacker,
                target.getPositionVec())
                    .setHitEntity(target);

            if (drops != null && drops.length != 0)
                this.getLuckyItem().getDropProcessor().processRandomDrop(
                    LuckyUtils.dropsFromStrArray(drops), dropData, luck);
            else this.getLuckyItem().getDropProcessor().processRandomDrop(dropData, luck);

        } catch (Exception e) {
            Lucky.error(e, DropProcessor.errorMessage());
        }

        return super.hitEntity(stack, target, attacker);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean hasEffect(ItemStack stack) { return true; }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn,
        List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        this.luckyItem.addLuckyTooltip(stack, worldIn, tooltip, flagIn);
    }
}
