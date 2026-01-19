package crazylimits.betterui.network;

import crazylimits.betterui.replacement.ServerReplacementRegistry;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class BetterUINetwork {

    private BetterUINetwork() {}

    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        // protocol version
        PayloadRegistrar registrar = event.registrar("1");

        // existing
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

        // NEW: trash delete
        registrar.playToServer(
                TrashDeletePayload.TYPE,
                TrashDeletePayload.STREAM_CODEC,
                (payload, ctx) -> {
                    ctx.enqueueWork(() -> {
                        if (ctx.player() instanceof ServerPlayer player) {
                            TrashDeletePayload.handle(payload, player);
                        }
                    });
                }
        );
    }
}