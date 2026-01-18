package crazylimits.betterui.replacement;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public final class ReplacementRegistry {

    private ReplacementRegistry() {}

    public static void register(
            ResourceLocation id,
            Component title,
            ServerReplacementRegistry.MenuFactory factory,
            Class<? extends Screen> screenToReplace
    ) {
        ServerReplacementRegistry.register(id, title, factory);
        ClientReplacementRegistry.register(screenToReplace, id);
    }
}