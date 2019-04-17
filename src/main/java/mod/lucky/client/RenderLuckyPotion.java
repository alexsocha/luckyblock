package mod.lucky.client;

import mod.lucky.Lucky;
import mod.lucky.entity.EntityLuckyPotion;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderSnowball;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderLuckyPotion extends RenderSnowball<EntityLuckyPotion> {
  private RenderItem renderItem;

  public RenderLuckyPotion(RenderManager renderManager, RenderItem renderItem) {
    super(renderManager, Lucky.lucky_potion, renderItem);
    this.renderItem = renderItem;
  }

  public ItemStack getItemStack(EntityLuckyPotion entity) {
    ItemStack itemStack = entity.getItemLuckyPotion();
    if (itemStack == null) itemStack = new ItemStack(Items.STICK, 1);
    return itemStack;
  }

  @Override
  public ItemStack getStackToRender(EntityLuckyPotion entity) {
    return this.getItemStack(entity);
  }
}
