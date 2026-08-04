package me.yisang.limbusego.item;

import me.yisang.limbusego.event.WeaponEvents;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

/**
 * 莊嚴哀悼（黑／白）：弩式裝填擊發，右鍵消耗一顆生蝶亡蝶發射彈幕，每手獨立 0.4 秒（8 tick）冷卻。
 * 可雙手持槍：某手冷卻中時 use() 回 PASS，交棒給另一手 → 左右手交替擊發（快速右鍵等於雙倍射速）。
 * 黑：命中 8 傷 + 凋零 II（4 秒）+ 沉淪 4p/3c
 * 白：命中 4 傷 + 失明（3 秒）+ 沉淪 3p/2c
 * 機制／數值對照插件 solemnlament.java + LimbusEGO 弩式擊發（隱藏 QUICK_CHARGE V）。
 */
public class SolemnLamentItem extends Item {
    public final boolean isBlack;

    public SolemnLamentItem(boolean isBlack, Settings settings) {
        super(settings);
        this.isBlack = isBlack;
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        // 此手冷卻中 → PASS，讓互動落到另一手（實現左右手交替擊發）
        if (WeaponEvents.isSolemnHandCooling(user, hand)) {
            return ActionResult.PASS;
        }
        // 無彈藥 → PASS（另一手同樣查共用背包，仍無則不擊發）
        if (WeaponEvents.findButterfly(user) == null && !user.getAbilities().creativeMode) {
            return ActionResult.PASS;
        }
        if (!world.isClient) {
            WeaponEvents.fireSolemnLament(user, (ServerWorld) world, isBlack, hand);
        }
        return ActionResult.SUCCESS;
    }
}
