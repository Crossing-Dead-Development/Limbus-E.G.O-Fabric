package me.yisang.limbusego.mixin;

import me.yisang.limbusego.item.ModSounds;
import me.yisang.limbusego.item.SolemnLamentItem;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.SoundEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

/**
 * 莊嚴哀悼弩機制對齊插件：
 * 1. 高速裝填（≈QUICK_CHARGE V）— 攔 getPullTime 回傳極短上弦時間。
 * 2. 自訂裝填音 — 攔 getLoadingSounds 回傳 solemn 裝填音，取代原版弩上弦音。
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
            cir.setReturnValue(2); // ≈QUICK_CHARGE V 高速裝填（>0 以避免 getPullProgress 除零）
        }
    }

    @Inject(
            method = "getLoadingSounds(Lnet/minecraft/item/ItemStack;)Lnet/minecraft/item/CrossbowItem$LoadingSounds;",
            at = @At("HEAD"),
            cancellable = true
    )
    private void limbusego$loadingSounds(ItemStack stack,
            CallbackInfoReturnable<CrossbowItem.LoadingSounds> cir) {
        if (stack.getItem() instanceof SolemnLamentItem) {
            RegistryEntry<SoundEvent> load = Registries.SOUND_EVENT.getEntry(ModSounds.SOLEMN_QUICK_LOAD_3);
            cir.setReturnValue(new CrossbowItem.LoadingSounds(
                    Optional.of(load), Optional.empty(), Optional.empty()));
        }
    }
}
