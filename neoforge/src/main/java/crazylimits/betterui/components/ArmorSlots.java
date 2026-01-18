package crazylimits.betterui.components;

import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ItemSlot;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvent;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvents;
import com.lowdragmc.lowdraglib2.integration.kjs.KJSBindings;
import com.lowdragmc.lowdraglib2.registry.annotation.LDLRegister;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.inventory.ArmorSlot;
import net.minecraft.world.inventory.InventoryMenu;
import org.appliedenergistics.yoga.YogaFlexDirection;

import java.util.Map;
import java.util.function.Consumer;

@KJSBindings
@LDLRegister(name = "armor-slots", group = "inventory", registry = "ldlib2:ui_element")
public class ArmorSlots extends UIElement {
    public static final Map<EquipmentSlot, ResourceLocation> TEXTURE_EMPTY_SLOTS = Map.of(EquipmentSlot.FEET, InventoryMenu.EMPTY_ARMOR_SLOT_BOOTS, EquipmentSlot.LEGS, InventoryMenu.EMPTY_ARMOR_SLOT_LEGGINGS, EquipmentSlot.CHEST, InventoryMenu.EMPTY_ARMOR_SLOT_CHESTPLATE, EquipmentSlot.HEAD, InventoryMenu.EMPTY_ARMOR_SLOT_HELMET);
    private static final EquipmentSlot[] SLOT_IDS = new EquipmentSlot[]{
            EquipmentSlot.HEAD,
            EquipmentSlot.CHEST,
            EquipmentSlot.LEGS,
            EquipmentSlot.FEET
    };
    public Col col;

    public ArmorSlots() {
        col = new ArmorSlots.Col();
        addChild(col);

        for (int i = 0; i < col.slots.length; i++) {
            var equipmentslot = SLOT_IDS[i];
            col.slots[i].setId("slot_%s".formatted(equipmentslot.name()));
        }

        addEventListener(UIEvents.MUI_CHANGED, this::onModularUIChanged);
        internalSetup();
    }

    protected void onModularUIChanged(UIEvent event) {
        var mui = getModularUI();
        if (mui != null && event.customData != mui) {
            var menu = mui.getMenu();
            var player = mui.player;
            if (menu != null && player != null) {
                var inventory = player.getInventory();
                for (int i = 0; i < col.slots.length; i++) {
                    var slot = col.slots[i];
                    EquipmentSlot equipmentslot = SLOT_IDS[i];
                    ResourceLocation resourcelocation = TEXTURE_EMPTY_SLOTS.get(equipmentslot);
                    slot.bind(new ArmorSlot(inventory, player, equipmentslot, 36 + 3 - i, 0, 0, resourcelocation));
                }
            }
        }
    }

    public ArmorSlots apply(Consumer<ItemSlot> consumer) {
        col.apply(consumer);
        return this;
    }

    public static class Col extends UIElement {
        public final ItemSlot[] slots = new ItemSlot[SLOT_IDS.length];

        public Col() {
            getLayout().setFlexDirection(YogaFlexDirection.COLUMN);
            addClass("__armor_col__");

            for (int i = 0; i < slots.length; i++) {
                slots[i] = new ItemSlot().slotStyle(slotStyle -> slotStyle.isPlayerSlot(true));
                addChild(slots[i]);
            }
        }

        public ArmorSlots.Col apply(Consumer<ItemSlot> consumer) {
            for (ItemSlot slot : slots) {
                consumer.accept(slot);
            }
            return this;
        }
    }
}
