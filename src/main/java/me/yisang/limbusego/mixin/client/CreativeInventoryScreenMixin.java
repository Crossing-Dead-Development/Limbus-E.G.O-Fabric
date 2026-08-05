package me.yisang.limbusego.mixin.client;

import net.fabricmc.fabric.impl.client.itemgroup.FabricCreativeGuiComponents;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 把 Fabric 的創造分頁翻頁按鈕（< 上一頁 / > 下一頁）從頁碼旁的小按鈕
 * 重新排到創造面板的左上／右上角。貼圖仍用 fabric:creative_buttons.png（資源包可重繪）。
 * priority 1500 確保跑在 fabric-item-group-api 加入按鈕之後。
 * 註：全域改動、耦合 Fabric 內部實作，Fabric 改版或其他動創造畫面的 mod 可能需同步/衝突。
 */
@Mixin(value = CreativeInventoryScreen.class, priority = 1500)
public abstract class CreativeInventoryScreenMixin extends Screen {

    protected CreativeInventoryScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void limbusego$repositionPageButtons(CallbackInfo ci) {
        final int bgW = 195; // 創造面板寬
        final int bgH = 136; // 創造面板高
        int left = (this.width - bgW) / 2;
        int top = (this.height - bgH) / 2;

        for (Element e : this.children()) {
            if (!(e instanceof FabricCreativeGuiComponents.ItemGroupButtonWidget btn)) continue;
            boolean prev = ((ItemGroupButtonWidgetAccessor) btn).limbusego$getType()
                    == FabricCreativeGuiComponents.Type.PREVIOUS;
            ClickableWidget w = btn;
            if (prev) {
                w.setX(left - w.getWidth() - 4);   // 左上角（面板左外側）
            } else {
                w.setX(left + bgW + 4);            // 右上角（面板右外側）
            }
            w.setY(top);
        }
    }
}
