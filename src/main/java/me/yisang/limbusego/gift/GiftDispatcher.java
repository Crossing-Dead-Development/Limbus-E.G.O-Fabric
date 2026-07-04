package me.yisang.limbusego.gift;

import io.wispforest.accessories.api.AccessoriesCapability;
import io.wispforest.accessories.api.Accessory;
import io.wispforest.accessories.api.AccessoryRegistry;
import io.wispforest.accessories.api.slot.SlotEntryReference;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;

import java.util.UUID;

/**
 * 中央飾品分派器（對映插件把 {@code Accessory} 8 種鉤子接到 Bukkit 事件的角色）。
 *
 * <ul>
 *   <li>{@code onPassiveTick}：由 {@link BaseGift#tick} 於 Accessories 每 tick 直接處理，不經此類。</li>
 *   <li>{@code onAttack}/{@code onDamaged}：{@link #onDamage} 於傷害流程分派，可改傷害值。</li>
 *   <li>{@code onKill}：{@link ServerLivingEntityEvents#AFTER_DEATH}。</li>
 *   <li>{@code onInteract}：{@link UseItemCallback}。</li>
 *   <li>{@code onQuit}：{@link ServerPlayConnectionEvents#DISCONNECT} 清冷卻 map。</li>
 * </ul>
 */
public final class GiftDispatcher {

    private static GiftDispatcher instance;

    public static GiftDispatcher get() {
        return instance;
    }

    private GiftDispatcher() {}

    public static void register() {
        instance = new GiftDispatcher();

        // onKill：擊殺者身上飾品；onOwnerDeath：死者自身飾品
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, source) -> {
            if (source.getAttacker() instanceof ServerPlayerEntity killer) {
                forEachGift(killer, (gift, stack) -> gift.dispatchKill(entity, killer, stack));
            }
            if (entity instanceof ServerPlayerEntity deceased) {
                LivingEntity killerLE = source.getAttacker() instanceof LivingEntity la ? la : null;
                forEachGift(deceased, (gift, stack) -> gift.dispatchOwnerDeath(killerLE, deceased, stack));
            }
        });

        // onInteract：右鍵使用物品
        UseItemCallback.EVENT.register((player, world, hand) -> {
            if (!world.isClient() && player instanceof ServerPlayerEntity sp) {
                forEachGift(sp, (gift, stack) -> gift.dispatchInteract(sp, stack));
            }
            return ActionResult.PASS;
        });

        // onQuit：清每玩家冷卻閘與飾品自訂 map
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            UUID id = handler.getPlayer().getUuid();
            for (BaseGift gift : GiftRegistry.all()) gift.onDisconnect(id);
        });
    }

    /**
     * 傷害流程分派（由 {@link me.yisang.limbusego.mixin.LivingEntityMixin} 於
     * StatusManager 之前呼叫，對映插件飾品監聽器在屬性乘區之前的順序）。
     * 回傳修改後的傷害值。
     */
    public float onDamage(LivingEntity victim, ServerWorld world, DamageSource source, float amount) {
        float dmg = amount;
        LivingEntity attackerLE = source.getAttacker() instanceof LivingEntity la ? la : null;

        // onAnyDamage：victim 飾品，任何傷害來源（含環境／DoT）
        if (victim instanceof ServerPlayerEntity vpAny) {
            AccessoriesCapability cap = AccessoriesCapability.get(vpAny);
            if (cap != null) {
                for (SlotEntryReference ref : cap.getAllEquipped()) {
                    if (asGift(ref) instanceof BaseGift g) {
                        dmg = g.dispatchAnyDamage(attackerLE, vpAny, ref.stack(), dmg, source);
                    }
                }
            }
        }

        // 以下僅實體來源傷害（近戰/彈幕），對齊插件 EntityDamageByEntityEvent
        if (source.getAttacker() == null && source.getSource() == null) return dmg;

        // 攻擊者飾品 onAttack
        if (attackerLE instanceof ServerPlayerEntity attacker) {
            AccessoriesCapability cap = AccessoriesCapability.get(attacker);
            if (cap != null) {
                for (SlotEntryReference ref : cap.getAllEquipped()) {
                    if (asGift(ref) instanceof BaseGift g) {
                        dmg = g.dispatchAttack(victim, attacker, ref.stack(), dmg);
                    }
                }
            }
        }

        // 受害者飾品 onDamaged
        if (victim instanceof ServerPlayerEntity vp) {
            AccessoriesCapability cap = AccessoriesCapability.get(vp);
            if (cap != null) {
                for (SlotEntryReference ref : cap.getAllEquipped()) {
                    if (asGift(ref) instanceof BaseGift g) {
                        dmg = g.dispatchDamaged(attackerLE, vp, ref.stack(), dmg);
                    }
                }
            }
        }

        return dmg;
    }

    // ── helper ──────────────────────────────────────────────────────────

    private static Accessory asGift(SlotEntryReference ref) {
        return AccessoryRegistry.getAccessoryOrDefault(ref.stack());
    }

    private interface GiftAction {
        void run(BaseGift gift, ItemStack stack);
    }

    private static void forEachGift(LivingEntity entity, GiftAction action) {
        AccessoriesCapability cap = AccessoriesCapability.get(entity);
        if (cap == null) return;
        for (SlotEntryReference ref : cap.getAllEquipped()) {
            if (AccessoryRegistry.getAccessoryOrDefault(ref.stack()) instanceof BaseGift g) {
                action.run(g, ref.stack());
            }
        }
    }
}
