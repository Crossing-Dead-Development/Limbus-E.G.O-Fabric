package me.yisang.limbusego.item;

import me.yisang.limbusego.LimbusEGOMod;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;

/**
 * 兩個自訂創造模式頁籤：E.G.O 武器／E.G.O 飾品。
 * 依設計決策，本模組物品一律不進原版頁籤。
 */
public class ModItemGroups {

    public static final RegistryKey<ItemGroup> WEAPONS_GROUP_KEY = RegistryKey.of(
            RegistryKeys.ITEM_GROUP, LimbusEGOMod.id("weapons"));

    public static final RegistryKey<ItemGroup> GIFTS_GROUP_KEY = RegistryKey.of(
            RegistryKeys.ITEM_GROUP, LimbusEGOMod.id("gifts"));

    public static void register() {
        Registry.register(Registries.ITEM_GROUP, WEAPONS_GROUP_KEY,
                FabricItemGroup.builder()
                        .icon(() -> new ItemStack(ModItems.BUTTERFLY_QUARTZ))
                        .displayName(Text.translatable("itemGroup.limbusego.weapons"))
                        .entries((context, entries) -> {
                            entries.add(ModItems.SOLEMN_LAMENT_BLACK);
                            entries.add(ModItems.SOLEMN_LAMENT_WHITE);
                            entries.add(ModItems.BUTTERFLY_QUARTZ);
                            entries.add(ModItems.SOLEMN_SHIELD);
                            entries.add(ModItems.TIGER_MARK);
                            entries.add(ModItems.SAVAGE_TIGER_MARK);
                        })
                        .build());

        Registry.register(Registries.ITEM_GROUP, GIFTS_GROUP_KEY,
                FabricItemGroup.builder()
                        .icon(() -> new ItemStack(ModItems.MOD_ICON))
                        .displayName(Text.translatable("itemGroup.limbusego.gifts"))
                        .entries((context, entries) -> {
                            // Phase 2：80 件飾品與殘影材料在此加入
                        })
                        .build());
    }
}
