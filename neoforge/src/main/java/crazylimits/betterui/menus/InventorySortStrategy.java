package crazylimits.betterui.menus;

import net.minecraft.world.item.ItemStack;

import java.util.List;

public interface InventorySortStrategy {
    /**
     * Sort non-empty stacks into final grid (slotCount entries).
     * Empty slots must be represented by ItemStack.EMPTY in the result.
     */
    List<ItemStack> sort(List<ItemStack> nonEmpty, int slotCount, int width);
}