package me.yisang.limbusego.gift.gifts;

import me.yisang.limbusego.gift.BaseGift;
import me.yisang.limbusego.status.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;

/** 檀香梅枝：被動中毒時轉化為回血；每 5 秒獲得迅捷 2·3。 */
public class SpicebushBranch extends BaseGift {

    public SpicebushBranch() {
        super("spicebush_branch", 3); // Tier III
    }

    @Override
    protected void onPassiveTick(ServerPlayerEntity player, ItemStack self) {
        if (player.hasStatusEffect(StatusEffects.POISON)) {
            player.removeStatusEffect(StatusEffects.POISON);
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 40, 0, true, true));
        }
        if (gate(player, 5000)) {
            applyScaled(player, StatusEffect.HASTE, 2, 3, player, self);
        }
    }
}
