package me.yisang.limbusego.event;

import me.yisang.limbusego.LimbusEGOMod;
import me.yisang.limbusego.item.DaCapoItem;
import me.yisang.limbusego.item.MimicryItem;
import me.yisang.limbusego.item.ModItems;
import me.yisang.limbusego.item.ModSounds;
import me.yisang.limbusego.item.RingBrushItem;
import me.yisang.limbusego.item.SolemnShieldItem;
import me.yisang.limbusego.item.WCorpKnifeItem;
import me.yisang.limbusego.status.StatusEffect;
import me.yisang.limbusego.status.StatusManager;
import me.yisang.limbusego.status.StatusState;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    // DaCapo 排程連擊
    private record DaCapoHit(int executeTick, PlayerEntity attacker, LivingEntity target, float damage, boolean special) {}
    private static final List<DaCapoHit> dacapoQueue = Collections.synchronizedList(new ArrayList<>());
    private static final java.util.Set<UUID> processingDaCapo = Collections.synchronizedSet(new java.util.HashSet<>());

    // 環刷雙擊追蹤
    private record BrushHit(UUID targetId, long timeMs) {}
    private static final Map<UUID, BrushHit> brushLastHit = new HashMap<>();

    private static final String[] DACAPO_NOTES = {
            "block.note_block.harp", "block.note_block.bass", "block.note_block.bell",
            "block.note_block.chime", "block.note_block.flute", "block.note_block.guitar",
            "block.note_block.pling", "block.note_block.xylophone", "block.note_block.iron_xylophone",
            "block.note_block.banjo", "block.note_block.bit", "block.note_block.cow_bell",
            "block.note_block.didgeridoo",
    };

    private static final net.minecraft.registry.entry.RegistryEntry<net.minecraft.entity.effect.StatusEffect>[] BRUSH_VANILLA =
            new net.minecraft.registry.entry.RegistryEntry[]{
                    StatusEffects.BLINDNESS, StatusEffects.SLOWNESS, StatusEffects.POISON,
                    StatusEffects.WEAKNESS, StatusEffects.WITHER
            };

    /** Limbus 隨機池：排除會反手 buff 敵人的（強壯 / 守護 / 迅捷）。 */
    private static final StatusEffect[] BRUSH_LIMBUS_POOL = {
            StatusEffect.BLEED, StatusEffect.BURN, StatusEffect.FRAGILE,
            StatusEffect.SINKING, StatusEffect.RUPTURE, StatusEffect.TREMOR, StatusEffect.BIND,
    };

    private static final int WCORP_CHARGE_CAP = 10;

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(WeaponEvents::onServerTick);
        AttackEntityCallback.EVENT.register(WeaponEvents::onAttack);
        UseEntityCallback.EVENT.register(WeaponEvents::onUseEntity);
    }

    private static void onServerTick(MinecraftServer server) {
        tickShieldAura(server);
        tickProjectiles(server);
        processDaCapo(server);
    }

    // ── 近戰武器攻擊分派 ──────────────────────────────────────────────────────

    private static ActionResult onAttack(PlayerEntity player, World world, Hand hand, Entity entity, EntityHitResult hit) {
        if (world.isClient) return ActionResult.PASS;
        if (hand != Hand.MAIN_HAND) return ActionResult.PASS;
        if (!(entity instanceof LivingEntity target)) return ActionResult.PASS;
        ServerWorld sw = (ServerWorld) world;
        ItemStack stack = player.getMainHandStack();

        if (stack.getItem() instanceof MimicryItem) {
            handleMimicry(player, sw, target);
            return ActionResult.PASS;
        }
        if (stack.getItem() instanceof WCorpKnifeItem) {
            handleWCorpKnife(player, sw);
            return ActionResult.PASS;
        }
        if (stack.getItem() instanceof DaCapoItem) {
            if (processingDaCapo.contains(player.getUuid())) return ActionResult.PASS;
            handleDaCapo(player, sw, target);
            return ActionResult.FAIL; // 取消一般攻擊
        }
        return ActionResult.PASS;
    }

    private static ActionResult onUseEntity(PlayerEntity player, World world, Hand hand, Entity entity, EntityHitResult hit) {
        if (world.isClient) return ActionResult.PASS;
        if (hand != Hand.MAIN_HAND) return ActionResult.PASS;
        if (!(entity instanceof LivingEntity target)) return ActionResult.PASS;
        if (player.getMainHandStack().getItem() instanceof RingBrushItem) {
            handleRingBrush(player, (ServerWorld) world, target);
            return ActionResult.SUCCESS;
        }
        return ActionResult.PASS;
    }

    // ── 擬態 ──────────────────────────────────────────────────────────────────

    private static void handleMimicry(PlayerEntity player, ServerWorld world, LivingEntity target) {
        StatusManager sm = LimbusEGOMod.getStatus();
        ServerPlayerEntity src = player instanceof ServerPlayerEntity sp ? sp : null;
        boolean crit = world.random.nextFloat() < 0.10f;
        if (crit) {
            float bonus = 40.0f + world.random.nextFloat() * 50.0f;
            target.damage(world, world.getDamageSources().playerAttack(player), bonus);
            world.spawnParticles(ParticleTypes.EXPLOSION_EMITTER,
                    target.getX(), target.getY() + 1, target.getZ(), 1, 0, 0, 0, 0);
            if (sm != null) sm.apply(player, StatusEffect.POWER, 3, 4, src);
        }
        float healthBefore = target.getHealth();
        world.getServer().execute(() -> {
            float dealt = healthBefore - target.getHealth();
            if (dealt > 0) player.heal(dealt * 0.25f);
        });
    }

    // ── W 公司匕首 ─────────────────────────────────────────────────────────────

    private static void handleWCorpKnife(PlayerEntity player, ServerWorld world) {
        StatusManager sm = LimbusEGOMod.getStatus();
        if (sm == null) return;
        StatusState s = sm.get(player);
        int cur = s == null ? 0 : s.potency(StatusEffect.CHARGE);
        ServerPlayerEntity src = player instanceof ServerPlayerEntity sp ? sp : null;
        if (cur < WCORP_CHARGE_CAP) sm.apply(player, StatusEffect.CHARGE, 1, 5, src);
        else sm.refresh(player, StatusEffect.CHARGE, 5);
        if (world.random.nextFloat() < 0.20f) {
            sm.apply(player, StatusEffect.CHARGE, 1, 1, src);
            if (src != null) src.sendMessage(Text.literal("§9§l⚡ 過載"), true);
            world.spawnParticles(ParticleTypes.ELECTRIC_SPARK,
                    player.getX(), player.getY() + 1.2, player.getZ(), 8, 0.2, 0.3, 0.2, 0.05);
        }
    }

    // ── DaCapo ────────────────────────────────────────────────────────────────

    private static void handleDaCapo(PlayerEntity player, ServerWorld world, LivingEntity target) {
        boolean special = world.random.nextFloat() < 0.40f;
        int hitCount = special ? 3 : 5;
        float damage = special ? 5.0f : 1.5f;
        int interval = special ? 4 : 2;
        int currentTick = world.getServer().getTicks();
        for (int i = 0; i < hitCount; i++) {
            dacapoQueue.add(new DaCapoHit(currentTick + i * interval, player, target, damage, special));
        }
    }

    private static void processDaCapo(MinecraftServer server) {
        int tick = server.getTicks();
        StatusManager sm = LimbusEGOMod.getStatus();
        dacapoQueue.removeIf(h -> {
            if (h.executeTick() > tick) return false;
            PlayerEntity p = h.attacker();
            LivingEntity tg = h.target();
            if (tg == null || !tg.isAlive()) return true;
            if (!(p.getMainHandStack().getItem() instanceof DaCapoItem)) return true;
            ServerWorld sw = server.getWorld(p.getWorld().getRegistryKey());
            if (sw == null) return true;
            ServerPlayerEntity src = p instanceof ServerPlayerEntity sp ? sp : null;

            processingDaCapo.add(p.getUuid());
            try {
                strikeDaCapo(sw, p, src, tg, h.damage(), h.special(), sm);
                for (Entity nearby : tg.getWorld().getOtherEntities(p, tg.getBoundingBox().expand(3.5))) {
                    if (!(nearby instanceof LivingEntity v)) continue;
                    if (nearby.equals(tg) || nearby instanceof PlayerEntity) continue;
                    if (nearby instanceof TameableEntity te && te.isTamed()) continue;
                    strikeDaCapo(sw, p, src, v, h.damage() * 0.7f, h.special(), sm);
                }
            } finally {
                processingDaCapo.remove(p.getUuid());
            }
            return true;
        });
    }

    private static void strikeDaCapo(ServerWorld sw, PlayerEntity p, ServerPlayerEntity src,
                                     LivingEntity v, float damage, boolean special, StatusManager sm) {
        // 先疊沉淪，緊接的傷害會消耗一 count → 造 potency 真傷（1p/1c 用完即銷）
        if (sm != null) sm.apply(v, StatusEffect.SINKING, 1, 1, src);
        v.damage(sw, sw.getDamageSources().playerAttack(p), damage);
        v.hurtTime = 0;
        int color = special ? 0xFFFFFF : 0xB2B2B2;
        sw.spawnParticles(new DustParticleEffect(color, 1.2f),
                v.getX(), v.getY() + 1, v.getZ(), 15, 0.3, 0.3, 0.3, 0);
        String inst = DACAPO_NOTES[sw.random.nextInt(DACAPO_NOTES.length)];
        float pitch = (float) Math.pow(2.0, (sw.random.nextInt(25) - 12) / 12.0);
        var evt = Registries.SOUND_EVENT.get(Identifier.of(inst));
        if (evt != null) {
            sw.playSound(null, v.getBlockPos(), evt, SoundCategory.PLAYERS, 0.8f, pitch);
        }
    }

    // ── 環指筆刷 ──────────────────────────────────────────────────────────────

    private static void handleRingBrush(PlayerEntity player, ServerWorld world, LivingEntity target) {
        long now = System.currentTimeMillis();
        UUID pid = player.getUuid();
        BrushHit last = brushLastHit.get(pid);
        boolean doubleHit = last != null && now - last.timeMs() < 1500 && last.targetId().equals(target.getUuid());
        int hits = doubleHit ? 2 : 1;
        for (int i = 0; i < hits; i++) applyBrushEffect(player, world, target);
        if (doubleHit) {
            brushLastHit.remove(pid);
        } else {
            player.setVelocity(player.getRotationVector().multiply(1.2).add(0, 0.2, 0));
            player.velocityModified = true;
            brushLastHit.put(pid, new BrushHit(target.getUuid(), now));
        }
        brushLastHit.entrySet().removeIf(e -> now - e.getValue().timeMs() > 1500);
    }

    private static void applyBrushEffect(PlayerEntity player, ServerWorld world, LivingEntity target) {
        StatusManager sm = LimbusEGOMod.getStatus();
        ServerPlayerEntity src = player instanceof ServerPlayerEntity sp ? sp : null;
        target.addStatusEffect(new StatusEffectInstance(BRUSH_VANILLA[world.random.nextInt(BRUSH_VANILLA.length)], 80, 1));
        target.damage(world, world.getDamageSources().playerAttack(player), 3.5f);
        int color = (world.random.nextInt(256) << 16) | (world.random.nextInt(100) << 8) | world.random.nextInt(100);
        world.spawnParticles(new DustParticleEffect(color, 1.5f),
                target.getX(), target.getY() + 1, target.getZ(), 20, 0.3, 0.3, 0.3, 0);
        if (sm != null) sm.apply(target, BRUSH_LIMBUS_POOL[world.random.nextInt(BRUSH_LIMBUS_POOL.length)], 1, 3, src);
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
