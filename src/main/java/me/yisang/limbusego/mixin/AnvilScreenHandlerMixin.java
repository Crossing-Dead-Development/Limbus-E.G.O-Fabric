package me.yisang.limbusego.mixin;

import me.yisang.limbusego.gift.GiftUpgrade;
import me.yisang.limbusego.gift.GiftUpgradeLogic;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.AnvilScreenHandler;
import net.minecraft.screen.Property;
import net.minecraft.screen.ScreenHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 鐵砧殘影升級：飾品（左，slot 0）＋同階殘影（右，slot 1）→ 等級 +1 的飾品（輸出，slot 2）。
 *
 * <p>於 {@code updateResult} 末端覆寫輸出：設 {@code repairItemUsage=1}（取走時只消耗
 * 1 個殘影，左飾品格由原生 onTakeOutput 清空 → 升級後飾品進游標），並設 XP 花費
 * （> 0 才允許取出）。消耗與扣經驗沿用原生 onTakeOutput，無需另攔。
 *
 * <p>輸入/輸出以繼承的 public {@link ScreenHandler#getSlot} 存取，避免跨類 shadow 父類欄位。
 */
@Mixin(AnvilScreenHandler.class)
public abstract class AnvilScreenHandlerMixin {

    @Shadow private int repairItemUsage;
    @Shadow @Final private Property levelCost;

    @Inject(method = "updateResult", at = @At("TAIL"))
    private void limbusego$giftUpgrade(CallbackInfo ci) {
        ScreenHandler self = (ScreenHandler) (Object) this;
        ItemStack result = GiftUpgrade.tryUpgrade(
                self.getSlot(AnvilScreenHandler.INPUT_1_ID).getStack(),
                self.getSlot(AnvilScreenHandler.INPUT_2_ID).getStack());
        if (result == null) return;
        self.getSlot(AnvilScreenHandler.OUTPUT_ID).setStack(result);
        this.repairItemUsage = 1; // 取走時消耗 1 個殘影
        this.levelCost.set(GiftUpgradeLogic.XP_COST);
        self.sendContentUpdates();
    }
}
