package crazylimits.betterui.components;

import com.lowdragmc.lowdraglib2.configurator.annotation.Configurable;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ItemSlot;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvent;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvents;
import com.lowdragmc.lowdraglib2.integration.kjs.KJSBindings;
import com.lowdragmc.lowdraglib2.registry.annotation.LDLRegister;
import com.mojang.datafixers.util.Pair;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.function.Consumer;

@KJSBindings
@LDLRegister(name = "result-slot", group = "inventory", registry = "ldlib2:ui_element")
public class ResultSlot extends UIElement {
    public final ResultContainer resultSlots = new ResultContainer();
    public CraftingContainer craftSlots;
    public ItemSlot itemSlot;

    @Configurable
    public String from = "crafting-slots";

    public ResultSlot() {
        itemSlot = new ItemSlot();
        addChild(itemSlot);
        itemSlot.setId("__result_slot__");

        addEventListener(UIEvents.MUI_CHANGED, this::onModularUIChanged);
        internalSetup();
    }

    protected void onModularUIChanged(UIEvent event) {
        var mui = getModularUI();
        if (mui != null && event.customData != mui) {
            var menu = mui.getMenu();
            var player = mui.player;
            var craftSlotsElement = mui.select("#crafting-slots").findFirst();
            if (menu != null && player != null && craftSlotsElement.isPresent() && craftSlotsElement.get() instanceof CraftingSlots craftSlotsEl) {
                this.craftSlots = craftSlotsEl.craftSlots;
                itemSlot.bind(new net.minecraft.world.inventory.ResultSlot(player, this.craftSlots, this.resultSlots, 0, 154, 28));
            }
        }
    }

    public ResultSlot apply(Consumer<ItemSlot> consumer) {
        consumer.accept(itemSlot);
        return this;
    }
}
