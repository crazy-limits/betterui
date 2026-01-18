package crazylimits.betterui;

import crazylimits.betterui.data.ModAttachments;
import crazylimits.betterui.menus.BetterInventoryMenu;
import crazylimits.betterui.network.BetteruiNetwork;
import crazylimits.betterui.replacement.ReplacementRegistry;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(Constants.MOD_ID)
public class Betterui {

    public Betterui(IEventBus modBus) {
        Constants.LOG.info("Hello NeoForge world!");
        CommonClass.init();

        BetteruiMenuTypes.register(modBus);

        ModAttachments.ATTACHMENT_TYPES.register(modBus);

        modBus.addListener(BetteruiNetwork::registerPayloads);

        ReplacementRegistry.register(
                BetteruiMenuTypes.INVENTORY.getId(),
                Component.empty(),
                BetterInventoryMenu::new,
                InventoryScreen.class
        );
    }
}