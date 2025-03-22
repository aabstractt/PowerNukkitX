package cn.nukkit.blockentity;

import cn.nukkit.block.Block;
import cn.nukkit.inventory.BaseInventory;
import cn.nukkit.inventory.ChestInventory;
import cn.nukkit.inventory.ContainerInventory;
import cn.nukkit.inventory.DoubleChestInventory;
import cn.nukkit.level.format.IChunk;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.tag.CompoundTag;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * @author MagicDroidX (Nukkit Project)
 */
public class BlockEntityChest extends BlockEntitySpawnableContainer {

    private @Nullable Integer pairX;
    private @Nullable Integer pairZ;

    protected DoubleChestInventory doubleInventory = null;

    public BlockEntityChest(IChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
        movable = true;
    }

    @Override
    protected ContainerInventory requireContainerInventory() {
        return Objects.requireNonNullElseGet(this.inventory, () -> new ChestInventory(this));
    }

    @Override
    public void close() {
        if (this.closed) return;

        unpair();
        this.getInventory().getViewers().forEach(p -> p.removeWindow(this.getInventory()));
        this.getRealInventory().getViewers().forEach(p -> p.removeWindow(this.getRealInventory()));

        this.closed = true;
        if (this.chunk != null) {
            this.chunk.removeBlockEntity(this);
        }
        if (this.level != null) {
            this.level.removeBlockEntity(this);
        }
        this.level = null;
    }

    @Override
    public boolean isBlockEntityValid() {
        String blockID = this.getBlock().getId();
        return blockID.equals(Block.CHEST) || blockID.equals(Block.TRAPPED_CHEST);
    }

    public int getSize() {
        return this.doubleInventory != null ? this.doubleInventory.getSize() : this.inventory.getSize();
    }

    @Override
    public BaseInventory getInventory() {
        if (this.doubleInventory == null && this.isPaired()) {
            this.checkPairing();
        }

        return this.doubleInventory != null ? this.doubleInventory : this.inventory;
    }

    public ChestInventory getRealInventory() {
        return (ChestInventory) inventory;
    }

    protected void checkPairing() {
        if (this.pairX == null || this.pairZ == null) return;

        if (!level.loadChunk(this.pairX >> 4, this.pairZ >> 4)) {
            this.doubleInventory = null;

            return;
        }

        BlockEntityChest pair = this.getPair();
        if (pair == null) {
            this.doubleInventory = null;

            this.pairX = this.pairZ = null;

            return;
        }

        if (!pair.isPaired()) {
            pair.pairWith(this);
            this.pairWith(pair);
        }

        if (pair.doubleInventory != null) {
            this.doubleInventory = pair.doubleInventory;
        } else if (this.doubleInventory == null) {
            if ((pair.getFloorX() + (pair.getFloorZ() << 15)) > (this.getFloorX() + (this.getFloorZ() << 15))) { //Order them correctly
                this.doubleInventory = pair.doubleInventory = new DoubleChestInventory(pair, this);
            } else {
                this.doubleInventory = pair.doubleInventory = new DoubleChestInventory(this, pair);
            }
        }
    }

    public boolean isPaired() {
        return this.pairX != null && this.pairZ != null;
    }

    protected @Nullable BlockEntityChest getPair() {
        if (this.pairX == null || this.pairZ == null) return null;

        BlockEntity blockEntity = this.getLevel().getBlockEntityIfLoaded(new Vector3(this.pairX, this.y, this.pairZ));
        return blockEntity instanceof BlockEntityChest ? (BlockEntityChest) blockEntity : null;
    }

    public boolean pairWith(BlockEntityChest chest) {
        if (this.isPaired() || chest.isPaired()) return false;

        this.createPair(chest);
        this.checkPairing();

        chest.spawnToAll();
        this.spawnToAll();

        return true;
    }

    public void createPair(BlockEntityChest chest) {
        this.pairX = chest.getFloorX();
        this.pairZ = chest.getFloorZ();

        chest.pairX = this.getFloorX();
        chest.pairZ = this.getFloorZ();
    }

    public boolean unpair() {
        BlockEntityChest chest = this.getPair();
        if (chest == null) return false;

        chest.doubleInventory = null;
        chest.pairX = null;
        chest.pairZ = null;
        chest.spawnToAll();

        this.doubleInventory = null;
        this.pairX = null;
        this.pairZ = null;
        this.spawnToAll();

        return true;
    }

    @Override
    public String getName() {
        return this.name != null ? this.name : "Chest";
    }

    @Override
    public boolean hasName() {
        return this.name != null;
    }

    @Override
    public void setName(String name) {
        this.name = name == null || name.isEmpty() ? null : name;
    }

    @Override
    public void onBreak(boolean isSilkTouch) {
        unpair();
        super.onBreak(isSilkTouch);
    }

    @Override
    public CompoundTag getSpawnCompound() {
        CompoundTag nbt = super.getSpawnCompound();
        if (this.name != null) nbt.putString("CustomName", this.name);

        if (this.pairX == null || this.pairZ == null) return nbt;

        return nbt.putInt("pairx", this.pairX).putInt("pairz", this.pairZ);
    }

    @Override
    public CompoundTag getCleanedNBT() {
        return super.getCleanedNBT().remove("pairx").remove("pairz");
    }

    @Override
    public void saveNBT() {
        super.saveNBT();

        if (this.name != null) this.namedTag.putString("CustomName", this.name);

        if (this.pairX == null || this.pairZ == null) return;

        this.namedTag.putInt("pairx", this.pairX);
        this.namedTag.putInt("pairz", this.pairZ);
    }

    @Override
    protected void initBlockEntity() {
        super.initBlockEntity();

        if (this.namedTag.contains("pairx") && this.namedTag.contains("pairz")) {
            this.pairX = this.namedTag.getInt("pairx");
            this.pairZ = this.namedTag.getInt("pairz");
        }
    }
}
