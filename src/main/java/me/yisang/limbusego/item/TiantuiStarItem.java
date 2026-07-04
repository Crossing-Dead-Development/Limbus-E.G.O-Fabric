package me.yisang.limbusego.item;

import me.yisang.limbusego.event.WeaponEvents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.consume.UseAction;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

/**
 * 天退星刀 — 居合衝刺。
 * 右鍵（虎標彈）：定身蓄力 1 秒 → 衝刺，路徑傷 8 + 燃燒 3 秒 + 震顫 5p/6c + 燒傷 4p/3c。
 * 潛行右鍵（猛虎標彈）：定身蓄力 3 秒 → 更快更遠，路徑傷 18 + 燃燒 5 秒 + 凋零 II + 震顫 8p/6c + 燒傷 6p/4c。
 * 數值對照插件 TiantuiStar.java（v1.3.0）。
 */
public class TiantuiStarItem extends Item {

    public TiantuiStarItem(Settings settings) { super(settings); }

    @Override public UseAction getUseAction(ItemStack stack) { return UseAction.BOW; }

    @Override public int getMaxUseTime(ItemStack stack, LivingEntity user) { return 72000; }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        if (hand != Hand.MAIN_HAND) return ActionResult.PASS;
        boolean savage = user.isSneaking();
        boolean hasAmmo = savage
                ? WeaponEvents.hasSavageTigerMark(user)
                : WeaponEvents.hasTigerMark(user);
        if (!hasAmmo && !user.getAbilities().creativeMode) return ActionResult.FAIL;
        if (!world.isClient) WeaponEvents.startTiantuiCharge(user, world, savage);
        user.setCurrentHand(hand);
        return ActionResult.CONSUME;
    }

    @Override
    public void usageTick(World world, LivingEntity user, ItemStack stack, int remainingUseTicks) {
        if (!(user instanceof PlayerEntity player)) return;
        boolean savage = WeaponEvents.isTiantuiSavage(player);
        int required = savage ? 60 : 20;
        int drawTicks = getMaxUseTime(stack, user) - remainingUseTicks;

        user.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 4, 200, false, false, false));

        if (!world.isClient && world instanceof ServerWorld sw) {
            WeaponEvents.tiantuiChargeTick(player, sw, savage, drawTicks);
            if (drawTicks >= required) {
                WeaponEvents.fireTiantuiDash(player, sw, savage);
                player.stopUsingItem();
            }
        }
    }

    @Override
    public boolean onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
        if (user instanceof PlayerEntity player) WeaponEvents.cancelTiantuiCharge(player);
        return false;
    }
}
