package me.yisang.limbusego.gift.gifts;

import me.yisang.limbusego.gift.BaseGift;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** 歸途：脫戰 5 秒後每次脫戰回復最多 50% 最大生命（每次戰鬥限一次）。 */
public class Homeward extends BaseGift {

    private final Map<UUID, Long> lastCombat = new HashMap<>();
    private final Map<UUID, Boolean> claimed = new HashMap<>();

    public Homeward() {
        super("homeward", 2); // Tier II
    }

    @Override
    protected float onAttack(LivingEntity target, ServerPlayerEntity attacker, ItemStack self, float amount) {
        markCombat(attacker);
        return amount;
    }

    @Override
    protected float onDamaged(LivingEntity attacker, ServerPlayerEntity victim, ItemStack self, float amount) {
        markCombat(victim);
        return amount;
    }

    @Override
    protected void onPassiveTick(ServerPlayerEntity player, ItemStack self) {
        UUID id = player.getUuid();
        Long last = lastCombat.get(id);
        if (last == null || claimed.getOrDefault(id, false)) return;
        if (nowMs(player) - last < 5000) return;
        float max = player.getMaxHealth();
        double heal = max * Math.min(0.5, 0.20 * multiplier(self));
        player.setHealth((float) Math.min(max, player.getHealth() + heal));
        claimed.put(id, true);
    }

    private void markCombat(ServerPlayerEntity player) {
        lastCombat.put(player.getUuid(), nowMs(player));
        claimed.put(player.getUuid(), false);
    }

    @Override
    protected void onQuit(UUID playerId) {
        lastCombat.remove(playerId);
        claimed.remove(playerId);
    }
}
