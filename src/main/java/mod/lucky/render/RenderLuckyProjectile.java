package mod.lucky.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import mod.lucky.Lucky;
import mod.lucky.entity.EntityLuckyProjectile;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.entity.passive.fish.PufferfishEntity;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;

public class RenderLuckyProjectile extends EntityRenderer<EntityLuckyProjectile> {
    public RenderLuckyProjectile(EntityRendererManager renderManager) {
        super(renderManager);
    }

    // doRender
    @Override
    public void func_225623_a_(EntityLuckyProjectile entity, float f1, float f2, MatrixStack matrix, IRenderTypeBuffer renderType, int i1) {
        try {
            if (entity.getItemEntity() != null) {
                // doRender
                this.renderManager.func_229084_a_(
                    entity.getItemEntity(),
                    0, -0.35, 0, f1, f2,
                    matrix, renderType, i1);
            }
        } catch (Exception e) {
            Lucky.error(e, "Error rendering lucky projectile");
        }
    }

    @Nullable
    @Override
    public ResourceLocation getEntityTexture(EntityLuckyProjectile entity) {
        return null;
    }
}
