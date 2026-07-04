package me.yisang.limbusego.status;

import me.yisang.limbusego.LimbusEGOMod;
import me.yisang.limbusego.Messages;
import me.yisang.limbusego.ServerScheduler;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.boss.ServerBossBar;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 理智值系統：範圍 -45 ~ +45，預設 0。
 * - 每命中 2 次 +1；每受擊 2 次 -1
 * - 脫戰 10s 後，SAN 為負值時每 2s +1 直到 0；正值不動
 * - SAN < -20 時每次下降跨過 -10 區間發 chat 提示 + 音效
 * - 觸底 -45 時，沉淪造成的傷害轉為「憂鬱」（×1.5，仍為真傷）
 * - BossBar 常態顯示：progress = (SAN + 45) / 90，SAN=0 剛好半滿
 *
 * 移植自插件 status/SanityManager.java，數值 1:1。
 */
public class SanityManager {

    public static final int SAN_MAX = 45;
    public static final int SAN_MIN = -45;
    private static final long OUT_COMBAT_MS = 10_000L;
    private static final int WARN_THRESHOLD = -20;
    private static final int DEBUFF_THRESHOLD = -30;

    // 屬性微調：每 1 點 SAN 的攻擊/速度乘區。刻意小幅度，避免高/低理智過於失衡。
    // 攻擊 ±0.3% / 點 → 極值 ±13.5%；速度 ±0.15% / 點 → 極值 ±6.75%。
    private static final double ATK_PER_SAN = 0.003;
    private static final double SPD_PER_SAN = 0.0015;

    private final Identifier atkModKey = LimbusEGOMod.id("san_atk");
    private final Identifier spdModKey = LimbusEGOMod.id("san_spd");

    private final Map<UUID, Integer> san = new ConcurrentHashMap<>();
    /** [0]=攻擊命中累計, [1]=受擊累計 */
    private final Map<UUID, int[]> counters = new ConcurrentHashMap<>();
    private final Map<UUID, Long> lastCombat = new ConcurrentHashMap<>();
    private final Map<UUID, ServerBossBar> bars = new ConcurrentHashMap<>();
    /** 上一 tick 的飽食度，用來偵測進食（上升量 → 等量回復 SAN）。 */
    private final Map<UUID, Integer> lastFood = new ConcurrentHashMap<>();

