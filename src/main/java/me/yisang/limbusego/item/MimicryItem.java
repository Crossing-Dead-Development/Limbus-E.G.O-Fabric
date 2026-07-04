package me.yisang.limbusego.item;

import net.minecraft.item.Item;

/**
 * 擬態：+12 攻擊 / -3.2 攻速。每次攻擊 10% 機率暴擊額外 40~90 傷害，
 * 吸取本次總傷害 25% 回血；暴擊時給自己強壯 3p/4c。效果在 WeaponEvents。
 */
public class MimicryItem extends Item {
    public MimicryItem(Settings settings) {
        super(settings);
    }
}
