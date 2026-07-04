package me.yisang.limbusego.gift.gifts;

import me.yisang.limbusego.gift.BaseGift;
import me.yisang.limbusego.status.StatusEffect;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;

/** 破碎的骨片：攻擊施加沉淪 2·2；攻擊抑鬱目標 +15% 傷害。 */
public class TangledBones extends BaseGift {

    public TangledBones() {
        super("tangled_bones", 2); // Tier II
    }

    @Override
    protected float onAttack(LivingEntity target, ServerPlayerEntity attacker, ItemStack self, float amount) {
        float dmg = amount;
        if (sanity().isDepressed(target)) {
            dmg *= 1.15f;
        }
        applyScaled(target, StatusEffect.SINKING, 2, 2, attacker, self);
        return dmg;
    }
}
