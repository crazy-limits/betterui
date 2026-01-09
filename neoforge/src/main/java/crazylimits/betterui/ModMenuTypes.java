package crazylimits.betterui;

import com.lowdragmc.lowdraglib2.gui.holder.ModularUIContainerMenu;
import crazylimits.betterui.menus.BetterInventoryMenu;
import crazylimits.betterui.menus.SimpleInventoryMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.network.IContainerFactory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(Registries.MENU, Constants.MOD_ID);

    public static final DeferredHolder<MenuType<?>, MenuType<ModularUIContainerMenu>> BETTER_INVENTORY_MENU = registerMenuType("better_inventory_menu", BetterInventoryMenu::new);
    public static final DeferredHolder<MenuType<?>, MenuType<SimpleInventoryMenu>> SIMPLE_INVENTORY_MENU = registerMenuType("simple_inventory_menu", SimpleInventoryMenu::new);

    public static <T extends AbstractContainerMenu>DeferredHolder<MenuType<?>, MenuType<T>> registerMenuType(String name, IContainerFactory<T> factory) {
        return MENU_TYPES.register(name, () -> IMenuTypeExtension.create(factory));
    }

    public static void register(IEventBus eventBus) {
        MENU_TYPES.register(eventBus);
    }
}
