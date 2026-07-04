package me.yisang.limbusego;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * 取代 BukkitScheduler 的極簡伺服端排程器：
 * - runNextTick：下一個 server tick 執行（真傷延後施放用，避免在傷害流程中遞迴）
 * - every：每 N tick 週期執行（DoT 分桶、SAN 脫戰回復用）
 */
public final class ServerScheduler {
    private ServerScheduler() {}

    private record Periodic(int interval, Consumer<MinecraftServer> task) {}

    private static final Queue<Consumer<MinecraftServer>> NEXT_TICK = new ConcurrentLinkedQueue<>();
    private static final List<Periodic> PERIODICS = new CopyOnWriteArrayList<>();
    private static long tick = 0;

    public static void init() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            tick++;
            Consumer<MinecraftServer> job;
            while ((job = NEXT_TICK.poll()) != null) {
                job.accept(server);
            }
            for (Periodic p : PERIODICS) {
                if (tick % p.interval == 0) p.task.accept(server);
            }
        });
    }

    public static void runNextTick(Consumer<MinecraftServer> task) {
        NEXT_TICK.add(task);
    }

    public static void every(int intervalTicks, Consumer<MinecraftServer> task) {
        PERIODICS.add(new Periodic(intervalTicks, task));
    }
}
