package mod.lucky.render;

import mod.lucky.Lucky;
import mod.lucky.entity.EntityLuckyProjectile;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;

public class RenderLuckyProjectile extends EntityRenderer<EntityLuckyProjectile> {
    public RenderLuckyProjectile(EntityRendererManager renderManager) {
        super(renderManager);
    }

    @Override
    public void doRender(EntityLuckyProjectile entity,
        double posX, double posY, double posZ,
        float unknownValue, float partialTicks) {

        try {
            if (entity.getItemEntity() != null) {
                this.renderManager.renderEntity(entity.getItemEntity(),
                    posX, posY - 0.35D, posZ,
                    0, partialTicks, true);
            }
        } catch (Exception e) {
            Lucky.error(e, "Error rendering lucky projectile");
        }
    }

    @Nullable
    @Override
    protected ResourceLocation getEntityTexture(EntityLuckyProjectile entity) {
        return null;
    }
}
