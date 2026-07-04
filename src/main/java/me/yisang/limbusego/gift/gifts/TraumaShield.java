package me.yisang.limbusego.gift.gifts;

import me.yisang.limbusego.gift.BaseGift;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;

/** 精神屏蔽力場：受傷時每 60 秒（隨升級縮短）吸收一次傷害並獲得 2 SAN。 */
public class TraumaShield extends BaseGift {

    public TraumaShield() {
        super("trauma_shield", 2); // Tier II
    }

    @Override
    protected float onDamaged(LivingEntity attacker, ServerPlayerEntity victim, ItemStack self, float amount) {
        if (gate(victim, (long) (60000 / multiplier(self)))) {
            sanity().gainSan(victim, 2);
            return 0f;
        }
        return amount;
    }
}
