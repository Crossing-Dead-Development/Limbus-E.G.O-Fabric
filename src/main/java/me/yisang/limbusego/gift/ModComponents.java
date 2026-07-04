package me.yisang.limbusego.gift;

import com.mojang.serialization.Codec;
import me.yisang.limbusego.LimbusEGOMod;
import net.minecraft.component.ComponentType;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

/**
 * 飾品自訂 Data Component。
 *
 * <p>{@code limbusego:gift_level}（Integer 0~3）記錄殘影升級等級，
 * 存在飾品物品本身而非玩家（對映插件 per-player PDC 的 Fabric 版設計決策）。
 * 佩戴時效果 potency 依此等級放大（見 {@link BaseGift#multiplier}）。
 */
public final class ModComponents {

    public static ComponentType<Integer> GIFT_LEVEL;

    private ModComponents() {}

    public static void register() {
        GIFT_LEVEL = Registry.register(
                Registries.DATA_COMPONENT_TYPE,
                LimbusEGOMod.id("gift_level"),
                ComponentType.<Integer>builder()
                        .codec(Codec.INT)
                        .packetCodec(PacketCodecs.VAR_INT)
                        .build());
    }
}
