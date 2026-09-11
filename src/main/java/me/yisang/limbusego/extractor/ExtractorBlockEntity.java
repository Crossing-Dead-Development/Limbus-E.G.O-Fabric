package me.yisang.limbusego.extractor;

import me.yisang.limbusego.gift.Vestiges;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;

/**
 * 提取機方塊實體：三槽庫存 + 進度。行為仿熔爐——條件全滿足自動跑，任一破掉就歸零暫停。
 *
 * <p>本類只做「讀槽位 → 呼叫 {@link ExtractionLogic} → 套用結果」，
 * 所有數值與可否啟動的判斷都在純函式裡。tick 內不丟例外。
 */
public class ExtractorBlockEntity extends LockableContainerBlockEntity {

    private static final String NBT_PROGRESS = "Progress";

    private DefaultedList<ItemStack> stacks = DefaultedList.ofSize(ExtractorScreenHandler.MACHINE_SLOTS, ItemStack.EMPTY);
    private int progress;
    private int maxProgress;

    private final PropertyDelegate delegate = new PropertyDelegate() {
        @Override public int get(int index) {
            return switch (index) {
                case 0 -> progress;
                case 1 -> maxProgress;
                default -> 0;
            };
        }
        @Override public void set(int index, int value) {
            switch (index) {
                case 0 -> progress = value;
                case 1 -> maxProgress = value;
                default -> { }
            }
        }
        @Override public int size() { return 2; }
    };

    public ExtractorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.EXTRACTOR_BLOCK_ENTITY, pos, state);
    }

    // ── tick ────────────────────────────────────────────────────────

    /** 僅伺服端（由 {@link ExtractorBlock#getTicker} 保證）。 */
    public static void tick(World world, BlockPos pos, BlockState state, ExtractorBlockEntity be) {
        ItemStack catalyst = be.stacks.get(ExtractorScreenHandler.SLOT_CATALYST);
        ItemStack enkephalin = be.stacks.get(ExtractorScreenHandler.SLOT_ENKEPHALIN);
        ItemStack output = be.stacks.get(ExtractorScreenHandler.SLOT_OUTPUT);

        int tier = Vestiges.tierOf(catalyst.getItem());
        List<Item> pool = ExtractionPools.of(tier);
        boolean canStart = ExtractionLogic.canStart(tier, enkephalin.getCount(), output.isEmpty(), pool.size());

        if (!canStart) {
            if (be.progress != 0) {
                be.progress = 0;
                be.markDirty();
            }
            return;
        }

        be.maxProgress = ExtractionLogic.durationOf(tier);
        be.progress++;
        if (be.progress < be.maxProgress) {
            be.markDirty();
            return;
        }

        // 完成：扣材料、抽飾品、放產出
        catalyst.decrement(1);
        enkephalin.decrement(ExtractionLogic.costOf(tier));
        int idx = ExtractionLogic.pick(pool.size(), world.random.nextDouble());
        be.stacks.set(ExtractorScreenHandler.SLOT_OUTPUT, new ItemStack(pool.get(idx)));
        be.progress = 0;
        be.markDirty();
        world.playSound(null, pos, SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE, SoundCategory.BLOCKS, 1.0f, 1.2f);
    }

    // ── Inventory ───────────────────────────────────────────────────

    @Override
    public int size() {
        return stacks.size();
    }

    @Override
    protected DefaultedList<ItemStack> getHeldStacks() {
        return stacks;
    }

    @Override
    protected void setHeldStacks(DefaultedList<ItemStack> stacks) {
        this.stacks = stacks;
    }

    /** 觸媒槽只收殘影、Enkephalin 槽只收 Enkephalin、產出槽不收（漏斗等自動化也受限）。 */
    @Override
    public boolean isValid(int slot, ItemStack stack) {
        return switch (slot) {
            case ExtractorScreenHandler.SLOT_CATALYST -> Vestiges.isVestige(stack.getItem());
            case ExtractorScreenHandler.SLOT_ENKEPHALIN -> stack.isOf(me.yisang.limbusego.item.ModItems.ENKEPHALIN);
            default -> false;
        };
    }

    // ── NamedScreenHandlerFactory ───────────────────────────────────

    @Override
    protected Text getContainerName() {
        return Text.translatable("container.limbusego.extractor");
    }

    @Override
    protected ScreenHandler createScreenHandler(int syncId, PlayerInventory playerInventory) {
        return new ExtractorScreenHandler(syncId, playerInventory, this, delegate);
    }

    // ── NBT ─────────────────────────────────────────────────────────

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.writeNbt(nbt, registries);
        Inventories.writeNbt(nbt, stacks, registries);
        nbt.putInt(NBT_PROGRESS, progress);
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.readNbt(nbt, registries);
        stacks = DefaultedList.ofSize(size(), ItemStack.EMPTY);
        Inventories.readNbt(nbt, stacks, registries);
        progress = nbt.getInt(NBT_PROGRESS);
    }
}
