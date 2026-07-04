package me.yisang.limbusego.gift.gifts;

import me.yisang.limbusego.gift.BaseGift;
import me.yisang.limbusego.status.StatusEffect;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;

/** 留戀：攻擊獲得呼吸法 2·2；呼吸法達 5 時額外獲得強壯 2·1。 */
public class Finifugality extends BaseGift {

    public Finifugality() {
        super("finifugality", 3); // Tier III
    }

    @Override
    protected float onAttack(LivingEntity target, ServerPlayerEntity attacker, ItemStack self, float amount) {
        applyScaled(attacker, StatusEffect.POISE, 2, 2, attacker, self);
        if (pot(attacker, StatusEffect.POISE) >= 5) {
            applyScaled(attacker, StatusEffect.POWER, 2, 1, attacker, self);
        }
        return amount;
    }
}
