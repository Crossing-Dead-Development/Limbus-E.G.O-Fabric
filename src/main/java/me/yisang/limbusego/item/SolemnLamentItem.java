package me.yisang.limbusego.item;

import me.yisang.limbusego.event.WeaponEvents;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

/**
 * 莊嚴哀悼（黑／白）：**必須主手與副手各持一把、且一黑一白**才能射擊。
 *
 * <p>右鍵一下打一發，無上弦階段。兩把各自冷卻 {@link #COOLDOWN_TICKS}。
 *
 * <p><b>輪流發射由 vanilla 免費提供，本類別不需要選槍邏輯。</b>
 * {@code MinecraftClient.doItemUse} 會依序試 MAIN_HAND、OFF_HAND，並在結果
 * {@code isAccepted()} 時停止；而 {@code interactItem} 在呼叫 {@code use} 之前
 * 就會先檢查冷卻並回傳 PASS。因此主手可用時打主手（SUCCESS 中止迴圈），
 * 主手冷卻時自動落到副手，兩把都冷卻就什麼也不做——連點的結果即為
 * 黑→白→黑→白，約每 0.6 秒一發。
 *
 * <p>黑：命中 8 傷 + 凋零 II（4 秒）+ 沉淪 4p/3c　白：命中 4 傷 + 失明（3 秒）+ 沉淪 3p/2c
 * （命中效果與彈道皆在 {@link WeaponEvents}，本類別只負責觸發。）
 *
 * <p>未配對時右鍵不擊發、不消耗彈藥、不進冷卻，僅在動作列提示；左鍵近戰不受限制。
 */
public class SolemnLamentItem extends Item {

    /** 每把槍各自的冷卻，1.2 秒。 */
    public static final int COOLDOWN_TICKS = 24;

    public final boolean isBlack;

    public SolemnLamentItem(boolean isBlack, Settings settings) {
        super(settings);
        this.isBlack = isBlack;
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        ItemStack main = user.getStackInHand(Hand.MAIN_HAND);
        ItemStack off = user.getStackInHand(Hand.OFF_HAND);

        if (!SolemnLamentLogic.isPaired(stackIsSolemn(main), stackIsBlack(main),
                stackIsSolemn(off), stackIsBlack(off))) {
            // 只由主手提示，否則黑+黑之類的組合會在同一次點擊收到兩則訊息
            if (!world.isClient && hand == Hand.MAIN_HAND) {
                user.sendMessage(Text.translatable("msg.limbusego.solemn_lament.need_pair")
                        .styled(s -> s.withColor(0xFF5555)), true);
            }
            return ActionResult.FAIL;
        }

        // 彈藥檢查在兩端都做，避免客戶端先進冷卻但伺服端沒開槍
        ItemStack ammo = WeaponEvents.findButterfly(user);
        if (ammo == null) return ActionResult.FAIL;

        ItemStack gun = user.getStackInHand(hand);
        user.getItemCooldownManager().set(gun, COOLDOWN_TICKS);

        if (world instanceof ServerWorld sw) {
            ammo.decrement(1);
            WeaponEvents.fireSolemnLament(user, sw, stackIsBlack(gun), hand);
            // 再上膛聲：原本綁在上弦流程的資產，改接在擊發之後
            sw.playSound(null, user.getBlockPos(), ModSounds.SOLEMN_QUICK_LOAD_3,
                    SoundCategory.PLAYERS, 0.7f, 1.0f);
        }
        return ActionResult.SUCCESS;
    }

    private static boolean stackIsSolemn(ItemStack stack) {
        return stack.getItem() instanceof SolemnLamentItem;
    }

    /** 非莊嚴哀悼一律回傳 false（呼叫端會先用 stackIsSolemn 過濾）。 */
    private static boolean stackIsBlack(ItemStack stack) {
        return stack.getItem() instanceof SolemnLamentItem s && s.isBlack;
    }
}
