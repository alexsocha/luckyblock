package mod.lucky.client;

import mod.lucky.item.ItemLuckyBow;
import net.minecraftforge.client.event.FOVUpdateEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientEventHandler {
    @SubscribeEvent(priority = EventPriority.NORMAL, receiveCanceled = true)
    public void onEvent(FOVUpdateEvent event) {
        if (event.getEntity().isHandActive()
            && event.getEntity().getActiveItemStack().getItem() instanceof ItemLuckyBow) {

            int i = event.getEntity().getItemInUseMaxCount();
            float f1 = i / 20.0F;

            if (f1 > 1.0F) {
                f1 = 1.0F;
            } else {
                f1 = f1 * f1;
            }
            event.setNewfov(event.getFov() * 1.0F - f1 * 0.15F);
        }
    }
}
