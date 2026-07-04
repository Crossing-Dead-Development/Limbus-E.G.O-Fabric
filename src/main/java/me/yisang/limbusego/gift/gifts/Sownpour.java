package me.yisang.limbusego.gift.gifts;

import me.yisang.limbusego.gift.BaseGift;
import me.yisang.limbusego.status.StatusEffect;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Box;

/** 暴雨：攻擊施加震顫 3·2；30% 機率（隨升級）連鎖打擊 3 格內敵人（50% 傷害）並追加震顫 2·1。 */
public class Sownpour extends BaseGift {

    public Sownpour() {
        super("sownpour", 4); // Tier IV
    }

    @Override
    protected float onAttack(LivingEntity target, ServerPlayerEntity attacker, ItemStack self, float amount) {
        applyScaled(target, StatusEffect.TREMOR, 3, 2, attacker, self);
        double m = multiplier(self);
        if (Math.random() >= Math.min(1.0, 0.30 * m)) return amount;
        if (!(target.getWorld() instanceof ServerWorld sw)) return amount;
        float chainDmg = (float) (amount * 0.5 * m);
        Box box = target.getBoundingBox().expand(3);
        for (LivingEntity le : sw.getEntitiesByClass(LivingEntity.class, box,
                e -> e != target && e != attacker && !(e instanceof PlayerEntity) && e.isAlive())) {
            le.damage(sw, sw.getDamageSources().playerAttack(attacker), chainDmg);
            apply(le, StatusEffect.TREMOR, 2, 1, attacker);
        }
        return amount;
    }
}
