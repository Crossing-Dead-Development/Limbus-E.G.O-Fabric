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

/** 武器管理員：點擊快速取得。 */
public class WeaponAdminGui {

    public static SimpleNamedScreenHandlerFactory create() {
        return new SimpleNamedScreenHandlerFactory((syncId, playerInventory, player) -> {
            SimpleInventory inv = new SimpleInventory(36);
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
            inv.setStack(i++, new ItemStack(ModItems.BUTTERFLY_QUARTZ, 64));
            inv.setStack(i++, new ItemStack(ModItems.TIGER_MARK, 64));
            inv.setStack(i++, new ItemStack(ModItems.SAVAGE_TIGER_MARK, 64));

            return new GenericContainerScreenHandler(ScreenHandlerType.GENERIC_9X4, syncId, playerInventory, inv, 4) {
                @Override
                public void onSlotClick(int slotIndex, int button, SlotActionType actionType, PlayerEntity player) {
                    if (slotIndex >= 0 && slotIndex < 36) {
                        ItemStack clicked = this.getInventory().getStack(slotIndex);
                        if (!clicked.isEmpty()) {
                            player.getInventory().offerOrDrop(clicked.copy());
                            player.sendMessage(Text.literal("§a已給予 ").append(clicked.getName()), false);
                        }
                        return;
                    }
                    super.onSlotClick(slotIndex, button, actionType, player);
                }

                @Override
                public ItemStack quickMove(PlayerEntity player, int index) {
                    return ItemStack.EMPTY;
                }
            };
        }, Text.literal("Limbus E.G.O — 武器管理員"));
    }
}
