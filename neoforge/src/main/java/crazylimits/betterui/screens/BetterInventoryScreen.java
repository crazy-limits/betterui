package crazylimits.betterui.screens;

import com.lowdragmc.lowdraglib2.gui.holder.ModularUIContainerScreen;
import crazylimits.betterui.Constants;
import crazylimits.betterui.menus.BetterInventoryMenu;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

public class BetterInventoryScreen extends ModularUIContainerScreen {
    public static final ResourceLocation LOCATION = ResourceLocation.fromNamespaceAndPath(
        Constants.MOD_ID, "gui/inventory_screen.xml"
    );

    public BetterInventoryScreen(Player player) {
        super(new BetterInventoryMenu(player), player.getInventory(), Component.translatable("container.crafting"));
    }

    @Override
    public void init() {
        if (this.minecraft.gameMode.hasInfiniteItems()) {
            this.minecraft.setScreen(new CreativeModeInventoryScreen(this.minecraft.player, this.minecraft.player.connection.enabledFeatures(), (Boolean)this.minecraft.options.operatorItemsTab().get()));
        } else {
            super.init();
        }
    }
}
