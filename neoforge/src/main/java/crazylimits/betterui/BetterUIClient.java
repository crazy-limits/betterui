package crazylimits.betterui;

import com.mojang.blaze3d.platform.InputConstants;
import crazylimits.betterui.screens.BetterInventoryScreen;
import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.client.settings.KeyModifier;
import net.neoforged.neoforge.common.util.Lazy;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = Constants.MOD_ID, value = Dist.CLIENT)
public class BetterUIClient {
    public static final Lazy<KeyMapping> TRASH_HOTKEY = Lazy.of(() -> new KeyMapping(
            "key.betterui.trash",
            KeyConflictContext.UNIVERSAL,
            KeyModifier.NONE,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_DELETE,
            KeyMapping.CATEGORY_INVENTORY
    ));

    @SubscribeEvent
    public static void registerMenuScreens(RegisterMenuScreensEvent event) {
        Constants.LOG.info("BetterUI Client initializing");

        event.register(
                BetterUIMenuTypes.INVENTORY.get(),
                BetterInventoryScreen::new
        );
    }

    @SubscribeEvent // on the mod event bus only on the physical client
    public static void registerBindings(RegisterKeyMappingsEvent event) {
        event.register(TRASH_HOTKEY.get());
    }
}