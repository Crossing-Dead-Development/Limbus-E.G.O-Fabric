# Limbus-E.G.O-Fabric — Limbus E.G.O Unified Mod (Fabric)

[繁體中文](README.md) | English

A Fabric mod that brings Limbus Company's E.G.O weapons and E.G.O gifts into Minecraft,
fully ported from the Paper plugin [Limbus-E.G.O](https://github.com/Crossing-Dead-Development/Limbus-E.G.O) v1.3.0.

- **Version**: 0.1.0 (Phase 1)
- **Minecraft version**: 1.21.4
- **Loader**: Fabric Loader 0.16.9+
- **Required dependencies**: [Fabric API](https://modrinth.com/mod/fabric-api), [Accessories](https://modrinth.com/mod/accessories)
- **Installation**: the mod and its dependencies must be installed on **both client and server**
- **Java**: 21

## Roadmap

| Phase | Content | Status |
|-------|---------|--------|
| Phase 1 (v0.1.x) | 12-status system, Sanity (SAN), 9 E.G.O weapons, `/limbusego weapon` commands & catalog | ✅ Done |
| Phase 2 (v0.2.x) | Accessories gift slots, Vestige anvil upgrades, **80 E.G.O gifts (all done)**; `/limbusego gift` commands & real textures in progress | 🚧 In progress |
| Phase 3 (v1.0.0) | Gacha chest / Thread lottery chest / Shop chest, gift catalog, language switching | ⬜ Not started |

## Weapons

Items live in the "E.G.O Weapons" custom creative tab. Textures are built in — no resource pack needed.

| Weapon | Attributes | Mechanic |
|---|---|---|
| Solemn Lament (Black) | — | Right-click consumes a Butterfly Quartz to fire a projectile (1.2s cooldown); on hit 8 dmg + Wither II + Sinking 4p/3c |
| Solemn Lament (White) | — | Same, on hit 4 dmg + Blindness + Sinking 3p/2c |
| Solemn Shield | — | While held, every 5 ticks applies Slowness II + Bind in a 5-block radius, and grants self Protection (cap 3) |
| Mimicry | +12 / −3.2 | 10% crit for +40~90 dmg, 25% lifesteal; crit grants self Power 3p/4c |
| DaCapo | +7 / −2.4 | Replaces normal attack with a combo: normal 5×1.5, special 3×5.0, AoE 3.5 blocks, each hit applies Sinking 1p/1c |
| Ring Brush | +7 / −2.4 | Right-click for 3.5 dmg + random debuff + Limbus status 1p/3c; double-hit within 1.5s does ×2, single-hit lunges forward |
| W Corp. Knife | +4 / −1.6 | Hits stack Charge (cap 10p, 1p/5c each, refresh count at cap), 20% overload for +1p/1c |
| Tiantui Star Blade | +8 / −2.4 | Right-click charge 1s dash (8 dmg + burn 3s + Tremor 5p/6c + Burn 4p/3c); sneak charge 3s savage (18 dmg + Wither II + Tremor 8p/6c + Burn 6p/4c) |
| Twilight | +9 / −2.4 + reach 1.5 | Low-HP damage scaling (→×2.5) + 30% true damage; sneak charge 1.5s Twilight Slash (fan + Wither + Rupture 5p/2c) |
| Tibia | +10 / −2.8 + reach 1.0 | Stacks Bleed 3p/2c + Melody bonus (+3% per 3 potency, cap 30%); sneak charge 2s Anatomize (+12p/6c Bleed and force-trigger 3 times) |
| Shadow-Vested Bladesinger | +9 / −2.6 | Stacks Poise to raise crit chance; low HP (<3 hearts) sneak-right-click a target → 5-slash |

Ammo: **Butterfly Quartz** (Solemn Lament), **Tiger Mark / Savage Tiger Mark** (Tiantui Star Blade).

## E.G.O Gifts Overview

**80 gifts** total, worn in the 5 universal [Accessories](https://modrinth.com/mod/accessories) slots (`limbusego:ego_gift`), listed in the "E.G.O Gifts" custom creative tab with built-in textures (no resource pack). Gifts span 9 systems, all mirroring Paper plugin v1.3.0:

| System | Count | Representative mechanics |
|---|---|---|
| Burn | 8 | Apply/extend Burn, fire-based damage bonuses |
| Bleed | 6 | Apply Bleed, lifesteal, on-kill spread |
| Sinking | 10 | Apply Sinking, bonus vs. sinking/depressed targets |
| Rupture | 11 | Apply/extend Rupture, Bind riders, on-kill spread |
| Tremor | 6 | Apply Tremor, chain strikes, on-death lightning retaliation |
| Poise | 7 | Self-stack Poise on attack, group buffs, SAN restore |
| Support | 15 | Power/Protection/Haste buffs, teleport backstab, cheat death |
| QoL | 12 | Pull drops & XP, cheat-death heal, duplicate loot, weather buffs |
| Original | 5 | Immunity to hunger/fire, on-kill Sinking spread, passive regen & SAN |

**Vestige anvil upgrades**: use the four Vestige tiers (Dark / Faint / Twinkling / Brilliant → tiers 1-4) on an anvil to upgrade a same-tier gift; levels 1→3 progressively raise the effect multiplier (1.25 / 1.50 / 2.00).

## The 12-Status System

Each entity tracks a `(potency, count)` pair per status, fully in-memory, cleared on death/unload.

| Status | Effect |
|--------|--------|
| Bleed | On the bearer's attack, consume 1 count → self takes potency × 0.5 true damage |
| Burn | Every 2s consume 1 count → potency true damage (DoT) |
| Fragile | Damage taken × (1 + potency × 15%) |
| Power | Damage dealt × (1 + potency × 10%); −1 count per swing |
| Sinking | On hit consume 1 count → potency true damage + player SAN −1 (Depression ×1.5 at bottom); higher potency lowers move speed (−2%/potency, cap −50%) |
| Rupture | On hit consume 1 count → potency × 2 true damage |
| Tremor | At potency ≥ 5, on hit → burst: consume all for potency × 3 true damage + derived Burn 5p/3c |
| Protection | Damage taken × (1 − potency × 5%), applied before Fragile |
| Haste | Speed potion wrapper (amplifier = potency−1, duration = count seconds) |
| Bind | Slowness potion wrapper, same |
| Poise | On swing, min(60%, potency × 5%) chance to crit ×1.75; −1 count per swing |
| Charge | Damage dealt × (1 + potency × 3%); −1 count per swing |

**Sanity (SAN)**: each player has a boss bar (range −45 to +45). Hitting/being hit/Sinking changes SAN; negative SAN recovers out of combat, eating restores SAN, and SAN tweaks attack damage and move speed. Below −30 causes Panic, −45 bottoms out and stacks debuffs. SAN resets to 0 on respawn.

## Commands

`/limbusego` (alias `/lego`):

| Command | Description | Permission |
|---|---|---|
| `/limbusego weapon give <player> <id> [count]` | Give a weapon | Level 2 |
| `/limbusego weapon catalog` | Open the weapon catalog (read-only) | Everyone |
| `/limbusego weapon admin` | Open the admin GUI (click to obtain) | Level 2 |
| `/limbusego weapon <id>` | Give yourself one | Level 2 |

## Differences from the Paper plugin

- Items are native mod items — **no server resource pack needed** (textures are built in)
- Gifts are worn via [Accessories](https://modrinth.com/mod/accessories) slots (5 universal slots) instead of the custom `/accessories` GUI; **the gift menu opener item no longer exists** (Phase 2)
- Items live in two custom creative tabs ("E.G.O Weapons" / "E.G.O Gifts") instead of vanilla tabs
- **The Chatuhu and Apocalypse Bird bundle items are not ported**
- Save data is not interchangeable between the two platforms

## Development

```
.\gradlew.bat build      # Build
.\gradlew.bat runClient  # Launch dev client
.\gradlew.bat runServer  # Launch dev server
```

## License

MIT
