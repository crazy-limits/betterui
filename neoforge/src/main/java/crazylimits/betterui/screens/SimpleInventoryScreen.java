package crazylimits.betterui.screens;

import crazylimits.betterui.menus.SimpleInventoryMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class SimpleInventoryScreen
        extends AbstractContainerScreen<SimpleInventoryMenu> {

    public static final ResourceLocation TEXTURE =
            AbstractContainerScreen.INVENTORY_LOCATION; // or your own

    public SimpleInventoryScreen(
            SimpleInventoryMenu menu,
            Inventory inventory,
            Component title
    ) {
        super(menu, inventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    public void init() {
        if (this.minecraft.gameMode.hasInfiniteItems()) {
            this.minecraft.setScreen(
                    new CreativeModeInventoryScreen(
                            this.minecraft.player,
                            this.minecraft.player.connection.enabledFeatures(),
                            (Boolean) this.minecraft.options.operatorItemsTab().get()
                    )
            );
        } else {
            super.init();
        }
    }

    @Override
    protected void renderBg(
            GuiGraphics guiGraphics,
            float partialTicks,
            int mouseX,
            int mouseY
    ) {
        guiGraphics.blit(
                TEXTURE,
                this.leftPos,
                this.topPos,
                0,
                0,
                this.imageWidth,
                this.imageHeight
        );
    }
}