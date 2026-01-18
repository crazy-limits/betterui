package crazylimits.betterui.components;

import com.lowdragmc.lowdraglib2.configurator.annotation.Configurable;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ItemSlot;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvent;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvents;
import com.lowdragmc.lowdraglib2.integration.kjs.KJSBindings;
import com.lowdragmc.lowdraglib2.registry.annotation.LDLRegister;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.inventory.*;
import org.appliedenergistics.yoga.YogaFlexDirection;

import java.util.ArrayList;
import java.util.Map;
import java.util.function.Consumer;

@KJSBindings
@LDLRegister(name = "crafting-slots", group = "inventory", registry = "ldlib2:ui_element")
public class CraftingSlots extends UIElement {
    public CraftingContainer craftSlots;
    public ArrayList<Row> rows = new ArrayList<>();

    @Configurable
    public int gridWidth = 2;
    @Configurable
    public int gridHeight = 2;

    public CraftingSlots() {
        getLayout().setFlexDirection(YogaFlexDirection.COLUMN);
        for (int i = 0; i < gridHeight; i++) {
            var row = new CraftingSlots.Row(gridWidth);
            row.slots.get(i).setId("crafting_slot_%s".formatted(i * gridHeight));
            rows.add(row);
            addChild(row);
        }

        addEventListener(UIEvents.MUI_CHANGED, this::onModularUIChanged);
        internalSetup();
    }

    protected void onModularUIChanged(UIEvent event) {
        var mui = getModularUI();
        if (mui != null && event.customData != mui) {
            var menu = mui.getMenu();
            if (menu != null) {
                craftSlots = new TransientCraftingContainer(menu, gridWidth, gridHeight);
                for (int i = 0; i < gridHeight; i++) {
                    for (int j = 0; j < gridWidth; j++) {
                        var slot = rows.get(i).slots.get(j);
                        slot.bind(new Slot(craftSlots, i + j * gridHeight, 0, 0));
                    }
                }
            }
        }
    }

    public CraftingSlots apply(Consumer<ItemSlot> consumer) {
        for (Row row : rows)
            row.apply(consumer);
        return this;
    }

    public static class Row extends UIElement {
        public final ArrayList<ItemSlot> slots = new ArrayList<>();

        public Row(int size) {
            getLayout().setFlexDirection(YogaFlexDirection.ROW);
            addClass("__crafting_row__");

            for (int i = 0; i < size; i++) {
                var slot = new ItemSlot().slotStyle(slotStyle -> slotStyle.isPlayerSlot(true));
                slots.add(slot);
                addChild(slot);
            }
        }

        public CraftingSlots.Row apply(Consumer<ItemSlot> consumer) {
            for (ItemSlot slot : slots) {
                consumer.accept(slot);
            }
            return this;
        }
    }
}
