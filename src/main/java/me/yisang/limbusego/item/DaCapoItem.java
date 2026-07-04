package me.yisang.limbusego.item;

import net.minecraft.item.Item;

/**
 * DaCapo：取消一般攻擊改為連擊。普通 60% → 5 擊 @1.5 每 2 tick；
 * 特殊 40% → 3 擊 @5.0 每 4 tick。每擊波及半徑 3.5 格（非玩家/非馴服）受 70% 傷害，
 * 且每擊附沉淪 1p/1c。效果在 WeaponEvents。
 */
public class DaCapoItem extends Item {
    public DaCapoItem(Settings settings) {
        super(settings);
    }
}
