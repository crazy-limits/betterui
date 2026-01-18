package crazylimits.betterui.mixin;

import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ItemSlot;
import com.lowdragmc.lowdraglib2.gui.ui.rendering.GUIContext;
import com.lowdragmc.lowdraglib2.gui.util.DrawerHelper;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ItemSlot.class)
public abstract class ItemSlotMixin {
    @Unique
    private final ItemSlot self = (ItemSlot) (Object) this;

    @Shadow
    protected Slot slot;

    @Shadow
    public abstract ItemStack getValue();

    /**
     * Full rewrite so the early-return doesn't prevent drawing Slot#getNoItemIcon().
     *
     * Rule:
     * - If the (final) slot value is empty AND an icon is available => always draw icon.
     * - If the (final) slot value is not empty => draw item.
     * - Hover overlay is independent (draw whenever hovered).
     */
    @Overwrite
    public void drawBackgroundAdditional(GUIContext guiContext) {
        // "Real" slot contents (authoritative for placeholder decision)
        boolean realEmpty = slot.getItem().isEmpty();
        Pair<ResourceLocation, ResourceLocation> noItemIcon =
                realEmpty ? slot.getNoItemIcon() : null;
        boolean hasIcon = noItemIcon != null;

        // "Displayed" stack (may be modified for drag-split / quick-craft preview)
        ItemStack displayStack = getValue();

        var mui = guiContext.modularUI;
        boolean hovered = self.isHover() || self.isSelfOrChildHover();
        boolean drawDraggingBackground = false;

        if (mui.getScreen() instanceof AbstractContainerScreen<?> containerScreen) {
            ItemStack carried = containerScreen.getMenu().getCarried();

            if (slot == containerScreen.clickedSlot
                    && !containerScreen.draggingItem.isEmpty()
                    && containerScreen.isSplittingStack
                    && !displayStack.isEmpty()) {
                displayStack = displayStack.copyWithCount(displayStack.getCount() / 2);
                drawDraggingBackground = true;
            } else if (containerScreen.isQuickCrafting
                    && containerScreen.quickCraftSlots.contains(slot)
                    && !carried.isEmpty()) {
                if (containerScreen.quickCraftSlots.size() == 1) {
                    return;
                }

                if (AbstractContainerMenu.canItemQuickReplace(slot, carried, true)
                        && containerScreen.getMenu().canDragTo(slot)) {
                    int max = Math.min(
                            carried.getMaxStackSize(),
                            slot.getMaxStackSize(carried)
                    );
                    int existing = slot.getItem().isEmpty() ? 0 : slot.getItem().getCount();
                    int placed =
                            AbstractContainerMenu.getQuickCraftPlaceCount(
                                    containerScreen.quickCraftSlots,
                                    containerScreen.quickCraftingType,
                                    carried
                            )
                                    + existing;

                    if (placed > max) placed = max;

                    // This is a preview; do NOT let it suppress placeholder logic.
                    displayStack = carried.copyWithCount(placed);
                    drawDraggingBackground = true;
                } else {
                    containerScreen.quickCraftSlots.remove(slot);
                    containerScreen.recalculateQuickCraftRemaining();
                }
            }
        }

        boolean hasItemToDraw = !displayStack.isEmpty();

        // If absolutely nothing to render, bail.
//        if (!drawDraggingBackground && !hovered && !hasItemToDraw && !hasIcon) return;

        UIElement element = (UIElement) (Object) this;

        float contentX = element.getContentX();
        float contentY = element.getContentY();
        float contentWidth = element.getContentWidth();
        float contentHeight = element.getContentHeight();
        if (contentWidth <= 0 || contentHeight <= 0) return;

        guiContext.pose.pushPose();
        guiContext.pose.scale(contentWidth / 16f, contentHeight / 16f, 1f);
        guiContext.pose.translate(
                contentX * 16f / contentWidth,
                contentY * 16f / contentHeight,
                -200f
        );

        if (drawDraggingBackground) {
            guiContext.drawTexture(ItemSlot.DRAGGING_BG, 0, 0, 16, 16);
        }

        // 1) Draw placeholder first (so preview/item can be on top)
        if (realEmpty && hasIcon) {
            TextureAtlasSprite sprite = Minecraft.getInstance()
                    .getTextureAtlas(noItemIcon.getFirst())
                    .apply(noItemIcon.getSecond());
            guiContext.graphics.blit(0, 0, 232, 16, 16, sprite);
        }

        // 2) Draw item / preview stack on top
        if (hasItemToDraw) {
            DrawerHelper.drawItemStack(guiContext.graphics, displayStack, 0, 0, -1, null);
        }

        if (hovered) {
            guiContext.drawTexture(
                    ((ItemSlot) (Object) this).getSlotStyle().hoverOverlay(),
                    0,
                    0,
                    16,
                    16
            );
        }

        guiContext.pose.popPose();
    }
}