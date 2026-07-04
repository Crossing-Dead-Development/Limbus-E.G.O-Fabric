package me.yisang.limbusego.gift;

import io.wispforest.accessories.api.Accessory;
import io.wispforest.accessories.api.slot.SlotReference;
import me.yisang.limbusego.status.StatusEffect;
import me.yisang.limbusego.status.StatusManager;
import me.yisang.limbusego.status.StatusState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 所有 E.G.O 飾品的基底（對映插件 {@code BaseAccessory}）。
 *
 * <p>同時實作 Accessories 的 {@link Accessory}：{@link #tick} 於伺服端每 tick
 * 轉呼 {@link #onPassiveTick}，取代插件的 20-tick 排程。攻擊／受擊／擊殺／互動
 * 由 {@link GiftDispatcher} 從傷害流程與事件手動分派。
 *
 * <p>升級倍率讀自「佩戴物品」的 {@link ModComponents#GIFT_LEVEL} 等級（0~3），
 * 倍率對照插件 {@code getUpgradeMultiplier}：1→1.25、2→1.50、3→2.00。
 */
public abstract class BaseGift implements Accessory {

    private final String id;
    /** 飾品階級 1~4（對映插件 TIER_MAP），決定可用哪一階殘影升級。 */
    private final int tier;
    /** 每玩家冷卻閘（對映插件 gateMap）。 */
    private final Map<UUID, Long> gateMap = new HashMap<>();

    protected BaseGift(String id, int tier) {
        this.id = id;
        this.tier = tier;
    }

    public String id() {
        return id;
    }

    public int tier() {
        return tier;
    }

    // ── Accessories Accessory 介面 ───────────────────────────────────────

    /** 佩戴中每 tick（客戶端＋伺服端都會呼叫），僅在伺服端轉呼被動。 */
    @Override
    public void tick(ItemStack stack, SlotReference reference) {
        if (reference.entity() instanceof ServerPlayerEntity player) {
            onPassiveTick(player, stack);
        }
    }

    // ── 飾品鉤子（子類覆寫，對映插件 Accessory 鉤子）─────────────────────

    /** 被動：每伺服 tick。 */
    protected void onPassiveTick(ServerPlayerEntity player, ItemStack self) {}

    /** 攻擊：回傳（可修改的）傷害值。src=攻擊者、victim=被攻擊者。 */
    protected float onAttack(LivingEntity victim, ServerPlayerEntity attacker, ItemStack self, float amount) {
        return amount;
    }

    /** 受擊：回傳（可修改的）傷害值。attacker 可能為 null（環境傷害）。僅實體來源傷害觸發。 */
    protected float onDamaged(LivingEntity attacker, ServerPlayerEntity victim, ItemStack self, float amount) {
        return amount;
    }

    /**
     * 任何傷害：victim 承受任何來源傷害時（含環境／DoT）觸發，回傳（可修改的）傷害值。
     * 對映插件 onAnyDamage（EntityDamageEvent）。
     */
    protected float onAnyDamage(LivingEntity attacker, ServerPlayerEntity victim, ItemStack self, float amount) {
        return amount;
    }

    /** 擊殺：killer 擊殺 victim 後。 */
    protected void onKill(LivingEntity victim, ServerPlayerEntity killer, ItemStack self) {}

    /** 佩戴者死亡：owner 死亡後，killer 可能為 null（環境死亡）。對映插件 onDeath。 */
    protected void onOwnerDeath(LivingEntity killer, ServerPlayerEntity owner, ItemStack self) {}

    /** 右鍵互動。 */
    protected void onInteract(ServerPlayerEntity player, ItemStack self) {}

    /** 玩家退出時的子類清理（清自訂 per-player map）。 */
    protected void onQuit(UUID playerId) {}

    /** 玩家退出：清冷卻 map 並呼叫子類清理。由 GiftDispatcher 統一呼叫。 */
    void onDisconnect(UUID playerId) {
        gateMap.remove(playerId);
        onQuit(playerId);
    }

    // GiftDispatcher 用的橋接（同包可見）
    float dispatchAttack(LivingEntity victim, ServerPlayerEntity attacker, ItemStack self, float amount) {
        return onAttack(victim, attacker, self, amount);
    }

    float dispatchDamaged(LivingEntity attacker, ServerPlayerEntity victim, ItemStack self, float amount) {
        return onDamaged(attacker, victim, self, amount);
    }

    float dispatchAnyDamage(LivingEntity attacker, ServerPlayerEntity victim, ItemStack self, float amount) {
        return onAnyDamage(attacker, victim, self, amount);
    }

    void dispatchKill(LivingEntity victim, ServerPlayerEntity killer, ItemStack self) {
        onKill(victim, killer, self);
    }

    void dispatchOwnerDeath(LivingEntity killer, ServerPlayerEntity owner, ItemStack self) {
        onOwnerDeath(killer, owner, self);
    }

    void dispatchInteract(ServerPlayerEntity player, ItemStack self) {
        onInteract(player, self);
    }

    // ── 12 屬性體系共用 helper（對映插件 BaseAccessory）──────────────────

    protected StatusManager status() {
        return StatusManager.get();
    }

    protected me.yisang.limbusego.status.SanityManager sanity() {
        return me.yisang.limbusego.LimbusEGOMod.getSanity();
    }

    /** 佩戴物品等級 → 升級倍率（見 {@link GiftUpgradeLogic#multiplier}）。 */
    protected double multiplier(ItemStack self) {
        return GiftUpgradeLogic.multiplier(self.getOrDefault(ModComponents.GIFT_LEVEL, 0));
    }

    /** 施加屬性，potency 依佩戴物品升級倍率取整放大。 */
    protected void applyScaled(LivingEntity target, StatusEffect eff, int p, int c, ServerPlayerEntity src, ItemStack self) {
        double m = multiplier(self);
        status().apply(target, eff, (int) Math.round(p * m), c, src);
    }

    /** 施加屬性，不做升級放大。 */
    protected void apply(LivingEntity target, StatusEffect eff, int p, int c, ServerPlayerEntity src) {
        status().apply(target, eff, p, c, src);
    }

    protected boolean has(LivingEntity e, StatusEffect eff) {
        return pot(e, eff) > 0;
    }

    protected int pot(LivingEntity e, StatusEffect eff) {
        StatusState s = status().get(e);
        return s == null ? 0 : s.potency(eff);
    }

    /** 單調時間（毫秒），以伺服 tick 換算，避免 wall-clock。供冷卻/計時用。 */
    protected long nowMs(ServerPlayerEntity p) {
        return p.getServer().getTicks() * 50L;
    }

    /** 每玩家冷卻閘：距上次觸發超過 ms 才回 true 並記錄本次。 */
    protected boolean gate(ServerPlayerEntity p, long ms) {
        long now = nowMs(p);
        Long last = gateMap.get(p.getUuid());
        if (last != null && now - last < ms) return false;
        gateMap.put(p.getUuid(), now);
        return true;
    }
}
