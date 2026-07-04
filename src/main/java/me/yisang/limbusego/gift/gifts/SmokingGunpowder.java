package me.yisang.limbusego.gift.gifts;

import me.yisang.limbusego.gift.BaseGift;
import me.yisang.limbusego.status.StatusEffect;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;

/** 有煙火藥：攻擊施加破裂 2·2，並使自身獲得迅捷 1·2。 */
public class SmokingGunpowder extends BaseGift {

    public SmokingGunpowder() {
        super("smoking_gunpowder", 2); // Tier II
    }

    @Override
    protected float onAttack(LivingEntity target, ServerPlayerEntity attacker, ItemStack self, float amount) {
        applyScaled(target, StatusEffect.RUPTURE, 2, 2, attacker, self);
        apply(attacker, StatusEffect.HASTE, 1, 2, attacker);
        return amount;
    }
}
