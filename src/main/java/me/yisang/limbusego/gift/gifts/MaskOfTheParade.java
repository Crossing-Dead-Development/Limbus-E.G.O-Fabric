package me.yisang.limbusego.gift.gifts;

import me.yisang.limbusego.gift.BaseGift;
import me.yisang.limbusego.status.StatusEffect;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Box;

/** 遊行的面具：潛行時持續隱身；攻擊施加流血 3·3；擊殺對半徑內敵擴散流血 2·2。 */
public class MaskOfTheParade extends BaseGift {

    public MaskOfTheParade() {
        super("mask_of_the_parade", 4); // Tier IV
    }

    @Override
    protected void onPassiveTick(ServerPlayerEntity player, ItemStack self) {
        if (player.isSneaking()) {
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.INVISIBILITY, 30, 0, true, false));
        } else {
            player.removeStatusEffect(StatusEffects.INVISIBILITY);
        }
    }

    @Override
    protected float onAttack(LivingEntity target, ServerPlayerEntity attacker, ItemStack self, float amount) {
        applyScaled(target, StatusEffect.BLEED, 3, 3, attacker, self);
        return amount;
    }

    @Override
    protected void onKill(LivingEntity victim, ServerPlayerEntity killer, ItemStack self) {
        double r = 5 * multiplier(self);
        Box box = victim.getBoundingBox().expand(r);
        for (LivingEntity e : victim.getWorld().getEntitiesByClass(LivingEntity.class, box,
                le -> le != killer && !(le instanceof PlayerEntity) && le.isAlive())) {
            apply(e, StatusEffect.BLEED, 2, 2, killer);
        }
    }
}
