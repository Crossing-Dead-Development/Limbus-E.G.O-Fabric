package me.yisang.limbusego.mixin.client;

import me.yisang.limbusego.item.SolemnLamentItem;
import me.yisang.limbusego.item.SolemnLamentLogic;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 莊嚴哀悼成功黑白雙持時，雙手都套用 CROSSBOW_HOLD，呈現舉槍姿態。
 *
 * <p>vanilla 的 getArmPose 把 CROSSBOW_HOLD 寫死判 {@code stack.isOf(Items.CROSSBOW)}，
 * 自訂物品拿不到，故在此補上。未配對時不介入，維持一般持物姿勢。
 */
@Mixin(PlayerEntityRenderer.class)
public class PlayerEntityRendererMixin {

    @Inject(
            method = "getArmPose(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/item/ItemStack;Lnet/minecraft/util/Hand;)Lnet/minecraft/client/render/entity/model/BipedEntityModel$ArmPose;",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void limbusego$solemnDualWieldPose(PlayerEntity player, ItemStack stack, Hand hand,
            CallbackInfoReturnable<BipedEntityModel.ArmPose> cir) {
        if (!(stack.getItem() instanceof SolemnLamentItem)) return;

        ItemStack main = player.getStackInHand(Hand.MAIN_HAND);
        ItemStack off = player.getStackInHand(Hand.OFF_HAND);
        if (SolemnLamentLogic.isPaired(stackIsSolemn(main), stackIsBlack(main),
                stackIsSolemn(off), stackIsBlack(off))) {
            cir.setReturnValue(BipedEntityModel.ArmPose.CROSSBOW_HOLD);
        }
    }

    private static boolean stackIsSolemn(ItemStack stack) {
        return stack.getItem() instanceof SolemnLamentItem;
    }

    private static boolean stackIsBlack(ItemStack stack) {
        return stack.getItem() instanceof SolemnLamentItem s && s.isBlack;
    }
}
