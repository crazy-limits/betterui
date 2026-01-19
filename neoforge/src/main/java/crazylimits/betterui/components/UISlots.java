package crazylimits.betterui.components;

import com.lowdragmc.lowdraglib2.configurator.annotation.Configurable;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.integration.kjs.KJSBindings;
import com.lowdragmc.lowdraglib2.registry.annotation.LDLRegister;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.item.ItemStack;
import org.appliedenergistics.yoga.YogaFlexDirection;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@KJSBindings
@LDLRegister(name = "slots", group = "inventory", registry = "ldlib2:ui_element")
public class UISlots extends UIElement {
    @Configurable
    public Integer columns = 0;

    @Configurable
    public Integer rows = 0;

    /**
     * If group is set, values in 'indexes' are group-local offsets.
     * If group is null/blank, 'indexes' are absolute indices in menu.slots.
     */
    @Configurable
    public String group;

    @Configurable
    public final List<Integer> indexes = new ArrayList<>();

    public final List<Row> rowsList = new ArrayList<>();

    public UISlots() {
        getLayout().setFlexDirection(YogaFlexDirection.COLUMN);
        internalSetup();
    }

    private void clearGrid() {
        rowsList.clear();
        clearAllChildren();
    }

    private void buildGrid() {
        clearGrid();
        if (columns == null || rows == null || columns <= 0 || rows <= 0) {
            return;
        }

        int size = rows * columns;
        while (indexes.size() < size) {
            indexes.add(0);
        }

        for (int r = 0; r < rows; r++) {
            int start = r * columns;
            int end = start + columns;
            List<Integer> rowIndexes = new ArrayList<>(indexes.subList(start, end));
            Row row = new Row(columns, rowIndexes, group);
            rowsList.add(row);
            addChild(row);
        }
    }

    public static List<Integer> parseIndexes(String indexes) {
        List<Integer> result = new ArrayList<>();
        if (indexes == null || indexes.isBlank()) {
            return result;
        }

        String[] parts = indexes.split(",");
        for (String part : parts) {
            part = part.trim();
            if (part.isEmpty()) continue;

            if (part.contains("-")) {
                String[] rangeParts = part.split("-");
                if (rangeParts.length != 2) continue;

                try {
                    int start = Integer.parseInt(rangeParts[0].trim());
                    int end = Integer.parseInt(rangeParts[1].trim());
                    if (start > end) {
                        int tmp = start;
                        start = end;
                        end = tmp;
                    }
                    for (int i = start; i <= end; i++) {
                        result.add(i);
                    }
                } catch (NumberFormatException ignored) {
                }
            } else {
                try {
                    int idx = Integer.parseInt(part);
                    result.add(idx);
                } catch (NumberFormatException ignored) {
                }
            }
        }

        return result;
    }

    public UISlots apply(Consumer<UISlot> consumer) {
        rowsList.forEach(row -> row.apply(consumer));
        return this;
    }

    public void setIndexes(String value) {
        indexes.clear();
        indexes.addAll(parseIndexes(value));
    }

    public void setColumns(int columns) {
        this.columns = columns;
    }

    public void setRows(int rows) {
        this.rows = rows;
    }

    @Override
    public void loadXml(Element element) {
        if (element.hasAttribute("indexes")) {
            setIndexes(element.getAttribute("indexes"));
        }
        if (element.hasAttribute("columns")) {
            setColumns(Integer.parseInt(element.getAttribute("columns")));
        }
        if (element.hasAttribute("rows")) {
            setRows(Integer.parseInt(element.getAttribute("rows")));
        }
        if (element.hasAttribute("group")) {
            group = element.getAttribute("group");
        }
        super.loadXml(element);
        buildGrid();
    }

    public static class Row extends UIElement {
        public final List<UISlot> slots = new ArrayList<>();
        private final List<Integer> indexes;
        private final String groupName;

        public Row(int columns, List<Integer> indexes, String groupName) {
            this.indexes = indexes;
            this.groupName = groupName;

            getLayout().setFlexDirection(YogaFlexDirection.ROW);
            addClass("__slots_row__");

            for (int i = 0; i < columns; i++) {
                var slot = new UISlot();
                slot.index = indexes.get(i);
                slot.group = groupName;
                slots.add(slot);
                addChild(slot);
            }
        }

        public UISlots.Row apply(Consumer<UISlot> consumer) {
            for (UISlot slot : slots) {
                consumer.accept(slot);
            }
            return this;
        }
    }
}