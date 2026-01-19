package crazylimits.betterui.components;

import com.lowdragmc.lowdraglib2.configurator.annotation.Configurable;
import com.lowdragmc.lowdraglib2.gui.sync.bindings.IObserver;
import com.lowdragmc.lowdraglib2.gui.texture.SpriteTexture;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ItemSlot;
import com.lowdragmc.lowdraglib2.integration.kjs.KJSBindings;
import com.lowdragmc.lowdraglib2.registry.annotation.LDLRegister;
import crazylimits.betterui.Constants;
import net.minecraft.client.resources.metadata.gui.GuiSpriteScaling;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.appliedenergistics.yoga.YogaPositionType;
import org.w3c.dom.Element;

import javax.annotation.Nonnull;

@KJSBindings
@LDLRegister(name = "slot", group = "inventory", registry = "ldlib2:ui_element")
public class UISlot extends UIElement {
    /**
     * If 'group' is set, this is the index inside that group.
     * Otherwise, this is the absolute index in menu.slots.
     */
    @Configurable
    public Integer index = 0;

    /** Optional group name for group-based binding. */
    @Configurable
    public String group;

    public final ItemSlot itemSlot = new ItemSlot();
    public final UIElement itemSlotEmpty = new UIElement();

    protected ItemStack itemStack = ItemStack.EMPTY;

    public UISlot() {
        addChild(itemSlot);
        itemSlotEmpty
                .getLayout()
                .positionType(YogaPositionType.ABSOLUTE)
                .top(0)
                .left(0);
        addChild(itemSlotEmpty);
    }

    public boolean isEmpty() {
        return itemStack == null || itemStack.isEmpty();
    }

    public void bind(@Nonnull Slot slot) {
        itemSlot.bind(slot);
        itemSlot.addClass("__slot_" + slot.index + "__");
    }

    @Override
    public void loadXml(Element element) {
        if (element.hasAttribute("index")) {
            index = Integer.parseInt(element.getAttribute("index"));
        }
        if (element.hasAttribute("group")) {
            group = element.getAttribute("group");
        }
        super.loadXml(element);
    }
}