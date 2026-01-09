package crazylimits.betterui.replacement;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.client.gui.screens.inventory.MenuAccess;

public interface ClientMenuReplacement<
        M extends AbstractContainerMenu,
        S extends Screen & MenuAccess<M>
        > {

    /**
     * Same id as server replacement; used for payload + registry lookup.
     */
    ResourceLocation id();

    /**
     * Vanilla screen class to intercept (e.g. InventoryScreen.class).
     */
    Class<? extends Screen> vanillaScreen();

    /**
     * Condition to decide if we replace (e.g. skip creative).
     */
    default boolean shouldReplace(Minecraft mc) {
        return true;
    }

    /**
     * Your custom MenuType (used to bind MenuType → Screen in
     * RegisterMenuScreensEvent).
     */
    MenuType<M> menuType();

    /**
     * Screen factory for RegisterMenuScreensEvent.
     */
    S createScreen(M menu, Inventory inv, Component title);
}