package crazylimits.betterui;

import crazylimits.betterui.network.BetteruiNetwork;
import crazylimits.betterui.replacement.ClientMenuReplacementHelper;
import crazylimits.betterui.replacement.MenuReplacementRegistry;
import crazylimits.betterui.menus.SimpleInventoryMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(Constants.MOD_ID)
public class Betterui {

    public Betterui(IEventBus modBus) {
        Constants.LOG.info("Hello NeoForge world!");
        CommonClass.init();

        ModMenuTypes.register(modBus);

        // Networking: payloads
        modBus.addListener(BetteruiNetwork::registerPayloads);

        // Server-side registration: what to open when client asks for SIMPLE_INVENTORY
        MenuReplacementRegistry.registerServerReplacement(
                BetterUiIds.SIMPLE_INVENTORY,
                Component.translatable("container.crafting"),
                (containerId, inv, player) ->
                        new SimpleInventoryMenu(containerId, inv)
        );

        // Client-side: intercept vanilla InventoryScreen and ask server to open SIMPLE_INVENTORY
        ClientMenuReplacementHelper.replaceScreenWithServerMenu(
                InventoryScreen.class,
                BetterUiIds.SIMPLE_INVENTORY,
                mc -> mc.gameMode == null || !mc.gameMode.hasInfiniteItems() // skip creative
        );
    }
}