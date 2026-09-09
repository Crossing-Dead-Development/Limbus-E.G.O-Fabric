package me.yisang.limbusego.gift.gifts;

import me.yisang.limbusego.gift.BaseGift;
import me.yisang.limbusego.status.StatusEffect;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import me.yisang.limbusego.tooltip.TooltipFormat;
import net.minecraft.text.Text;
import java.util.List;

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

    @Override
    public List<Text> describe(int level) {
        return List.of(
            TooltipFormat.section("tooltip.limbusego.clear_mirror_calm_water.attack"),
            TooltipFormat.body("tooltip.limbusego.clear_mirror_calm_water.attack.poise",
                    TooltipFormat.status(StatusEffect.POISE), TooltipFormat.potency(scaled(3, level), 2)),
            TooltipFormat.section("tooltip.limbusego.clear_mirror_calm_water.kill"),
            TooltipFormat.body("tooltip.limbusego.clear_mirror_calm_water.kill.power",
                    TooltipFormat.status(StatusEffect.POWER), TooltipFormat.potency(scaled(3, level), 2)));
    }
}
