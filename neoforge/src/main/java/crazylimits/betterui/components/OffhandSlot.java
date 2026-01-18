package crazylimits.betterui.components;

import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ItemSlot;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvent;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvents;
import com.lowdragmc.lowdraglib2.integration.kjs.KJSBindings;
import com.lowdragmc.lowdraglib2.registry.annotation.LDLRegister;
import com.mojang.datafixers.util.Pair;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.appliedenergistics.yoga.YogaFlexDirection;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.function.Consumer;

@KJSBindings
@LDLRegister(name = "offhand-slot", group = "inventory", registry = "ldlib2:ui_element")
public class OffhandSlot extends UIElement {
    public ItemSlot itemSlot;

    public OffhandSlot() {
        itemSlot = new ItemSlot();
        addChild(itemSlot);
        itemSlot.setId("offhand_slot");

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
                itemSlot.bind(new Slot(inventory, 40, 0, 0) {
                    public void setByPlayer(ItemStack a, ItemStack b) {
                        player.onEquipItem(EquipmentSlot.OFFHAND, b, a);
                        super.setByPlayer(a, b);
                    }

                    public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
                        return Pair.of(InventoryMenu.BLOCK_ATLAS, InventoryMenu.EMPTY_ARMOR_SLOT_SHIELD);
                    }
                });
            }
        }
    }

    public OffhandSlot apply(Consumer<ItemSlot> consumer) {
        consumer.accept(itemSlot);
        return this;
    }
}
