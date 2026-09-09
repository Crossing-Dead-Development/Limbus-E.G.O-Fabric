package me.yisang.limbusego.gift.gifts;

import me.yisang.limbusego.gift.BaseGift;
import me.yisang.limbusego.status.StatusEffect;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import me.yisang.limbusego.tooltip.TooltipFormat;
import net.minecraft.text.Text;
import java.util.List;

/** 手鏡：受擊時對攻擊者施加束縛 2·2 與脆弱 1·2。 */
public class HandheldMirror extends BaseGift {

    public HandheldMirror() {
        super("handheld_mirror", 4); // Tier IV
    }

    @Override
    protected float onDamaged(LivingEntity attacker, ServerPlayerEntity victim, ItemStack self, float amount) {
        if (attacker != null) {
            applyScaled(attacker, StatusEffect.BIND, 2, 2, victim, self);
            apply(attacker, StatusEffect.FRAGILE, 1, 2, victim);
        }
        return amount;
    }

    @Override
    public List<Text> describe(int level) {
        return List.of(
            TooltipFormat.section("tooltip.limbusego.handheld_mirror.damaged"),
            TooltipFormat.body("tooltip.limbusego.handheld_mirror.damaged.bind",
                    TooltipFormat.status(StatusEffect.BIND), TooltipFormat.potency(scaled(2, level), 2)),
            TooltipFormat.body("tooltip.limbusego.handheld_mirror.damaged.fragile",
                    TooltipFormat.status(StatusEffect.FRAGILE), TooltipFormat.potency(1, 2)));
    }
}
