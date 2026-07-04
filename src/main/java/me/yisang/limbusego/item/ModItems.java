package me.yisang.limbusego.item;

import me.yisang.limbusego.LimbusEGOMod;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Rarity;

/**
 * 物品註冊。武器類別在 P1-T5 ~ T7 逐批加入；
 * 不移植插翅虎（chatuhu）與終末鳥（apocalypse_bird）組合包。
 */
public class ModItems {

    public static Item MOD_ICON;
    public static Item BUTTERFLY_QUARTZ;
    public static Item TIGER_MARK;
    public static Item SAVAGE_TIGER_MARK;
    public static Item SOLEMN_LAMENT_BLACK;
    public static Item SOLEMN_LAMENT_WHITE;
    public static Item SOLEMN_SHIELD;
    public static Item MIMICRY;
    public static Item DACAPO;
    public static Item RING_BRUSH;
    public static Item W_CORP_KNIFE;
    public static Item TIANTUI_STAR;
    public static Item TWILIGHT;
    public static Item TIBIA;
    public static Item BLADESINGER;

    public static void register() {
        MOD_ICON = reg("mod_icon", new Item(key("mod_icon")));

        BUTTERFLY_QUARTZ = reg("butterfly_quartz",
                new Item(key("butterfly_quartz").maxCount(64).rarity(Rarity.UNCOMMON)));

        SOLEMN_LAMENT_BLACK = reg("solemn_lament_black",
                new SolemnLamentItem(true, key("solemn_lament_black").maxCount(1).rarity(Rarity.EPIC)));

        SOLEMN_LAMENT_WHITE = reg("solemn_lament_white",
                new SolemnLamentItem(false, key("solemn_lament_white").maxCount(1).rarity(Rarity.EPIC)));

        SOLEMN_SHIELD = reg("solemn_shield",
                new SolemnShieldItem(key("solemn_shield").maxCount(1).rarity(Rarity.RARE)));

        TIGER_MARK = reg("tiger_mark",
                new Item(key("tiger_mark").maxCount(64).rarity(Rarity.UNCOMMON)));

        SAVAGE_TIGER_MARK = reg("savage_tiger_mark",
                new Item(key("savage_tiger_mark").maxCount(64).rarity(Rarity.RARE)));

        MIMICRY = reg("mimicry",
                new MimicryItem(key("mimicry").maxCount(1).rarity(Rarity.EPIC)
                        .component(DataComponentTypes.ATTRIBUTE_MODIFIERS,
                                weaponModifiers("mimicry", 12.0, -3.2))));

        DACAPO = reg("dacapo",
                new DaCapoItem(key("dacapo").maxCount(1).rarity(Rarity.EPIC)
                        .component(DataComponentTypes.ATTRIBUTE_MODIFIERS,
                                weaponModifiers("dacapo", 7.0, -2.4))));

        RING_BRUSH = reg("ring_brush",
                new RingBrushItem(key("ring_brush").maxCount(1).rarity(Rarity.EPIC)
                        .component(DataComponentTypes.ATTRIBUTE_MODIFIERS,
                                weaponModifiers("ring_brush", 7.0, -2.4))));

        W_CORP_KNIFE = reg("w_corp_knife",
                new WCorpKnifeItem(key("w_corp_knife").maxCount(1).rarity(Rarity.RARE)
                        .component(DataComponentTypes.ATTRIBUTE_MODIFIERS,
                                weaponModifiers("w_corp_knife", 4.0, -1.6))));

        TIANTUI_STAR = reg("tiantui_star",
                new TiantuiStarItem(key("tiantui_star").maxCount(1).rarity(Rarity.EPIC)
                        .component(DataComponentTypes.ATTRIBUTE_MODIFIERS,
                                weaponModifiers("tiantui_star", 8.0, -2.4))));

        TWILIGHT = reg("twilight",
                new TwilightItem(key("twilight").maxCount(1).rarity(Rarity.EPIC)
                        .component(DataComponentTypes.ATTRIBUTE_MODIFIERS,
                                twilightModifiers())));

        TIBIA = reg("tibia",
                new TibiaItem(key("tibia").maxCount(1).rarity(Rarity.EPIC)
                        .component(DataComponentTypes.ATTRIBUTE_MODIFIERS,
                                reachModifiers("tibia", 10.0, -2.8, 1.0))));

        BLADESINGER = reg("bladesinger",
                new BladesingerItem(key("bladesinger").maxCount(1).rarity(Rarity.EPIC)
                        .component(DataComponentTypes.ATTRIBUTE_MODIFIERS,
                                weaponModifiers("bladesinger", 9.0, -2.6))));
    }

    /** 薄暝：攻擊/攻速 + 互動距離 +1.5。 */
    private static AttributeModifiersComponent twilightModifiers() {
        return reachModifiers("twilight", 9.0, -2.4, 1.5);
    }

    /** 近戰武器 + 互動距離 modifier。 */
    static AttributeModifiersComponent reachModifiers(String id, double damage, double speed, double reach) {
        return AttributeModifiersComponent.builder()
                .add(EntityAttributes.ATTACK_DAMAGE,
                        new EntityAttributeModifier(LimbusEGOMod.id(id + "_damage"), damage,
                                EntityAttributeModifier.Operation.ADD_VALUE),
                        AttributeModifierSlot.MAINHAND)
                .add(EntityAttributes.ATTACK_SPEED,
                        new EntityAttributeModifier(LimbusEGOMod.id(id + "_speed"), speed,
                                EntityAttributeModifier.Operation.ADD_VALUE),
                        AttributeModifierSlot.MAINHAND)
                .add(EntityAttributes.ENTITY_INTERACTION_RANGE,
                        new EntityAttributeModifier(LimbusEGOMod.id(id + "_reach"), reach,
                                EntityAttributeModifier.Operation.ADD_VALUE),
                        AttributeModifierSlot.MAINHAND)
                .build();
    }

    static Item.Settings key(String name) {
        return new Item.Settings()
                .registryKey(RegistryKey.of(RegistryKeys.ITEM, LimbusEGOMod.id(name)));
    }

    /** 近戰武器共用：主手攻擊/攻速 modifier。 */
    static AttributeModifiersComponent weaponModifiers(String id, double damage, double speed) {
        return AttributeModifiersComponent.builder()
                .add(EntityAttributes.ATTACK_DAMAGE,
                        new EntityAttributeModifier(
                                LimbusEGOMod.id(id + "_damage"),
                                damage,
                                EntityAttributeModifier.Operation.ADD_VALUE),
                        AttributeModifierSlot.MAINHAND)
                .add(EntityAttributes.ATTACK_SPEED,
                        new EntityAttributeModifier(
                                LimbusEGOMod.id(id + "_speed"),
                                speed,
                                EntityAttributeModifier.Operation.ADD_VALUE),
                        AttributeModifierSlot.MAINHAND)
                .build();
    }

    static <T extends Item> T reg(String name, T item) {
        return Registry.register(Registries.ITEM, LimbusEGOMod.id(name), item);
    }
}
