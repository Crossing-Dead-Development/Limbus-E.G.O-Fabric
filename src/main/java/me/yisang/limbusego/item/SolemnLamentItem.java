package me.yisang.limbusego.item;

import me.yisang.limbusego.event.WeaponEvents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;

import java.util.List;
import java.util.function.Predicate;

/**
 * 莊嚴哀悼（黑／白）：真弩式兩段式擊發（底材為弩，對齊插件玩家動作與手感）。
 * 右鍵按住上弦（弩動畫）→ 裝填一顆生蝶亡蝶並維持已裝填；再右鍵擊發蝴蝶彈幕（非箭矢）。
 * 黑：命中 8 傷 + 凋零 II（4 秒）+ 沉淪 4p/3c　白：命中 4 傷 + 失明（3 秒）+ 沉淪 3p/2c
 * 機制／數值對照插件 solemnlament.java + LimbusEGO 弩式擊發。
 */
public class SolemnLamentItem extends CrossbowItem {

    public final boolean isBlack;

    public SolemnLamentItem(boolean isBlack, Settings settings) {
        super(settings);
        this.isBlack = isBlack;
    }

    /** 可裝填的彈藥＝生蝶亡蝶（非箭矢）。 */
    @Override
    public Predicate<ItemStack> getProjectiles() {
        return stack -> stack.isOf(ModItems.BUTTERFLY_QUARTZ);
    }

    @Override
    public Predicate<ItemStack> getHeldProjectiles() {
        return getProjectiles();
    }

    @Override
    public int getRange() {
        return 8;
    }

    /** 擊發：改發蝴蝶彈幕，不射出原版箭矢；並清除已裝填狀態讓弩可再次上弦。 */
    @Override
    public void shootAll(ServerWorld world, LivingEntity shooter, Hand hand, ItemStack stack,
                         List<ItemStack> projectiles, float speed, float divergence,
                         boolean critical, LivingEntity target) {
        if (shooter instanceof PlayerEntity player) {
            WeaponEvents.fireSolemnLament(player, world, isBlack, hand);
        }
        // 原版在 shootAll 內清空 CHARGED_PROJECTILES；我們未呼叫 super，需自行清除
        stack.set(net.minecraft.component.DataComponentTypes.CHARGED_PROJECTILES,
                net.minecraft.component.type.ChargedProjectilesComponent.of(java.util.List.of()));
    }
}
