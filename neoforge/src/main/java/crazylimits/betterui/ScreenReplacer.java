package crazylimits.betterui;

import net.minecraft.client.gui.screens.Screen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

@EventBusSubscriber(
        modid = crazylimits.betterui.Constants.MOD_ID,
        value = Dist.CLIENT
)
public class ScreenReplacer {
    private static final Map<Class<? extends Screen>, Function<Screen, Screen>> REPLACERS =
            new LinkedHashMap<>();

    private ScreenReplacer() {}

    public static <T extends Screen> void register(
            Class<T> originalClass,
            Function<T, Screen> factory
    ) {
        REPLACERS.put(
                originalClass,
                (Function<Screen, Screen>) factory
        );
    }

    @SubscribeEvent
    public static void onScreenOpening(ScreenEvent.Opening event) {
        Screen newScreen = event.getNewScreen();
        if (newScreen == null) return;

        for (Map.Entry<Class<? extends Screen>, Function<Screen, Screen>> entry
                : REPLACERS.entrySet()) {

            Class<? extends Screen> keyClass = entry.getKey();

            if (keyClass.isInstance(newScreen)) {
                Screen replaced = entry.getValue().apply(newScreen);
                if (replaced != null && replaced != newScreen) {
                    event.setNewScreen(replaced);
                }
                break;
            }
        }
    }
}
