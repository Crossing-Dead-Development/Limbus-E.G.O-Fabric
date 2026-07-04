package me.yisang.limbusego.status;

import me.yisang.limbusego.LimbusEGOMod;
import me.yisang.limbusego.Messages;
import me.yisang.limbusego.ServerScheduler;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Limbus 屬性層/計數的中央管理器（Fabric 移植版，規則與插件 1:1）。
 *
 * 資料完全 in-memory（ConcurrentHashMap），mob unload / 死亡就丟掉。
 *
 * 觸發規則：
 *   BLEED     — 帶血者攻擊時消耗 1 count → 對自己造 potency × 0.5 真傷
 *   BURN      — 每 40 tick 週期消耗 1 count → 對自己造 potency 真傷
 *   FRAGILE   — 承傷乘 (1 + potency × 15%) 乘區
 *   POWER     — 出手乘 (1 + potency × 10%) 乘區；每次出手 -1 count
 *   SINKING   — 受擊消耗 1 count → potency 真傷 + 玩家 SAN -1（SAN 觸底轉憂鬱 ×1.5）
 *   RUPTURE   — 受擊消耗 1 count → potency × 2 真傷
 *   TREMOR    — 累積 potency；受擊且 potency ≥ 5 時爆發 → 消耗全部 count
 *               造 potency × 3 真傷 + 派生灼熱（追加 BURN 5p/3c）
 *   PROTECTION— 承傷乘 (1 - potency × 5%) 乘區（在 FRAGILE 之前套）
 *   HASTE/BIND— potion wrapper：直接轉 Speed / Slowness，不進 states map
 *   CHARGE    — 出手乘 (1 + potency × 3%)，每次出手 -1 count
 *   POISE     — 出手 min(60%, potency × 5%) 機率爆擊 ×1.75，每次出手 -1 count
 *
 * DoT 分 4 桶輪流結算，每 10 tick 處理 1/4，攤平負載。
 * 傷害乘區由 LivingEntityMixin 攔截 LivingEntity.damage() 進入 {@link #onDamage}。
 */
public class StatusManager {
    private static final int BUCKET_COUNT = 4;
    private static final int BUCKET_INTERVAL_TICKS = 10; // 40 tick 一輪
    private static final double BLEED_COEF = 0.5;
    private static final double FRAGILE_PER_POTENCY = 0.15;
    private static final double POWER_PER_POTENCY = 0.10;
    private static final double DEPRESSION_MULT = 1.5;
    private static final double RUPTURE_MULT = 2.0;
    private static final double TREMOR_MULT = 3.0;
    private static final int TREMOR_BURST_THRESHOLD = 5;
    private static final int TREMOR_DERIV_BURN_POTENCY = 5;
    private static final int TREMOR_DERIV_BURN_COUNT = 3;
    private static final double PROTECTION_PER_POTENCY = 0.05;
    private static final double SINKING_SPEED_PER_POTENCY = 0.02;
    private static final double SINKING_SPEED_MAX = 0.5;
    private static final double CHARGE_PER_POTENCY = 0.03;
    private static final double POISE_CRIT_PER_POTENCY = 0.05;
    private static final double POISE_CRIT_MAX = 0.60;
    private static final double POISE_CRIT_MULT = 1.75;

    private static StatusManager instance;

    public static StatusManager get() {
        return instance;
    }

    private final SanityManager sanity;
    private final ConcurrentHashMap<UUID, StatusState> states = new ConcurrentHashMap<>();
    private final Identifier sinkingSpeedKey = LimbusEGOMod.id("sinking_speed");
    /** 正在承受本系統真傷的實體，mixin 看到時直接放行避免遞迴。 */
    private final Set<UUID> inTrueDamage = ConcurrentHashMap.newKeySet();
    private int tickBucket = 0;

    public StatusManager(SanityManager sanity) {
        this.sanity = sanity;
        instance = this;
    }

    public void start() {
        ServerScheduler.every(BUCKET_INTERVAL_TICKS, server -> burnTick(server.getWorlds()));
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, source) -> {
            // 清 SINKING 移速 modifier（玩家 attribute 會跨復活保留，要顯式移除）
            syncSinkingSpeed(entity, null);
            states.remove(entity.getUuid());
        });
    }

    /** 對外 API：施加屬性（無 source，適合系統派生或自身效果）。 */
    public void apply(LivingEntity target, StatusEffect effect, int potency, int count) {
        apply(target, effect, potency, count, null);
    }

    /** 對外 API：施加屬性，並讓 source（施術者）看到 ActionBar 反饋。 */
    public void apply(LivingEntity target, StatusEffect effect, int potency, int count, ServerPlayerEntity source) {
        if (target == null || !target.isAlive() || potency <= 0 || count <= 0) return;

        // HASTE / BIND 是 potion wrapper，不進 states map
        if (effect == StatusEffect.HASTE || effect == StatusEffect.BIND) {
            applyPotionWrapper(target, effect, potency, count);
            showEffectApplied(target, effect, potency, count, source);
            return;
        }

        StatusState s = states.computeIfAbsent(target.getUuid(), k -> new StatusState());
        s.add(effect, potency, count);
        if (effect == StatusEffect.SINKING) syncSinkingSpeed(target, s);
        showEffectApplied(target, effect, potency, count, source);
    }

    private void applyPotionWrapper(LivingEntity target, StatusEffect effect, int potency, int count) {
        var type = effect == StatusEffect.HASTE ? StatusEffects.SPEED : StatusEffects.SLOWNESS;
        int amplifier = Math.max(0, potency - 1);
        int duration = count * 20; // count 表秒數
        target.addStatusEffect(new StatusEffectInstance(type, duration, amplifier, true, true, true));
    }

    public StatusState get(LivingEntity e) {
        return states.get(e.getUuid());
    }

    /**
     * 續 count（延長現存效果持續時間），不改 potency。若該效果不在或 potency ≤ 0 則 no-op。
     * 用於 potency 已達上限時只要 refresh 生效時間的場景（例：W公司匕首持續攻擊維持充能）。
     */
    public void refresh(LivingEntity target, StatusEffect effect, int addCount) {
        if (target == null || addCount <= 0) return;
        StatusState s = states.get(target.getUuid());
        if (s == null || s.potency(effect) <= 0) return;
        s.add(effect, 0, addCount);
    }

    /**
     * 強制引爆流血：不需受目標主動攻擊即可觸發，最多 times 次。
     * 提比婭 Anatomize 用來解決「呆怪不揮砍不觸發」問題。
     */
    public void triggerBleed(LivingEntity target, ServerPlayerEntity source, int times) {
        if (target == null || times <= 0) return;
        StatusState s = states.get(target.getUuid());
        if (s == null) return;
        int potency = s.potency(StatusEffect.BLEED);
        if (potency <= 0) return;
        int consumed = s.consume(StatusEffect.BLEED, times);
        if (consumed <= 0) return;
        double dmg = potency * BLEED_COEF * consumed;
        scheduleTrueDamage(target, source, dmg, StatusEffect.BLEED);
        if (s.isEmpty()) states.remove(target.getUuid());
    }

    // ── DoT tick（燒傷） ───────────────────────────────────────────────

    private void burnTick(Iterable<ServerWorld> worlds) {
        int bucket = tickBucket;
        tickBucket = (tickBucket + 1) % BUCKET_COUNT;

        List<UUID> toRemove = new ArrayList<>();
        for (Map.Entry<UUID, StatusState> en : states.entrySet()) {
            if ((en.getKey().hashCode() & 0x7fffffff) % BUCKET_COUNT != bucket) continue;

            LivingEntity le = findEntity(worlds, en.getKey());
            if (le == null || !le.isAlive()) {
                toRemove.add(en.getKey());
                continue;
            }

            StatusState s = en.getValue();
            int potency = s.potency(StatusEffect.BURN);
            if (potency > 0 && s.consume(StatusEffect.BURN, 1) > 0) {
                dealTrueDamage(le, null, potency, StatusEffect.BURN);
            }
            if (s.isEmpty()) toRemove.add(en.getKey());
        }
        for (UUID id : toRemove) states.remove(id);
    }

    private LivingEntity findEntity(Iterable<ServerWorld> worlds, UUID id) {
        for (ServerWorld w : worlds) {
            Entity e = w.getEntity(id);
            if (e instanceof LivingEntity le) return le;
        }
        return null;
    }

    // ── 傷害中央處理（由 LivingEntityMixin 呼叫） ────────────────────

    /**
     * 攔截 LivingEntity.damage()：套乘區並處理受擊觸發，回傳修改後的傷害值。
     * 只處理「實體造成的傷害」（有 attacker 或 source entity），與插件版
     * EntityDamageByEntityEvent 的觸發範圍一致。
     */
    public float onDamage(LivingEntity victim, ServerWorld world, DamageSource source, float amount) {
        // 忽略本系統自己造成的真傷，避免遞迴
        if (inTrueDamage.contains(victim.getUuid())) return amount;
        // 僅處理實體來源傷害（近戰/彈幕），對齊插件 EntityDamageByEntityEvent 範圍
        if (source.getAttacker() == null && source.getSource() == null) return amount;

        LivingEntity attacker = source.getAttacker() instanceof LivingEntity la ? la : null;

        StatusState atkS = attacker == null ? null : states.get(attacker.getUuid());
        StatusState vicS = states.get(victim.getUuid());

        double dmg = amount;

        // POWER / CHARGE：攻擊者輸出乘區；POISE：機率爆擊。每次出手 -1 count。
        if (attacker != null && atkS != null) {
            int power = atkS.potency(StatusEffect.POWER);
            int charge = atkS.potency(StatusEffect.CHARGE);
            int poise = atkS.potency(StatusEffect.POISE);
            double mult = 1.0;
            if (power > 0) mult *= (1.0 + power * POWER_PER_POTENCY);
            if (charge > 0) mult *= (1.0 + charge * CHARGE_PER_POTENCY);
            boolean crit = false;
            if (poise > 0) {
                double chance = Math.min(POISE_CRIT_MAX, poise * POISE_CRIT_PER_POTENCY);
                if (Math.random() < chance) {
                    crit = true;
                    mult *= POISE_CRIT_MULT;
                }
            }
            if (mult != 1.0) dmg *= mult;
            if (crit && attacker instanceof ServerPlayerEntity pa) {
                sendActionBar(pa, Messages.fmt(Messages.STATUS_POISE_CRIT, POISE_CRIT_MULT));
            }
            // 每次出手消耗 1 count（Limbus buff 語意：每回合自然衰減）
            if (power > 0) atkS.consume(StatusEffect.POWER, 1);
            if (charge > 0) atkS.consume(StatusEffect.CHARGE, 1);
            if (poise > 0) atkS.consume(StatusEffect.POISE, 1);
        }

        // PROTECTION：受害者減傷乘區（先於 FRAGILE，讓易損不會被完全抵消）
        if (vicS != null) {
            int prot = vicS.potency(StatusEffect.PROTECTION);
            if (prot > 0) dmg *= Math.max(0.0, 1.0 - prot * PROTECTION_PER_POTENCY);
        }

        // FRAGILE：受害者承傷乘區
        if (vicS != null) {
            int f = vicS.potency(StatusEffect.FRAGILE);
            if (f > 0) dmg *= (1.0 + f * FRAGILE_PER_POTENCY);
        }

        // SAN 計數（僅玩家）
        if (attacker instanceof ServerPlayerEntity pa) sanity.onPlayerAttack(pa);
        if (victim instanceof ServerPlayerEntity pv) sanity.onPlayerHurt(pv);

        // BLEED：帶血者「攻擊時」對自己扣血
        if (attacker != null && atkS != null) {
            int bleedPotency = atkS.potency(StatusEffect.BLEED);
            if (bleedPotency > 0 && atkS.consume(StatusEffect.BLEED, 1) > 0) {
                scheduleTrueDamage(attacker, null, bleedPotency * BLEED_COEF, StatusEffect.BLEED);
            }
        }

        // SINKING：受擊消耗
        if (vicS != null) {
            int sedPotency = vicS.potency(StatusEffect.SINKING);
            if (sedPotency > 0 && vicS.consume(StatusEffect.SINKING, 1) > 0) {
                boolean depressed = sanity.isDepressed(victim);
                double tdmg = sedPotency * (depressed ? DEPRESSION_MULT : 1.0);
                scheduleTrueDamage(victim,
                        attacker instanceof ServerPlayerEntity pa ? pa : null,
                        tdmg,
                        depressed ? null : StatusEffect.SINKING);
                if (victim instanceof ServerPlayerEntity pv) sanity.dropSan(pv, 1);
                syncSinkingSpeed(victim, vicS);
            }
        }

        // RUPTURE：受擊消耗 1 count → potency × 2 真傷
        if (vicS != null) {
            int rupPotency = vicS.potency(StatusEffect.RUPTURE);
            if (rupPotency > 0 && vicS.consume(StatusEffect.RUPTURE, 1) > 0) {
                scheduleTrueDamage(victim,
                        attacker instanceof ServerPlayerEntity pa ? pa : null,
                        rupPotency * RUPTURE_MULT, StatusEffect.RUPTURE);
            }
        }

        // TREMOR：受擊且 potency 達閾值 → 爆發（消耗全部 + 派生灼熱）
        if (vicS != null) {
            int tremorPotency = vicS.potency(StatusEffect.TREMOR);
            if (tremorPotency >= TREMOR_BURST_THRESHOLD) {
                int cnt = vicS.count(StatusEffect.TREMOR);
                vicS.consume(StatusEffect.TREMOR, cnt);
                ServerPlayerEntity src = attacker instanceof ServerPlayerEntity pa ? pa : null;
                scheduleTrueDamage(victim, src, tremorPotency * TREMOR_MULT, StatusEffect.TREMOR);
                // 派生灼熱：追加 BURN
                if (victim.isAlive()) {
                    apply(victim, StatusEffect.BURN, TREMOR_DERIV_BURN_POTENCY, TREMOR_DERIV_BURN_COUNT);
                }
                if (src != null) {
                    sendActionBar(src, Messages.fmt(Messages.STATUS_TREMOR_BURST, tremorPotency));
                }
            }
        }

        // 清理空狀態
        if (attacker != null && atkS != null && atkS.isEmpty()) states.remove(attacker.getUuid());
        if (vicS != null && vicS.isEmpty()) states.remove(victim.getUuid());

        return (float) dmg;
    }

    // ── 傷害施加（避免遞迴） ────────────────────────────────────────

    /** 對外真傷 API：供飾品引爆/斬殺/擴散等效果使用（帶屬性標籤顯示、防遞迴）。 */
    public void hurtTrue(LivingEntity target, ServerPlayerEntity source, double amount, StatusEffect label) {
        if (amount <= 0) return;
        scheduleTrueDamage(target, source, amount, label);
    }

    private void scheduleTrueDamage(LivingEntity target, ServerPlayerEntity source, double amount, StatusEffect labelOrNullForDepression) {
        ServerScheduler.runNextTick(server -> dealTrueDamage(target, source, amount, labelOrNullForDepression));
    }

    /** null label = 憂鬱傷害。 */
    private void dealTrueDamage(LivingEntity target, ServerPlayerEntity source, double amount, StatusEffect label) {
        if (target == null || !target.isAlive()) return;
        World w = target.getWorld();
        if (!(w instanceof ServerWorld world)) return;
        inTrueDamage.add(target.getUuid());
        try {
            target.damage(world, world.getDamageSources().generic(), (float) amount);
        } finally {
            inTrueDamage.remove(target.getUuid());
        }
        showDamage(target, source, amount, label);
    }

    // ── 顯示 ────────────────────────────────────────────────────────

    private void showEffectApplied(LivingEntity target, StatusEffect e, int potency, int count, ServerPlayerEntity source) {
        String txt = Messages.fmt(Messages.STATUS_APPLIED, e.color, e.zh, potency, count);
        if (target instanceof ServerPlayerEntity p) sendActionBar(p, txt);
        if (source != null && !source.equals(target)) sendActionBar(source, txt);
    }

    /**
     * 沉淪移速：讀當前 SINKING potency，套 ADD_MULTIPLIED_TOTAL modifier
     * 到 MOVEMENT_SPEED。-2% / potency，上限 -50%。potency 歸零時清 modifier。
     */
    private void syncSinkingSpeed(LivingEntity target, StatusState s) {
        EntityAttributeInstance inst = target.getAttributeInstance(EntityAttributes.MOVEMENT_SPEED);
        if (inst == null) return;
        inst.removeModifier(sinkingSpeedKey);
        int p = s == null ? 0 : s.potency(StatusEffect.SINKING);
        if (p <= 0) return;
        double amount = -Math.min(SINKING_SPEED_MAX, p * SINKING_SPEED_PER_POTENCY);
        inst.addPersistentModifier(new EntityAttributeModifier(
                sinkingSpeedKey, amount, EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
    }

    private void showDamage(LivingEntity target, ServerPlayerEntity source, double amount, StatusEffect label) {
        String tag = label == null
                ? "§4" + Messages.STATUS_DEPRESSION
                : (label.color + label.zh);
        String amt = String.format("%.1f", amount);
        if (target instanceof ServerPlayerEntity p) sendActionBar(p, Messages.fmt(Messages.STATUS_DAMAGE_TARGET, amt, tag));
        if (source != null) sendActionBar(source, Messages.fmt(Messages.STATUS_DAMAGE_SOURCE, tag, amt));
    }

    private void sendActionBar(ServerPlayerEntity p, String msg) {
        p.sendMessage(Text.literal(msg), true);
    }
}