    public void start() {
        // 每 2s 檢查脫戰恢復
        ServerScheduler.every(40, server -> {
            for (ServerPlayerEntity p : server.getPlayerManager().getPlayerList()) {
                recoveryTick(p);
            }
        });
        // 進食回復飽食度時，同步回復理智值（每 1 飢餓點 → +1 SAN）。
        // 對應插件版 FoodLevelChangeEvent：每 tick 比對飽食度，只取上升量。
        ServerScheduler.every(1, server -> {
            for (ServerPlayerEntity p : server.getPlayerManager().getPlayerList()) {
                int cur = p.getHungerManager().getFoodLevel();
                Integer prev = lastFood.put(p.getUuid(), cur);
                if (prev != null && cur > prev) gainSan(p, cur - prev);
            }
        });
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> onJoin(handler.player));
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> onQuit(handler.player));
        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
            if (!alive) resetOnRespawn(newPlayer);
            else transferBossBar(oldPlayer, newPlayer);
        });
    }

    private void recoveryTick(ServerPlayerEntity p) {
        // 低理智 debuff 定期補刷（讓玩家離開閾值後自然消退）
        if (getSan(p) <= DEBUFF_THRESHOLD) applyLowSanDebuffs(p);

        Long lc = lastCombat.get(p.getUuid());
        if (lc == null) return;
        if (System.currentTimeMillis() - lc < OUT_COMBAT_MS) return;
        int cur = getSan(p);
        if (cur < 0) setSan(p, cur + 1);
    }

    public int getSan(ServerPlayerEntity p) {
        return san.getOrDefault(p.getUuid(), 0);
    }

    public void setSan(ServerPlayerEntity p, int v) {
        v = Math.max(SAN_MIN, Math.min(SAN_MAX, v));
        int old = getSan(p);
        san.put(p.getUuid(), v);
        updateBossBar(p, v);
        applyAttributeModifiers(p, v);
        if (v <= DEBUFF_THRESHOLD) applyLowSanDebuffs(p);

        // 只有「下降」才提示；每跨過一個 -10 區間才響一次
        boolean droppedByTen = v < old && Math.floorDiv(v, 10) < Math.floorDiv(old, 10);
        if (droppedByTen && v < WARN_THRESHOLD) {
            p.sendMessage(Text.literal(Messages.fmt(Messages.SANITY_WARN_DROP, v, SAN_MAX)));
            p.getWorld().playSound(null, p.getX(), p.getY(), p.getZ(),
                    SoundEvents.ENTITY_WITHER_AMBIENT, SoundCategory.PLAYERS, 0.4f, 0.5f);
        }
        if (old > DEBUFF_THRESHOLD && v <= DEBUFF_THRESHOLD) {
            p.sendMessage(Text.literal(Messages.SANITY_PANIC));
        }
        if (old > SAN_MIN && v == SAN_MIN) {
            p.sendMessage(Text.literal(Messages.SANITY_BOTTOM));
            p.getWorld().playSound(null, p.getX(), p.getY(), p.getZ(),
                    SoundEvents.ENTITY_WITHER_SPAWN, SoundCategory.PLAYERS, 0.5f, 0.5f);
        }
    }

    /**
     * 根據 SAN 套用 ATTACK_DAMAGE / MOVEMENT_SPEED 微調 modifier。
     * MULTIPLY_SCALAR_1 對應 ADD_MULTIPLIED_TOTAL。
     */
    private void applyAttributeModifiers(ServerPlayerEntity p, int sanValue) {
        setModifier(p, EntityAttributes.ATTACK_DAMAGE, atkModKey, sanValue * ATK_PER_SAN);
        setModifier(p, EntityAttributes.MOVEMENT_SPEED, spdModKey, sanValue * SPD_PER_SAN);
    }

    private void setModifier(ServerPlayerEntity p, RegistryEntry<EntityAttribute> attr,
                             Identifier key, double amount) {
        EntityAttributeInstance inst = p.getAttributeInstance(attr);
        if (inst == null) return;
        inst.removeModifier(key);
        if (amount == 0.0) return;
        inst.addPersistentModifier(new EntityAttributeModifier(
                key, amount, EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
    }

    /**
     * SAN ≤ -30 時施加失明 I + 虛弱 I；觸底 -45 時再追加緩速 IV。
     * 時效 3 秒，由 recovery/setSan 定期補刷。
     */
    private void applyLowSanDebuffs(ServerPlayerEntity p) {
        p.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 60, 0, true, false, true));
        p.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 60, 0, true, false, true));
        if (getSan(p) <= SAN_MIN) {
            p.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 60, 3, true, false, true));
        }
    }

    private void clearAttributeModifiers(ServerPlayerEntity p) {
        setModifier(p, EntityAttributes.ATTACK_DAMAGE, atkModKey, 0.0);
        setModifier(p, EntityAttributes.MOVEMENT_SPEED, spdModKey, 0.0);
    }

    /** 攻擊命中：每 2 次 +1 SAN。 */
    public void onPlayerAttack(ServerPlayerEntity p) {
        int[] c = counters.computeIfAbsent(p.getUuid(), k -> new int[]{0, 0});
        c[0]++;
        lastCombat.put(p.getUuid(), System.currentTimeMillis());
        if (c[0] >= 2) {
            c[0] = 0;
            setSan(p, getSan(p) + 1);
        }
    }

    /** 受擊：每 2 次 -1 SAN。 */
    public void onPlayerHurt(ServerPlayerEntity p) {
        int[] c = counters.computeIfAbsent(p.getUuid(), k -> new int[]{0, 0});
        c[1]++;
        lastCombat.put(p.getUuid(), System.currentTimeMillis());
        if (c[1] >= 2) {
            c[1] = 0;
            setSan(p, getSan(p) - 1);
        }
    }

    /** 沉淪扣理智：每消耗 1 count 扣 1 SAN。 */
    public void dropSan(ServerPlayerEntity p, int amount) {
        if (amount <= 0) return;
        setSan(p, getSan(p) - amount);
        lastCombat.put(p.getUuid(), System.currentTimeMillis());
    }

    /** 回復 SAN（飾品回復理智用），上限由 setSan 自動夾住。 */
    public void gainSan(ServerPlayerEntity p, int amount) {
        if (amount <= 0) return;
        setSan(p, getSan(p) + amount);
    }

    /** 死亡重生後理智歸零：SAN 回 0、命中/受擊計數與戰鬥計時一併重置。 */
    public void resetOnRespawn(ServerPlayerEntity p) {
        counters.remove(p.getUuid());
        lastCombat.remove(p.getUuid());
        // 重生會把飽食度回滿，先重置基準值，避免被當成進食而誤加 SAN
        lastFood.put(p.getUuid(), p.getHungerManager().getFoodLevel());
        transferBossBar(null, p);
        setSan(p, 0);
    }

    /** 是否處於憂鬱狀態（SAN 觸底）。 */
    public boolean isDepressed(LivingEntity e) {
        if (!(e instanceof ServerPlayerEntity p)) return false;
        return getSan(p) <= SAN_MIN;
    }

    public void onJoin(ServerPlayerEntity p) {
        san.putIfAbsent(p.getUuid(), 0);
        ServerBossBar bar = new ServerBossBar(
                Text.literal(Messages.fmt(Messages.SANITY_BAR_TITLE, "§b", 0, SAN_MAX)),
                BossBar.Color.BLUE, BossBar.Style.NOTCHED_10);
        bar.setPercent(0.5f);
        bar.addPlayer(p);
        bars.put(p.getUuid(), bar);
        updateBossBar(p, getSan(p));
    }

    public void onQuit(ServerPlayerEntity p) {
        ServerBossBar b = bars.remove(p.getUuid());
        if (b != null) b.clearPlayers();
        counters.remove(p.getUuid());
        lastCombat.remove(p.getUuid());
        lastFood.remove(p.getUuid());
        san.remove(p.getUuid());
        clearAttributeModifiers(p);
    }

    /** 重生/跨界後 ServerPlayerEntity 實例會換新，BossBar 要重新掛人。 */
    private void transferBossBar(ServerPlayerEntity oldPlayer, ServerPlayerEntity newPlayer) {
        ServerBossBar bar = bars.get(newPlayer.getUuid());
        if (bar == null) return;
        if (oldPlayer != null) bar.removePlayer(oldPlayer);
        bar.clearPlayers();
        bar.addPlayer(newPlayer);
    }

    private void updateBossBar(ServerPlayerEntity p, int cur) {
        ServerBossBar bar = bars.get(p.getUuid());
        if (bar == null) return;
        float prog = (cur + 45.0f) / 90.0f;
        bar.setPercent(Math.max(0.0f, Math.min(1.0f, prog)));
        String color = cur >= 0 ? "§b" : (cur < WARN_THRESHOLD ? "§5" : "§c");
        bar.setName(Text.literal(Messages.fmt(Messages.SANITY_BAR_TITLE, color, cur, SAN_MAX)));
        if (cur < WARN_THRESHOLD) bar.setColor(BossBar.Color.PURPLE);
        else if (cur < 0) bar.setColor(BossBar.Color.RED);
        else bar.setColor(BossBar.Color.BLUE);
    }
}
