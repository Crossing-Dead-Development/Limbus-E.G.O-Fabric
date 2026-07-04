package me.yisang.limbusego.gift.gifts;

import me.yisang.limbusego.gift.BaseGift;
import me.yisang.limbusego.status.StatusEffect;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Box;

/** 霧化吸入器：被動每 5 秒使自身與 5 格內玩家獲得呼吸法 2·2。 */
public class Nebulizer extends BaseGift {

    public Nebulizer() {
        super("nebulizer", 3); // Tier III
    }

    @Override
    protected void onPassiveTick(ServerPlayerEntity player, ItemStack self) {
        if (!gate(player, 5000)) return;
        applyScaled(player, StatusEffect.POISE, 2, 2, player, self);
        Box box = player.getBoundingBox().expand(5);
        for (PlayerEntity p2 : player.getWorld().getEntitiesByClass(PlayerEntity.class, box,
                p -> p != player && p.isAlive())) {
            applyScaled(p2, StatusEffect.POISE, 2, 2, player, self);
        }
    }
}
