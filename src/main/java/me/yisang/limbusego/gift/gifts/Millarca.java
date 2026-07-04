package me.yisang.limbusego.gift.gifts;

import me.yisang.limbusego.gift.BaseGift;
import me.yisang.limbusego.status.StatusEffect;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;

/** 蜜拉卡：攻擊施加流血 2·2；攻擊流血中目標時偷取 1 點生命（隨升級提升）。 */
public class Millarca extends BaseGift {

    public Millarca() {
        super("millarca", 4); // Tier IV
    }

    @Override
    protected float onAttack(LivingEntity target, ServerPlayerEntity attacker, ItemStack self, float amount) {
        boolean wasBleeding = has(target, StatusEffect.BLEED);
        applyScaled(target, StatusEffect.BLEED, 2, 2, attacker, self);
        if (wasBleeding) {
            double heal = 1 * multiplier(self);
            attacker.setHealth((float) Math.min(attacker.getMaxHealth(), attacker.getHealth() + heal));
        }
        return amount;
    }
}
