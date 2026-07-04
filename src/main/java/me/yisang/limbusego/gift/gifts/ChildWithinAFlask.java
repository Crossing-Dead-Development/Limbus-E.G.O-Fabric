package me.yisang.limbusego.gift.gifts;

import me.yisang.limbusego.gift.BaseGift;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

/** 瓶中嬰孩：受致命傷時每 2 分鐘免死一次，回復 4 生命、獲得抗性並擊退附近敵人。 */
public class ChildWithinAFlask extends BaseGift {

    public ChildWithinAFlask() {
        super("child_within_a_flask", 2); // Tier II
    }

    @Override
    protected float onAnyDamage(LivingEntity attacker, ServerPlayerEntity victim, ItemStack self, float amount,
                                DamageSource source) {
        if (victim.getHealth() - amount > 0) return amount;          // 非致命
        if (!gate(victim, (long) (120_000 / multiplier(self)))) return amount; // 冷卻中→放行致命傷

        victim.setHealth(Math.min(victim.getMaxHealth(), 4.0f));
        victim.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 60, 3, true, true));

        Box box = victim.getBoundingBox().expand(3);
        for (LivingEntity le : victim.getWorld().getEntitiesByClass(LivingEntity.class, box,
                e -> e != victim && !(e instanceof PlayerEntity) && e.isAlive())) {
            Vec3d push = le.getPos().subtract(victim.getPos());
            if (push.lengthSquared() == 0) push = new Vec3d(0, 0.2, 0);
            push = push.normalize().multiply(1.1);
            le.setVelocity(push.x, 0.35, push.z);
            le.velocityModified = true;
        }
        victim.sendMessage(Text.literal("§7瓶中嬰孩護你一命！"), true);
        return 0f;
    }
}
