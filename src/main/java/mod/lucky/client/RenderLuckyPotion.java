package mod.lucky.client;

import mod.lucky.Lucky;
import mod.lucky.entity.EntityLuckyPotion;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderSprite;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RenderLuckyPotion extends RenderSprite<EntityLuckyPotion> {

  public RenderLuckyPotion(RenderManager renderManager, ItemRenderer renderItem) {
    super(renderManager, Lucky.luckyPotion, renderItem);
  }

  private ItemStack getItemStack(EntityLuckyPotion entity) {
    ItemStack itemStack = entity.getItemStack();
    if (itemStack == null) itemStack = new ItemStack(Items.STICK, 1);
    return itemStack;
  }

  @Override
  public ItemStack getStackToRender(EntityLuckyPotion entity) {
    return this.getItemStack(entity);
  }
}
