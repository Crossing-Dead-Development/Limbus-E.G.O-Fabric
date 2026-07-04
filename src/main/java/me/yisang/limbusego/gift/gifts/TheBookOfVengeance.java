package me.yisang.limbusego.gift.gifts;

import me.yisang.limbusego.gift.BaseGift;
import me.yisang.limbusego.status.StatusEffect;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;

/** 復仇帳簿：受擊獲得強壯 2·2 與守護 1·2。 */
public class TheBookOfVengeance extends BaseGift {

    public TheBookOfVengeance() {
        super("the_book_of_vengeance", 4); // Tier IV
    }

    @Override
    protected float onDamaged(LivingEntity attacker, ServerPlayerEntity victim, ItemStack self, float amount) {
        applyScaled(victim, StatusEffect.POWER, 2, 2, victim, self);
        apply(victim, StatusEffect.PROTECTION, 1, 2, victim);
        return amount;
    }
}
