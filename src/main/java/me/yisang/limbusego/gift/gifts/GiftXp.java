package me.yisang.limbusego.gift.gifts;

import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Box;

/**
 * 「擊殺時經驗 +X%」的共用實作。
 *
 * <p>Fabric 無 {@code EntityDeathEvent#setDroppedExp}；由於 AFTER_DEATH 觸發時死亡掉落的經驗球
 * 已生成於世界，改為統計死者周圍剛生成的經驗球總量，再補生一顆對應比例的新球。
 */
final class GiftXp {
    private GiftXp() {}

    static void bonusExp(LivingEntity victim, double fraction) {
        if (!(victim.getWorld() instanceof ServerWorld sw)) return;
        Box box = victim.getBoundingBox().expand(3);
        int total = 0;
        for (ExperienceOrbEntity orb : sw.getEntitiesByClass(ExperienceOrbEntity.class, box, o -> o.isAlive())) {
            total += orb.getExperienceAmount();
        }
        int bonus = (int) (total * fraction);
        if (bonus > 0) {
            ExperienceOrbEntity.spawn(sw, victim.getPos(), bonus);
        }
    }
}
