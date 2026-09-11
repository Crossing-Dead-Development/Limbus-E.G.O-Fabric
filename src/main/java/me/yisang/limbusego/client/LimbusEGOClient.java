package me.yisang.limbusego.client;

import net.fabricmc.api.ClientModInitializer;

public class LimbusEGOClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        EgoTooltipHandler.register();
        net.minecraft.client.gui.screen.ingame.HandledScreens.register(
                me.yisang.limbusego.extractor.ModBlocks.EXTRACTOR_SCREEN_HANDLER, ExtractorScreen::new);
    }
}
