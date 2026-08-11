package me.yisang.limbusego.status;

import com.mojang.serialization.Codec;
import me.yisang.limbusego.LimbusEGOMod;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;

/**
 * SAN 的持久化掛點。只存數值本身（一個 int），寫進玩家 NBT。
 *
 * <p>命中／受擊計數、脫戰計時、環境曝光累計器一律不存，重登重置無害。
 *
 * <p>刻意<b>不</b>呼叫 {@code copyOnDeath()}：Fabric 預設在玩家死亡時不複製 attachment，
 * 正好對應既有的「死亡重生 SAN 歸零」規則（{@link SanityManager#resetOnRespawn}）。
 * 跨維度與回終界時 Fabric 會自動複製，因此那些情境下 SAN 自然保值。
 */
public final class SanityAttachments {

    /** 玩家 SAN 的持久化 attachment。須於 mod 初始化時呼叫 {@link #register()} 後才可用。 */
    public static AttachmentType<Integer> SAN;

    private SanityAttachments() {}

    public static void register() {
        SAN = AttachmentRegistry.createPersistent(LimbusEGOMod.id("san"), Codec.INT);
    }
}
