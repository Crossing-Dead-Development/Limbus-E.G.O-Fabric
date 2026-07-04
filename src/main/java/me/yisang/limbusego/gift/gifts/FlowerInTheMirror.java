package me.yisang.limbusego.gift.gifts;

import me.yisang.limbusego.gift.BaseGift;
import me.yisang.limbusego.status.StatusEffect;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Box;

/** 鏡中花：攻擊施加破裂 2·2；被動每 5 秒對 5 格內敵人施加破裂 2·1。 */
public class FlowerInTheMirror extends BaseGift {

    public FlowerInTheMirror() {
        super("flower_in_the_mirror", 3); // Tier III
    }

    @Override
    protected void onPassiveTick(ServerPlayerEntity player, ItemStack self) {
        if (!gate(player, 5000)) return;
        double r = 5 * multiplier(self);
        Box box = player.getBoundingBox().expand(r);
        for (LivingEntity e : player.getWorld().getEntitiesByClass(LivingEntity.class, box,
                le -> le != player && !(le instanceof PlayerEntity) && le.isAlive())) {
            apply(e, StatusEffect.RUPTURE, 2, 1, player);
        }
    }

    @Override
    protected float onAttack(LivingEntity target, ServerPlayerEntity attacker, ItemStack self, float amount) {
        applyScaled(target, StatusEffect.RUPTURE, 2, 2, attacker, self);
        return amount;
    }
}
