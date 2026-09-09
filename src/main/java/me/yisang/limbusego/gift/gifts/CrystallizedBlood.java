package me.yisang.limbusego.gift.gifts;

import me.yisang.limbusego.gift.BaseGift;
import me.yisang.limbusego.status.StatusEffect;
import me.yisang.limbusego.status.StatusState;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import me.yisang.limbusego.tooltip.TooltipFormat;
import net.minecraft.text.Text;
import java.util.List;
import me.yisang.limbusego.gift.GiftUpgradeLogic;

/** 血液結晶：每 5 秒消耗全部流血層數回血（層數一半，上限 4，隨升級提升）。 */
public class CrystallizedBlood extends BaseGift {

    public CrystallizedBlood() {
        super("crystallized_blood", 2); // Tier II
    }

    @Override
    protected void onPassiveTick(ServerPlayerEntity player, ItemStack self) {
        if (!gate(player, 5000)) return;
        StatusState s = status().get(player);
        if (s == null || s.potency(StatusEffect.BLEED) <= 0) return;
        int p = s.potency(StatusEffect.BLEED);
        s.consume(StatusEffect.BLEED, Integer.MAX_VALUE); // 消耗全部
        double heal = Math.min(4 * multiplier(self), p * 0.5);
        player.setHealth((float) Math.min(player.getMaxHealth(), player.getHealth() + heal));
    }

    @Override
    public List<Text> describe(int level) {
        return List.of(
            TooltipFormat.section("tooltip.limbusego.crystallized_blood.passive"),
            TooltipFormat.body("tooltip.limbusego.crystallized_blood.passive.consume",
                    TooltipFormat.status(StatusEffect.BLEED)),
            TooltipFormat.body("tooltip.limbusego.crystallized_blood.passive.heal",
                    TooltipFormat.num(4 * GiftUpgradeLogic.multiplier(level))));
    }
}
