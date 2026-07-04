package me.yisang.limbusego.item;

import me.yisang.limbusego.event.WeaponEvents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.consume.UseAction;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

/**
 * 薄暝 Twilight。近戰瀕死增傷 + 30% 真實傷害（在 WeaponEvents.onAttack）。
 * 潛行右鍵：蓄力 1.5 秒 → 暮光斬（前方扇形波 + 凋零 II + 破裂 5p/2c），6 秒冷卻。
 * 數值對照插件 TwilightWeapon.java（v1.3.0）。
 */
public class TwilightItem extends Item {

    private static final int CHARGE_TICKS = 30;

    public TwilightItem(Settings settings) { super(settings); }

    @Override public UseAction getUseAction(ItemStack stack) { return UseAction.SPEAR; }

    @Override public int getMaxUseTime(ItemStack stack, LivingEntity user) { return 72000; }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        if (hand != Hand.MAIN_HAND) return ActionResult.PASS;
        if (!user.isSneaking()) return ActionResult.PASS;
        if (!world.isClient && world instanceof ServerWorld sw) {
            if (!WeaponEvents.twilightSpecialReady(user)) return ActionResult.FAIL;
            WeaponEvents.twilightChargeStart(user, sw);
        }
        user.setCurrentHand(hand);
        return ActionResult.CONSUME;
    }

    @Override
    public void usageTick(World world, LivingEntity user, ItemStack stack, int remainingUseTicks) {
        if (!(user instanceof PlayerEntity player)) return;
        int drawTicks = getMaxUseTime(stack, user) - remainingUseTicks;
        if (!world.isClient && world instanceof ServerWorld sw) {
            WeaponEvents.twilightChargeTick(player, sw);
            if (drawTicks >= CHARGE_TICKS) {
                WeaponEvents.twilightSlash(player, sw);
                player.stopUsingItem();
            }
        }
    }
}
