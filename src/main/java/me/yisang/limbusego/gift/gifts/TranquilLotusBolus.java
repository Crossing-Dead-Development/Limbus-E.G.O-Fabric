package me.yisang.limbusego.gift.gifts;

import me.yisang.limbusego.gift.BaseGift;
import me.yisang.limbusego.status.StatusEffect;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** 靜蓮丸：被動每 5 秒獲得守護 2·2，每 10 秒回復 1 SAN。 */
public class TranquilLotusBolus extends BaseGift {

    private final Map<UUID, Long> sanTicks = new HashMap<>();

    public TranquilLotusBolus() {
        super("tranquil_lotus_bolus", 4); // Tier IV
    }

    @Override
    protected void onPassiveTick(ServerPlayerEntity player, ItemStack self) {
        if (gate(player, 5000)) {
            applyScaled(player, StatusEffect.PROTECTION, 2, 2, player, self);
        }
        long now = nowMs(player);
        Long last = sanTicks.get(player.getUuid());
        if (last == null || now - last >= 10_000) {
            sanity().gainSan(player, 1);
            sanTicks.put(player.getUuid(), now);
        }
    }

    @Override
    protected void onQuit(UUID playerId) {
        sanTicks.remove(playerId);
    }
}
