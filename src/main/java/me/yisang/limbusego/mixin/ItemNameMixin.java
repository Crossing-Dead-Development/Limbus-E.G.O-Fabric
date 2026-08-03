package me.yisang.limbusego.mixin;

import me.yisang.limbusego.LimbusEGOMod;
import me.yisang.limbusego.item.WeaponStyles;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 為 limbusego 武器提供樣式化顯示名稱（顏色／粗體）。
 * 1.21.4 的 {@code Item.Settings} 會用翻譯鍵預設無樣式 item_name 蓋掉手動設的 ITEM_NAME 元件，
 * 因此改在此攔截 {@code Item.getName(ItemStack)} 回傳 {@link WeaponStyles#styledName(String)}。
 * 非武器（styledName 回傳 null）維持原行為。
 */
@Mixin(Item.class)
public abstract class ItemNameMixin {

    @Inject(
            method = "getName(Lnet/minecraft/item/ItemStack;)Lnet/minecraft/text/Text;",
            at = @At("HEAD"),
            cancellable = true
    )
    private void limbusego$styledWeaponName(ItemStack stack, CallbackInfoReturnable<Text> cir) {
        Identifier itemId = Registries.ITEM.getId((Item) (Object) this);
        if (!LimbusEGOMod.MOD_ID.equals(itemId.getNamespace())) return;
        Text styled = WeaponStyles.styledName(itemId.getPath());
        if (styled != null) {
            cir.setReturnValue(styled);
        }
    }
}
