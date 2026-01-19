package crazylimits.betterui.menus;

import net.minecraft.core.component.DataComponentHolder;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class InventorySortHelper {

    private InventorySortHelper() {}

    public static String absoluteKey(ItemStack stack) {
        String base = registryName(stack.getItem());
        String components = componentsSignature(stack);
        return base + "||" + components;
    }

    public static String tagSignature(ItemStack stack) {
        try {
            Stream<TagKey<Item>> stream = stack.getTags();
            List<String> tags = stream
                    .map(tk -> tk.location().toString())
                    .sorted()
                    .collect(Collectors.toList());
            if (tags.isEmpty()) return "";
            return String.join("|", tags);
        } catch (Throwable t) {
            return "";
        }
    }

    public static String safeDisplayName(ItemStack stack) {
        try {
            return stack.getHoverName().getString();
        } catch (Throwable t) {
            try {
                return stack.getDisplayName().getString();
            } catch (Throwable ignored) {
                return registryName(stack.getItem());
            }
        }
    }

    public static String registryName(Item item) {
        try {
            var key = BuiltInRegistries.ITEM.getKey(item);
            if (key != null) {
                return key.toString();
            }
        } catch (Throwable ignored) {
        }
        return item.getDescriptionId();
    }

    public static String componentsSignature(ItemStack stack) {
        DataComponentMap map = componentsOf(stack);
        if (map == null) return "";
        try {
            return map.toString();
        } catch (Throwable ignored) {
        }
        return "";
    }

    public static DataComponentMap componentsOf(ItemStack stack) {
        if (stack instanceof DataComponentHolder holder) {
            return holder.getComponents();
        }
        return null;
    }

    /**
     * Global comparator for sorting stacks in inventory.
     */
    public static int compareStacks(ItemStack a, ItemStack b) {
        String absA = absoluteKey(a);
        String absB = absoluteKey(b);
        int c = absA.compareTo(absB);
        if (c != 0) return c;

        String tagA = tagSignature(a);
        String tagB = tagSignature(b);
        c = tagA.compareTo(tagB);
        if (c != 0) return c;

        String nameA = safeDisplayName(a);
        String nameB = safeDisplayName(b);
        c = nameA.compareToIgnoreCase(nameB);
        if (c != 0) return c;

        String regA = registryName(a.getItem());
        String regB = registryName(b.getItem());
        return regA.compareTo(regB);
    }

    /**
     * Can two stacks be merged into one (same item + same components).
     */
    public static boolean canMerge(ItemStack a, ItemStack b) {
        if (a.isEmpty() || b.isEmpty()) return false;
        if (!a.isStackable() || !b.isStackable()) return false;

        try {
            // 1.21+ API name; adjust if your mappings differ
            return ItemStack.isSameItemSameComponents(a, b);
        } catch (Throwable ignored) {
        }

        if (a.getItem() != b.getItem()) return false;

        DataComponentMap ca = componentsOf(a);
        DataComponentMap cb = componentsOf(b);
        return Objects.equals(ca, cb);
    }
}