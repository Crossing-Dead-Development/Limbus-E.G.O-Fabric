package me.yisang.limbusego.gui;

import me.yisang.limbusego.item.ModItems;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;

/** 武器圖鑑：只看不拿。 */
public class WeaponCatalogGui {

    public static SimpleNamedScreenHandlerFactory create() {
        return new SimpleNamedScreenHandlerFactory((syncId, playerInventory, player) -> {
            SimpleInventory inv = new SimpleInventory(54);
            int i = 0;
            inv.setStack(i++, new ItemStack(ModItems.SOLEMN_LAMENT_BLACK));
            inv.setStack(i++, new ItemStack(ModItems.SOLEMN_LAMENT_WHITE));
            inv.setStack(i++, new ItemStack(ModItems.SOLEMN_SHIELD));
            inv.setStack(i++, new ItemStack(ModItems.MIMICRY));
            inv.setStack(i++, new ItemStack(ModItems.DACAPO));
            inv.setStack(i++, new ItemStack(ModItems.RING_BRUSH));
            inv.setStack(i++, new ItemStack(ModItems.W_CORP_KNIFE));
            inv.setStack(i++, new ItemStack(ModItems.TIANTUI_STAR));
            inv.setStack(i++, new ItemStack(ModItems.TWILIGHT));
            inv.setStack(i++, new ItemStack(ModItems.TIBIA));
            inv.setStack(i++, new ItemStack(ModItems.BLADESINGER));

            return new GenericContainerScreenHandler(ScreenHandlerType.GENERIC_9X6, syncId, playerInventory, inv, 6) {
                @Override
                public void onSlotClick(int slotIndex, int button, SlotActionType actionType, PlayerEntity player) {
                    if (slotIndex >= 0 && slotIndex < 54) return; // 圖鑑：禁止取出
                    super.onSlotClick(slotIndex, button, actionType, player);
                }

                @Override
                public ItemStack quickMove(PlayerEntity player, int index) {
                    return ItemStack.EMPTY;
                }
            };
        }, Text.literal("Limbus E.G.O — 武器圖鑑"));
    }
}
