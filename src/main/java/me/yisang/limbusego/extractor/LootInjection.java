package me.yisang.limbusego.extractor;

import me.yisang.limbusego.gift.Vestiges;
import me.yisang.limbusego.item.ModItems;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.item.Item;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.condition.RandomChanceLootCondition;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.provider.number.ConstantLootNumberProvider;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 用 {@code LootTableEvents.MODIFY} 動態附加 Enkephalin 與殘影掉落，不覆寫任何原生 JSON。
 *
 * <p>Enkephalin：所有生物（SpawnGroup 非 MISC）25%。殘影四階各自對應一組怪物（spec §3.3）；
 * Tier IV 只從三個 Boss 掉，讓單機進程有明確里程碑。
 */
public final class LootInjection {

    public static final float ENKEPHALIN_CHANCE = 0.25f;
    public static final float DARK_CHANCE = 0.02f;
    public static final float FAINT_CHANCE = 0.05f;
    public static final float TWINKLING_CHANCE = 0.08f;
    public static final float BRILLIANT_CHANCE = 1.00f;

    /** 一般敵對生物 → dark_vestige。 */
    private static final List<EntityType<?>> TIER_I = List.of(
            EntityType.ZOMBIE, EntityType.ZOMBIE_VILLAGER, EntityType.HUSK, EntityType.DROWNED,
            EntityType.SKELETON, EntityType.STRAY, EntityType.BOGGED, EntityType.SPIDER, EntityType.CAVE_SPIDER,
            EntityType.CREEPER, EntityType.SLIME, EntityType.PHANTOM, EntityType.SILVERFISH, EntityType.WITCH);
    /** 地獄／掠奪者類 → faint_vestige。 */
    private static final List<EntityType<?>> TIER_II = List.of(
            EntityType.PIGLIN, EntityType.PIGLIN_BRUTE, EntityType.ZOMBIFIED_PIGLIN, EntityType.HOGLIN, EntityType.ZOGLIN,
            EntityType.BLAZE, EntityType.MAGMA_CUBE, EntityType.GHAST,
            EntityType.PILLAGER, EntityType.VINDICATOR, EntityType.RAVAGER, EntityType.BREEZE);
    /** 終界人、凋零骷髏、喚魔者 → twinkling_vestige。 */
    private static final List<EntityType<?>> TIER_III = List.of(
            EntityType.ENDERMAN, EntityType.WITHER_SKELETON, EntityType.EVOKER);
    /** 三個 Boss → brilliant_vestige。 */
    private static final List<EntityType<?>> TIER_IV = List.of(
            EntityType.ENDER_DRAGON, EntityType.WITHER, EntityType.WARDEN);

    private LootInjection() {}

    public static void register() {
        Set<RegistryKey<LootTable>> living = new HashSet<>();
        for (EntityType<?> type : Registries.ENTITY_TYPE) {
            if (type.getSpawnGroup() == SpawnGroup.MISC) continue;
            type.getLootTableKey().ifPresent(living::add);
        }
        Set<RegistryKey<LootTable>> tier1 = keysOf(TIER_I);
        Set<RegistryKey<LootTable>> tier2 = keysOf(TIER_II);
        Set<RegistryKey<LootTable>> tier3 = keysOf(TIER_III);
        Set<RegistryKey<LootTable>> tier4 = keysOf(TIER_IV);

        LootTableEvents.MODIFY.register((key, tableBuilder, source, registries) -> {
            if (!source.isBuiltin()) return;
            if (living.contains(key)) tableBuilder.pool(chancePool(ModItems.ENKEPHALIN, ENKEPHALIN_CHANCE));
            if (tier1.contains(key)) tableBuilder.pool(chancePool(Vestiges.DARK_VESTIGE, DARK_CHANCE));
            if (tier2.contains(key)) tableBuilder.pool(chancePool(Vestiges.FAINT_VESTIGE, FAINT_CHANCE));
            if (tier3.contains(key)) tableBuilder.pool(chancePool(Vestiges.TWINKLING_VESTIGE, TWINKLING_CHANCE));
            if (tier4.contains(key)) tableBuilder.pool(chancePool(Vestiges.BRILLIANT_VESTIGE, BRILLIANT_CHANCE));
        });
    }

    private static Set<RegistryKey<LootTable>> keysOf(List<EntityType<?>> types) {
        Set<RegistryKey<LootTable>> out = new HashSet<>();
        for (EntityType<?> t : types) t.getLootTableKey().ifPresent(out::add);
        return out;
    }

    private static LootPool.Builder chancePool(Item item, float chance) {
        return LootPool.builder()
                .rolls(ConstantLootNumberProvider.create(1))
                .conditionally(RandomChanceLootCondition.builder(chance))
                .with(ItemEntry.builder(item));
    }
}
