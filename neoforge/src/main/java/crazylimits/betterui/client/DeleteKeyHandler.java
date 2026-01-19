package crazylimits.betterui.client;

import crazylimits.betterui.BetterUIClient;
import crazylimits.betterui.Constants;
import crazylimits.betterui.network.TrashDeletePayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(value = Dist.CLIENT)
public class DeleteKeyHandler {

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        var mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        // Only act when a container screen is open
        if (!(mc.screen instanceof AbstractContainerScreen<?> screen)) {
            return;
        }

        // Check our Delete key mapping each tick
        while (BetterUIClient.TRASH_HOTKEY.get().consumeClick()) {
            var hovered = screen.getSlotUnderMouse();
            if (hovered == null || !hovered.hasItem()) continue;

            int containerId = screen.getMenu().containerId;
            int slotIndex = hovered.index;

            PacketDistributor.sendToServer(
                    new TrashDeletePayload(containerId, slotIndex)
            );
        }
    }
}