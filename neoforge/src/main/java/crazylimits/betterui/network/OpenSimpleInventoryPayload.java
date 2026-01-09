package crazylimits.betterui.network;

import crazylimits.betterui.Constants;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import io.netty.buffer.ByteBuf;

// No extra data needed, but we still define a payload for type-safety
public record OpenSimpleInventoryPayload() implements CustomPacketPayload {

    public static final Type<OpenSimpleInventoryPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(
                    Constants.MOD_ID,
                    "open_simple_inventory"
            ));

    // No data -> use unit codec
    public static final StreamCodec<ByteBuf, OpenSimpleInventoryPayload>
            STREAM_CODEC = StreamCodec.unit(new OpenSimpleInventoryPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}