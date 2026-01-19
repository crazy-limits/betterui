package crazylimits.betterui.menus;

import com.lowdragmc.lowdraglib2.gui.factory.IContainerUIHolder;
import com.lowdragmc.lowdraglib2.gui.holder.ModularUIContainerMenu;
import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.UI;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Button;
import com.lowdragmc.lowdraglib2.utils.XmlUtils;
import crazylimits.betterui.BetterUIMenuTypes;
import crazylimits.betterui.Constants;
import crazylimits.betterui.TrashInventory;
import crazylimits.betterui.components.Entity;
import crazylimits.betterui.components.UISlot;
import crazylimits.betterui.components.UISlots;
import crazylimits.betterui.screens.BetterInventoryScreen;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.resources.ResourceLocation;
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

import java.util.*;

import static net.minecraft.world.inventory.InventoryMenu.*;

/**
 * BetterInventoryMenu – inventory screen with XML-driven layout,
 * but slot indices and ranges stay compatible with vanilla InventoryMenu.
 */
public class BetterInventoryMenu extends ModularUIContainerMenu implements GroupableSlots {

    /* =========================
     *  GROUP NAMES (PUBLIC API)
     * ========================= */

    /** 0 – crafting result */
    public static final String GROUP_CRAFTING_RESULT = "crafting_result";

    /** 1–4 – 2x2 crafting grid */
    public static final String GROUP_CRAFTING_GRID = "crafting_grid";

    /** 5–8 – armor slots (HEAD, CHEST, LEGS, FEET) */
    public static final String GROUP_ARMOR = "armor";

    /** 9–35 – main inventory (3x9) */
    public static final String GROUP_INVENTORY = "inventory";

    /** 36–44 – hotbar (9 slots) */
    public static final String GROUP_HOTBAR = "hotbar";

    /** 45 – offhand/shield */
    public static final String GROUP_OFFHAND = "offhand";

    /**
     * Special UI-only trash slot.
     * Not part of the normal container slot indices and NOT used in quickMoveStack.
     */
    public static final String GROUP_TRASH = "trash";

    /* ==============================
     *  VANILLA-COMPATIBLE CONSTANTS
     * ============================== */

    // These remain for internal logic and quickMoveStack ranges, but
    // they are derived from the standard InventoryMenu layout.
    public static final int RESULT_SLOT_INDEX = 0;

    public static final int CRAFTING_SLOTS_START = 1;     // 1–4
    public static final int CRAFTING_SLOTS_COUNT = 4;
    public static final int CRAFTING_SLOTS_END = CRAFTING_SLOTS_START + CRAFTING_SLOTS_COUNT; // 5 (exclusive upper in loops)

    public static final int ARMOR_SLOTS_START = 5;        // 5–8
    public static final int ARMOR_SLOTS_COUNT = 4;
    public static final int ARMOR_SLOTS_END = ARMOR_SLOTS_START + ARMOR_SLOTS_COUNT; // 9

    public static final int INVENTORY_SLOTS_START = 9;    // 9–35
    public static final int INVENTORY_SLOTS_END = 36;     // exclusive upper bound in loops
    public static final int INVENTORY_SLOTS_COUNT = INVENTORY_SLOTS_END - INVENTORY_SLOTS_START;

    public static final int HOTBAR_SLOTS_START = 36;      // 36–44
    public static final int HOTBAR_SLOTS_END = 45;        // exclusive
    public static final int HOTBAR_SLOTS_COUNT = HOTBAR_SLOTS_END - HOTBAR_SLOTS_START;

    public static final int OFFHAND_SLOT_INDEX = 45;      // 45
    public static final int TRASH_SLOT_INDEX = 46;

    /* ==============
     *  FIELDS
     * ============== */

