package crazylimits.betterui.menus;

import net.minecraft.world.item.ItemStack;

import java.util.*;

public class InventorySortStrategyCluster implements InventorySortStrategy {

    // 2) Build group objects
    static class Group {
        final String key;
        final List<ItemStack> stacks;

        Group(String key, List<ItemStack> stacks) {
            this.key = key;
            this.stacks = stacks;
        }

        int size() {
            return stacks.size();
        }
    }

    @Override
    public List<ItemStack> sort(List<ItemStack> nonEmpty, int slotCount, int width) {
        if (slotCount <= 0) return Collections.emptyList();
        if (nonEmpty.isEmpty()) {
            ItemStack[] empty = new ItemStack[slotCount];
            Arrays.fill(empty, ItemStack.EMPTY);
            return Arrays.asList(empty);
        }

        int rows = Math.max(1, slotCount / Math.max(1, width));

        // 1) Group by absoluteKey
        Map<String, List<ItemStack>> groupsMap = new HashMap<>();
        for (ItemStack stack : nonEmpty) {
            String key = InventorySortHelper.absoluteKey(stack);
            groupsMap.computeIfAbsent(key, k -> new ArrayList<>()).add(stack.copy());
        }

        List<Group> groups = new ArrayList<>();
        for (Map.Entry<String, List<ItemStack>> e : groupsMap.entrySet()) {
            List<ItemStack> list = e.getValue();
            list.sort(InventorySortHelper::compareStacks);
            groups.add(new Group(e.getKey(), list));
        }

        // 3) Sort groups: bigger first, then by representative
        groups.sort((g1, g2) -> {
            int c = Integer.compare(g2.size(), g1.size());
            if (c != 0) return c;
            return InventorySortHelper.compareStacks(g1.stacks.get(0), g2.stacks.get(0));
        });

        // 4) Prepare 1D grid (row-major)
        ItemStack[] grid = new ItemStack[slotCount];
        Arrays.fill(grid, ItemStack.EMPTY);

        int pos = 0;

        for (Group group : groups) {
            int size = group.size();
            if (size <= 0) continue;

            int blobWidth = Math.min(width, (int) Math.ceil(Math.sqrt(size)));
            int blobHeight = (int) Math.ceil((double) size / blobWidth);

            pos = nextFreePos(grid, pos);
            if (pos >= slotCount) break;

            int startRow = pos / width;
            int startCol = pos % width;

            // If not enough horizontal room, start next row
            if (startCol + blobWidth > width) {
                startRow++;
                if (startRow >= rows) {
                    placeGroupRowMajor(group, grid, slotCount, pos);
                    break;
                }
                pos = startRow * width;
                startCol = 0;
            }

            // If not enough vertical room, try full width or fallback
            if (startRow + blobHeight > rows) {
                blobWidth = width;
                blobHeight = (int) Math.ceil((double) size / blobWidth);
                if (startRow + blobHeight > rows) {
                    placeGroupRowMajor(group, grid, slotCount, pos);
                    pos = nextFreePos(grid, pos);
                    continue;
                }
            }

            int placed = 0;
            for (int r = 0; r < blobHeight && placed < size; r++) {
                for (int c = 0; c < blobWidth && placed < size; c++) {
                    int idx = (startRow + r) * width + (startCol + c);
                    if (idx >= slotCount) break;

                    if (!grid[idx].isEmpty()) {
                        int free = findFreeInRect(
                                grid,
                                width,
                                slotCount,
                                startRow,
                                startCol,
                                blobWidth,
                                blobHeight
                        );
                        if (free == -1) {
                            placeGroupRowMajorRemaining(
                                    group,
                                    placed,
                                    grid,
                                    slotCount,
                                    pos
                            );
                            placed = size;
                            break;
                        } else {
                            idx = free;
                        }
                    }

                    grid[idx] = group.stacks.get(placed++);
                }
            }

            pos = nextFreePos(grid, pos);
        }

        return Arrays.asList(grid);
    }

    private int nextFreePos(ItemStack[] grid, int startPos) {
        int p = startPos;
        while (p < grid.length && !grid[p].isEmpty()) {
            p++;
        }
        return p;
    }

    private int findFreeInRect(ItemStack[] grid,
                               int width,
                               int slotCount,
                               int startRow,
                               int startCol,
                               int blobWidth,
                               int blobHeight) {
        for (int r = 0; r < blobHeight; r++) {
            for (int c = 0; c < blobWidth; c++) {
                int idx = (startRow + r) * width + (startCol + c);
                if (idx >= slotCount) return -1;
                if (grid[idx].isEmpty()) return idx;
            }
        }
        return -1;
    }

    private void placeGroupRowMajor(Group group,
                                    ItemStack[] grid,
                                    int slotCount,
                                    int startPos) {
        placeGroupRowMajorRemaining(group, 0, grid, slotCount, startPos);
    }

    private void placeGroupRowMajorRemaining(Group group,
                                             int startIndexInGroup,
                                             ItemStack[] grid,
                                             int slotCount,
                                             int startPos) {
        int pos = startPos;
        for (int i = startIndexInGroup; i < group.size(); i++) {
            while (pos < slotCount && !grid[pos].isEmpty()) {
                pos++;
            }
            if (pos >= slotCount) return;
            grid[pos] = group.stacks.get(i);
            pos++;
        }
    }
}