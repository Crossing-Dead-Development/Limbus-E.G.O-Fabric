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

/** 夢中的電子羊：被動緩降；擊殺獲得強壯 2·3。 */
public class DreamingElectricSheep extends BaseGift {

    public DreamingElectricSheep() {
        super("dreaming_electric_sheep", 2); // Tier II
    }

    @Override
    protected void onPassiveTick(ServerPlayerEntity player, ItemStack self) {
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOW_FALLING, 30, 0, true, false));
    }

    @Override
    protected void onKill(LivingEntity victim, ServerPlayerEntity killer, ItemStack self) {
        applyScaled(killer, StatusEffect.POWER, 2, 3, killer, self);
    }

    @Override
    public List<Text> describe(int level) {
        return List.of(
            TooltipFormat.section("tooltip.limbusego.dreaming_electric_sheep.passive"),
            TooltipFormat.body("tooltip.limbusego.dreaming_electric_sheep.passive.slowfall"),
            TooltipFormat.section("tooltip.limbusego.dreaming_electric_sheep.kill"),
            TooltipFormat.body("tooltip.limbusego.dreaming_electric_sheep.kill.power",
                    TooltipFormat.status(StatusEffect.POWER), TooltipFormat.potency(scaled(2, level), 3)));
    }
}
