package me.yisang.limbusego.mixin.client;

import me.yisang.limbusego.item.SolemnLamentItem;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 莊嚴哀悼是自訂弩物品（非 Items.CROSSBOW）。vanilla 的 getArmPose 把「已裝填握持」姿勢
 * (CROSSBOW_HOLD) 寫死判 stack.isOf(Items.CROSSBOW)，導致自訂弩上弦後手不舉起。
 * 此處攔截：對莊嚴哀悼在上弦中回 CROSSBOW_CHARGE、已裝填回 CROSSBOW_HOLD，補回弩姿勢。
 */
@Mixin(PlayerEntityRenderer.class)
public class PlayerEntityRendererMixin {

    @Inject(
            method = "getArmPose(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/item/ItemStack;Lnet/minecraft/util/Hand;)Lnet/minecraft/client/render/entity/model/BipedEntityModel$ArmPose;",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void limbusego$solemnCrossbowPose(PlayerEntity player, ItemStack stack, Hand hand,
            CallbackInfoReturnable<BipedEntityModel.ArmPose> cir) {
        if (!(stack.getItem() instanceof SolemnLamentItem)) return;
        if (player.isUsingItem() && player.getActiveHand() == hand) {
            cir.setReturnValue(BipedEntityModel.ArmPose.CROSSBOW_CHARGE);
        } else if (CrossbowItem.isCharged(stack)) {
            cir.setReturnValue(BipedEntityModel.ArmPose.CROSSBOW_HOLD);
        }
    }
}
