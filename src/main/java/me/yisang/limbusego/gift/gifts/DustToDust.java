package me.yisang.limbusego.gift.gifts;

import me.yisang.limbusego.gift.BaseGift;
import me.yisang.limbusego.status.StatusEffect;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Box;
import me.yisang.limbusego.tooltip.TooltipFormat;
import net.minecraft.text.Text;
import java.util.List;
import me.yisang.limbusego.gift.GiftUpgradeLogic;

/** 土歸土：攻擊施加燒傷 3·2；擊殺時對半徑內敵人擴散燒傷 3·2（半徑隨升級放大）。 */
public class DustToDust extends BaseGift {

    public DustToDust() {
        super("dust_to_dust", 3); // Tier III
    }

    @Override
    protected float onAttack(LivingEntity target, ServerPlayerEntity attacker, ItemStack self, float amount) {
        applyScaled(target, StatusEffect.BURN, 3, 2, attacker, self);
        return amount;
    }

    @Override
    protected void onKill(LivingEntity victim, ServerPlayerEntity killer, ItemStack self) {
        double r = 3 * multiplier(self);
        Box box = victim.getBoundingBox().expand(r);
        for (LivingEntity e : victim.getWorld().getEntitiesByClass(LivingEntity.class, box,
                le -> le != killer && !(le instanceof PlayerEntity) && le.isAlive())) {
            apply(e, StatusEffect.BURN, 3, 2, killer);
        }
    }

    @Override
    public List<Text> describe(int level) {
        return List.of(
            TooltipFormat.section("tooltip.limbusego.dust_to_dust.attack"),
            TooltipFormat.body("tooltip.limbusego.dust_to_dust.attack.burn",
                    TooltipFormat.status(StatusEffect.BURN), TooltipFormat.potency(scaled(3, level), 2)),
            TooltipFormat.section("tooltip.limbusego.dust_to_dust.kill"),
            TooltipFormat.body("tooltip.limbusego.dust_to_dust.kill.spread",
                    TooltipFormat.num(3 * GiftUpgradeLogic.multiplier(level)),
                    TooltipFormat.status(StatusEffect.BURN), TooltipFormat.potency(3, 2)));
    }
}
