package me.yisang.limbusego.client;

import me.yisang.limbusego.gift.BaseGift;
import me.yisang.limbusego.gift.GiftRegistry;
import me.yisang.limbusego.item.WeaponTooltips;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.List;

/**
 * 客戶端 tooltip：未按 Shift 顯示提示列，按住 Shift 展開機制說明。
 *
 * <p>放在客戶端而非 LORE 元件，因為 Shift 是純客戶端狀態，而且飾品說明
 * 需要依 stack 的升級等級即時計算。
 *
 * <p>本 callback 在 vanilla 把名稱、LORE 等既有行加入 {@code lines} 之後才觸發，
 * 因此直接 append 即可讓說明出現在風味台詞之後。
 */
public final class EgoTooltipHandler {

    private EgoTooltipHandler() {}

    public static void register() {
        ItemTooltipCallback.EVENT.register((stack, context, type, lines) -> {
            List<Text> details = detailsFor(stack);
            if (details.isEmpty()) return;

            lines.add(Text.empty());
            if (Screen.hasShiftDown()) {
                lines.addAll(details);
                lines.add(Text.empty());
                lines.add(footerFor(stack));
            } else {
                lines.add(TooltipFormat.hint());
            }
        });
    }

    private static List<Text> detailsFor(ItemStack stack) {
        BaseGift gift = GiftRegistry.byItem(stack.getItem());
        if (gift != null) return gift.describe(stack);

        Identifier id = Registries.ITEM.getId(stack.getItem());
        if (!"limbusego".equals(id.getNamespace())) return List.of();
        return WeaponTooltips.of(id.getPath());
    }

    private static Text footerFor(ItemStack stack) {
        BaseGift gift = GiftRegistry.byItem(stack.getItem());
        return gift != null ? TooltipFormat.giftTag(gift.tier()) : TooltipFormat.egoTag();
    }
}
