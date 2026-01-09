package crazylimits.betterui;

import net.minecraft.client.gui.screens.Screen;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class ScreenReplacerOld {
    public static Map<Class<? extends Screen>, Function<Screen, Screen>> SCREEN_REPLACEMENTS = new HashMap<>();

    public static <T extends Screen> void register(
            Class<T> original,
            Function<T, Screen> factory
    ) {
        // Erasure-safe cast; we only call this with matching T
        SCREEN_REPLACEMENTS.put(original, (Function<Screen, Screen>) factory);
    }

    public static Screen getReplacement(Screen original) {
        if (original == null) {
            return null;
        }
        var factory = SCREEN_REPLACEMENTS.get(original.getClass());
        if (factory == null) {
            return original;
        }
        return factory.apply(original);
    }
}
