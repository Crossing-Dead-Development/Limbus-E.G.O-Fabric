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

    public static void register() {
        MOD_ICON = reg("mod_icon", new Item(key("mod_icon")));

        BUTTERFLY_QUARTZ = reg("butterfly_quartz",
                new Item(key("butterfly_quartz").maxCount(64).rarity(Rarity.UNCOMMON)));

        TIGER_MARK = reg("tiger_mark",
                new Item(key("tiger_mark").maxCount(64).rarity(Rarity.UNCOMMON)));

        SAVAGE_TIGER_MARK = reg("savage_tiger_mark",
                new Item(key("savage_tiger_mark").maxCount(64).rarity(Rarity.RARE)));
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
