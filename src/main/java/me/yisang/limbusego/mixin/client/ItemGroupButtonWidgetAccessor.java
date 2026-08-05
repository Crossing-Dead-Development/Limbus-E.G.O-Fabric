package me.yisang.limbusego.mixin.client;

import net.fabricmc.fabric.impl.client.itemgroup.FabricCreativeGuiComponents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * 讀取 Fabric 翻頁按鈕的 type（NEXT/PREVIOUS），供重排時判斷左右。
 * 註：耦合 fabric-item-group-api 內部實作，Fabric 改版可能需同步調整。
 */
@Mixin(FabricCreativeGuiComponents.ItemGroupButtonWidget.class)
public interface ItemGroupButtonWidgetAccessor {
    @Accessor("type")
    FabricCreativeGuiComponents.Type limbusego$getType();
}
