package me.yisang.limbusego.status;

import me.yisang.limbusego.LimbusEGOMod;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;

import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 10 個純顯示的原版狀態效果，讓玩家在物品欄／HUD 看到自己身上的 Limbus 屬性。
 *
 * <p>HASTE / BIND 不在此列：它們已是原版速度／緩速的 wrapper，本來就會顯示。
 * 翻譯鍵由原版從 registry id 推導為 {@code effect.limbusego.<path>}，
 * 其值必須與 tooltip 用的 {@code status.limbusego.<path>} 一致（有測試把關）。
 */
public final class ModStatusEffects {

    /** 會被鏡射的 Limbus 屬性，順序即 GUI 效果列的註冊順序。 */
    public static final List<StatusEffect> MIRRORED = List.of(
            StatusEffect.BLEED, StatusEffect.BURN, StatusEffect.FRAGILE,
            StatusEffect.SINKING, StatusEffect.RUPTURE, StatusEffect.TREMOR,
            StatusEffect.POWER, StatusEffect.PROTECTION, StatusEffect.POISE, StatusEffect.CHARGE);

    private static final Map<StatusEffect, RegistryEntry<net.minecraft.entity.effect.StatusEffect>> ENTRIES =
            new EnumMap<>(StatusEffect.class);

    private ModStatusEffects() {}

    /** Limbus 屬性 → 原版效果 registry entry；不鏡射者回傳 null。 */
    public static RegistryEntry<net.minecraft.entity.effect.StatusEffect> entry(StatusEffect effect) {
        return ENTRIES.get(effect);
    }

    public static void register() {
        if (!ENTRIES.isEmpty()) return;
        for (StatusEffect e : MIRRORED) {
            String path = e.name().toLowerCase(Locale.ROOT);
            var vanilla = new MirrorStatusEffect(categoryOf(e), e.rgb);
            ENTRIES.put(e, Registry.registerReference(Registries.STATUS_EFFECT, LimbusEGOMod.id(path), vanilla));
        }
    }

    private static StatusEffectCategory categoryOf(StatusEffect e) {
        return switch (e) {
            case POWER, PROTECTION, POISE, CHARGE -> StatusEffectCategory.BENEFICIAL;
            default -> StatusEffectCategory.HARMFUL;
        };
    }
}
