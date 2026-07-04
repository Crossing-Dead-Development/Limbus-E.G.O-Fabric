package me.yisang.limbusego.gift.gifts;

import me.yisang.limbusego.gift.BaseGift;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Box;

/** 緣分殘片：被動吸引經驗球並使隊友再生；擊殺時經驗 +50%。 */
public class PieceOfRelationship extends BaseGift {

    public PieceOfRelationship() {
        super("piece_of_relationship", 4); // Tier IV
    }

    @Override
    protected void onPassiveTick(ServerPlayerEntity player, ItemStack self) {
        double m = multiplier(self);
        Box orbBox = player.getBoundingBox().expand(16 * m);
        for (ExperienceOrbEntity orb : player.getWorld().getEntitiesByClass(ExperienceOrbEntity.class, orbBox,
                o -> o.isAlive())) {
            orb.setPosition(player.getX(), player.getY(), player.getZ());
        }
        Box allyBox = player.getBoundingBox().expand(5);
        for (PlayerEntity ally : player.getWorld().getEntitiesByClass(PlayerEntity.class, allyBox,
                p -> p != player && p.isAlive())) {
            ally.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 120, 0, true, false));
        }
    }

    @Override
    protected void onKill(LivingEntity victim, ServerPlayerEntity killer, ItemStack self) {
        GiftXp.bonusExp(victim, 0.5);
    }
}
