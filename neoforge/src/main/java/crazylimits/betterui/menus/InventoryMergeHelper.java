package crazylimits.betterui.menus;

import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public final class InventoryMergeHelper {

    private InventoryMergeHelper() {}

    /**
     * Merge stacks:
     * - Only moves counts between stacks, never deletes them.
     * - Uses InventorySortHelper.canMerge to check compatibility.
     * - Returns a compacted list of non-empty stacks.
     */
    public static List<ItemStack> mergeStacks(List<ItemStack> input) {
        if (input.isEmpty()) return List.of();

        // Work on copies so we never mutate caller's stacks directly.
        List<ItemStack> stacks = new ArrayList<>(input.size());
        for (ItemStack st : input) {
            stacks.add(st.copy());
        }

        // Forward-merge
        for (int i = 0; i < stacks.size(); i++) {
            ItemStack base = stacks.get(i);
            if (base.isEmpty() || !base.isStackable()) continue;

            int maxSize = base.getMaxStackSize();

            for (int j = i + 1; j < stacks.size(); j++) {
                ItemStack other = stacks.get(j);
                if (other.isEmpty() || !other.isStackable()) continue;
                if (!InventorySortHelper.canMerge(base, other)) continue;

                int space = maxSize - base.getCount();
                if (space <= 0) break;

                int toMove = Math.min(space, other.getCount());
                base.grow(toMove);
                other.shrink(toMove);

                if (other.isEmpty()) {
                    stacks.set(j, ItemStack.EMPTY);
                }

                if (base.getCount() >= maxSize) {
                    break;
                }
            }
        }

        // Compact non-empty
        List<ItemStack> compacted = new ArrayList<>();
        for (ItemStack st : stacks) {
            if (!st.isEmpty()) {
                compacted.add(st);
            }
        }
        return compacted;
    }
}