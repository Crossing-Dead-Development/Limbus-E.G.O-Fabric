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
 * 鴻璐·脛骨 Tibia。近戰疊流血 + Melody 增傷（在 WeaponEvents.onAttack）。
 * 潛行右鍵：蓄力 2 秒 → 解剖斬（前方 5 格扇形，重疊流血並強制引爆），8 秒冷卻。
 * 數值對照插件 TibiaWeapon.java（v1.3.0）。
 */
public class TibiaItem extends Item {

    private static final int CHARGE_TICKS = 40;

    public TibiaItem(Settings settings) { super(settings); }

    @Override public UseAction getUseAction(ItemStack stack) { return UseAction.SPEAR; }

    @Override public int getMaxUseTime(ItemStack stack, LivingEntity user) { return 72000; }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        if (hand != Hand.MAIN_HAND) return ActionResult.PASS;
        if (!user.isSneaking()) return ActionResult.PASS;
        if (!world.isClient && world instanceof ServerWorld sw) {
            if (!WeaponEvents.tibiaSpecialReady(user)) return ActionResult.FAIL;
            WeaponEvents.tibiaChargeStart(user, sw);
        }
        user.setCurrentHand(hand);
        return ActionResult.CONSUME;
    }

    @Override
    public void usageTick(World world, LivingEntity user, ItemStack stack, int remainingUseTicks) {
        if (!(user instanceof PlayerEntity player)) return;
        int drawTicks = getMaxUseTime(stack, user) - remainingUseTicks;
        if (!world.isClient && world instanceof ServerWorld sw) {
            WeaponEvents.tibiaChargeTick(player, sw);
            if (drawTicks >= CHARGE_TICKS) {
                WeaponEvents.tibiaAnatomize(player, sw);
                player.stopUsingItem();
            }
        }
    }
}
