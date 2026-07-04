package me.yisang.limbusego.gift.gifts;

import me.yisang.limbusego.gift.BaseGift;
import me.yisang.limbusego.status.StatusEffect;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;

/** 近身格鬥手冊：近戰攻擊獲得呼吸法 2·2。 */
public class CQCManual extends BaseGift {

    public CQCManual() {
        super("cqc_manual", 2); // Tier II
    }

    @Override
    protected float onAttack(LivingEntity target, ServerPlayerEntity attacker, ItemStack self, float amount) {
        applyScaled(attacker, StatusEffect.POISE, 2, 2, attacker, self);
        return amount;
    }
}
