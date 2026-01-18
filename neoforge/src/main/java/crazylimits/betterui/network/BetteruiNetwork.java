package crazylimits.betterui.network;

import crazylimits.betterui.replacement.ServerReplacementRegistry;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class BetteruiNetwork {

    private BetteruiNetwork() {}

    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        // Network protocol version for your mod, must match client/server
        PayloadRegistrar registrar = event.registrar("1");

        registrar.playToServer(
                OpenMenuReplacementPayload.TYPE,
                OpenMenuReplacementPayload.STREAM_CODEC,
                (payload, ctx) -> {
                    ctx.enqueueWork(() -> {
                        if (ctx.player() instanceof ServerPlayer player) {
                            ServerReplacementRegistry.openOnServer(payload.id(), player);
                        }
                    });
                }
        );
    }
}