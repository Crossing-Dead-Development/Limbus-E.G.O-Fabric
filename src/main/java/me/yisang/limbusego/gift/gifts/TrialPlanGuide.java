package me.yisang.limbusego.gift.gifts;

import me.yisang.limbusego.gift.BaseGift;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import me.yisang.limbusego.tooltip.TooltipFormat;
import net.minecraft.text.Text;
import java.util.List;

/** 試用規劃指南：被動村莊英雄 I；擊殺時經驗 +50%。 */
public class TrialPlanGuide extends BaseGift {

    public TrialPlanGuide() {
        super("trial_plan_guide", 3); // Tier III
    }

    @Override
    protected void onPassiveTick(ServerPlayerEntity player, ItemStack self) {
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.HERO_OF_THE_VILLAGE, 120, 0, true, false));
    }

    @Override
    protected void onKill(LivingEntity victim, ServerPlayerEntity killer, ItemStack self) {
        GiftXp.bonusExp(victim, 0.5);
    }

    @Override
    public List<Text> describe(int level) {
        return List.of(
            TooltipFormat.section("tooltip.limbusego.trial_plan_guide.passive"),
            TooltipFormat.body("tooltip.limbusego.trial_plan_guide.passive.hero"),
            TooltipFormat.section("tooltip.limbusego.trial_plan_guide.kill"),
            TooltipFormat.body("tooltip.limbusego.trial_plan_guide.kill.exp", 50));
    }
}
