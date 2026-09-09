package me.yisang.limbusego.gift.gifts;

import me.yisang.limbusego.gift.BaseGift;
import me.yisang.limbusego.status.StatusEffect;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import me.yisang.limbusego.tooltip.TooltipFormat;
import net.minecraft.text.Text;
import java.util.List;

/** 首席管家的秘籍：攻擊施加束縛 2·2；擊殺時回復 2 點生命。 */
public class ChiefButlersSecretArts extends BaseGift {

    public ChiefButlersSecretArts() {
        super("chief_butlers_secret_arts", 3); // Tier III
    }

    @Override
    protected float onAttack(LivingEntity target, ServerPlayerEntity attacker, ItemStack self, float amount) {
        applyScaled(target, StatusEffect.BIND, 2, 2, attacker, self);
        return amount;
    }

    @Override
    protected void onKill(LivingEntity victim, ServerPlayerEntity killer, ItemStack self) {
        killer.setHealth((float) Math.min(killer.getMaxHealth(), killer.getHealth() + 2.0));
    }

    @Override
    public List<Text> describe(int level) {
        return List.of(
            TooltipFormat.section("tooltip.limbusego.chief_butlers_secret_arts.attack"),
            TooltipFormat.body("tooltip.limbusego.chief_butlers_secret_arts.attack.bind",
                    TooltipFormat.status(StatusEffect.BIND), TooltipFormat.potency(scaled(2, level), 2)),
            TooltipFormat.section("tooltip.limbusego.chief_butlers_secret_arts.kill"),
            TooltipFormat.body("tooltip.limbusego.chief_butlers_secret_arts.kill.heal", 2));
    }
}
