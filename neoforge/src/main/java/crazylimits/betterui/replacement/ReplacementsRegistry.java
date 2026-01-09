package crazylimits.betterui.replacement;

import crazylimits.betterui.network.OpenMenuReplacementPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;

public final class ReplacementsRegistry {

    private static final List<ServerMenuReplacement<?>> SERVER_REPLACEMENTS =
            new ArrayList<>();
    private static final List<ClientMenuReplacement<?, ?>> CLIENT_REPLACEMENTS =
            new ArrayList<>();

    private ReplacementsRegistry() {}

    // -------- SERVER SIDE --------

    public static void registerServer(ServerMenuReplacement<?> replacement) {
        SERVER_REPLACEMENTS.add(replacement);

        // Wire into existing MenuReplacementRegistry
        MenuReplacementRegistry.registerServerReplacement(
                replacement.id(),
                replacement.title(),
                (containerId, inv, player) ->
                        // unchecked but safe if you keep types consistent
                        ((ServerMenuReplacement) replacement)
                                .createMenu(containerId, inv, player)
        );
    }

    // -------- CLIENT SIDE --------

    public static void registerClient(ClientMenuReplacement<?, ?> replacement) {
        CLIENT_REPLACEMENTS.add(replacement);

        // Hook up ScreenReplacer so pressing E etc. sends payload
        crazylimits.betterui.ScreenReplacer.register(
                replacement.vanillaScreen(),
                screen -> {
                    Minecraft mc = Minecraft.getInstance();
                    if (!replacement.shouldReplace(mc)) {
                        return screen; // keep vanilla
                    }
                    if (mc.player == null) {
                        return screen;
                    }

                    PacketDistributor.sendToServer(
                            new OpenMenuReplacementPayload(replacement.id())
                    );

                    // Cancel vanilla screen; server will open menu
                    return null;
                }
        );
    }

    /**
     * Called from BetteruiClient's RegisterMenuScreensEvent handler.
     */
    public static void registerMenuScreens(RegisterMenuScreensEvent event) {
        for (ClientMenuReplacement<?, ?> rep : CLIENT_REPLACEMENTS) {
            // Raw cast but types are aligned by design
            registerOneMenuScreen(event, rep);
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static <M extends net.minecraft.world.inventory.AbstractContainerMenu,
            S extends Screen & net.minecraft.client.gui.screens.inventory.MenuAccess<M>>
    void registerOneMenuScreen(
            RegisterMenuScreensEvent event,
            ClientMenuReplacement<M, S> rep
    ) {
        event.register(
                rep.menuType(),
                rep::createScreen
        );
    }
}