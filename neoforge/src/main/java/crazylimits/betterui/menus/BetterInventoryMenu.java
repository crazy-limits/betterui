package crazylimits.betterui.menus;

import com.lowdragmc.lowdraglib2.gui.factory.IContainerUIHolder;
import com.lowdragmc.lowdraglib2.gui.holder.ModularUIContainerMenu;
import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.UI;
import com.lowdragmc.lowdraglib2.utils.XmlUtils;
import crazylimits.betterui.ModMenuTypes;
import crazylimits.betterui.screens.BetterInventoryScreen;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;

public class BetterInventoryMenu extends ModularUIContainerMenu {
    public BetterInventoryMenu(int containerId, Inventory inventory) {
        super(ModMenuTypes.BETTER_INVENTORY_MENU.get(), containerId, inventory, new BetterInventoryHolderMenu());
    }

    public BetterInventoryMenu(int containerId, Inventory inventory, RegistryFriendlyByteBuf buf) {
        this(containerId, inventory);
    }

    public static class BetterInventoryHolderMenu implements IContainerUIHolder {
        public ModularUI createUI(Player player) {
            var xml = XmlUtils.loadXml(BetterInventoryScreen.LOCATION);
            var ui = UI.of(xml);
            return ModularUI.of(ui, player);
        }

        public boolean isStillValid(Player player) {
            return stillValid(
                    ContainerLevelAccess.NULL,
                    player,
                    null
            );
        }
    }
}
