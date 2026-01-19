package crazylimits.betterui.screens;

import com.lowdragmc.lowdraglib2.gui.holder.ModularUIContainerMenu;
import com.lowdragmc.lowdraglib2.gui.holder.ModularUIContainerScreen;
import com.mojang.blaze3d.platform.InputConstants;
import crazylimits.betterui.BetterUIClient;
import crazylimits.betterui.Constants;
import crazylimits.betterui.network.BetterUINetwork;
import crazylimits.betterui.network.TrashDeletePayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.neoforged.neoforge.network.PacketDistributor;

public class BetterInventoryScreen extends ModularUIContainerScreen {
    public static final ResourceLocation LOCATION = ResourceLocation.fromNamespaceAndPath(
        Constants.MOD_ID, "gui/inventory_screen.xml"
    );

    public BetterInventoryScreen(ModularUIContainerMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    protected void renderSlot(GuiGraphics pGuiGraphics, Slot pSlot) {
    }

    @Override
    protected void renderSlotHighlight(GuiGraphics guiGraphics, Slot slot, int mouseX, int mouseY, float partialTick) {
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // Check our Delete key mapping explicitly (NeoForge docs style)
        var key = BetterUIClient.TRASH_HOTKEY.get();
        if (key.matches(keyCode, scanCode)) {
            Slot hovered = this.getSlotUnderMouse();
            if (hovered != null && hovered.hasItem()) {
                int containerId = this.getMenu().containerId;
                int slotIndex = hovered.index;

                PacketDistributor.sendToServer(
                        new TrashDeletePayload(containerId, slotIndex)
                );
            }

            // We handled this key
            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
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
