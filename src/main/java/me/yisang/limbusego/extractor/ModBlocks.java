package me.yisang.limbusego.extractor;

import me.yisang.limbusego.LimbusEGOMod;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.screen.ScreenHandlerType;

/** 提取機的方塊、BlockEntityType、ScreenHandlerType 註冊。 */
public final class ModBlocks {

    public static ScreenHandlerType<ExtractorScreenHandler> EXTRACTOR_SCREEN_HANDLER;

    private ModBlocks() {}

    public static void register() {
        // ScreenHandlerType 建構子由 fabric-transitive-access-wideners 開放
        EXTRACTOR_SCREEN_HANDLER = Registry.register(Registries.SCREEN_HANDLER, LimbusEGOMod.id("extractor"),
                new ScreenHandlerType<>(ExtractorScreenHandler::new, FeatureFlags.VANILLA_FEATURES));
    }
}
