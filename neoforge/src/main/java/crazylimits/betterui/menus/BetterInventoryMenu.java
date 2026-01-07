package crazylimits.betterui.menus;

import com.lowdragmc.lowdraglib2.gui.factory.IContainerUIHolder;
import com.lowdragmc.lowdraglib2.gui.holder.ModularUIContainerMenu;
import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.UI;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ItemSlot;
import com.lowdragmc.lowdraglib2.utils.XmlUtils;
import crazylimits.betterui.Constants;
import net.minecraft.world.entity.player.Player;

import crazylimits.betterui.screens.BetterInventoryScreen;
import net.minecraft.world.inventory.Slot;

import java.util.HashMap;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BetterInventoryMenu extends ModularUIContainerMenu {
    public BetterInventoryMenu(Player player) {
        super(null, 0, player.getInventory(), new BetterInventoryHolderMenu());
    }

    public static class BetterInventoryHolderMenu implements IContainerUIHolder {
        public ModularUI createUI(Player player) {
            var xml = XmlUtils.loadXml(BetterInventoryScreen.LOCATION);
            var ui = UI.of(xml);

//            HashMap<Integer, ItemSlot> slotElementsMap = new HashMap<>();
//            Pattern pattern = Pattern.compile("slot_(\\d+)");
//            ui.selectRegex("slot_\\d+")
//                    .filter(el -> el instanceof ItemSlot)
//                    .forEach(el -> {
//                        String input = el.getId();
//                        Matcher matcher = pattern.matcher(input);
//                        if (matcher.matches()) {
//                            slotElementsMap.put(Integer.parseInt(matcher.group(1)), (ItemSlot) el);
//                        }
//                    });
//
//            player.inventoryMenu.slots.forEach(slot -> {
//                ItemSlot itemSlotElem = slotElementsMap.get(slot.index);
//                if (itemSlotElem == null) return;
//                itemSlotElem.bind(new Slot(player.getInventory(), slot.index, 0, 0));
//                Constants.LOG.info("Bound slot index #{} to {}", itemSlotElem.getId(), slot.index);
//            });

            return ModularUI.of(ui, player);
        }

        public boolean isStillValid(Player player) {
            return true;
        }
    }
}
