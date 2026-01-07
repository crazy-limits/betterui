package crazylimits.betterui.mixin;

import crazylimits.betterui.ScreenReplacer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MixinMinecraft {

    @Shadow
    public abstract void setScreen(@Nullable Screen pGuiScreen);

    @Inject(method = "setScreen", at = @At("HEAD"), cancellable = true)
    private void betterui$setScreen(Screen screen, CallbackInfo ci) {
        if (screen == null) {
            return;
        }

        var replacement = ScreenReplacer.getReplacement(screen);

        // If there is a replacement, call the shadowed setScreen with that one
        if (replacement != screen) {
            ci.cancel(); // cancel original call
            this.setScreen(replacement);
        }
    }
}
