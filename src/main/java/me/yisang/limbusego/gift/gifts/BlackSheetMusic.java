package me.yisang.limbusego.gift.gifts;

import me.yisang.limbusego.gift.BaseGift;
import me.yisang.limbusego.status.StatusEffect;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import me.yisang.limbusego.tooltip.TooltipFormat;
import net.minecraft.text.Text;
import java.util.List;

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

    @Override
    public List<Text> describe(int level) {
        return List.of(
            TooltipFormat.section("tooltip.limbusego.black_sheet_music.attack"),
            TooltipFormat.body("tooltip.limbusego.black_sheet_music.attack.sinking",
                    TooltipFormat.status(StatusEffect.SINKING), TooltipFormat.potency(scaled(3, level), 3)),
            TooltipFormat.section("tooltip.limbusego.black_sheet_music.vs_deep",
                    TooltipFormat.status(StatusEffect.SINKING), 4),
            TooltipFormat.body("tooltip.limbusego.black_sheet_music.vs_deep.bonus", 25));
    }
}
