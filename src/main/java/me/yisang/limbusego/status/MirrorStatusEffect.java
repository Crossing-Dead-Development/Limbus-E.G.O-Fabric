package me.yisang.limbusego.status;

import net.minecraft.entity.effect.StatusEffectCategory;

/**
 * 純顯示用的原版狀態效果空殼：沒有屬性修飾符、不覆寫 tick 行為。
 * 真正的層數與觸發都在 {@link StatusManager}，這裡只是讓 GUI 有東西可畫。
 *
 * <p>原版 {@code StatusEffect} 建構子是 protected，必須子類化才能實例化。
 */
public class MirrorStatusEffect extends net.minecraft.entity.effect.StatusEffect {
    public MirrorStatusEffect(StatusEffectCategory category, int rgb) {
        super(category, rgb);
    }
}
