package crazylimits.betterui.mixin;

import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.UI;
import com.lowdragmc.lowdraglib2.utils.XmlUtils;
import crazylimits.betterui.Constants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import org.w3c.dom.Document;

@Mixin(InventoryScreen.class)
public class InventoryScreenMixin {
//    @Unique
//    private final InventoryScreen betterui$self = (InventoryScreen) (Object) this;
//
//    @Unique
//    private final ResourceLocation betterui$xmlLocation = ResourceLocation.fromNamespaceAndPath(
//            Constants.MOD_ID, "gui/inventory_screen.xml"
//    );
//    @Unique
//    private Document betterui$xmlDocument = null;
//
//    @Inject(method = "init", at = @At("TAIL"))
//    private void betterui$init(CallbackInfo ci) {
//        betterui$xmlDocument = XmlUtils.loadXml(betterui$xmlLocation);
//        if (betterui$xmlDocument != null) {
//            var ui = UI.of(betterui$xmlDocument);
//            var modularUi = ModularUI.of(ui, Minecraft.getInstance().player);
//            modularUi.setScreenAndInit(betterui$self);
//            GuiEventListener widget = modularUi.getWidget();
//
//            ((ScreenInvoker) betterui$self).betterui$invokeClearWidgets();
//            ((ScreenInvoker) betterui$self).betterui$invokeAddRenderableWidget(widget);
//        }
//    }
//
//    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
//    private void betterui$render(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick, CallbackInfo ci) {
//        if (betterui$xmlDocument != null) {
//            ci.cancel();
//        }
//    }
//
//    @Inject(method = "renderBg", at = @At("HEAD"), cancellable = true)
//    private void betterui$renderBg(GuiGraphics pGuiGraphics, float pPartialTick, int pMouseX, int pMouseY, CallbackInfo ci) {
//        if (betterui$xmlDocument != null) {
//            ci.cancel();
//        }
//    }
}