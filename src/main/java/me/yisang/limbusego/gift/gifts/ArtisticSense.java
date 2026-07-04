package me.yisang.limbusego.gift.gifts;

import me.yisang.limbusego.gift.BaseGift;
import me.yisang.limbusego.status.StatusEffect;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;

/** 美感：攻擊施加沉淪 2·2；攻擊沉淪中或抑鬱目標 +25% 傷害。 */
public class ArtisticSense extends BaseGift {

    public ArtisticSense() {
        super("artistic_sense", 4); // Tier IV
    }

    @Override
    protected float onAttack(LivingEntity target, ServerPlayerEntity attacker, ItemStack self, float amount) {
        float dmg = amount;
        if (has(target, StatusEffect.SINKING) || sanity().isDepressed(target)) {
            dmg *= 1.25f;
        }
        applyScaled(target, StatusEffect.SINKING, 2, 2, attacker, self);
        return dmg;
    }
}
