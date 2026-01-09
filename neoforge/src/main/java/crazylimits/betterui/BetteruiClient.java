package crazylimits.betterui;

import crazylimits.betterui.replacement.ReplacementsRegistry;
import crazylimits.betterui.replacements.SurvivalInventoryReplacement;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@EventBusSubscriber(modid = Constants.MOD_ID, value = Dist.CLIENT)
public class BetteruiClient {

    static {
        // Client-side part of replacements
        ReplacementsRegistry.registerClient(SurvivalInventoryReplacement.Client.INSTANCE);
    }

    @SubscribeEvent
    public static void registerMenuScreens(RegisterMenuScreensEvent event) {
        // Let registry register all screens from client replacements
        ReplacementsRegistry.registerMenuScreens(event);
    }
}