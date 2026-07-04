package me.yisang.limbusego.gift.gifts;

import me.yisang.limbusego.gift.BaseGift;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;

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
}
