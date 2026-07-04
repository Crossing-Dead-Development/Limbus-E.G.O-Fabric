package me.yisang.limbusego.gift.gifts;

import me.yisang.limbusego.gift.BaseGift;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Box;

/** 神諭：潛行時每 10 秒使周圍生物發光 3 秒。 */
public class Oracle extends BaseGift {

    public Oracle() {
        super("oracle", 2); // Tier II
    }

    @Override
    protected void onPassiveTick(ServerPlayerEntity player, ItemStack self) {
        if (!player.isSneaking() || !gate(player, 10_000)) return;
        double r = 12 * multiplier(self);
        Box box = player.getBoundingBox().expand(r);
        for (LivingEntity e : player.getWorld().getEntitiesByClass(LivingEntity.class, box,
                le -> le != player && !(le instanceof PlayerEntity) && le.isAlive())) {
            e.addStatusEffect(new StatusEffectInstance(StatusEffects.GLOWING, 60, 0, true, false));
        }
    }
}
