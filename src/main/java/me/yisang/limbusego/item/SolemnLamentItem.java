package me.yisang.limbusego.item;

import me.yisang.limbusego.event.WeaponEvents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.consume.UseAction;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

/**
 * 莊嚴哀悼（黑／白）：弩式兩段式擊發（對齊插件，底材為弩）。
 * 第一次右鍵按住 → 弩上弦動作（快速上弦約 0.4 秒）→ 充能完成，消耗一顆生蝶亡蝶、進入「已裝填」並維持。
 * 第二次右鍵 → 擊發彈幕。每手獨立追蹤已裝填狀態，可於主手或副手使用。
 * 黑：命中 8 傷 + 凋零 II（4 秒）+ 沉淪 4p/3c　白：命中 4 傷 + 失明（3 秒）+ 沉淪 3p/2c
 * 機制／數值對照插件 solemnlament.java + LimbusEGO 弩式擊發（隱藏 QUICK_CHARGE V）。
 */
public class SolemnLamentItem extends Item {

    /** 快速上弦所需 tick（插件 QUICK_CHARGE V 換算後約 0.4 秒）。 */
    private static final int CHARGE_TICKS = 8;

    public final boolean isBlack;

    public SolemnLamentItem(boolean isBlack, Settings settings) {
        super(settings);
        this.isBlack = isBlack;
    }

    /** 弩持握／上弦手臂動作。 */
    @Override public UseAction getUseAction(ItemStack stack) { return UseAction.CROSSBOW; }

    @Override public int getMaxUseTime(ItemStack stack, LivingEntity user) { return 72000; }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        if (WeaponEvents.isSolemnCharged(user, hand)) {
            // 第二段：已裝填 → 擊發
            if (!world.isClient) {
                WeaponEvents.fireSolemnLament(user, (ServerWorld) world, isBlack, hand);
                WeaponEvents.setSolemnCharged(user, hand, false);
            }
            return ActionResult.SUCCESS;
        }
        // 第一段：開始上弦（需有彈藥）
        if (WeaponEvents.findButterfly(user) == null && !user.getAbilities().creativeMode) {
            return ActionResult.FAIL;
        }
        user.setCurrentHand(hand);
        return ActionResult.CONSUME;
    }

    @Override
    public void usageTick(World world, LivingEntity user, ItemStack stack, int remainingUseTicks) {
        if (world.isClient || !(user instanceof PlayerEntity player)) return;
        Hand hand = player.getActiveHand();
        if (WeaponEvents.isSolemnCharged(player, hand)) return; // 已裝填則不重複充能

        int drawTicks = getMaxUseTime(stack, user) - remainingUseTicks;
        if (drawTicks < CHARGE_TICKS) return;

        // 上弦完成：消耗彈藥、進入已裝填、播裝填完成音、結束上弦動作
        ItemStack ammo = WeaponEvents.findButterfly(player);
        if (ammo == null && !player.getAbilities().creativeMode) {
            player.stopUsingItem();
            return;
        }
        if (ammo != null) ammo.decrement(1);
        WeaponEvents.setSolemnCharged(player, hand, true);
        world.playSound(null, player.getBlockPos(), ModSounds.SOLEMN_QUICK_LOAD_3,
                SoundCategory.PLAYERS, 0.6f, 1.0f);
        player.stopUsingItem();
    }

    @Override
    public boolean onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
        // 提前放開（未達充能）→ 不裝填、不耗彈藥（已裝填狀態於 usageTick 處理，此處無需動作）
        return false;
    }
}
