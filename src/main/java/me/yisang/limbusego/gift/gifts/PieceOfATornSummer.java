package me.yisang.limbusego.gift.gifts;

import me.yisang.limbusego.gift.BaseGift;
import me.yisang.limbusego.status.StatusEffect;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;

/** 破碎之夏的殘片：受火焰或熔岩傷害時獲得強壯 2·2。 */
public class PieceOfATornSummer extends BaseGift {

    public PieceOfATornSummer() {
        super("piece_of_a_torn_summer", 4); // Tier IV
    }

    @Override
    protected float onAnyDamage(LivingEntity attacker, ServerPlayerEntity victim, ItemStack self, float amount,
                                DamageSource source) {
        if (source.isOf(DamageTypes.IN_FIRE) || source.isOf(DamageTypes.ON_FIRE)
                || source.isOf(DamageTypes.HOT_FLOOR) || source.isOf(DamageTypes.LAVA)) {
            applyScaled(victim, StatusEffect.POWER, 2, 2, victim, self);
        }
        return amount;
    }
}
