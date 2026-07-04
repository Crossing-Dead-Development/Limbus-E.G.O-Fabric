package me.yisang.limbusego.gift.gifts;

import me.yisang.limbusego.gift.BaseGift;
import me.yisang.limbusego.status.StatusEffect;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;

/** 首席管家的秘籍：攻擊施加束縛 2·2；擊殺時回復 2 點生命。 */
public class ChiefButlersSecretArts extends BaseGift {

    public ChiefButlersSecretArts() {
        super("chief_butlers_secret_arts", 3); // Tier III
    }

    @Override
    protected float onAttack(LivingEntity target, ServerPlayerEntity attacker, ItemStack self, float amount) {
        applyScaled(target, StatusEffect.BIND, 2, 2, attacker, self);
        return amount;
    }

    @Override
    protected void onKill(LivingEntity victim, ServerPlayerEntity killer, ItemStack self) {
        killer.setHealth((float) Math.min(killer.getMaxHealth(), killer.getHealth() + 2.0));
    }
}
