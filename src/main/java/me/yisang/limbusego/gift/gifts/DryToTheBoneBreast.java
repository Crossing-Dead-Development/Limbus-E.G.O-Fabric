package me.yisang.limbusego.gift.gifts;

import me.yisang.limbusego.gift.BaseGift;
import me.yisang.limbusego.status.StatusEffect;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;

/** 乾巴柴澀雞胸肉：被動飽食度不流失、飽足時力量 I；攻擊破裂中目標延長破裂 2 層。 */
public class DryToTheBoneBreast extends BaseGift {

    public DryToTheBoneBreast() {
        super("dry_to_the_bone_breast", 3); // Tier III
    }

    @Override
    protected void onPassiveTick(ServerPlayerEntity player, ItemStack self) {
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.SATURATION, 30, 0, true, false));
        if (player.getHungerManager().getFoodLevel() >= 20) {
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, 30, 0, true, false));
        } else {
            player.removeStatusEffect(StatusEffects.STRENGTH);
        }
    }

    @Override
    protected float onAttack(LivingEntity target, ServerPlayerEntity attacker, ItemStack self, float amount) {
        if (has(target, StatusEffect.RUPTURE)) {
            status().refresh(target, StatusEffect.RUPTURE, 2);
        }
        return amount;
    }
}
