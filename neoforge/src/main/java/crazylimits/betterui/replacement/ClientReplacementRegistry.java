package crazylimits.betterui.replacement;

import crazylimits.betterui.Constants;
import crazylimits.betterui.network.OpenMenuReplacementPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

@EventBusSubscriber(
        modid = Constants.MOD_ID,
        value = Dist.CLIENT
)
public final class ClientReplacementRegistry {
    private static final Map<Class<? extends Screen>, Function<Screen, Screen>> SCREENS_MAP = new LinkedHashMap<>();

    private ClientReplacementRegistry() {}

    public static <T extends Screen> void register(
            Class<T> vanillaScreen,
            ResourceLocation replacementId
    ) {
        register(vanillaScreen, replacementId, mc -> true);
    }

    public static <T extends Screen> void register(
            Class<T> vanillaScreen,
            ResourceLocation replacementId,
            Predicate<Minecraft> shouldReplace
    ) {
        register(
                vanillaScreen,
                original -> {
                    Minecraft mc = Minecraft.getInstance();
                    if (!shouldReplace.test(mc)) {
                        return original; // keep vanilla
                    }
                    if (mc.player == null) {
                        return original;
                    }

                    PacketDistributor.sendToServer(
                            new OpenMenuReplacementPayload(replacementId)
                    );

                    // cancel this vanilla screen; server will open our menu
                    return null;
                }
        );
    }

    private static <T extends Screen> void register(
            Class<T> vanillaScreen,
            Function<T, Screen> factory
    ) {
        SCREENS_MAP.put(
                vanillaScreen,
                original -> factory.apply((T) original)
        );
    }

    @SubscribeEvent
    public static void onScreenOpening(ScreenEvent.Opening event) {
        Screen newScreen = event.getNewScreen();
        if (newScreen == null) return;

        for (Map.Entry<Class<? extends Screen>, Function<Screen, Screen>> entry : SCREENS_MAP.entrySet()) {

            Class<? extends Screen> keyClass = entry.getKey();

            if (keyClass.isInstance(newScreen)) {
                Screen replaced = entry.getValue().apply(newScreen);

                if (replaced == newScreen) {
                    // Keep vanilla screen, do nothing
                } else if (replaced == null) {
                    // Cancel opening this screen; we expect the server to open
                    // a menu shortly (e.g. via openMenu).
                    event.setNewScreen(null);
                } else {
                    // Replace vanilla with our custom screen right now
                    event.setNewScreen(replaced);
                }
                break;
            }
        }
    }
}