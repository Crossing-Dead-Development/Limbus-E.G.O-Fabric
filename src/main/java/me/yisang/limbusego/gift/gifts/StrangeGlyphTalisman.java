package me.yisang.limbusego.gift.gifts;

import me.yisang.limbusego.gift.BaseGift;
import me.yisang.limbusego.status.StatusEffect;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Box;

/** 異文符咒：擊殺時對 5 格內敵人擴散破裂 3·2。 */
public class StrangeGlyphTalisman extends BaseGift {

    public StrangeGlyphTalisman() {
        super("strange_glyph_talisman", 2); // Tier II
    }

    @Override
    protected void onKill(LivingEntity victim, ServerPlayerEntity killer, ItemStack self) {
        double r = 5 * multiplier(self);
        Box box = victim.getBoundingBox().expand(r);
        for (LivingEntity e : victim.getWorld().getEntitiesByClass(LivingEntity.class, box,
                le -> le != killer && !(le instanceof PlayerEntity) && le.isAlive())) {
            apply(e, StatusEffect.RUPTURE, 3, 2, killer);
        }
    }
}
