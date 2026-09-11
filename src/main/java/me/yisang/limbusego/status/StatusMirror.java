package me.yisang.limbusego.status;

import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * 把玩家身上的 Limbus 屬性鏡射成原版狀態效果（只給 GUI 看）。
 *
 * <p>採週期同步而非在 apply / consume 逐點呼叫：consume 散落在十幾處，
 * 漏一處就會留下永遠不消失的假效果；週期同步只有這一個地方會錯。
 * 只掃線上玩家——怪物的回饋由施加粒子負責。
 *
 * <p>鏡射效果一律無限持續，層數歸零時由本類顯式移除；
 * amplifier = potency − 1。不顯示原版效果粒子（GUI 圖示才是目的）。
 */
public final class StatusMirror {
    private final StatusManager manager;

    public StatusMirror(StatusManager manager) {
        this.manager = manager;
    }

    /** 每輪對每位玩家比對 10 個鏡射屬性；StatusState 不存在時仍要清殘留效果。 */
    public void sync(Iterable<ServerPlayerEntity> players) {
        for (ServerPlayerEntity p : players) {
            StatusState state = manager.get(p);
            for (StatusEffect e : ModStatusEffects.MIRRORED) {
                RegistryEntry<net.minecraft.entity.effect.StatusEffect> entry = ModStatusEffects.entry(e);
                if (entry == null) continue;
                int potency = state == null ? 0 : state.potency(e);
                StatusEffectInstance current = p.getStatusEffect(entry);
                Integer currentAmp = current == null ? null : current.getAmplifier();
                var decision = StatusDisplayLogic.decide(potency, currentAmp);
                switch (decision.action()) {
                    case ADD -> p.addStatusEffect(instance(entry, decision.amplifier()));
                    case UPDATE -> {
                        // 原版 addStatusEffect 不會降 amplifier，先移除再套
                        p.removeStatusEffect(entry);
                        p.addStatusEffect(instance(entry, decision.amplifier()));
                    }
                    case REMOVE -> p.removeStatusEffect(entry);
                    case NONE -> { }
                }
            }
        }
    }

    private static StatusEffectInstance instance(RegistryEntry<net.minecraft.entity.effect.StatusEffect> entry, int amplifier) {
        // ambient=false, showParticles=false, showIcon=true
        return new StatusEffectInstance(entry, StatusEffectInstance.INFINITE, amplifier, false, false, true);
    }
}
