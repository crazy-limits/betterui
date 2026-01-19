package crazylimits.betterui.menus;

import net.minecraft.world.inventory.Slot;

import java.util.List;

public interface GroupableSlots {
    List<Slot> getSlots(String group);
    Slot getSlot(String group, int index);
}
