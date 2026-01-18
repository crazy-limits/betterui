package crazylimits.betterui.data;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.util.INBTSerializable;

public class TrashData implements INBTSerializable<CompoundTag> {

    private ItemStack stack = ItemStack.EMPTY;

    public ItemStack getStack() {
        return stack;
    }

    public void setStack(ItemStack stack) {
        this.stack = stack;
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        if (!stack.isEmpty()) {
            stack.save(provider);
            tag.put("stack", new CompoundTag());
        }
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag compoundTag) {
        if (compoundTag.contains("stack", Tag.TAG_COMPOUND)) {
            this.stack = ItemStack.parseOptional(provider, compoundTag.getCompound("stack")); // or parseOptional if needed
        } else {
            this.stack = ItemStack.EMPTY;
        }
    }
}