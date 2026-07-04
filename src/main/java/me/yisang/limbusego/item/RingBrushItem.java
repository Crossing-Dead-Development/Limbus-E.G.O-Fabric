package me.yisang.limbusego.item;

import net.minecraft.item.Item;

/**
 * 環指筆刷：右鍵目標造 3.5 傷 + 隨機原版負面效果 + Limbus 隨機屬性池 1p/3c；
 * 1.5 秒內對同目標二次右鍵 → 雙擊（2 次效果），單擊會使自己向前突進。效果在 WeaponEvents。
 */
public class RingBrushItem extends Item {
    public RingBrushItem(Settings settings) {
        super(settings);
    }
}
