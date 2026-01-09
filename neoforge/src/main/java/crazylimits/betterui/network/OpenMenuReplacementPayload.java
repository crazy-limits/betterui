package crazylimits.betterui.network;

import crazylimits.betterui.Constants;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record OpenMenuReplacementPayload(ResourceLocation id)
        implements CustomPacketPayload {

    public static final Type<OpenMenuReplacementPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(
                    Constants.MOD_ID,
                    "open_menu_replacement"
            ));

    public static final StreamCodec<ByteBuf, OpenMenuReplacementPayload>
            STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC,
            OpenMenuReplacementPayload::id,
            OpenMenuReplacementPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}