    private static final Map<EquipmentSlot, ResourceLocation> TEXTURE_EMPTY_SLOTS = Map.of(EquipmentSlot.FEET, EMPTY_ARMOR_SLOT_BOOTS, EquipmentSlot.LEGS, EMPTY_ARMOR_SLOT_LEGGINGS, EquipmentSlot.CHEST, EMPTY_ARMOR_SLOT_CHESTPLATE, EquipmentSlot.HEAD, EMPTY_ARMOR_SLOT_HELMET);
    private static final EquipmentSlot[] SLOT_IDS = new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};

    private final Player owner;

    private final CraftingContainer craftingContainer;
    private final TrashInventory trashInventory;
    private final ResultContainer resultContainer = new ResultContainer();

    /**
     * Group name -> ordered list of slots in that group.
     * Indices in the list correspond to indices in this.slots.
     */
    private final Map<String, List<Slot>> slotMap = new HashMap<>();

    // Sorting strategies
    private final InventorySortStrategy rowSortStrategy = new InventorySortStrategyRow();
    private final InventorySortStrategy clusterSortStrategy = new InventorySortStrategyCluster();

    // choose default strategy
    private InventorySortStrategy activeSortStrategy = rowSortStrategy;

    public BetterInventoryMenu(int containerId, Inventory inventory) {
        super(BetterUIMenuTypes.INVENTORY.get(), containerId, inventory, new BetterInventoryHolderMenu());

        owner = inventory.player;
        craftingContainer = new TransientCraftingContainer(this, 2, 2);
        trashInventory = new TrashInventory(owner);

        initSlots(inventory);

        // Now that slots and groups are ready, let the holder bind UI elements
        if (this.uiHolder instanceof BetterInventoryHolderMenu holder) {
            holder.bindSlots(this);
        }
    }

    public BetterInventoryMenu(int containerId, Inventory inventory, RegistryFriendlyByteBuf buf) {
        this(containerId, inventory);
    }

    /* =================
     *  SLOT INITIALIZE
     * ================= */

    private void initSlots(Inventory playerInv) {
        // Local builders for each group, so we can easily put them into slotMap at the end.
        List<Slot> craftingResultSlots = new ArrayList<>(1);
        List<Slot> craftingGridSlots = new ArrayList<>(CRAFTING_SLOTS_COUNT);
        List<Slot> armorSlots = new ArrayList<>(ARMOR_SLOTS_COUNT);
        List<Slot> inventorySlots = new ArrayList<>(INVENTORY_SLOTS_COUNT);
        List<Slot> hotbarSlots = new ArrayList<>(HOTBAR_SLOTS_COUNT);
        List<Slot> offhandSlots = new ArrayList<>(1);
        List<Slot> trashSlots = new ArrayList<>(1);

        /*
         * 0: crafting result
         * Matches InventoryMenu: this.addSlot(new ResultSlot(...));
         */
        {
            Slot slot = new ResultSlot(
                    playerInv.player,
                    this.craftingContainer,
                    this.resultContainer,
                    RESULT_SLOT_INDEX,
                    0, // x,y are irrelevant for logic; UI will reposition via XML
                    0
            );
            this.addSlot(slot);
            craftingResultSlots.add(slot);
        }

        /*
         * 1–4: 2x2 crafting grid
         * Vanilla uses indices 1..4; we keep that order.
         */
        for (int i = 0; i < 2; ++i) {
            for (int j = 0; j < 2; ++j) {
                int containerIndex = j + i * 2; // index in crafting container
                Slot slot = new Slot(this.craftingContainer, containerIndex, 0, 0);
                this.addSlot(slot);
                craftingGridSlots.add(slot);
            }
        }

        /*
         * 5–8: armor slots (HEAD, CHEST, LEGS, FEET)
         * Vanilla uses EquipmentSlot[HEAD, CHEST, LEGS, FEET] => indices 5..8.
         */
        for (int k = 0; k < 4; ++k) {
            EquipmentSlot equipmentSlot = SLOT_IDS[k];
            Slot slot = new ArmorSlot(
                    playerInv,
                    owner,
                    equipmentSlot,
                    39 - k,  // vanilla inventory index in InventoryMenu (39..36)
                    0,
                    0,
                    TEXTURE_EMPTY_SLOTS.get(equipmentSlot)
            );
            this.addSlot(slot);
            armorSlots.add(slot);
        }

        /*
         * 9–35: main inventory (3x9)
         */
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                int invIndex = col + (row + 1) * 9; // vanilla: 9..35
                Slot slot = new Slot(playerInv, invIndex, 0, 0);
                this.addSlot(slot);
                inventorySlots.add(slot);
            }
        }

        /*
         * 36–44: hotbar (0..8 in player inventory)
         */
        for (int i = 0; i < 9; ++i) {
            Slot slot = new Slot(playerInv, i, 0, 0);
            this.addSlot(slot);
            hotbarSlots.add(slot);
        }

        /*
         * 45: offhand slot
         *
         * In vanilla InventoryMenu there is a special Slot at index 45
         * for offhand/shield. We replicate that logic.
         */
        {
            Slot offhandSlot = new Slot(playerInv, 40, 0, 0) {
                @Override
                public void setByPlayer(ItemStack newStack, ItemStack original) {
                    owner.onEquipItem(EquipmentSlot.OFFHAND, original, newStack);
                    super.setByPlayer(newStack, original);
                }
            };
            this.addSlot(offhandSlot);
            offhandSlots.add(offhandSlot);
        }

        {
            Slot trashSlot = new Slot(trashInventory, 50, 0, 0) {
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
                    if (original.isEmpty()) {
                        super.setByPlayer(stack, original);
                        return;
                    }

                    if (stack.isEmpty()) {
                        Constants.LOG.info("Trashed item (removed by player): {}", original);
                        super.setByPlayer(stack, original);
                        return;
                    }

                    if (ItemStack.isSameItem(stack, original)) {
                        int max = original.getMaxStackSize();
                        int current = original.getCount();
                        if (current >= max) return;

                        int spaceLeft = max - current;
                        int toMove = Math.min(spaceLeft, stack.getCount());
                        original.grow(toMove);
                        stack.shrink(toMove);
                        super.setByPlayer(original, original);
                    } else {
                        original.setCount(0);
                        Constants.LOG.info("Trashed item (replaced by new type): {}", original);
                        super.setByPlayer(stack, original);
                    }
                }
            };
            this.addSlot(trashSlot);
            trashSlots.add(trashSlot);
        }

        // Store groups
        slotMap.put(GROUP_CRAFTING_RESULT, Collections.unmodifiableList(craftingResultSlots));
        slotMap.put(GROUP_CRAFTING_GRID, Collections.unmodifiableList(craftingGridSlots));
        slotMap.put(GROUP_ARMOR, Collections.unmodifiableList(armorSlots));
        slotMap.put(GROUP_INVENTORY, Collections.unmodifiableList(inventorySlots));
        slotMap.put(GROUP_HOTBAR, Collections.unmodifiableList(hotbarSlots));
        slotMap.put(GROUP_OFFHAND, Collections.unmodifiableList(offhandSlots));
        slotMap.put(GROUP_TRASH, Collections.unmodifiableList(trashSlots));
    }

    /* ====================
     *  GROUPABLESLOTS API
     * ==================== */

    @Override
    public List<Slot> getSlots(String group) {
        if (group == null) {
            return List.of();
        }
        var list = slotMap.get(group);
        return list != null ? list : List.of();
    }

    @Override
    public Slot getSlot(String group, int index) {
        if (group == null) {
            return null;
        }
        var list = slotMap.get(group);
        if (list == null || index < 0 || index >= list.size()) {
            return null;
        }
        return list.get(index);
    }

    /* ===============
     *  SORT / MERGE
     * =============== */

    public void sortInventory() {
        final int start = INVENTORY_SLOTS_START;
        final int endExclusive = INVENTORY_SLOTS_END;
        final int slotCount = endExclusive - start;
        final int width = HOTBAR_SLOTS_COUNT;

        List<ItemStack> nonEmpty = new ArrayList<>();
        for (int i = start; i < endExclusive; i++) {
            ItemStack st = this.slots.get(i).getItem();
            if (!st.isEmpty()) {
                nonEmpty.add(st.copy());
            }
        }

        List<ItemStack> sorted = activeSortStrategy.sort(nonEmpty, slotCount, width);

        int writeIndex = start;
        for (ItemStack st : sorted) {
            this.slots.get(writeIndex++).set(st);
        }

        this.broadcastChanges();
    }

    public void mergeStacks() {
        final int start = INVENTORY_SLOTS_START;
        final int endExclusive = INVENTORY_SLOTS_END;

        List<ItemStack> nonEmpty = new ArrayList<>();
        for (int i = start; i < endExclusive; i++) {
            ItemStack st = this.slots.get(i).getItem();
            if (!st.isEmpty()) {
                nonEmpty.add(st.copy());
            }
        }

        List<ItemStack> merged = InventoryMergeHelper.mergeStacks(nonEmpty);

        int idx = start;
        for (ItemStack st : merged) {
            if (idx >= endExclusive) break;
            this.slots.get(idx++).set(st);
        }

        while (idx < endExclusive) {
            this.slots.get(idx++).set(ItemStack.EMPTY);
        }

        this.broadcastChanges();
    }

    /* ==========================
     *  CRAFTING / RESULT UPDATE
     * ========================== */

    @Override
    public void slotsChanged(Container inventory) {
        var level = this.owner.level();
        if (!level.isClientSide) {
            CraftingInput craftingInput = this.craftingContainer.asCraftInput();
            ServerPlayer serverPlayer = (ServerPlayer) this.owner;
            ItemStack result = ItemStack.EMPTY;
            Optional<RecipeHolder<CraftingRecipe>> opt =
                    level.getServer()
                            .getRecipeManager()
                            .getRecipeFor(RecipeType.CRAFTING, craftingInput, level, (ResourceLocation) null);

            if (opt.isPresent()) {
                RecipeHolder<CraftingRecipe> holder = opt.get();
                CraftingRecipe recipe = holder.value();
                if (this.resultContainer.setRecipeUsed(level, serverPlayer, holder)) {
                    ItemStack assembled =
                            recipe.assemble(craftingInput, level.registryAccess());
                    if (assembled.isItemEnabled(level.enabledFeatures())) {
                        result = assembled;
                    }
                }
            }

            this.resultContainer.setItem(0, result);
            this.setRemoteSlot(RESULT_SLOT_INDEX, result);
            serverPlayer.connection.send(
                    new ClientboundContainerSetSlotPacket(
                            this.containerId,
                            this.incrementStateId(),
                            RESULT_SLOT_INDEX,
                            result
                    )
            );
        }
    }

    // in BetterInventoryMenu
    public void moveSlotToTrash(Slot src, ServerPlayer player) {
        var trashSlot = this.slots.get(TRASH_SLOT_INDEX);
        if (trashSlot == null) {
            // No trash slot bound; just delete
            if (!src.hasItem()) return;
            src.remove(src.getItem().getCount());
            this.broadcastChanges();
            return;
        }

        if (!src.hasItem()) return;

        ItemStack srcStack = src.getItem();
        ItemStack trashStack = trashSlot.getItem();

        // Case 1: trash empty → move full stack from src to trash
        if (trashStack.isEmpty()) {
            // Move the entire source stack into trash
            trashSlot.set(srcStack.copy());
            src.remove(srcStack.getCount());
            this.broadcastChanges();
            Constants.LOG.info(
                    "Trash: moved {} to empty trash slot from {}",
                    srcStack,
                    src.index
            );
            return;
        }

        // Case 2: same item type → stack up to max
        if (ItemStack.isSameItem(srcStack, trashStack)) {
            int max = trashStack.getMaxStackSize();
            int current = trashStack.getCount();

            if (current >= max) {
                // Trash already full for this item -> delete source item
                src.remove(srcStack.getCount());
                this.broadcastChanges();
                Constants.LOG.info(
                        "Trash: trash full for {}, deleted {} from {}",
                        trashStack,
                        srcStack,
                        src.index
                );
                return;
            }

            int spaceLeft = max - current;
            int toMove = Math.min(spaceLeft, srcStack.getCount());

            // Grow trash, shrink source
            trashStack.grow(toMove);
            srcStack.shrink(toMove);

            trashSlot.set(trashStack); // ensure slot is marked changed
            if (srcStack.isEmpty()) {
                src.set(ItemStack.EMPTY);
            } else {
                src.set(srcStack);
            }

            this.broadcastChanges();
            Constants.LOG.info(
                    "Trash: stacked {} of {} into trash (now {}), from slot {}",
                    toMove,
                    srcStack.getItem(),
                    trashStack.getCount(),
                    src.index
            );
            return;
        }

        // Case 3: different item → discard trash and replace with new
        // Example: trashSlot=1 Oak, deleting 1 Birch:
        // result: trashSlot=1 Birch, old Oak vanishes
        trashStack.setCount(0); // discard old trash content
        ItemStack newTrash = srcStack.copy();

        trashSlot.set(newTrash);
        src.remove(srcStack.getCount());

        this.broadcastChanges();
        Constants.LOG.info(
                "Trash: replaced previous trash content with {} from slot {}",
                newTrash,
                src.index
        );
    }

    /* =================
     *  QUICK MOVE LOGIC
     * ================= */

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack stackInSlot = slot.getItem();
        itemstack = stackInSlot.copy();
        EquipmentSlot equipmentSlot = player.getEquipmentSlotForItem(stackInSlot);

        // 0: result slot
        if (index == RESULT_SLOT_INDEX) {
            if (!this.moveItemStackTo(stackInSlot,
                    INVENTORY_SLOTS_START,
                    HOTBAR_SLOTS_END,
                    true)) {
                return ItemStack.EMPTY;
            }
            slot.onQuickCraft(stackInSlot, itemstack);
        }
        // 1–4: crafting grid
        else if (index >= CRAFTING_SLOTS_START && index < CRAFTING_SLOTS_END) {
            if (!this.moveItemStackTo(stackInSlot,
                    INVENTORY_SLOTS_START,
                    HOTBAR_SLOTS_END,
                    false)) {
                return ItemStack.EMPTY;
            }
        }
        // 5–8: armor slots
        else if (index >= ARMOR_SLOTS_START && index < ARMOR_SLOTS_END) {
            if (!this.moveItemStackTo(stackInSlot,
                    INVENTORY_SLOTS_START,
                    HOTBAR_SLOTS_END,
                    false)) {
                return ItemStack.EMPTY;
            }
        }
        // move to armor
        else if (equipmentSlot.getType() == EquipmentSlot.Type.HUMANOID_ARMOR) {
            // armor slots: 5..8 in order HEAD, CHEST, LEGS, FEET
            int armorIndex = switch (equipmentSlot) {
                case HEAD -> 5;
                case CHEST -> 6;
                case LEGS -> 7;
                case FEET -> 8;
                default -> -1;
            };
            if (armorIndex != -1 && !this.slots.get(armorIndex).hasItem()) {
                if (!this.moveItemStackTo(stackInSlot,
                        armorIndex,
                        armorIndex + 1,
                        false)) {
                    return ItemStack.EMPTY;
                }
            }
        }
        // move to offhand
        else if (equipmentSlot == EquipmentSlot.OFFHAND
                && !this.slots.get(OFFHAND_SLOT_INDEX).hasItem()) {
            if (!this.moveItemStackTo(stackInSlot,
                    OFFHAND_SLOT_INDEX,
                    OFFHAND_SLOT_INDEX + 1,
                    false)) {
                return ItemStack.EMPTY;
            }
        }
        // 9–35: main inventory -> hotbar
        else if (index >= INVENTORY_SLOTS_START && index < INVENTORY_SLOTS_END) {
            if (!this.moveItemStackTo(stackInSlot,
                    HOTBAR_SLOTS_START,
                    HOTBAR_SLOTS_END,
                    false)) {
                return ItemStack.EMPTY;
            }
        }
        // 36–44: hotbar -> main inventory
        else if (index >= HOTBAR_SLOTS_START && index < HOTBAR_SLOTS_END) {
            if (!this.moveItemStackTo(stackInSlot,
                    INVENTORY_SLOTS_START,
                    INVENTORY_SLOTS_END,
                    false)) {
                return ItemStack.EMPTY;
            }
        }
        // anything else
        else if (!this.moveItemStackTo(stackInSlot,
                INVENTORY_SLOTS_START,
                HOTBAR_SLOTS_END,
                false)) {
            return ItemStack.EMPTY;
        }

        if (stackInSlot.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY, itemstack);
        } else {
            slot.setChanged();
        }

        if (stackInSlot.getCount() == itemstack.getCount()) {
            return ItemStack.EMPTY;
        }

        slot.onTake(player, stackInSlot);
        if (index == RESULT_SLOT_INDEX) {
            player.drop(stackInSlot, false);
        }

        return itemstack;
    }

    /* ===================
     *  HOLDER / UI SETUP
     * =================== */

    public static class BetterInventoryHolderMenu implements IContainerUIHolder {

        private UI ui;

        @Override
        public ModularUI createUI(Player player) {
            var xml = XmlUtils.loadXml(BetterInventoryScreen.LOCATION);
            this.ui = UI.of(xml);

            // Bind player entity previews
            var entityElements = ui.select("#entity");
            entityElements.forEach(entity -> {
                if (entity instanceof Entity entityComponent) {
                    entityComponent.setEntity(player);
                }
            });

            // Trash: still bound here, UI-only
            ui.select("#trash").findFirst().ifPresent(el -> {
                if (el instanceof UISlot itemSlot) {
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
                            if (original.isEmpty()) {
                                super.setByPlayer(stack, original);
                                return;
                            }

                            if (stack.isEmpty()) {
                                Constants.LOG.info("Trashed item (removed by player): {}", original);
                                super.setByPlayer(stack, original);
                                return;
                            }

                            if (ItemStack.isSameItem(stack, original)) {
                                int max = original.getMaxStackSize();
                                int current = original.getCount();
                                if (current >= max) return;

                                int spaceLeft = max - current;
                                int toMove = Math.min(spaceLeft, stack.getCount());
                                original.grow(toMove);
                                stack.shrink(toMove);
                                super.setByPlayer(original, original);
                            } else {
                                original.setCount(0);
                                Constants.LOG.info("Trashed item (replaced by new type): {}", original);
                                super.setByPlayer(stack, original);
                            }
                        }
                    });
                }
            });

            // Sort / merge buttons: handlers bound later when we have the menu
            return ModularUI.of(ui, player);
        }

        /**
         * Called from BetterInventoryMenu AFTER initSlots().
         */
        public void bindSlots(BetterInventoryMenu menu) {
            if (ui == null) return;
            var player = menu.owner;

            // 1) Bind all <slot> elements (UISlot)
            ui.select("slot").forEach(el -> {
                if (!(el instanceof UISlot uiSlot)) return;

                // Trash slot handled separately above by id="trash"
                if ("trash".equals(uiSlot.getId())) {
                    return;
                }

                var group = uiSlot.group;
                int localIndex = uiSlot.index != null ? uiSlot.index : 0;

                try {
                    Slot target = null;
                    if (group != null && !group.isBlank()) {
                        target = menu.getSlot(group, localIndex);
                    } else {
                        target = menu.getSlot(localIndex);
                    }
                    if (target != null) {
                        uiSlot.bind(target);
                    }
                } catch (Exception ignored) {
                }
            });

            // 2) Bind <slots> grids (UISlots)
            ui.select("slots").forEach(el -> {
                if (!(el instanceof UISlots uiSlots)) return;

                var group = uiSlots.group;
                for (var row : uiSlots.rowsList) {
                    for (var uiSlot : row.slots) {
                        // Already bound? skip
                        if (uiSlot.itemSlot.getSlot() != null) continue;

                        var localIndex = uiSlot.index != null ? uiSlot.index : 0;
                        try {
                            Slot target = null;
                            if (group != null && !group.isBlank()) {
                                target = menu.getSlot(group, localIndex);
                            } else {
                                target = menu.getSlot(localIndex);
                            }
                            if (target != null) {
                                uiSlot.bind(target);
                            }
                        } catch (Exception ignored) {
                        }
                    }
                }
            });

            // 4) Wire buttons now that we have the menu instance
            ui.select("#sort").findFirst().ifPresent(el -> {
                if (el instanceof Button sortButton) {
                    sortButton.setOnServerClick(e -> menu.sortInventory());
                }
            });

            ui.select("#merge").findFirst().ifPresent(el -> {
                if (el instanceof Button mergeButton) {
                    mergeButton.setOnServerClick(e -> menu.mergeStacks());
                }
            });
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