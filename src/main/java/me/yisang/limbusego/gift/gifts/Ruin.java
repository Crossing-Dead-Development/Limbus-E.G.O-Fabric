package me.yisang.limbusego.gift.gifts;

import me.yisang.limbusego.gift.BaseGift;
import me.yisang.limbusego.status.StatusEffect;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;

/** 破滅：攻擊施加破裂 3·2；對已破裂目標追加脆弱 1·1，但自身損失 0.5 生命。 */
public class Ruin extends BaseGift {

    public Ruin() {
        super("ruin", 4); // Tier IV
    }

    @Override
    protected float onAttack(LivingEntity target, ServerPlayerEntity attacker, ItemStack self, float amount) {
        boolean already = has(target, StatusEffect.RUPTURE);
        applyScaled(target, StatusEffect.RUPTURE, 3, 2, attacker, self);
        if (already) {
            apply(target, StatusEffect.FRAGILE, 1, 1, attacker);
            attacker.setHealth((float) Math.max(1.0, attacker.getHealth() - 0.5));
        }
        return amount;
    }
}
