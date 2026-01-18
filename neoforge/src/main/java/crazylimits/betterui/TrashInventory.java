package crazylimits.betterui;

import crazylimits.betterui.data.ModAttachments;
import crazylimits.betterui.data.TrashData;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class TrashInventory implements Container {
    private final Player player;

    public TrashInventory(Player player) {
        this.player = player;
    }

    private TrashData data() {
        return player.getData(ModAttachments.PLAYER_TRASH.get());
    }

    private ItemStack getTrash() {
        return data().getStack();
    }

    private void setTrash(ItemStack stack) {
        data().setStack(stack);
        // If you ever switch to using setData instead of mutating the same instance,
        // NeoForge will auto-mark dirty. For simple INBTSerializable objects, this
        // pattern (mutate, then save via serializeNBT) is fine.
    }

    @Override
    public int getContainerSize() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return getTrash().isEmpty();
    }

    @Override
    public ItemStack getItem(int index) {
        return getTrash();
    }

    @Override
    public ItemStack removeItem(int index, int count) {
        ItemStack current = getTrash();
        if (current.isEmpty() || count <= 0) return ItemStack.EMPTY;

        ItemStack result = current.split(count);
        setTrash(current.isEmpty() ? ItemStack.EMPTY : current);
        setChanged();
        return result;
    }

    @Override
    public ItemStack removeItemNoUpdate(int index) {
        ItemStack current = getTrash();
        setTrash(ItemStack.EMPTY);
        return current;
    }

    @Override
    public void setItem(int index, ItemStack stack) {
        setTrash(stack);
        setChanged();
    }

    @Override
    public int getMaxStackSize() {
        return 64;
    }

    @Override
    public void setChanged() {
        // Optionally trigger custom sync to client if needed.
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public boolean canPlaceItem(int index, ItemStack stack) {
        return true;
    }

    @Override
    public void clearContent() {
        setTrash(ItemStack.EMPTY);
        setChanged();
    }
}