package mod.lucky.client;

import mod.lucky.entity.EntityLuckyProjectile;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

public class RenderLuckyProjectile extends Render {
    protected RenderLuckyProjectile(RenderManager renderManager) {
        super(renderManager);
    }

    @Override
    public void doRender(
        Entity entity,
        double posX,
        double posY,
        double posZ,
        float unknownValue,
        float partialTicks) {
        try {
            if (entity instanceof EntityLuckyProjectile) {
                EntityLuckyProjectile luckyProjectile = (EntityLuckyProjectile) entity;
                if (luckyProjectile.getItem() != null)
                    this.renderManager.renderEntity(
                        luckyProjectile.getItem(), posX, posY - 0.35D, posZ, 0, partialTicks, true);
            }
        } catch (Exception e) {
            System.err.println("Lucky Block: Error rendering lucky projectile");
            e.printStackTrace();
        }
    }

    @Override
    protected ResourceLocation getEntityTexture(Entity entity) {
        return null;
    }
}
