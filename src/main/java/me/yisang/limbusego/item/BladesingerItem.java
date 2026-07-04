package me.yisang.limbusego.item;

import net.minecraft.item.Item;

/**
 * 著影揮刀 Bladesinger。近戰對自己疊呼吸法（POISE）提高爆擊率（在 WeaponEvents.onAttack）；
 * 低血（&lt; 3 顆心）潛行右鍵目標 → 肉斬骨斷五連斬，12 秒冷卻。效果在 WeaponEvents。
 * 數值對照插件 ShadowBladesinger.java（v1.3.0）。
 */
public class BladesingerItem extends Item {
    public BladesingerItem(Settings settings) {
        super(settings);
    }
}
