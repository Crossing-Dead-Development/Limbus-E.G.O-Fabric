package me.yisang.limbusego.gift.gifts;

import me.yisang.limbusego.gift.BaseGift;
import me.yisang.limbusego.status.StatusEffect;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;

/** 彼方之星：攻擊沉淪中目標時獲得 1 SAN（隨升級）並延長沉淪 1 層。 */
public class DistantStar extends BaseGift {

    public DistantStar() {
        super("distant_star", 3); // Tier III
    }

    @Override
    protected float onAttack(LivingEntity target, ServerPlayerEntity attacker, ItemStack self, float amount) {
        if (has(target, StatusEffect.SINKING)) {
            sanity().gainSan(attacker, (int) Math.round(1 * multiplier(self)));
            status().refresh(target, StatusEffect.SINKING, 1);
        }
        return amount;
    }
}
