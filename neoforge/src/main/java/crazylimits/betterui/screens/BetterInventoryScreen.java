package crazylimits.betterui.screens;

import com.lowdragmc.lowdraglib2.gui.holder.ModularUIContainerMenu;
import com.lowdragmc.lowdraglib2.gui.holder.ModularUIContainerScreen;
import crazylimits.betterui.Constants;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class BetterInventoryScreen extends ModularUIContainerScreen {
    public static final ResourceLocation LOCATION = ResourceLocation.fromNamespaceAndPath(
        Constants.MOD_ID, "gui/inventory_screen.xml"
    );

    public BetterInventoryScreen(ModularUIContainerMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

//    public ModularUI createUI(Player player) {
//        var xml = XmlUtils.loadXml(BetterInventoryScreen.LOCATION);
//        var ui = UI.of(xml);
//
//        player.inventoryMenu.slots.forEach(slot -> {
//            var elements = ui.select("#slot_" + slot.index);
//            elements.forEach(element -> {
//                if (element instanceof ItemSlot itemSlot) {
//                    itemSlot.bind(slot);
//                }
//            });
//        });
//
//        return ModularUI.of(ui, player);
//    }
//
    @Override
    public void init() {
        if (this.minecraft.gameMode.hasInfiniteItems()) {
            this.minecraft.setScreen(new CreativeModeInventoryScreen(this.minecraft.player, this.minecraft.player.connection.enabledFeatures(), (Boolean)this.minecraft.options.operatorItemsTab().get()));
        } else {
            super.init();
        }
    }

//    @Override
//    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
//
//    }
//
//    @Override
//    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
//
//    }
}
