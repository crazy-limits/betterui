package crazylimits.betterui.replacements;

import crazylimits.betterui.Constants;
import crazylimits.betterui.menus.SimpleInventoryMenu;
import crazylimits.betterui.screens.SimpleInventoryScreen;
import crazylimits.betterui.replacement.ServerMenuReplacement;
import crazylimits.betterui.replacement.ClientMenuReplacement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;

public final class SurvivalInventoryReplacement {
    public static final ResourceLocation SIMPLE_INVENTORY =
            ResourceLocation.fromNamespaceAndPath(
                    Constants.MOD_ID,
                    "simple_inventory"
            );

    private SurvivalInventoryReplacement() {}

    // -------- SERVER SPEC --------

    public enum Server implements ServerMenuReplacement<SimpleInventoryMenu> {
        INSTANCE;

        @Override
        public net.minecraft.resources.ResourceLocation id() {
            return SIMPLE_INVENTORY;
        }

        @Override
        public Component title() {
            return Component.translatable("container.crafting");
        }

        @Override
        public SimpleInventoryMenu createMenu(
                int containerId,
                Inventory inv,
                ServerPlayer player
        ) {
            return new SimpleInventoryMenu(containerId, inv);
        }
    }

    // -------- CLIENT SPEC --------

    public enum Client
            implements ClientMenuReplacement<SimpleInventoryMenu, SimpleInventoryScreen> {
        INSTANCE;

        @Override
        public net.minecraft.resources.ResourceLocation id() {
            return SIMPLE_INVENTORY;
        }

        @Override
        public Class<? extends net.minecraft.client.gui.screens.Screen> vanillaScreen() {
            return InventoryScreen.class;
        }

        @Override
        public boolean shouldReplace(Minecraft mc) {
            // Skip creative inventory
            return mc.gameMode == null || !mc.gameMode.hasInfiniteItems();
        }

        @Override
        public MenuType<SimpleInventoryMenu> menuType() {
            return crazylimits.betterui.ModMenuTypes.SIMPLE_INVENTORY_MENU.get();
        }

        @Override
        public SimpleInventoryScreen createScreen(
                SimpleInventoryMenu menu,
                Inventory inv,
                Component title
        ) {
            return new SimpleInventoryScreen(menu, inv, title);
        }
    }
}