package crazylimits.betterui.replacement;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;

public interface ServerMenuReplacement<M extends AbstractContainerMenu> {

    ResourceLocation id();

    Component title();

    /**
     * Factory used on the server when opening the menu.
     */
    M createMenu(int containerId, Inventory inv, ServerPlayer player);
}