package me.yisang.limbusego.gift.gifts;

import me.yisang.limbusego.gift.BaseGift;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import me.yisang.limbusego.tooltip.TooltipFormat;
import net.minecraft.text.Text;
import java.util.List;
import me.yisang.limbusego.gift.GiftUpgradeLogic;

/** 石板字符：擊殺時回復生命與飽食度。 */
public class Lithograph extends BaseGift {

    public Lithograph() {
        super("lithograph", 1); // Tier I
    }

    @Override
    protected void onKill(LivingEntity victim, ServerPlayerEntity killer, ItemStack self) {
        float max = killer.getMaxHealth();
        killer.setHealth((float) Math.min(max, killer.getHealth() + 2.0 * multiplier(self)));
        var hunger = killer.getHungerManager();
        hunger.setFoodLevel(Math.min(20, hunger.getFoodLevel() + 2));
    }

    @Override
    public List<Text> describe(int level) {
        return List.of(
            TooltipFormat.section("tooltip.limbusego.lithograph.kill"),
            TooltipFormat.body("tooltip.limbusego.lithograph.kill.heal",
                    TooltipFormat.num(2 * GiftUpgradeLogic.multiplier(level))),
            TooltipFormat.body("tooltip.limbusego.lithograph.kill.food", 2));
    }
}
