package crazylimits.betterui.menus;

import com.lowdragmc.lowdraglib2.gui.factory.IContainerUIHolder;
import com.lowdragmc.lowdraglib2.gui.holder.ModularUIContainerMenu;
import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.UI;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ItemSlot;
import com.lowdragmc.lowdraglib2.utils.XmlUtils;
import crazylimits.betterui.BetteruiMenuTypes;
import crazylimits.betterui.Constants;
import crazylimits.betterui.TrashInventory;
import crazylimits.betterui.components.Entity;
import crazylimits.betterui.screens.BetterInventoryScreen;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;

import java.util.Objects;
import java.util.Optional;

public class BetterInventoryMenu extends ModularUIContainerMenu {
    public static int HEAD_SLOT_IDX = 5;
    public static int CHEST_SLOT_IDX = 6;
    public static int LEGS_SLOT_IDX = 7;
    public static int FEET_SLOT_IDX = 8;
    public static int OFFHAND_SLOT_IDX = 45;
    public static int RESULT_SLOT_IDX = 0;

    public static int ARMOR_SLOTS_START = 5;
    public static final int ARMOR_SLOTS_COUNT = 4;
    public static int ARMOR_SLOTS_END = 9;

    public static int CRAFTING_SLOTS_START = 1;
    public static final int CRAFTING_SLOTS_COUNT = 4;
    public static int CRAFTING_SLOTS_END = 5;

    public static int INVENTORY_SLOTS_START = 9;
    public static final int INVENTORY_SLOTS_COUNT = 27;
    public static int INVENTORY_SLOTS_END = 36;

    public static int HOTBAR_SLOTS_START = 36;
    public static final int HOTBAR_SLOTS_COUNT = 9;
    public static int HOTBAR_SLOTS_END = 45;

    private final Player owner;
    private final CraftingContainer craftSlots;
    private final ResultContainer resultSlots = new ResultContainer();

    public BetterInventoryMenu(int containerId, Inventory inventory) {
        super(BetteruiMenuTypes.INVENTORY.get(), containerId, inventory, new BetterInventoryHolderMenu());

        owner = inventory.player;

        var ui = this.getModularUI().ui;

        var inventorySlots = ui.select("inventory-slots item-slot");
        var indexes = inventorySlots.map(element -> {
            if (element instanceof ItemSlot itemSlot) {
                return itemSlot.getSlot().index;
            }
            return null;
        }).filter(Objects::nonNull).sorted().toList();
        if (indexes.size() == 36) {
            INVENTORY_SLOTS_START = indexes.get(0);
            INVENTORY_SLOTS_END = indexes.get(26);
            HOTBAR_SLOTS_START = indexes.get(27);
            HOTBAR_SLOTS_END = indexes.get(35);
        }

        var armorElements = ui.selectRegex("slot_[a-zA-Z]+");
        armorElements.forEach(element -> {
            if (element instanceof ItemSlot itemSlot) {
                String id = element.getId();
                var index = this.slots.indexOf(itemSlot.getSlot());
                var prefix = "slot_";
                if ((prefix + EquipmentSlot.HEAD.name()).equals(id)) {
                    HEAD_SLOT_IDX = index;
                    ARMOR_SLOTS_START = index;
                } else if ((prefix + EquipmentSlot.CHEST.name()).equals(id)) {
                    CHEST_SLOT_IDX = index;
                } else if ((prefix + EquipmentSlot.LEGS.name()).equals(id)) {
                    LEGS_SLOT_IDX = index;
                } else if ((prefix + EquipmentSlot.FEET.name()).equals(id)) {
                    FEET_SLOT_IDX = index;
                    ARMOR_SLOTS_END = index + 1;
                }
            }
        });

        var craftingElement1 = ui.select("crafting_slot_1").findFirst();
        var craftingElement2 = ui.select("crafting_slot_2").findFirst();
        var craftingElement3 = ui.select("crafting_slot_3").findFirst();
        var craftingElement4 = ui.select("crafting_slot_4").findFirst();
        var craftingElementResult = ui.select("crafting_slot_result").findFirst();

        if (craftingElement1.isPresent() && craftingElement2.isPresent() &&
            craftingElement3.isPresent() && craftingElement4.isPresent() &&
            craftingElementResult.isPresent()) {
            CRAFTING_SLOTS_START = this.slots.indexOf(((ItemSlot) craftingElement1.get()).getSlot());
            CRAFTING_SLOTS_END = this.slots.indexOf(((ItemSlot) craftingElement4.get()).getSlot()) + 1;
            RESULT_SLOT_IDX = this.slots.indexOf(((ItemSlot) craftingElementResult.get()).getSlot());
        }

        var craftSlots = ui.select("#crafting_slots").findFirst();
        if (craftSlots.isPresent() && craftSlots.get() instanceof crazylimits.betterui.components.CraftingSlots craftingSlots) {
            this.craftSlots = craftingSlots.craftSlots;
        } else {
            this.craftSlots = new TransientCraftingContainer(this, 2, 2);
        }

        var resultSlot = ui.select("#result_slot").findFirst();
        if (resultSlot.isPresent() && resultSlot.get() instanceof ItemSlot resultSlotComponent) {
            resultSlotComponent.bind(
                    new net.minecraft.world.inventory.ResultSlot(
                            inventory.player,
                            this.craftSlots,
                            this.resultSlots,
                            RESULT_SLOT_IDX,
                            0,
                            0
                    )
            );
        }
    }

