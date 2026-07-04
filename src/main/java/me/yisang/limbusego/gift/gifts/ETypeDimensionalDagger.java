package me.yisang.limbusego.gift.gifts;

import me.yisang.limbusego.gift.BaseGift;
import me.yisang.limbusego.status.StatusEffect;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;

/** E型次元短劍：攻擊獲得充能 2·2；25% 機率瞬移背刺（1.5 倍傷害）並改獲充能 4·2。 */
public class ETypeDimensionalDagger extends BaseGift {

    public ETypeDimensionalDagger() {
        super("e_type_dimensional_dagger", 2); // Tier II
    }

    @Override
    protected float onAttack(LivingEntity target, ServerPlayerEntity attacker, ItemStack self, float amount) {
        double m = multiplier(self);
        if (Math.random() < Math.min(1.0, 0.25 * m)) {
            Vec3d dir = target.getRotationVector();
            double bx = target.getX() - dir.x * 1.5;
            double bz = target.getZ() - dir.z * 1.5;
            attacker.networkHandler.requestTeleport(bx, target.getY(), bz, target.getYaw(), target.getPitch());
            apply(attacker, StatusEffect.CHARGE, 4, 2, attacker);
            return (float) (amount * 1.5 * m);
        }
        apply(attacker, StatusEffect.CHARGE, 2, 2, attacker);
        return amount;
    }
}
