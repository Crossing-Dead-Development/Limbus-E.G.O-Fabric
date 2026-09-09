package me.yisang.limbusego.gift.gifts;

import me.yisang.limbusego.gift.BaseGift;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Box;
import me.yisang.limbusego.tooltip.TooltipFormat;
import net.minecraft.text.Text;
import java.util.List;
import me.yisang.limbusego.gift.GiftUpgradeLogic;

/** 某人的裝置：被動吸引附近掉落物與經驗球。 */
public class SomeonesDevice extends BaseGift {

    public SomeonesDevice() {
        super("someones_device", 3); // Tier III
    }

    @Override
    protected void onPassiveTick(ServerPlayerEntity player, ItemStack self) {
        double r = 6 * multiplier(self);
        Box box = player.getBoundingBox().expand(r);
        for (Entity e : player.getWorld().getEntitiesByClass(Entity.class, box,
                x -> x instanceof ItemEntity || x instanceof ExperienceOrbEntity)) {
            e.setPosition(player.getX(), player.getY(), player.getZ());
        }
    }

    @Override
    public List<Text> describe(int level) {
        return List.of(
            TooltipFormat.section("tooltip.limbusego.someones_device.passive"),
            TooltipFormat.body("tooltip.limbusego.someones_device.passive.magnet",
                    TooltipFormat.num(6 * GiftUpgradeLogic.multiplier(level))));
    }
}
