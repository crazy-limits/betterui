package crazylimits.betterui.components;

import com.lowdragmc.lowdraglib2.configurator.annotation.Configurable;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvent;
import com.lowdragmc.lowdraglib2.integration.kjs.KJSBindings;
import com.lowdragmc.lowdraglib2.registry.annotation.LDLRegister;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.inventory.ArmorSlot;
import net.minecraft.world.inventory.Slot;

@KJSBindings
@LDLRegister(name = "equipment-item-slot", group = "inventory", registry = "ldlib2:ui_element")
public class EquipmentItemSlot extends UIElement {
    @Configurable
    public String type;

    public Slot slot;

    public EquipmentItemSlot() {

    }

//    protected void onModularUIChanged(UIEvent event) {
//        var mui = getModularUI();
//        if (mui != null && event.customData != mui) {
//            var menu = mui.getMenu();
//            var player = mui.player;
//            if (menu != null && player != null) {
//                var inventory = player.getInventory();
//                EquipmentSlot.valueOf(type);
//                ResourceLocation resourcelocation = TEXTURE_EMPTY_SLOTS.get(equipmentslot);
//                slot.bind(new ArmorSlot(inventory, player, equipmentslot, 36 + 3 - i, 0, 0, resourcelocation));
//            }
//        }
//    }
}
