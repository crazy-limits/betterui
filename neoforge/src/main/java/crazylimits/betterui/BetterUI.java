package crazylimits.betterui;

import crazylimits.betterui.data.ModAttachments;
import crazylimits.betterui.menus.BetterInventoryMenu;
import crazylimits.betterui.network.BetterUINetwork;
import crazylimits.betterui.replacement.ReplacementRegistry;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(Constants.MOD_ID)
public class BetterUI {

    public BetterUI(IEventBus modBus) {
        Constants.LOG.info("Hello NeoForge world!");
        CommonClass.init();

        BetterUIMenuTypes.register(modBus);

        ModAttachments.ATTACHMENT_TYPES.register(modBus);

        modBus.addListener(BetterUINetwork::registerPayloads);

        ReplacementRegistry.register(
                BetterUIMenuTypes.INVENTORY.getId(),
                Component.empty(),
                BetterInventoryMenu::new,
                InventoryScreen.class
        );
    }
}