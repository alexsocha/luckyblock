package mod.lucky.client;

import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraftforge.fml.client.registry.IRenderFactory;

public class RenderFactoryLuckyProjectile implements IRenderFactory {
  @Override
  public Render createRenderFor(RenderManager manager) {
    return new RenderLuckyProjectile(manager);
  }
}
