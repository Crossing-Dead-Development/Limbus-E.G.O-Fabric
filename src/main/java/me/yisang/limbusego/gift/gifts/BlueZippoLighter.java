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

/**
 * 藍色Zippo牌打火機：攻擊 20% 機率施加燒傷 2·2；右鍵每 8 秒點燃附近目標。
 *
 * <p>插件的「右鍵方塊放火」需方塊點擊資訊，而 Fabric 分派器只接 {@code UseItemCallback}
 * （無方塊上下文），故僅移植「點燃附近生物」路徑。
 */
public class BlueZippoLighter extends BaseGift {

    public BlueZippoLighter() {
        super("blue_zippo_lighter", 1); // Tier I
    }

    @Override
    protected float onAttack(LivingEntity target, ServerPlayerEntity attacker, ItemStack self, float amount) {
        if (Math.random() < 0.20 * multiplier(self)) {
            apply(target, StatusEffect.BURN, 2, 2, attacker);
        }
        return amount;
    }

    @Override
    protected void onInteract(ServerPlayerEntity player, ItemStack self) {
        if (!gate(player, (long) (8000 / multiplier(self)))) return;
        Box box = player.getBoundingBox().expand(4);
        for (LivingEntity e : player.getWorld().getEntitiesByClass(LivingEntity.class, box,
                le -> le != player && !(le instanceof PlayerEntity) && le.isAlive())) {
            e.setFireTicks(100);
            return;
        }
    }

    @Override
    public List<Text> describe(int level) {
        double m = GiftUpgradeLogic.multiplier(level);
        return List.of(
            TooltipFormat.section("tooltip.limbusego.blue_zippo_lighter.attack"),
            TooltipFormat.body("tooltip.limbusego.blue_zippo_lighter.attack.burn",
                    TooltipFormat.num(Math.min(1.0, 0.20 * m) * 100),
                    TooltipFormat.status(StatusEffect.BURN), TooltipFormat.potency(2, 2)),
            TooltipFormat.section("tooltip.limbusego.blue_zippo_lighter.use"),
            TooltipFormat.body("tooltip.limbusego.blue_zippo_lighter.use.ignite", 4, 5),
            TooltipFormat.body("tooltip.limbusego.blue_zippo_lighter.use.cooldown",
                    TooltipFormat.num(8.0 / m)));
    }
}
