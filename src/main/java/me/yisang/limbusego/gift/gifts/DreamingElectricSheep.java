package me.yisang.limbusego.gift.gifts;

import me.yisang.limbusego.gift.BaseGift;
import me.yisang.limbusego.status.StatusEffect;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;

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
}
