package crazylimits.betterui;

import crazylimits.betterui.network.BetteruiNetwork;
import crazylimits.betterui.replacement.ReplacementsRegistry;
import crazylimits.betterui.replacements.SurvivalInventoryReplacement;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(Constants.MOD_ID)
public class Betterui {

    public Betterui(IEventBus modBus) {
        Constants.LOG.info("Hello NeoForge world!");
        CommonClass.init();

        ModMenuTypes.register(modBus);

        // Networking
        modBus.addListener(BetteruiNetwork::registerPayloads);

        // Register server side of replacements
        ReplacementsRegistry.registerServer(SurvivalInventoryReplacement.Server.INSTANCE);
    }
}