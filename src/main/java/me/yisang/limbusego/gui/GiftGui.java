package me.yisang.limbusego.gui;

import me.yisang.limbusego.gift.ModGifts;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;

import java.util.List;

/**
 * E.G.O 飾品 GUI：圖鑑（唯讀）與管理員（點擊取得）共用。
 * 80 件分頁顯示，上 5 列（45 格）為飾品，底列為換頁控制。
 */
public final class GiftGui {

    private static final int ITEMS_PER_PAGE = 45;   // 上 5 列
    private static final int PREV_SLOT = 45;
    private static final int INFO_SLOT = 49;
    private static final int NEXT_SLOT = 53;

    private GiftGui() {}

    public static SimpleNamedScreenHandlerFactory catalog() {
        return create(false);
    }

    public static SimpleNamedScreenHandlerFactory admin() {
        return create(true);
    }

    private static SimpleNamedScreenHandlerFactory create(boolean admin) {
        return new SimpleNamedScreenHandlerFactory((syncId, playerInventory, player) -> {
            List<Item> gifts = ModGifts.ordered();
            SimpleInventory inv = new SimpleInventory(54);

            return new GenericContainerScreenHandler(ScreenHandlerType.GENERIC_9X6, syncId, playerInventory, inv, 6) {
                int page = 0;
                final int maxPage = Math.max(0, (gifts.size() - 1) / ITEMS_PER_PAGE);

                {
                    fill();
                }

                void fill() {
                    for (int s = 0; s < 54; s++) inv.setStack(s, ItemStack.EMPTY);
                    int start = page * ITEMS_PER_PAGE;
                    for (int k = 0; k < ITEMS_PER_PAGE && start + k < gifts.size(); k++) {
                        inv.setStack(k, new ItemStack(gifts.get(start + k)));
                    }
                    if (page > 0) inv.setStack(PREV_SLOT, named(Items.ARROW, "§e← 上一頁"));
                    if (page < maxPage) inv.setStack(NEXT_SLOT, named(Items.ARROW, "§e下一頁 →"));
                    inv.setStack(INFO_SLOT, named(Items.PAPER, "§f第 " + (page + 1) + " / " + (maxPage + 1) + " 頁"));
                    sendContentUpdates();
                }

                @Override
                public void onSlotClick(int slotIndex, int button, SlotActionType actionType, PlayerEntity p) {
                    if (slotIndex == PREV_SLOT && page > 0) { page--; fill(); return; }
                    if (slotIndex == NEXT_SLOT && page < maxPage) { page++; fill(); return; }
                    if (slotIndex >= 0 && slotIndex < 54) {
                        if (admin && slotIndex < ITEMS_PER_PAGE) {
                            ItemStack clicked = inv.getStack(slotIndex);
                            if (!clicked.isEmpty()) {
                                p.getInventory().offerOrDrop(clicked.copy());
                                p.sendMessage(Text.literal("§a已給予 ").append(clicked.getName()), false);
                            }
                        }
                        return; // 兩模式都禁止把 GUI 物品拖進背包
                    }
                    super.onSlotClick(slotIndex, button, actionType, p);
                }

                @Override
                public ItemStack quickMove(PlayerEntity p, int index) {
                    return ItemStack.EMPTY;
                }
            };
        }, Text.literal(admin ? "Limbus E.G.O — 飾品管理員" : "Limbus E.G.O — 飾品圖鑑"));
    }

    private static ItemStack named(Item item, String name) {
        ItemStack st = new ItemStack(item);
        st.set(DataComponentTypes.CUSTOM_NAME, Text.literal(name));
        return st;
    }
}
