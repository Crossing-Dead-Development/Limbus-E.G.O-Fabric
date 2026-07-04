package me.yisang.limbusego.mixin;

import me.yisang.limbusego.status.StatusManager;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * 攔截 LivingEntity.damage()，把傷害值交給 StatusManager 套
 * POWER/CHARGE/POISE/PROTECTION/FRAGILE 乘區並處理受擊觸發
 * （BLEED/SINKING/RUPTURE/TREMOR、SAN 計數）。
 * 對應插件版 EntityDamageByEntityEvent（HIGH priority）。
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    @ModifyVariable(
            method = "damage(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/entity/damage/DamageSource;F)Z",
            at = @At("HEAD"),
            argsOnly = true
    )
    private float limbusego$applyStatusDamage(float amount, ServerWorld world, DamageSource source) {
        StatusManager mgr = StatusManager.get();
        if (mgr == null) return amount;
        LivingEntity self = (LivingEntity) (Object) this;
        if (!self.isAlive() || self.isInvulnerableTo(world, source)) return amount;
        return mgr.onDamage(self, world, source, amount);
    }
}
