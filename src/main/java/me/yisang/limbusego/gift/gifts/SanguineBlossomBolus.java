package me.yisang.limbusego.gift.gifts;

import me.yisang.limbusego.gift.BaseGift;
import me.yisang.limbusego.status.StatusEffect;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** 血花丸：脫離戰鬥（5 秒未受傷）持續緩慢回血；攻擊施加流血 2·2。 */
public class SanguineBlossomBolus extends BaseGift {

    private final Map<UUID, Long> lastDamaged = new HashMap<>();

    public SanguineBlossomBolus() {
        super("sanguine_blossom_bolus", 2); // Tier II
    }

    @Override
    protected float onAnyDamage(LivingEntity attacker, ServerPlayerEntity victim, ItemStack self, float amount) {
        lastDamaged.put(victim.getUuid(), nowMs(victim));
        return amount;
    }

    @Override
    protected void onPassiveTick(ServerPlayerEntity player, ItemStack self) {
        long last = lastDamaged.getOrDefault(player.getUuid(), 0L);
        if (nowMs(player) - last < 5000) return;
        int level = multiplier(self) >= 2.0 ? 1 : 0;
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 30, level, true, false));
    }

    @Override
    protected float onAttack(LivingEntity target, ServerPlayerEntity attacker, ItemStack self, float amount) {
        applyScaled(target, StatusEffect.BLEED, 2, 2, attacker, self);
        return amount;
    }

    @Override
    protected void onQuit(UUID playerId) {
        lastDamaged.remove(playerId);
    }
}
