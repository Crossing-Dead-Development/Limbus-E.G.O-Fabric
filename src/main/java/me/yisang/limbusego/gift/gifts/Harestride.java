package me.yisang.limbusego.gift.gifts;

import me.yisang.limbusego.gift.BaseGift;
import me.yisang.limbusego.status.StatusEffect;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import me.yisang.limbusego.tooltip.TooltipFormat;
import net.minecraft.text.Text;
import java.util.List;

/** 卯足：被動速度 II、跳躍提升 I；速度效果中攻擊施加破裂 2·2。 */
public class Harestride extends BaseGift {

    public Harestride() {
        super("harestride", 2); // Tier II
    }

    @Override
    protected void onPassiveTick(ServerPlayerEntity player, ItemStack self) {
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 30, 1, true, false));
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.JUMP_BOOST, 30, 0, true, false));
    }

    @Override
    protected float onAttack(LivingEntity target, ServerPlayerEntity attacker, ItemStack self, float amount) {
        if (attacker.hasStatusEffect(StatusEffects.SPEED)) {
            applyScaled(target, StatusEffect.RUPTURE, 2, 2, attacker, self);
        }
        return amount;
    }

    @Override
    public List<Text> describe(int level) {
        return List.of(
            TooltipFormat.section("tooltip.limbusego.harestride.passive"),
            TooltipFormat.body("tooltip.limbusego.harestride.passive.buffs"),
            TooltipFormat.section("tooltip.limbusego.harestride.attack"),
            TooltipFormat.body("tooltip.limbusego.harestride.attack.rupture",
                    TooltipFormat.status(StatusEffect.RUPTURE), TooltipFormat.potency(scaled(2, level), 2)));
    }
}
