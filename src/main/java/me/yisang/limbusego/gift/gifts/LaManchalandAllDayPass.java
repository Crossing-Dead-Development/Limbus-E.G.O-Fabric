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

/** 拉．曼查樂園自由通行證：被動速度 I＋跳躍提升 I；攻擊流血中目標獲得呼吸法 2·2。 */
public class LaManchalandAllDayPass extends BaseGift {

    public LaManchalandAllDayPass() {
        super("la_manchaland_all_day_pass", 3); // Tier III
    }

    @Override
    protected void onPassiveTick(ServerPlayerEntity player, ItemStack self) {
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 30, 0, true, false));
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.JUMP_BOOST, 30, 0, true, false));
    }

    @Override
    protected float onAttack(LivingEntity target, ServerPlayerEntity attacker, ItemStack self, float amount) {
        if (has(target, StatusEffect.BLEED)) {
            applyScaled(attacker, StatusEffect.POISE, 2, 2, attacker, self);
        }
        return amount;
    }

    @Override
    public List<Text> describe(int level) {
        return List.of(
            TooltipFormat.section("tooltip.limbusego.la_manchaland_all_day_pass.passive"),
            TooltipFormat.body("tooltip.limbusego.la_manchaland_all_day_pass.passive.buffs"),
            TooltipFormat.section("tooltip.limbusego.la_manchaland_all_day_pass.attack"),
            TooltipFormat.body("tooltip.limbusego.la_manchaland_all_day_pass.attack.poise",
                    TooltipFormat.status(StatusEffect.POISE), TooltipFormat.potency(scaled(2, level), 2)));
    }
}
