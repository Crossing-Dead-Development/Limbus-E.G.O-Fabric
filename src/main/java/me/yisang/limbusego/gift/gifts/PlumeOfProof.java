package me.yisang.limbusego.gift.gifts;

import me.yisang.limbusego.gift.BaseGift;
import me.yisang.limbusego.status.StatusEffect;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;

/** 證明的羽飾：攻擊施加束縛 1·2 並獲得迅捷 1·2。 */
public class PlumeOfProof extends BaseGift {

    public PlumeOfProof() {
        super("plume_of_proof", 1); // Tier I
    }

    @Override
    protected float onAttack(LivingEntity target, ServerPlayerEntity attacker, ItemStack self, float amount) {
        applyScaled(target, StatusEffect.BIND, 1, 2, attacker, self);
        apply(attacker, StatusEffect.HASTE, 1, 2, attacker);
        return amount;
    }
}