    public BetterInventoryMenu(int containerId, Inventory inventory, RegistryFriendlyByteBuf buf) {
        this(containerId, inventory);
    }

    public void slotsChanged(Container pInventory) {
//        CraftingMenu.slotChangedCraftingGrid(this, this.owner.level(), this.owner, this.craftSlots, this.resultSlots, (RecipeHolder)null);
        var level = this.owner.level();
        if (!level.isClientSide) {
            CraftingInput craftinginput = this.craftSlots.asCraftInput();
            ServerPlayer serverplayer = (ServerPlayer) this.owner;
            ItemStack itemstack = ItemStack.EMPTY;
            Optional<RecipeHolder<CraftingRecipe>> optional = level.getServer().getRecipeManager().getRecipeFor(RecipeType.CRAFTING, craftinginput, level, (RecipeHolder) null);
            if (optional.isPresent()) {
                RecipeHolder<CraftingRecipe> recipeholder = (RecipeHolder)optional.get();
                CraftingRecipe craftingrecipe = (CraftingRecipe)recipeholder.value();
                if (this.resultSlots.setRecipeUsed(level, serverplayer, recipeholder)) {
                    ItemStack itemstack1 = craftingrecipe.assemble(craftinginput, level.registryAccess());
                    if (itemstack1.isItemEnabled(level.enabledFeatures())) {
                        itemstack = itemstack1;
                    }
                }
            }

            this.resultSlots.setItem(0, itemstack);
            this.setRemoteSlot(0, itemstack);
            serverplayer.connection.send(new ClientboundContainerSetSlotPacket(this.containerId, this.incrementStateId(), 0, itemstack));
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();
            EquipmentSlot equipmentslot = player.getEquipmentSlotForItem(itemstack);
            var equipmentidx = switch (equipmentslot) {
                case HEAD -> HEAD_SLOT_IDX;
                case CHEST -> CHEST_SLOT_IDX;
                case LEGS -> LEGS_SLOT_IDX;
                case FEET -> FEET_SLOT_IDX;
                default -> -1;
            };
            if (index == RESULT_SLOT_IDX) {
                if (!this.moveItemStackTo(itemstack1, INVENTORY_SLOTS_START, HOTBAR_SLOTS_END, true)) {
                    return ItemStack.EMPTY;
                }

                slot.onQuickCraft(itemstack1, itemstack);
            } else if (index >= CRAFTING_SLOTS_START && index < CRAFTING_SLOTS_END) {
                if (!this.moveItemStackTo(itemstack1, INVENTORY_SLOTS_START, HOTBAR_SLOTS_END, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (index >= ARMOR_SLOTS_START && index < ARMOR_SLOTS_END) {
                if (!this.moveItemStackTo(itemstack1, INVENTORY_SLOTS_START, HOTBAR_SLOTS_END, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (equipmentslot.getType() == EquipmentSlot.Type.HUMANOID_ARMOR && !this.slots.get(equipmentidx).hasItem()) {
                if (!this.moveItemStackTo(itemstack1, equipmentidx, equipmentidx + 1, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (equipmentslot == EquipmentSlot.OFFHAND && !((Slot)this.slots.get(OFFHAND_SLOT_IDX)).hasItem()) {
                if (!this.moveItemStackTo(itemstack1, OFFHAND_SLOT_IDX, OFFHAND_SLOT_IDX + 1, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (index >= INVENTORY_SLOTS_START && index < INVENTORY_SLOTS_END) {
                if (!this.moveItemStackTo(itemstack1, HOTBAR_SLOTS_START, HOTBAR_SLOTS_END, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (index >= HOTBAR_SLOTS_START && index < HOTBAR_SLOTS_END) {
                if (!this.moveItemStackTo(itemstack1, INVENTORY_SLOTS_START, INVENTORY_SLOTS_END, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(itemstack1, INVENTORY_SLOTS_START, HOTBAR_SLOTS_END, false)) {
                return ItemStack.EMPTY;
            }

            if (itemstack1.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY, itemstack);
            } else {
                slot.setChanged();
            }

            if (itemstack1.getCount() == itemstack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(player, itemstack1);
            if (index == RESULT_SLOT_IDX) {
                player.drop(itemstack1, false);
            }
        }

        return itemstack;
    }

    public static class BetterInventoryHolderMenu implements IContainerUIHolder {
        public ModularUI createUI(Player player) {
            var xml = XmlUtils.loadXml(BetterInventoryScreen.LOCATION);
            var ui = UI.of(xml);

//            var slotElements = ui.selectRegex("slot_\\d+");
//            var menu = player.inventoryMenu;
//            slotElements.forEach(element -> {
//                if (element instanceof ItemSlot itemSlot) {
//                    String id = element.getId();
//                    int slotIndex = Integer.parseInt(id.substring(5));
//                    itemSlot.bind(menu.getSlot(slotIndex));
//                }
//            });

            var entityElements = ui.select("#entity");
            entityElements.forEach(entity -> {
                if (entity instanceof Entity entityComponent) {
                    entityComponent.setEntity(player);
                }
            });

            var trashElement = ui.select("#trash").findFirst();
            if (trashElement.isPresent()) {
                if (trashElement.get() instanceof ItemSlot itemSlot) {
                    itemSlot.bind(new Slot(new TrashInventory(player), 50, 0, 0) {
                        @Override
                        public boolean mayPickup(Player player) {
                            return true;
                        }

                        @Override
                        public boolean mayPlace(ItemStack stack) {
                            return true;
                        }

                        @Override
                        public void setByPlayer(ItemStack stack, ItemStack original) {
                            // If there was nothing there before, or nothing is being placed now,
                            // just fall back to normal behavior and/or trash the original.
                            if (original.isEmpty()) {
                                // No previous item -> just set normally
                                super.setByPlayer(stack, original);
                                return;
                            }

                            if (stack.isEmpty()) {
                                // Player removed the item from the slot: trash the original
//                                original.setCount(0);
                                Constants.LOG.info("Trashed item (removed by player): {}", original);
                                super.setByPlayer(stack, original);
                                return;
                            }

                            // At this point, both `stack` (incoming) and `original` (current in slot)
                            // are non-empty.

                            // Check if they are the same item type and can stack together
                            if (ItemStack.isSameItem(stack, original)) {
                                int maxStackSize = original.getMaxStackSize();
                                int currentCount = original.getCount();

                                // If already at or above max, do nothing
                                if (currentCount >= maxStackSize) {
                                    return; // skip – do nothing
                                }

                                // Determine how much we can add without exceeding max
                                int spaceLeft = maxStackSize - currentCount;
                                int toMove = Math.min(spaceLeft, stack.getCount());

                                // Increment the count in the slot
                                original.grow(toMove);
                                // Decrease the incoming stack
                                stack.shrink(toMove);

                                // Put the updated original into the slot. `original` here represents
                                // the new content of the slot.
                                super.setByPlayer(original, original);
                            } else {
                                // Different item type: trash the original and replace with the new one
                                original.setCount(0);
                                Constants.LOG.info("Trashed item (replaced by new type): {}", original);
                                super.setByPlayer(stack, original);
                            }
                        }
                    });
                }
            }

//            var armorElements = ui.selectRegex("slot_[a-zA-Z]+");
//            armorElements.forEach(element -> {
//                if (element instanceof ItemSlot itemSlot) {
//                    String id = element.getId();
//                    var index = itemSlot.getSlot().getSlotIndex();
//                    var prefix = "slot_";
//                    if ((prefix + EquipmentSlot.HEAD.name()).equals(id)) {
//                        HEAD_SLOT_IDX = index;
//                    } else if ((prefix + EquipmentSlot.CHEST.name()).equals(id)) {
//                        CHEST_SLOT_IDX = index;
//                    } else if ((prefix + EquipmentSlot.LEGS.name()).equals(id)) {
//                        LEGS_SLOT_IDX = index;
//                    } else if ((prefix + EquipmentSlot.FEET.name()).equals(id)) {
//                        FEET_SLOT_IDX = index;
//                    }
//                }
//            });
//
//            var inventorySlots = ui.select("inventory-slots item-slot");
//            var indexes = inventorySlots.map(element -> {
//                if (element instanceof ItemSlot itemSlot) {
//                    return itemSlot.getSlot().getSlotIndex();
//                }
//                return null;
//            }).filter(Objects::nonNull).sorted().toList();
//            if (indexes.size() == 36) {
//                INVENTORY_SLOTS_START = indexes.get(0);
//                INVENTORY_SLOTS_END = indexes.get(26);
//                HOTBAR_SLOTS_START = indexes.get(27);
//                HOTBAR_SLOTS_END = indexes.get(35);
//            }

            return ModularUI.of(ui, player);
        }

        public boolean isStillValid(Player player) {
            return stillValid(
                    ContainerLevelAccess.NULL,
                    player,
                    null
            );
        }
    }
}
