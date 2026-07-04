package me.yisang.limbusego.gift.gifts;

import me.yisang.limbusego.gift.BaseGift;
import me.yisang.limbusego.status.StatusEffect;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;

/** 明鏡止水：攻擊獲得呼吸法 3·2；擊殺獲得強壯 3·2。 */
public class ClearMirrorCalmWater extends BaseGift {

    public ClearMirrorCalmWater() {
        super("clear_mirror_calm_water", 4); // Tier IV
    }

    @Override
    protected float onAttack(LivingEntity target, ServerPlayerEntity attacker, ItemStack self, float amount) {
        applyScaled(attacker, StatusEffect.POISE, 3, 2, attacker, self);
        return amount;
    }

    @Override
    protected void onKill(LivingEntity victim, ServerPlayerEntity killer, ItemStack self) {
        applyScaled(killer, StatusEffect.POWER, 3, 2, killer, self);
    }
}
