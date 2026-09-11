package me.yisang.limbusego.extractor;

import me.yisang.limbusego.gift.Vestiges;
import me.yisang.limbusego.item.ModItems;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ArrayPropertyDelegate;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;

/**
 * 提取機的容器：3 個機器槽（觸媒／Enkephalin／產出）+ 36 個玩家槽。
 * 進度用 {@link PropertyDelegate} 同步（[0]=progress、[1]=maxProgress），與原版熔爐同做法。
 * 沒有按鈕，所以不需要任何自訂封包。
 */
public class ExtractorScreenHandler extends ScreenHandler {

    public static final int SLOT_CATALYST = 0;
    public static final int SLOT_ENKEPHALIN = 1;
    public static final int SLOT_OUTPUT = 2;
    public static final int MACHINE_SLOTS = 3;

    private static final int PLAYER_SLOTS = 36;
    private static final int PROPERTY_COUNT = 2;

    private final Inventory inventory;
    private final PropertyDelegate delegate;

    /** 客戶端建構子：由 ScreenHandlerType 透過封包建立，庫存與進度隨後同步。 */
    public ExtractorScreenHandler(int syncId, PlayerInventory playerInv) {
        this(syncId, playerInv, new SimpleInventory(MACHINE_SLOTS), new ArrayPropertyDelegate(PROPERTY_COUNT));
    }

    /** 伺服端建構子：由 {@link ExtractorBlockEntity#createScreenHandler} 傳入實際庫存與進度。 */
    public ExtractorScreenHandler(int syncId, PlayerInventory playerInv, Inventory inventory, PropertyDelegate delegate) {
        super(ModBlocks.EXTRACTOR_SCREEN_HANDLER, syncId);
        checkSize(inventory, MACHINE_SLOTS);
        checkDataCount(delegate, PROPERTY_COUNT);
        this.inventory = inventory;
        this.delegate = delegate;
        inventory.onOpen(playerInv.player);

        addSlot(new Slot(inventory, SLOT_CATALYST, 56, 17) {
            @Override public boolean canInsert(ItemStack stack) { return Vestiges.isVestige(stack.getItem()); }
        });
        addSlot(new Slot(inventory, SLOT_ENKEPHALIN, 56, 53) {
            @Override public boolean canInsert(ItemStack stack) { return stack.isOf(ModItems.ENKEPHALIN); }
        });
        addSlot(new Slot(inventory, SLOT_OUTPUT, 116, 35) {
            @Override public boolean canInsert(ItemStack stack) { return false; }
        });

        // 玩家背包 3×9
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInv, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }
        // 快捷列
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInv, col, 8 + col * 18, 142));
        }

        addProperties(delegate);
    }

    public int getProgress() {
        return delegate.get(0);
    }

    public int getMaxProgress() {
        return delegate.get(1);
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return inventory.canPlayerUse(player);
    }

    /** Shift 點擊：機器槽 → 玩家；玩家 → 依物品種類進觸媒或 Enkephalin 槽，否則背包 ⇄ 快捷列。 */
    @Override
    public ItemStack quickMove(PlayerEntity player, int slotIndex) {
        Slot slot = slots.get(slotIndex);
        if (slot == null || !slot.hasStack()) return ItemStack.EMPTY;

        ItemStack original = slot.getStack();
        ItemStack copy = original.copy();
        int playerStart = MACHINE_SLOTS;
        int playerEnd = MACHINE_SLOTS + PLAYER_SLOTS;

        if (slotIndex < MACHINE_SLOTS) {
            if (!insertItem(original, playerStart, playerEnd, true)) return ItemStack.EMPTY;
            slot.onQuickTransfer(original, copy);
        } else {
            boolean moved;
            if (Vestiges.isVestige(original.getItem())) {
                moved = insertItem(original, SLOT_CATALYST, SLOT_CATALYST + 1, false);
            } else if (original.isOf(ModItems.ENKEPHALIN)) {
                moved = insertItem(original, SLOT_ENKEPHALIN, SLOT_ENKEPHALIN + 1, false);
            } else if (slotIndex < playerStart + 27) {
                moved = insertItem(original, playerStart + 27, playerEnd, false);   // 背包 → 快捷列
            } else {
                moved = insertItem(original, playerStart, playerStart + 27, false); // 快捷列 → 背包
            }
            if (!moved) return ItemStack.EMPTY;
        }

        if (original.isEmpty()) slot.setStack(ItemStack.EMPTY);
        else slot.markDirty();
        if (original.getCount() == copy.getCount()) return ItemStack.EMPTY;
        slot.onTakeItem(player, original);
        return copy;
    }

    @Override
    public void onClosed(PlayerEntity player) {
        super.onClosed(player);
        inventory.onClose(player);
    }
}
