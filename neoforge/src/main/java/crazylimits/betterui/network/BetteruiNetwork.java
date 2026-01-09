package crazylimits.betterui.network;

import crazylimits.betterui.Constants;
import crazylimits.betterui.menus.SimpleInventoryMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = Constants.MOD_ID)
public class BetteruiNetwork {

    @SubscribeEvent
    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(Constants.MOD_ID)
                .versioned("1"); // your protocol version

        registrar.playToServer(
                OpenSimpleInventoryPayload.TYPE,
                OpenSimpleInventoryPayload.STREAM_CODEC,
                (payload, ctx) -> {
                    // enqueueWork is the correct way to switch to the main server thread
                    ctx.enqueueWork(() -> {
                        if (!(ctx.player() instanceof ServerPlayer player)) {
                            return;
                        }

                        player.openMenu(
                                new SimpleMenuProvider(
                                        (containerId, inv, p) ->
                                                new SimpleInventoryMenu(containerId, inv),
                                        Component.translatable("container.crafting")
                                )
                        );
                    });
                }
        );
    }
}