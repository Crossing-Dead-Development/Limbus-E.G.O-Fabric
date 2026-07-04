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

        // ── 破裂組（rupture，含束縛掛靠，11）──────────────────────────
        reg("dry_to_the_bone_breast", new DryToTheBoneBreast());
        reg("ebony_brooch", new EbonyBrooch());
        reg("flower_in_the_mirror", new FlowerInTheMirror());
        reg("harestride", new Harestride());
        reg("moon_in_the_water", new MoonInTheWater());
        reg("ruin", new Ruin());
        reg("smoking_gunpowder", new SmokingGunpowder());
        reg("strange_glyph_inscriptions", new StrangeGlyphInscriptions());
        reg("strange_glyph_talisman", new StrangeGlyphTalisman());
        reg("thunderbranch", new Thunderbranch());
        reg("chief_butlers_secret_arts", new ChiefButlersSecretArts());

        // ── 震顫組(tremor，6)──────────────────────────────────────────
        reg("green_spirit", new GreenSpirit());
        reg("nixie_divergence", new NixieDivergence());
        reg("sour_liquor_aroma", new SourLiquorAroma());
        reg("sownpour", new Sownpour());
        reg("piece_of_crumbled_egg", new PieceOfCrumbledEgg());
        reg("handheld_mirror", new HandheldMirror());

        // ── 呼吸法組（poise，7）────────────────────────────────────────
        reg("cask_spirits", new CaskSpirits());
        reg("clear_mirror_calm_water", new ClearMirrorCalmWater());
        reg("emerald_elytra", new EmeraldElytra());
        reg("finifugality", new Finifugality());
        reg("keenbranch", new Keenbranch());
        reg("nebulizer", new Nebulizer());
        reg("cqc_manual", new CQCManual());

        // ── 輔助組（support，15）── ※鮮血裝飾為先前試水件
        reg("bloody_gadget", new BloodyGadget());
        reg("dreaming_electric_sheep", new DreamingElectricSheep());
        reg("dueling_manual_book_3", new DuelingManualBook3());
        reg("illusory_hunt", new IllusoryHunt());
        reg("late_bloomers_tattoo", new LateBloomersTattoo());
        reg("hardship", new Hardship());
        reg("phantom_pain", new PhantomPain());
        reg("tenacity_bolus", new TenacityBolus());
        reg("the_book_of_vengeance", new TheBookOfVengeance());
        reg("special_contract", new SpecialContract());
        reg("plume_of_proof", new PlumeOfProof());
        reg("spicebush_branch", new SpicebushBranch());
        reg("carmilla", new Carmilla());
        reg("e_type_dimensional_dagger", new ETypeDimensionalDagger());
        reg("trauma_shield", new TraumaShield());

        // ── 便利組（qol，12）──────────────────────────────────────────
        reg("blue_zippo_lighter", new BlueZippoLighter());
        reg("child_within_a_flask", new ChildWithinAFlask());
        reg("golden_urn", new GoldenUrn());
        reg("homeward", new Homeward());
        reg("lithograph", new Lithograph());
        reg("oracle", new Oracle());
        reg("prejudice", new Prejudice());
        reg("piece_of_relationship", new PieceOfRelationship());
        reg("rusty_commemorative_coin", new RustyCommemorativeCoin());
        reg("someones_device", new SomeonesDevice());
        reg("sunshower", new Sunshower());
        reg("trial_plan_guide", new TrialPlanGuide());

        // ── 原創組（original，5）──────────────────────────────────────
        reg("endless_hunger", new EndlessHunger());
        reg("flower_mound", new FlowerMound());
        reg("jin_gang_bolus", new JinGangBolus());
        reg("piece_of_a_torn_summer", new PieceOfATornSummer());
        reg("tranquil_lotus_bolus", new TranquilLotusBolus());
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
