package me.yisang.limbusego.gift;

import io.wispforest.accessories.api.AccessoryRegistry;
import me.yisang.limbusego.LimbusEGOMod;
import me.yisang.limbusego.gift.gifts.*;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Rarity;

import java.util.ArrayList;
import java.util.List;

/**
 * E.G.O 飾品物品註冊。依插件 9 體系分組批次補齊 80 件，每組一批。
 * 註冊順序即創造頁籤顯示順序（{@link #ordered()}）。
 */
public final class ModGifts {

    /** 依註冊順序保存所有飾品物品，供創造頁籤列舉。 */
    private static final List<Item> ORDERED = new ArrayList<>();

    private ModGifts() {}

    public static void register() {
        // ── 燒傷組（burn，8）──────────────────────────────────────────
        reg("ardent_flower", new ArdentFlower());
        reg("ashes_to_ashes", new AshesToAshes());
        reg("bloodflame_sword", new BloodflameSword());
        reg("dust_to_dust", new DustToDust());
        reg("glimpse_of_flames", new GlimpseOfFlames());
        reg("hot_n_juicy_drumstick", new HotNJuicyDrumstick());
        reg("pain_of_stifled_rage", new PainOfStifledRage());
        reg("royal_jelly_perfume", new RoyalJellyPerfume());

        // ── 流血組（bleed，6）─────────────────────────────────────────
        reg("crystallized_blood", new CrystallizedBlood());
        reg("la_manchaland_all_day_pass", new LaManchalandAllDayPass());
        reg("la_manchaland_standard_pass", new LaManchalandStandardPass());
        reg("mask_of_the_parade", new MaskOfTheParade());
        reg("millarca", new Millarca());
        reg("sanguine_blossom_bolus", new SanguineBlossomBolus());

        // ── 沉淪組（sinking，10）──────────────────────────────────────
        reg("artistic_sense", new ArtisticSense());
        reg("black_sheet_music", new BlackSheetMusic());
        reg("broken_compass", new BrokenCompass());
        reg("cold_illusion", new ColdIllusion());
        reg("distant_star", new DistantStar());
        reg("frozen_cries", new FrozenCries());
        reg("mental_corruption_boosting_gas", new MentalCorruptionBoostingGas());
        reg("rags", new Rags());
        reg("rest", new Rest());
        reg("tangled_bones", new TangledBones());

        // ── 輔助組（support）── ※先前試水的鮮血裝飾屬此組，其餘待補
        reg("bloody_gadget", new BloodyGadget());
    }

    public static List<Item> ordered() {
        return ORDERED;
    }

    private static Item reg(String name, BaseGift gift) {
        Item item = new Item(new Item.Settings()
                .registryKey(RegistryKey.of(RegistryKeys.ITEM, LimbusEGOMod.id(name)))
                .maxCount(1)
                .rarity(Rarity.EPIC));
        Registry.register(Registries.ITEM, LimbusEGOMod.id(name), item);
        AccessoryRegistry.register(item, gift);
        GiftRegistry.register(gift, item);
        ORDERED.add(item);
        return item;
    }
}
