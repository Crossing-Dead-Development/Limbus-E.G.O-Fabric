package me.yisang.limbusego.gift.gifts;

import me.yisang.limbusego.gift.BaseGift;
import me.yisang.limbusego.status.StatusEffect;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

/** 雷擊木：攻擊施加破裂 2·2；10% 機率（隨升級）召喚閃電並追加破裂 2·1。 */
public class Thunderbranch extends BaseGift {

    public Thunderbranch() {
        super("thunderbranch", 3); // Tier III
    }

    @Override
    protected float onAttack(LivingEntity target, ServerPlayerEntity attacker, ItemStack self, float amount) {
        apply(target, StatusEffect.RUPTURE, 2, 2, attacker);
        if (Math.random() < Math.min(1.0, 0.10 * multiplier(self))) {
            if (target.getWorld() instanceof ServerWorld sw) {
                LightningEntity bolt = new LightningEntity(EntityType.LIGHTNING_BOLT, sw);
                bolt.setPosition(target.getX(), target.getY(), target.getZ());
                bolt.setCosmetic(true);
                sw.spawnEntity(bolt);
            }
            apply(target, StatusEffect.RUPTURE, 2, 1, attacker);
        }
        return amount;
    }
}
