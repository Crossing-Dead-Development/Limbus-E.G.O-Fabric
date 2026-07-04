package me.yisang.limbusego.gift.gifts;

import me.yisang.limbusego.gift.BaseGift;
import me.yisang.limbusego.status.StatusEffect;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;

/** 手鏡：受擊時對攻擊者施加束縛 2·2 與脆弱 1·2。 */
public class HandheldMirror extends BaseGift {

    public HandheldMirror() {
        super("handheld_mirror", 4); // Tier IV
    }

    @Override
    protected float onDamaged(LivingEntity attacker, ServerPlayerEntity victim, ItemStack self, float amount) {
        if (attacker != null) {
            applyScaled(attacker, StatusEffect.BIND, 2, 2, victim, self);
            apply(attacker, StatusEffect.FRAGILE, 1, 2, victim);
        }
        return amount;
    }
}
