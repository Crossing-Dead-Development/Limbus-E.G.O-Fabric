package me.yisang.limbusego;

import me.yisang.limbusego.item.ModItemGroups;
import me.yisang.limbusego.item.ModItems;
import me.yisang.limbusego.item.ModSounds;
import me.yisang.limbusego.status.SanityManager;
import me.yisang.limbusego.status.StatusManager;
import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LimbusEGOMod implements ModInitializer {
    public static final String MOD_ID = "limbusego";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static SanityManager sanity;
    private static StatusManager status;

    public static Identifier id(String path) {
        return Identifier.of(MOD_ID, path);
    }

    public static SanityManager getSanity() {
        return sanity;
    }

    public static StatusManager getStatus() {
        return status;
    }

    @Override
    public void onInitialize() {
        ModItems.register();
        ModItemGroups.register();
        ModSounds.register();

        ServerScheduler.init();

        sanity = new SanityManager();
        sanity.start();

        status = new StatusManager(sanity);
        status.start();

        me.yisang.limbusego.event.WeaponEvents.register();

        LOGGER.info("Limbus E.G.O Fabric 初始化完成");
    }
}
