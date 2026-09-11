package me.yisang.limbusego.extractor;

import me.yisang.limbusego.LimbusEGOMod;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.sound.BlockSoundGroup;

/** 提取機的方塊、BlockItem、BlockEntityType、ScreenHandlerType 註冊。 */
public final class ModBlocks {

    public static Block EXTRACTOR;
    public static Item EXTRACTOR_ITEM;
    public static BlockEntityType<ExtractorBlockEntity> EXTRACTOR_BLOCK_ENTITY;
    public static ScreenHandlerType<ExtractorScreenHandler> EXTRACTOR_SCREEN_HANDLER;

    private ModBlocks() {}

    public static void register() {
        var id = LimbusEGOMod.id("extractor");

        EXTRACTOR = Registry.register(Registries.BLOCK, id, new ExtractorBlock(AbstractBlock.Settings.create()
                .registryKey(RegistryKey.of(RegistryKeys.BLOCK, id))
                .strength(3.5f)
                .requiresTool()
                .sounds(BlockSoundGroup.METAL)));

        EXTRACTOR_ITEM = Registry.register(Registries.ITEM, id, new BlockItem(EXTRACTOR, new Item.Settings()
                .registryKey(RegistryKey.of(RegistryKeys.ITEM, id))
                .useBlockPrefixedTranslationKey()));

        EXTRACTOR_BLOCK_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE, id,
                FabricBlockEntityTypeBuilder.create(ExtractorBlockEntity::new, EXTRACTOR).build());

        // ScreenHandlerType 建構子由 fabric-transitive-access-wideners 開放
        EXTRACTOR_SCREEN_HANDLER = Registry.register(Registries.SCREEN_HANDLER, id,
                new ScreenHandlerType<>(ExtractorScreenHandler::new, FeatureFlags.VANILLA_FEATURES));
    }
}
