package me.yisang.limbusego.event;

import me.yisang.limbusego.LimbusEGOMod;
import me.yisang.limbusego.ServerScheduler;
import me.yisang.limbusego.item.BladesingerItem;
import me.yisang.limbusego.item.DaCapoItem;
import me.yisang.limbusego.item.MimicryItem;
import me.yisang.limbusego.item.ModItems;
import me.yisang.limbusego.item.ModSounds;
import me.yisang.limbusego.item.RingBrushItem;
import me.yisang.limbusego.item.SolemnShieldItem;
import me.yisang.limbusego.item.TiantuiStarItem;
import me.yisang.limbusego.item.TibiaItem;
import me.yisang.limbusego.item.TwilightItem;
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
    private static final int BLADESINGER_POISE_CAP = 10;

    // 天推星蓄力/衝刺
    private static final Map<UUID, Boolean> tiantuiSavage = new HashMap<>();
    private record DashData(UUID ownerId, Vec3d vel, boolean savage, java.util.Set<UUID> hit, int[] ticks, boolean[] firstSlash) {}
    private static final List<DashData> activeDashes = Collections.synchronizedList(new ArrayList<>());

    // 暮暉/提比婭特殊冷卻與著影揮刀連斬冷卻（wall-clock ms）
    private static final Map<UUID, Long> twilightCd = new HashMap<>();
    private static final Map<UUID, Long> tibiaCd = new HashMap<>();
    private static final Map<UUID, Long> bladesingerCd = new HashMap<>();

    private static final double TWILIGHT_TRUE_FRACTION = 0.30;
    private static final double TWILIGHT_MAX_LOWHP_BONUS = 1.5;

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(WeaponEvents::onServerTick);
        AttackEntityCallback.EVENT.register(WeaponEvents::onAttack);
        UseEntityCallback.EVENT.register(WeaponEvents::onUseEntity);
    }

    private static void onServerTick(MinecraftServer server) {
        tickShieldAura(server);
        tickProjectiles(server);
        processDaCapo(server);
        tickTiantuiDashes(server);
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
        if (stack.getItem() instanceof BladesingerItem) {
            handleBladesinger(player, sw);
            return ActionResult.PASS;
        }
        if (stack.getItem() instanceof TwilightItem) {
            handleTwilight(player, sw, target);
            return ActionResult.FAIL; // 取消一般攻擊，改用自訂 70/30 傷害
        }
        if (stack.getItem() instanceof TibiaItem) {
            handleTibiaMelee(player, sw, target);
            return ActionResult.FAIL;
        }
        if (stack.getItem() instanceof TiantuiStarItem) {
            sw.playSound(null, target.getBlockPos(), ModSounds.TIANTUI_SLASH, SoundCategory.PLAYERS, 1.0f, 1.0f);
            return ActionResult.PASS;
        }
        return ActionResult.PASS;
    }

    private static ActionResult onUseEntity(PlayerEntity player, World world, Hand hand, Entity entity, EntityHitResult hit) {
        if (world.isClient) return ActionResult.PASS;
        if (hand != Hand.MAIN_HAND) return ActionResult.PASS;
        if (!(entity instanceof LivingEntity target)) return ActionResult.PASS;
        ItemStack stack = player.getMainHandStack();
        if (stack.getItem() instanceof RingBrushItem) {
            handleRingBrush(player, (ServerWorld) world, target);
            return ActionResult.SUCCESS;
        }
        if (stack.getItem() instanceof BladesingerItem) {
            if (player.isSneaking() && player.getHealth() < 6.0f) {
                bladesingerSlash(player, (ServerWorld) world, target);
                return ActionResult.SUCCESS;
            }
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

    // ── 著影揮刀 ──────────────────────────────────────────────────────────────

    private static void handleBladesinger(PlayerEntity player, ServerWorld world) {
        StatusManager sm = LimbusEGOMod.getStatus();
        if (sm == null) return;
        ServerPlayerEntity src = player instanceof ServerPlayerEntity sp ? sp : null;
        StatusState s = sm.get(player);
        int cur = s == null ? 0 : s.potency(StatusEffect.POISE);
        if (cur < BLADESINGER_POISE_CAP) sm.apply(player, StatusEffect.POISE, 1, 4, src);
        else sm.refresh(player, StatusEffect.POISE, 4);
    }

    private static void bladesingerSlash(PlayerEntity player, ServerWorld world, LivingEntity target) {
        long now = System.currentTimeMillis();
        if (now < bladesingerCd.getOrDefault(player.getUuid(), 0L)) return;
        bladesingerCd.put(player.getUuid(), now + 12_000L);

        world.playSound(null, player.getBlockPos(), net.minecraft.sound.SoundEvents.ITEM_TRIDENT_THUNDER.value(),
                SoundCategory.PLAYERS, 0.6f, 1.6f);
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 4 * 5 + 4, 6, true, false, true));
        Vec3d anchor = player.getPos();

        // 5 連斬，每 4 tick 一刀，共 20 tick
        for (int i = 0; i < 5; i++) {
            int delay = i * 4;
            final int idx = i;
            ServerScheduler.runNextTickDelayed(delay, () -> {
                if (!player.isAlive() || !target.isAlive()) return;
                if (!(player.getMainHandStack().getItem() instanceof BladesingerItem)) return;
                player.setVelocity(Vec3d.ZERO);
                player.velocityDirty = true;
                if (player.getPos().squaredDistanceTo(anchor) > 0.09) {
                    player.requestTeleport(anchor.x, anchor.y, anchor.z);
                }
                world.spawnParticles(ParticleTypes.CRIT, target.getX(), target.getY() + 1, target.getZ(), 6, 0.3, 0.3, 0.3, 0.1);
                target.damage(world, world.getDamageSources().playerAttack(player), 7.0f);
                target.hurtTime = 0;
                target.timeUntilRegen = 0;
                world.playSound(null, target.getBlockPos(), net.minecraft.sound.SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP,
                        SoundCategory.PLAYERS, 1.0f, 1.3f + idx * 0.05f);
            });
        }
    }

    // ── 暮暉 ──────────────────────────────────────────────────────────────────

    public static boolean twilightSpecialReady(PlayerEntity p) {
        return System.currentTimeMillis() >= twilightCd.getOrDefault(p.getUuid(), 0L);
    }

    public static void twilightChargeStart(PlayerEntity player, ServerWorld sw) {
        twilightCd.put(player.getUuid(), System.currentTimeMillis() + 6000L + 30 * 50L);
    }

    public static void twilightChargeTick(PlayerEntity player, ServerWorld sw) {
        sw.spawnParticles(ParticleTypes.WHITE_ASH, player.getX(), player.getY() + 1.0, player.getZ(), 6, 0.5, 0.5, 0.5, 0.01);
        sw.spawnParticles(new DustParticleEffect(0x6C5B9E, 1.2f), player.getX(), player.getY() + 1.0, player.getZ(), 4, 0.4, 0.4, 0.4, 0);
    }

    private static double lowHpMult(PlayerEntity p) {
        double max = p.getMaxHealth();
        double frac = Math.max(0.0, Math.min(1.0, p.getHealth() / max));
        return 1.0 + TWILIGHT_MAX_LOWHP_BONUS * (1.0 - frac);
    }

    private static void handleTwilight(PlayerEntity player, ServerWorld sw, LivingEntity target) {
        double mult = lowHpMult(player);
        double base = player.getAttributeValue(net.minecraft.entity.attribute.EntityAttributes.ATTACK_DAMAGE);
        double total = base * mult;
        target.damage(sw, sw.getDamageSources().playerAttack(player), (float) (total * (1.0 - TWILIGHT_TRUE_FRACTION)));
        dealTrueDamage(target, total * TWILIGHT_TRUE_FRACTION);
        sw.spawnParticles(ParticleTypes.WHITE_ASH, target.getX(), target.getY() + 1.0, target.getZ(), 8, 0.3, 0.4, 0.3, 0.01);
    }

    public static void twilightSlash(PlayerEntity player, ServerWorld sw) {
        StatusManager sm = LimbusEGOMod.getStatus();
        ServerPlayerEntity src = player instanceof ServerPlayerEntity sp ? sp : null;
        double mult = lowHpMult(player);
        double dmg = 14.0 * mult;
        double range = 6.0;
        Vec3d look = flatLook(player);

        sw.playSound(null, player.getBlockPos(), net.minecraft.sound.SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP,
                SoundCategory.PLAYERS, 1.2f, 0.6f);
        Vec3d eye = player.getEyePos();
        for (double d = 1.2; d <= range; d += 1.6) {
            for (double deg = -55; deg <= 55; deg += 5) {
                Vec3d v = rotateY(look, Math.toRadians(deg)).multiply(d);
                sw.spawnParticles(ParticleTypes.SWEEP_ATTACK, eye.x + v.x, eye.y + v.y, eye.z + v.z, 1, 0, 0, 0, 0);
            }
        }

        for (Entity e : sw.getOtherEntities(player, player.getBoundingBox().expand(range))) {
            if (!(e instanceof LivingEntity target)) continue;
            Vec3d to = flatTo(player, target);
            if (to.lengthSquared() < 1.0e-6) continue;
            double angle = Math.acos(Math.max(-1, Math.min(1, look.dotProduct(to.normalize()))));
            if (angle > Math.toRadians(55)) continue;
            target.damage(sw, sw.getDamageSources().playerAttack(player), (float) (dmg * (1.0 - TWILIGHT_TRUE_FRACTION)));
            dealTrueDamage(target, dmg * TWILIGHT_TRUE_FRACTION);
            target.addStatusEffect(new StatusEffectInstance(StatusEffects.WITHER, 80, 1));
            if (sm != null) sm.apply(target, StatusEffect.RUPTURE, 5, 2, src);
        }
    }

    // ── 提比婭 ────────────────────────────────────────────────────────────────

    private static final int TIBIA_BLEED_POTENCY = 3;
    private static final int TIBIA_BLEED_COUNT = 2;
    private static final int TIBIA_SLASH_BLEED_POTENCY = 12;
    private static final int TIBIA_SLASH_BLEED_COUNT = 6;
    private static final int TIBIA_SLASH_FORCE = 3;
    private static final double TIBIA_MELODY_PER_3 = 0.03;
    private static final double TIBIA_MELODY_MAX = 0.30;
    private static final double TIBIA_TRUE_FRACTION = 0.35;
    private static final double TIBIA_SLASH_BASE = 16.0;
    private static final double TIBIA_SLASH_RANGE = 5.0;

    private static double tibiaMelody(LivingEntity target) {
        StatusManager sm = LimbusEGOMod.getStatus();
        if (sm == null) return 0.0;
        StatusState s = sm.get(target);
        if (s == null) return 0.0;
        int potency = s.potency(StatusEffect.BLEED);
        return Math.min(TIBIA_MELODY_MAX, Math.floor(potency / 3.0) * TIBIA_MELODY_PER_3);
    }

    private static void handleTibiaMelee(PlayerEntity player, ServerWorld sw, LivingEntity target) {
        StatusManager sm = LimbusEGOMod.getStatus();
        ServerPlayerEntity src = player instanceof ServerPlayerEntity sp ? sp : null;
        double bonus = tibiaMelody(target);
        double base = player.getAttributeValue(net.minecraft.entity.attribute.EntityAttributes.ATTACK_DAMAGE);
        target.damage(sw, sw.getDamageSources().playerAttack(player), (float) (base * (1.0 + bonus)));
        if (sm != null) sm.apply(target, StatusEffect.BLEED, TIBIA_BLEED_POTENCY, TIBIA_BLEED_COUNT, src);
        sw.spawnParticles(new DustParticleEffect(0x8B0000, 1.2f), target.getX(), target.getY() + 1, target.getZ(), 8, 0.3, 0.4, 0.3, 0);
    }

    public static boolean tibiaSpecialReady(PlayerEntity p) {
        return System.currentTimeMillis() >= tibiaCd.getOrDefault(p.getUuid(), 0L);
    }

    public static void tibiaChargeStart(PlayerEntity player, ServerWorld sw) {
        tibiaCd.put(player.getUuid(), System.currentTimeMillis() + 8000L + 40 * 50L);
        sw.playSound(null, player.getBlockPos(), net.minecraft.sound.SoundEvents.ENTITY_WARDEN_HEARTBEAT,
                SoundCategory.PLAYERS, 0.8f, 0.8f);
    }

    public static void tibiaChargeTick(PlayerEntity player, ServerWorld sw) {
        sw.spawnParticles(new DustParticleEffect(0x8B0000, 1.3f), player.getX(), player.getY() + 1.0, player.getZ(), 6, 0.5, 0.5, 0.5, 0);
        sw.spawnParticles(ParticleTypes.CRIMSON_SPORE, player.getX(), player.getY() + 1.0, player.getZ(), 3, 0.4, 0.4, 0.4, 0.01);
    }

    public static void tibiaAnatomize(PlayerEntity player, ServerWorld sw) {
        StatusManager sm = LimbusEGOMod.getStatus();
        ServerPlayerEntity src = player instanceof ServerPlayerEntity sp ? sp : null;
        Vec3d look = flatLook(player);
        sw.playSound(null, player.getBlockPos(), net.minecraft.sound.SoundEvents.ENTITY_RAVAGER_ROAR,
                SoundCategory.PLAYERS, 1.0f, 0.7f);
        Vec3d origin = player.getEyePos();
        for (double deg = -60; deg <= 60; deg += 6) {
            Vec3d dir = rotateY(look, Math.toRadians(deg));
            for (double d = 1.0; d <= TIBIA_SLASH_RANGE; d += 1.2) {
                Vec3d p = origin.add(dir.multiply(d));
                sw.spawnParticles(ParticleTypes.SWEEP_ATTACK, p.x, p.y, p.z, 1, 0, 0, 0, 0);
            }
        }
        for (Entity e : sw.getOtherEntities(player, player.getBoundingBox().expand(TIBIA_SLASH_RANGE))) {
            if (!(e instanceof LivingEntity target)) continue;
            Vec3d to = flatTo(player, target);
            if (to.lengthSquared() < 1.0e-6) continue;
            double angle = Math.acos(Math.max(-1, Math.min(1, look.dotProduct(to.normalize()))));
            if (angle > Math.toRadians(60)) continue;
            double bonus = tibiaMelody(target);
            double dmg = TIBIA_SLASH_BASE * (1.0 + bonus);
            target.damage(sw, sw.getDamageSources().playerAttack(player), (float) (dmg * (1.0 - TIBIA_TRUE_FRACTION)));
            dealTrueDamage(target, dmg * TIBIA_TRUE_FRACTION);
            if (sm != null && target.isAlive()) {
                sm.apply(target, StatusEffect.BLEED, TIBIA_SLASH_BLEED_POTENCY, TIBIA_SLASH_BLEED_COUNT, src);
                sm.triggerBleed(target, src, TIBIA_SLASH_FORCE);
            }
        }
    }

    // ── 天推星 ────────────────────────────────────────────────────────────────

    public static boolean hasTigerMark(PlayerEntity p) { return findItem(p, ModItems.TIGER_MARK) != null; }
    public static boolean hasSavageTigerMark(PlayerEntity p) { return findItem(p, ModItems.SAVAGE_TIGER_MARK) != null; }
    public static boolean isTiantuiSavage(PlayerEntity p) { return tiantuiSavage.getOrDefault(p.getUuid(), false); }

    public static void startTiantuiCharge(PlayerEntity player, World world, boolean savage) {
        tiantuiSavage.put(player.getUuid(), savage);
        world.playSound(null, player.getBlockPos(),
                savage ? ModSounds.TIANTUI_CHARGE_SAV_1 : ModSounds.TIANTUI_CHARGE_TIGER,
                SoundCategory.PLAYERS, 0.9f, 1.0f);
    }

    public static void cancelTiantuiCharge(PlayerEntity player) {
        tiantuiSavage.remove(player.getUuid());
    }

    public static void tiantuiChargeTick(PlayerEntity player, ServerWorld sw, boolean savage, int drawTicks) {
        if (savage) {
            if (drawTicks == 20) sw.playSound(null, player.getBlockPos(), ModSounds.TIANTUI_CHARGE_SAV_2, SoundCategory.PLAYERS, 0.9f, 1.0f);
            else if (drawTicks == 35) sw.playSound(null, player.getBlockPos(), ModSounds.TIANTUI_CHARGE_SAV_3, SoundCategory.PLAYERS, 0.9f, 1.0f);
        }
        sw.spawnParticles(savage ? ParticleTypes.FLAME : ParticleTypes.CRIT,
                player.getX(), player.getY() + 1.0, player.getZ(), savage ? 6 : 3, 0.4, 0.4, 0.4, 0.01);
    }

    public static void fireTiantuiDash(PlayerEntity player, ServerWorld sw, boolean savage) {
        ItemStack ammo = findItem(player, savage ? ModItems.SAVAGE_TIGER_MARK : ModItems.TIGER_MARK);
        if (ammo == null && !player.getAbilities().creativeMode) { cancelTiantuiCharge(player); return; }
        if (ammo != null) ammo.decrement(1);
        cancelTiantuiCharge(player);

        Vec3d dir = flatLook(player).multiply(savage ? 1.85 : 1.2);
        activeDashes.add(new DashData(player.getUuid(), dir, savage,
                Collections.synchronizedSet(new java.util.HashSet<>()), new int[]{ savage ? 10 : 8 }, new boolean[]{false}));
        sw.playSound(null, player.getBlockPos(), ModSounds.TIANTUI_DASH, SoundCategory.PLAYERS, 1.0f, savage ? 0.85f : 1.0f);
    }

    private static void tickTiantuiDashes(MinecraftServer server) {
        StatusManager sm = LimbusEGOMod.getStatus();
        activeDashes.removeIf(d -> {
            ServerWorld sw = null;
            PlayerEntity player = null;
            for (ServerWorld w : server.getWorlds()) {
                PlayerEntity p = w.getPlayerByUuid(d.ownerId());
                if (p != null) { sw = w; player = p; break; }
            }
            if (player == null) return true;

            player.setVelocity(d.vel());
            player.velocityModified = true;

            float dmg = d.savage() ? 18.0f : 8.0f;
            sw.spawnParticles(ParticleTypes.SWEEP_ATTACK, player.getX(), player.getY() + 1.0, player.getZ(), 1, 0, 0, 0, 0);
            sw.spawnParticles(d.savage() ? ParticleTypes.FLAME : ParticleTypes.CRIT,
                    player.getX(), player.getY() + 1.0, player.getZ(), d.savage() ? 8 : 4, 0.3, 0.3, 0.3, 0.02);

            ServerPlayerEntity src = player instanceof ServerPlayerEntity sp ? sp : null;
            for (Entity e : sw.getOtherEntities(player, player.getBoundingBox().expand(1.8, 1.5, 1.8))) {
                if (!(e instanceof LivingEntity target)) continue;
                if (!d.hit().add(e.getUuid())) continue;
                if (!d.firstSlash()[0]) {
                    d.firstSlash()[0] = true;
                    target.timeUntilRegen = 0;
                }
                target.damage(sw, sw.getDamageSources().playerAttack(player), dmg);
                target.hurtTime = 0;
                target.setVelocity(d.vel().multiply(0.4).add(0, 0.25, 0));
                target.velocityModified = true;
                target.setOnFireForTicks(d.savage() ? 100 : 60);
                if (d.savage()) target.addStatusEffect(new StatusEffectInstance(StatusEffects.WITHER, 60, 1));
                if (sm != null) {
                    sm.apply(target, StatusEffect.TREMOR, d.savage() ? 8 : 5, 6, src);
                    sm.apply(target, StatusEffect.BURN, d.savage() ? 6 : 4, d.savage() ? 4 : 3, src);
                }
            }
            d.ticks()[0]--;
            return d.ticks()[0] <= 0;
        });
    }

    // ── 共用工具 ──────────────────────────────────────────────────────────────

    public static ItemStack findButterfly(PlayerEntity player) {
        return findItem(player, ModItems.BUTTERFLY_QUARTZ);
    }

    private static ItemStack findItem(PlayerEntity player, net.minecraft.item.Item item) {
        for (ItemStack s : player.getInventory().main) {
            if (s.getItem() == item) return s;
        }
        return null;
    }

    private static Vec3d flatLook(PlayerEntity player) {
        Vec3d look = player.getRotationVector();
        look = new Vec3d(look.x, 0, look.z);
        return look.lengthSquared() < 1.0e-6 ? new Vec3d(0, 0, 1) : look.normalize();
    }

    private static Vec3d flatTo(PlayerEntity player, LivingEntity target) {
        Vec3d to = target.getPos().subtract(player.getPos());
        return new Vec3d(to.x, 0, to.z);
    }

    /** 真實傷害：先扣吸收再扣生命，無視盔甲與抗性。 */
    private static void dealTrueDamage(LivingEntity target, double dmg) {
        if (dmg <= 0 || !target.isAlive()) return;
        float absorb = target.getAbsorptionAmount();
        if (absorb > 0) {
            float used = (float) Math.min(absorb, dmg);
            target.setAbsorptionAmount(absorb - used);
            dmg -= used;
        }
        if (dmg <= 0) return;
        target.setHealth((float) Math.max(0.0, target.getHealth() - dmg));
    }

    private static Vec3d rotateY(Vec3d v, double rad) {
        double cos = Math.cos(rad), sin = Math.sin(rad);
        return new Vec3d(v.x * cos + v.z * sin, v.y, -v.x * sin + v.z * cos);
    }
}
