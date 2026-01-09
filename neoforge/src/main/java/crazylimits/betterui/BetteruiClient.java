package crazylimits.betterui;

import crazylimits.betterui.screens.SimpleInventoryScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@EventBusSubscriber(modid = Constants.MOD_ID, value = Dist.CLIENT)
public class BetteruiClient {

    @SubscribeEvent
    public static void registerMenuScreens(RegisterMenuScreensEvent event) {
        event.register(
                ModMenuTypes.SIMPLE_INVENTORY_MENU.get(),
                SimpleInventoryScreen::new
        );
    }
}