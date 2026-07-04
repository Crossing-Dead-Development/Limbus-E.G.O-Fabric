package me.yisang.limbusego.gift.gifts;

import me.yisang.limbusego.gift.BaseGift;
import me.yisang.limbusego.status.StatusEffect;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Box;

/**
 * 藍色Zippo牌打火機：攻擊 20% 機率施加燒傷 2·2；右鍵每 8 秒點燃附近目標。
 *
 * <p>插件的「右鍵方塊放火」需方塊點擊資訊，而 Fabric 分派器只接 {@code UseItemCallback}
 * （無方塊上下文），故僅移植「點燃附近生物」路徑。
 */
public class BlueZippoLighter extends BaseGift {

    public BlueZippoLighter() {
        super("blue_zippo_lighter", 1); // Tier I
    }

    @Override
    protected float onAttack(LivingEntity target, ServerPlayerEntity attacker, ItemStack self, float amount) {
        if (Math.random() < 0.20 * multiplier(self)) {
            apply(target, StatusEffect.BURN, 2, 2, attacker);
        }
        return amount;
    }

    @Override
    protected void onInteract(ServerPlayerEntity player, ItemStack self) {
        if (!gate(player, (long) (8000 / multiplier(self)))) return;
        Box box = player.getBoundingBox().expand(4);
        for (LivingEntity e : player.getWorld().getEntitiesByClass(LivingEntity.class, box,
                le -> le != player && !(le instanceof PlayerEntity) && le.isAlive())) {
            e.setFireTicks(100);
            return;
        }
    }
}
