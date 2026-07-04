package me.yisang.limbusego.item;

import net.minecraft.item.Item;

/**
 * W 公司匕首：+4 攻擊 / -1.6 攻速。每次命中對自己疊充能（CHARGE，上限 10p，每擊 1p/5c，
 * 滿層則續 count）；20% 機率過載額外 +1p/+1c。效果在 WeaponEvents。
 */
public class WCorpKnifeItem extends Item {
    public WCorpKnifeItem(Settings settings) {
        super(settings);
    }
}
