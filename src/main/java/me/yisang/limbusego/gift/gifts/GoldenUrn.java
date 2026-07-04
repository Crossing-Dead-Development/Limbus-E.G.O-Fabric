package me.yisang.limbusego.gift.gifts;

import me.yisang.limbusego.gift.BaseGift;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Box;

import java.util.List;

/** 金甕：擊殺時 15% 機率複製目標掉落物。 */
public class GoldenUrn extends BaseGift {

    public GoldenUrn() {
        super("golden_urn", 2); // Tier II
    }

    @Override
    protected void onKill(LivingEntity victim, ServerPlayerEntity killer, ItemStack self) {
        if (Math.random() >= 0.15 * multiplier(self)) return;
        if (!(victim.getWorld() instanceof ServerWorld sw)) return;
        // AFTER_DEATH 時死亡掉落已生成於世界，快照後各複製一份（複製件不會被重複掃描）
        Box box = victim.getBoundingBox().expand(2);
        List<ItemEntity> drops = sw.getEntitiesByClass(ItemEntity.class, box, e -> e.isAlive());
        for (ItemEntity ie : drops) {
            ItemEntity clone = new ItemEntity(sw, ie.getX(), ie.getY(), ie.getZ(), ie.getStack().copy());
            sw.spawnEntity(clone);
        }
    }
}
