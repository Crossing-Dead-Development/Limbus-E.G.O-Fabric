package me.yisang.limbusego.gift.gifts;

import me.yisang.limbusego.gift.BaseGift;
import me.yisang.limbusego.status.StatusEffect;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;

/** 黑色樂譜：攻擊施加沉淪 3·3；攻擊抑鬱或沉淪≥4 目標 +25% 傷害。 */
public class BlackSheetMusic extends BaseGift {

    public BlackSheetMusic() {
        super("black_sheet_music", 4); // Tier IV
    }

    @Override
    protected float onAttack(LivingEntity target, ServerPlayerEntity attacker, ItemStack self, float amount) {
        float dmg = amount;
        if (sanity().isDepressed(target) || pot(target, StatusEffect.SINKING) >= 4) {
            dmg *= 1.25f;
        }
        applyScaled(target, StatusEffect.SINKING, 3, 3, attacker, self);
        return dmg;
    }
}
