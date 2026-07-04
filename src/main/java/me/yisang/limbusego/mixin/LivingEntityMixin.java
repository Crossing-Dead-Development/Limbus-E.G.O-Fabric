package me.yisang.limbusego.mixin;

import me.yisang.limbusego.gift.GiftDispatcher;
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
        // 先飾品（對映插件 NORMAL 優先度），再屬性乘區（插件 HIGH），順序一致
        float dmg = amount;
        GiftDispatcher gifts = GiftDispatcher.get();
        if (gifts != null) dmg = gifts.onDamage(self, world, source, dmg);
        return mgr.onDamage(self, world, source, dmg);
    }
}
