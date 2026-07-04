package me.yisang.limbusego.gift.gifts;

import me.yisang.limbusego.gift.BaseGift;
import me.yisang.limbusego.status.StatusEffect;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.BeeEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Box;

/**
 * 蜂王漿香水：被動使附近蜜蜂停止攻擊自己；
 * 受擊時對燒傷中的攻擊者 -15% 傷害，並對攻擊者施加燒傷 2·2。
 */
public class RoyalJellyPerfume extends BaseGift {

    public RoyalJellyPerfume() {
        super("royal_jelly_perfume", 4); // Tier IV
    }

    @Override
    protected void onPassiveTick(ServerPlayerEntity player, ItemStack self) {
        Box box = player.getBoundingBox().expand(8);
        for (BeeEntity bee : player.getWorld().getEntitiesByClass(BeeEntity.class, box,
                b -> player.equals(b.getTarget()))) {
            bee.setTarget(null);
        }
    }

    @Override
    protected float onDamaged(LivingEntity attacker, ServerPlayerEntity victim, ItemStack self, float amount) {
        if (attacker == null) return amount;
        float dmg = amount;
        if (has(attacker, StatusEffect.BURN)) {
            dmg *= 0.85f;
        }
        applyScaled(attacker, StatusEffect.BURN, 2, 2, victim, self);
        return dmg;
    }
}
