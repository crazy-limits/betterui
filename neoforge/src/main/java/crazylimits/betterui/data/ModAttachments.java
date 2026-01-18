package crazylimits.betterui.data;

import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public class ModAttachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, "betterui");

    public static final Supplier<AttachmentType<TrashData>> PLAYER_TRASH =
            ATTACHMENT_TYPES.register(
                    "player_trash",
                    () -> AttachmentType
                            .serializable(TrashData::new) // requires INBTSerializable, which TrashData is
                            .copyOnDeath()                // optional, if you want it on respawn
                            .build()
            );
}