package me.yisang.limbusego.mixin;

import me.yisang.limbusego.item.SolemnLamentItem;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 莊嚴哀悼高速裝填：對齊插件隱藏「快速上弦 V」（QUICK_CHARGE 5）。
 * vanilla getPullTime 讀 QUICK_CHARGE 附魔縮短上弦時間；1.21.4 附魔為動態註冊、物品註冊時不便掛，
 * 故直接攔截 getPullTime 對莊嚴哀悼回傳極短上弦時間（近乎瞬發）。
 */
@Mixin(CrossbowItem.class)
public class CrossbowItemMixin {

    @Inject(
            method = "getPullTime(Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/LivingEntity;)I",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void limbusego$quickLoad(ItemStack stack, LivingEntity shooter,
            CallbackInfoReturnable<Integer> cir) {
        if (stack.getItem() instanceof SolemnLamentItem) {
            cir.setReturnValue(5); // ≈QUICK_CHARGE V 高速裝填（>0 以避免 getPullProgress 除零）
        }
    }
}
