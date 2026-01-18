package crazylimits.betterui.replacement;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.SimpleMenuProvider;

import java.util.HashMap;
import java.util.Map;

public final class ServerReplacementRegistry {

    @FunctionalInterface
    public interface MenuFactory {
        AbstractContainerMenu create(
                int containerId,
                Inventory inv
        );
    }

    private static final Map<ResourceLocation, MenuFactory> MENU_FACTORIES = new HashMap<>();
    private static final Map<ResourceLocation, Component> TITLES = new HashMap<>();

    private ServerReplacementRegistry() {}

    public static void register(
            ResourceLocation id,
            Component title,
            MenuFactory factory
    ) {
        if (MENU_FACTORIES.put(id, factory) != null) {
            throw new IllegalStateException("Duplicate menu replacement id: " + id);
        }
        TITLES.put(id, title);
    }

    public static void openOnServer(ResourceLocation id, ServerPlayer player) {
        MenuFactory factory = MENU_FACTORIES.get(id);
        if (factory == null) {
            return; // or log
        }

        Component title = TITLES.getOrDefault(id, Component.empty());

        player.openMenu(
                new SimpleMenuProvider(
                        (containerId, inv, p) ->
                                factory.create(containerId, inv),
                        title
                )
        );
    }
}