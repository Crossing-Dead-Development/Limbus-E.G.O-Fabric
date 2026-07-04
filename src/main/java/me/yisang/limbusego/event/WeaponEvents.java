package me.yisang.limbusego.event;

import me.yisang.limbusego.LimbusEGOMod;
import me.yisang.limbusego.item.ModItems;
import me.yisang.limbusego.item.ModSounds;
import me.yisang.limbusego.item.SolemnShieldItem;
import me.yisang.limbusego.status.StatusEffect;
import me.yisang.limbusego.status.StatusManager;
import me.yisang.limbusego.status.StatusState;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * E.G.O 武器的伺服端事件與 tick 邏輯。武器分批加入（P1-T5 起）。
 * 數值全數對照插件 v1.3.0，效果串接 {@link StatusManager}。
 */
public class WeaponEvents {

    private static final int SOLEMN_COOLDOWN_TICKS = 24; // 1.2 秒
    private static final float SOLEMN_PROJECTILE_SPEED = 3.0f;
    private static final int SOLEMN_PROJECTILE_LIFETIME = 100; // 5 秒

    private record ProjectileData(ItemEntity entity, UUID ownerId, boolean isBlack, int[] ticksAlive) {}
    private static final List<ProjectileData> activeProjectiles = Collections.synchronizedList(new ArrayList<>());

    private static int shieldTick = 0;

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(WeaponEvents::onServerTick);
    }

    private static void onServerTick(MinecraftServer server) {
        tickShieldAura(server);
        tickProjectiles(server);
    }

    // ── 莊嚴哀悼 ──────────────────────────────────────────────────────────────

    public static void fireSolemnLament(PlayerEntity player, ServerWorld world, boolean isBlack) {
        ItemStack ammo = findButterfly(player);
        if (!player.getAbilities().creativeMode && ammo == null) return;
        if (ammo != null) ammo.decrement(1);

        player.getItemCooldownManager().set(player.getMainHandStack(), SOLEMN_COOLDOWN_TICKS);

        ItemStack visual = new ItemStack(ModItems.BUTTERFLY_QUARTZ);
        ItemEntity proj = new ItemEntity(world, player.getX(), player.getEyeY(), player.getZ(), visual);
        proj.setPickupDelay(32767);
        proj.setVelocity(player.getRotationVector().multiply(SOLEMN_PROJECTILE_SPEED));
        proj.setNeverDespawn();
        world.spawnEntity(proj);

        activeProjectiles.add(new ProjectileData(proj, player.getUuid(), isBlack, new int[]{0}));

        world.playSound(null, player.getBlockPos(), ModSounds.SOLEMN_SHOOT,
                SoundCategory.PLAYERS, 0.8f, 1.0f);
    }

    private static void tickProjectiles(MinecraftServer server) {
        activeProjectiles.removeIf(data -> {
            ItemEntity proj = data.entity();
            if (!proj.isAlive()) return true;

            data.ticksAlive()[0]++;
            if (data.ticksAlive()[0] > SOLEMN_PROJECTILE_LIFETIME) {
                proj.discard();
                return true;
            }

            ServerWorld sw = (ServerWorld) proj.getWorld();
            sw.spawnParticles(ParticleTypes.SQUID_INK, proj.getX(), proj.getY(), proj.getZ(), 2, 0.02, 0.02, 0.02, 0.01);
            sw.spawnParticles(ParticleTypes.WHITE_ASH, proj.getX(), proj.getY(), proj.getZ(), 4, 0.05, 0.05, 0.05, 0.01);

            if (proj.isOnGround()) {
                sw.spawnParticles(ParticleTypes.SQUID_INK, proj.getX(), proj.getY(), proj.getZ(), 8, 0.1, 0.1, 0.1, 0.05);
                proj.discard();
                return true;
            }

            PlayerEntity owner = sw.getPlayerByUuid(data.ownerId());
            if (owner == null) { proj.discard(); return true; }

            for (Entity nearby : sw.getOtherEntities(proj, proj.getBoundingBox().expand(0.8))) {
                if (!(nearby instanceof LivingEntity target)) continue;
                if (nearby.equals(owner)) continue;

                sw.playSound(null, proj.getBlockPos(), ModSounds.SOLEMN_HIT, SoundCategory.PLAYERS, 1.0f, 1.0f);
                sw.spawnParticles(ParticleTypes.SQUID_INK, proj.getX(), proj.getY(), proj.getZ(), 15, 0.1, 0.1, 0.1, 0.05);

                StatusManager sm = LimbusEGOMod.getStatus();
                ServerPlayerEntity src = owner instanceof ServerPlayerEntity sp ? sp : null;
                if (data.isBlack()) {
                    target.damage(sw, sw.getDamageSources().playerAttack(owner), 8.0f);
                    target.addStatusEffect(new StatusEffectInstance(StatusEffects.WITHER, 80, 1));
                    if (sm != null) sm.apply(target, StatusEffect.SINKING, 4, 3, src);
                } else {
                    target.damage(sw, sw.getDamageSources().playerAttack(owner), 4.0f);
                    target.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 60, 0));
                    if (sm != null) sm.apply(target, StatusEffect.SINKING, 3, 2, src);
                }

                proj.discard();
                return true;
            }

            return false;
        });
    }

    // ── 聖宣盾牌光環 ─────────────────────────────────────────────────────────

    private static void tickShieldAura(MinecraftServer server) {
        shieldTick++;
        if (shieldTick % 5 != 0) return;

        StatusManager sm = LimbusEGOMod.getStatus();
        for (ServerWorld world : server.getWorlds()) {
            for (PlayerEntity player : world.getPlayers()) {
                boolean hasShield =
                        player.getMainHandStack().getItem() instanceof SolemnShieldItem ||
                        player.getOffHandStack().getItem() instanceof SolemnShieldItem;
                if (!hasShield) continue;

                world.spawnParticles(ParticleTypes.WHITE_ASH,
                        player.getX(), player.getY() + 1, player.getZ(), 8, 0.4, 0.4, 0.4, 0.02);

                ServerPlayerEntity src = player instanceof ServerPlayerEntity sp ? sp : null;
                // 自身守護：上限 3 potency
                if (sm != null) {
                    StatusState s = sm.get(player);
                    int cur = s == null ? 0 : s.potency(StatusEffect.PROTECTION);
                    if (cur < 3) sm.apply(player, StatusEffect.PROTECTION, 1, 40, src);
                }

                for (Entity e : world.getOtherEntities(player, player.getBoundingBox().expand(5))) {
                    if (e instanceof LivingEntity target) {
                        target.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 40, 1));
                        if (sm != null) sm.apply(target, StatusEffect.BIND, 1, 2, src);
                    }
                }
            }
        }
    }

    // ── 輔助 ─────────────────────────────────────────────────────────────────

    public static ItemStack findButterfly(PlayerEntity player) {
        for (ItemStack s : player.getInventory().main) {
            if (s.getItem() == ModItems.BUTTERFLY_QUARTZ) return s;
        }
        return null;
    }
}
