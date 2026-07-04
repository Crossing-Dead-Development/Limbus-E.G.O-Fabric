package me.yisang.limbusego.item;

import net.minecraft.item.Item;

/**
 * 聖宣盾牌：持有（主手或副手）時每 5 tick 對半徑 5 格內生物施加緩速 II 與束縛，
 * 並為自身補守護（PROTECTION，上限 3）。效果邏輯在 WeaponEvents.tickShieldAura。
 */
public class SolemnShieldItem extends Item {
    public SolemnShieldItem(Settings settings) {
        super(settings);
    }
}
