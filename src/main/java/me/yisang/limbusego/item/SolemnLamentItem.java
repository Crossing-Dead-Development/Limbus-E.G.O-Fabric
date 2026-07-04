package me.yisang.limbusego.item;

import me.yisang.limbusego.event.WeaponEvents;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

/**
 * 莊嚴哀悼（黑／白）：右鍵消耗一顆蝴蝶石英發射彈幕，1.2 秒冷卻。
 * 黑：命中 8 傷 + 凋零 II（4 秒）+ 沉淪 4p/3c
 * 白：命中 4 傷 + 失明（3 秒）+ 沉淪 3p/2c
 * 數值對照插件 solemnlament.java（v1.3.0）。
 */
public class SolemnLamentItem extends Item {
    public final boolean isBlack;

    public SolemnLamentItem(boolean isBlack, Settings settings) {
        super(settings);
        this.isBlack = isBlack;
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        if (hand != Hand.MAIN_HAND) return ActionResult.PASS;
        if (user.getItemCooldownManager().isCoolingDown(user.getMainHandStack())) {
            return ActionResult.FAIL;
        }
        if (WeaponEvents.findButterfly(user) == null && !user.getAbilities().creativeMode) {
            return ActionResult.FAIL;
        }
        if (!world.isClient) {
            WeaponEvents.fireSolemnLament(user, (ServerWorld) world, isBlack);
        }
        return ActionResult.SUCCESS;
    }
}
