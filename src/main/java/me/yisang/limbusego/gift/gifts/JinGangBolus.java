package me.yisang.limbusego.gift.gifts;

import me.yisang.limbusego.gift.BaseGift;
import me.yisang.limbusego.status.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import me.yisang.limbusego.tooltip.TooltipFormat;
import net.minecraft.text.Text;
import java.util.List;

/** 金剛丸：被動吸收 I；每 5 秒獲得守護 2·3。 */
public class JinGangBolus extends BaseGift {

    public JinGangBolus() {
        super("jin_gang_bolus", 4); // Tier IV
    }

    @Override
    protected void onPassiveTick(ServerPlayerEntity player, ItemStack self) {
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.ABSORPTION, 30, 0, true, false));
        if (!gate(player, 5000)) return;
        applyScaled(player, StatusEffect.PROTECTION, 2, 3, player, self);
    }

    @Override
    public List<Text> describe(int level) {
        return List.of(
            TooltipFormat.section("tooltip.limbusego.jin_gang_bolus.passive"),
            TooltipFormat.body("tooltip.limbusego.jin_gang_bolus.passive.absorption"),
            TooltipFormat.body("tooltip.limbusego.jin_gang_bolus.passive.protection",
                    TooltipFormat.status(StatusEffect.PROTECTION), TooltipFormat.potency(scaled(2, level), 3)));
    }
}
