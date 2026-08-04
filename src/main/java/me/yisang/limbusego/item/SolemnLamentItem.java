package me.yisang.limbusego.item;

import me.yisang.limbusego.event.WeaponEvents;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ChargedProjectilesComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

import java.util.List;
import java.util.function.Predicate;

/**
 * 莊嚴哀悼（黑／白）：真弩式兩段式擊發（底材為弩，對齊插件玩家動作與手感）。
 * 右鍵按住上弦（弩動畫，需背包有生蝶亡蝶）→ 裝填一顆並維持已裝填；再右鍵擊發蝴蝶彈幕（非箭矢）。
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

    /**
     * 擊發（CrossbowItem.use 已裝填時呼叫的正是此 7 參數版）：改發蝴蝶彈幕、不射原版箭矢，
     * 並清空 CHARGED_PROJECTILES 讓弩可再次上弦。
     */
    @Override
    public void shootAll(World world, LivingEntity shooter, Hand hand, ItemStack stack,
                         float speed, float divergence, LivingEntity target) {
        if (!world.isClient && world instanceof ServerWorld sw && shooter instanceof PlayerEntity player) {
            WeaponEvents.fireSolemnLament(player, sw, isBlack, hand);
        }
        // 原版 7 參數 shootAll 在此清空已裝填；我們未呼叫 super，需自行清除
        stack.set(DataComponentTypes.CHARGED_PROJECTILES, ChargedProjectilesComponent.of(List.of()));
    }
}
