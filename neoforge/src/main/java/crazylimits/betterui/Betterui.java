package crazylimits.betterui;

import crazylimits.betterui.network.OpenSimpleInventoryPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(Constants.MOD_ID)
public class Betterui {

    public Betterui(IEventBus eventBus) {
        Constants.LOG.info("Hello NeoForge world!");
        CommonClass.init();

        ModMenuTypes.register(eventBus);

        ScreenReplacer.register(
                InventoryScreen.class,
                original -> {
                    Minecraft mc = Minecraft.getInstance();
                    if (mc.player == null) return original;

                    // Keep creative inventory unchanged
                    if (mc.gameMode != null && mc.gameMode.hasInfiniteItems()) {
                        return original;
                    }

                    // Ask server to open our custom inventory menu
                    PacketDistributor.sendToServer(new OpenSimpleInventoryPayload());

                    // Return null => ScreenReplacer cancels opening vanilla screen
                    // The server will shortly respond with openMenu, which opens our screen.
                    return null;
                }
        );
    }
}