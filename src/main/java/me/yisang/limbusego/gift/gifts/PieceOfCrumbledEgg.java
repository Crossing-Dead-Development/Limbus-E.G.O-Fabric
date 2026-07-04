package me.yisang.limbusego.gift.gifts;

import me.yisang.limbusego.gift.BaseGift;
import me.yisang.limbusego.status.StatusEffect;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

/** 破碎之卵的殘片：死亡時對殺手落雷並施加震顫 5·3。 */
public class PieceOfCrumbledEgg extends BaseGift {

    public PieceOfCrumbledEgg() {
        super("piece_of_crumbled_egg", 4); // Tier IV
    }

    @Override
    protected void onOwnerDeath(LivingEntity killer, ServerPlayerEntity owner, ItemStack self) {
        if (killer == null) return;
        if (killer.getWorld() instanceof ServerWorld sw) {
            LightningEntity bolt = new LightningEntity(EntityType.LIGHTNING_BOLT, sw);
            bolt.setPosition(killer.getX(), killer.getY(), killer.getZ());
            sw.spawnEntity(bolt);
        }
        applyScaled(killer, StatusEffect.TREMOR, 5, 3, owner, self);
    }
}
