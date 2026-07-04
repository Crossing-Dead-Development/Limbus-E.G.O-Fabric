package me.yisang.limbusego.gift;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;

/**
 * 鐵砧殘影升級的物品層邏輯：把 {@link GiftUpgradeLogic} 的純規則套到實際 ItemStack。
 * 由 {@link me.yisang.limbusego.mixin.AnvilScreenHandlerMixin} 於 updateResult 呼叫。
 */
public final class GiftUpgrade {

    private GiftUpgrade() {}

    /**
     * 嘗試以 right（殘影）升級 left（飾品）。
     *
     * @return 升級後的飾品（等級 +1、更新 lore）；若非「飾品＋同階殘影且未達上限」則回 {@code null}
     */
    public static ItemStack tryUpgrade(ItemStack left, ItemStack right) {
        if (left == null || right == null || left.isEmpty() || right.isEmpty()) return null;

        BaseGift gift = GiftRegistry.byItem(left.getItem());
        if (gift == null) return null;

        int vestigeTier = Vestiges.tierOf(right.getItem());
        if (vestigeTier < 0) return null;

        int oldLevel = left.getOrDefault(ModComponents.GIFT_LEVEL, 0);
        int newLevel = GiftUpgradeLogic.resolveUpgrade(gift.tier(), vestigeTier, oldLevel);
        if (newLevel < 0) return null;

        ItemStack result = left.copy();
        result.setCount(1);
        result.set(ModComponents.GIFT_LEVEL, newLevel);
        applyUpgradeLore(result, oldLevel, newLevel);
        return result;
    }

    /**
     * 更新升級 lore：本系統的升級行恆為最後一行，故舊等級 > 0 時先移除末行再補新行，
     * 不影響飾品原有描述行（未來 80 飾品各帶描述時仍成立）。
     */
    private static void applyUpgradeLore(ItemStack stack, int oldLevel, int newLevel) {
        LoreComponent existing = stack.get(DataComponentTypes.LORE);
        List<Text> lines = existing == null ? new ArrayList<>() : new ArrayList<>(existing.lines());
        if (oldLevel > 0 && !lines.isEmpty()) {
            lines.remove(lines.size() - 1);
        }
        lines.add(Text.translatable("item.limbusego.gift.upgrade_lore", newLevel)
                .styled(s -> s.withColor(Formatting.GRAY).withItalic(false)));
        stack.set(DataComponentTypes.LORE, new LoreComponent(lines));
    }
}
