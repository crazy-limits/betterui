package crazylimits.betterui.network;

import crazylimits.betterui.menus.BetterInventoryMenu;
import crazylimits.betterui.Constants;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;

public record TrashDeletePayload(int containerId, int slotIndex)
        implements CustomPacketPayload {

    public static final Type<TrashDeletePayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(
                    Constants.MOD_ID,
                    "trash_delete"
            ));

    public static final StreamCodec<FriendlyByteBuf, TrashDeletePayload> STREAM_CODEC =
            CustomPacketPayload.codec(
                    TrashDeletePayload::write,
                    TrashDeletePayload::new
            );

    public TrashDeletePayload(FriendlyByteBuf buf) {
        this(buf.readVarInt(), buf.readVarInt());
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeVarInt(containerId);
        buf.writeVarInt(slotIndex);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    /** Server-side handler logic */
    public static void handle(TrashDeletePayload payload, ServerPlayer player) {
        player.server.execute(() -> {
            AbstractContainerMenu menu = player.containerMenu;
            if (menu == null || menu.containerId != payload.containerId()) {
                return;
            }

            int idx = payload.slotIndex();
            if (idx < 0 || idx >= menu.slots.size()) {
                return;
            }

            Slot src = menu.slots.get(idx);
            if (!src.hasItem()) {
                return;
            }

            if (menu instanceof BetterInventoryMenu betterInv) {
                betterInv.moveSlotToTrash(src, player);
            } else {
                // Fallback: just clear/drop
                var stack = src.remove(src.getItem().getCount());
                player.drop(stack, false);
                menu.broadcastChanges();
                Constants.LOG.info("TrashDeletePayload: fallback delete from slot {} {}", idx, stack);
            }
        });
    }
}