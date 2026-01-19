package crazylimits.betterui.menus;

import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class InventorySortStrategyRow implements InventorySortStrategy {

    @Override
    public List<ItemStack> sort(List<ItemStack> nonEmpty, int slotCount, int width) {
        if (slotCount <= 0) return Collections.emptyList();

        List<ItemStack> sorted = new ArrayList<>(nonEmpty);
        sorted.sort(InventorySortHelper::compareStacks);

        ItemStack[] result = new ItemStack[slotCount];
        Arrays.fill(result, ItemStack.EMPTY);

        int i = 0;
        for (ItemStack stack : sorted) {
            if (i >= slotCount) break;
            result[i++] = stack;
        }
        return Arrays.asList(result);
    }
}