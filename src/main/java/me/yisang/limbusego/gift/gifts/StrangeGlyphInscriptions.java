package me.yisang.limbusego.gift.gifts;

import me.yisang.limbusego.gift.BaseGift;
import me.yisang.limbusego.status.StatusEffect;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;

/** 篆刻的異文：攻擊破裂中目標時 +20% 傷害（隨升級，上限 30%）並延長破裂 1 層。 */
public class StrangeGlyphInscriptions extends BaseGift {

    public StrangeGlyphInscriptions() {
        super("strange_glyph_inscriptions", 3); // Tier III
    }

    @Override
    protected float onAttack(LivingEntity target, ServerPlayerEntity attacker, ItemStack self, float amount) {
        float dmg = amount;
        if (has(target, StatusEffect.RUPTURE)) {
            dmg *= (float) (1.0 + Math.min(0.30, 0.20 * multiplier(self)));
            status().refresh(target, StatusEffect.RUPTURE, 1);
        }
        return dmg;
    }
}
