package me.yisang.limbusego.gift.gifts;

import me.yisang.limbusego.gift.BaseGift;
import me.yisang.limbusego.status.StatusEffect;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;

/** 鮮血裝飾：被動每 5 秒獲得強壯（POWER）2·2。 */
public class BloodyGadget extends BaseGift {

    public BloodyGadget() {
        super("bloody_gadget", 1); // Tier I → dark_vestige
    }

    @Override
    protected void onPassiveTick(ServerPlayerEntity player, ItemStack self) {
        if (!gate(player, 5000)) return;
        applyScaled(player, StatusEffect.POWER, 2, 2, player, self);
    }
}
