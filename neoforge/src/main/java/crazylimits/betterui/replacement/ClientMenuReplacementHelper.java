package crazylimits.betterui.replacement;

import crazylimits.betterui.ScreenReplacer;
import crazylimits.betterui.network.OpenMenuReplacementPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.function.Predicate;

public final class ClientMenuReplacementHelper {

    private ClientMenuReplacementHelper() {}

    public static <T extends Screen> void replaceScreenWithServerMenu(
            Class<T> vanillaScreen,
            ResourceLocation replacementId,
            Predicate<Minecraft> shouldReplace
    ) {
        ScreenReplacer.register(
                vanillaScreen,
                screen -> {
                    Minecraft mc = Minecraft.getInstance();
                    if (!shouldReplace.test(mc)) {
                        return screen; // keep vanilla
                    }
                    if (mc.player == null) {
                        return screen;
                    }

                    PacketDistributor.sendToServer(
                            new OpenMenuReplacementPayload(replacementId)
                    );

                    // cancel this vanilla screen; server will open our menu
                    return null;
                }
        );
    }

    public static <T extends Screen> void replaceScreenWithServerMenu(
            Class<T> vanillaScreen,
            ResourceLocation replacementId
    ) {
        replaceScreenWithServerMenu(vanillaScreen, replacementId, mc -> true);
    }
}