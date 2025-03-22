package cn.nukkit.block;

import cn.nukkit.Player;
import cn.nukkit.block.property.CommonBlockProperties;
import cn.nukkit.block.property.CommonPropertyMap;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemBlock;
import cn.nukkit.level.Sound;
import cn.nukkit.math.BlockFace;
import cn.nukkit.math.Vector3;
import cn.nukkit.network.protocol.LevelSoundEventPacketV1;
import cn.nukkit.utils.Faceable;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

import static cn.nukkit.block.property.CommonBlockProperties.END_PORTAL_EYE_BIT;
import static cn.nukkit.block.property.CommonBlockProperties.MINECRAFT_CARDINAL_DIRECTION;

/**
 * @author Pub4Game
 * @since 26.12.2015
 */
public class BlockEndPortalFrame extends BlockTransparent implements Faceable {

    public static final BlockProperties PROPERTIES = new BlockProperties(END_PORTAL_FRAME,
            MINECRAFT_CARDINAL_DIRECTION,
            END_PORTAL_EYE_BIT
    );

    @Override
    @NotNull public BlockProperties getProperties() {
        return PROPERTIES;
    }

    public BlockEndPortalFrame() {
        this(PROPERTIES.getDefaultState());
    }

    public BlockEndPortalFrame(BlockState blockstate) {
        super(blockstate);
    }
    
    @Override
    public double getResistance() {
        return 3600000;
    }

    @Override
    public double getHardness() {
        return -1;
    }

    @Override
    public int getLightLevel() {
        return 1;
    }

    @Override
    public int getWaterloggingLevel() {
        return 1;
    }

    @Override
    public String getName() {
        return "End Portal Frame";
    }

    @Override
    public boolean isBreakable(@NotNull Vector3 vector, int layer, @Nullable BlockFace face, @Nullable Item item, @Nullable Player player) {
        return player != null && player.isCreative();
    }

    @Override
    public double getMaxY() {
        return this.y + (this.isEndPortalEye() ? 1 : 0.8125);
    }

    @Override
    public boolean canBePushed() {
        return false;
    }

    @Override
    public  boolean canBePulled() {
        return false;
    }

    @Override
    public boolean hasComparatorInputOverride() {
        return true;
    }

    @Override
    public int getComparatorInputOverride() {
        return this.isEndPortalEye() ? 15 : 0;
    }

    @Override
    public boolean canBeActivated() {
        return true;
    }

    @Override
    public boolean onActivate(@NotNull Item item, Player player, BlockFace blockFace, float fx, float fy, float fz) {
        if (this.isEndPortalEye() || player == null || !item.getId().equals(Item.ENDER_EYE)) return false;

        this.setEndPortalEye(true);
        this.getLevel().setBlock(this, this, true, true);
        this.getLevel().addSound(this, Sound.BLOCK_END_PORTAL_FRAME_FILL);

        for (int i = 0; i < 4; i++) {
            for (int j = -1; j <= 1; j++) {
                Block block = this.getSide(BlockFace.fromHorizontalIndex(i), 2)
                        .getSide(BlockFace.fromHorizontalIndex((i + 1) % 4), j);
                if (!this.isCompletedPortal(block)) continue;

                for (int k = -1; k <= 1; k++) {
                    for (int l = -1; l <= 1; l++) {
                        this.getLevel().setBlock(block.add(k, 0, l), Block.get(Block.END_PORTAL), true);
                    }
                }

                this.getLevel().addLevelSoundEvent(this, LevelSoundEventPacketV1.SOUND_BLOCK_END_PORTAL_SPAWN);

                return true;
            }
        }

        return true;
    }

    private boolean isCompletedPortal(@NotNull Block center) {
        for (int i = 0; i < 4; i++) {
            for (int j = -1; j <= 1; j++) {
                Block block = center.getSide(BlockFace.fromHorizontalIndex(i), 2)
                        .getSide(BlockFace.fromHorizontalIndex((i + 1) % 4), j);
                if (block instanceof BlockEndPortalFrame && ((BlockEndPortalFrame) block).isEndPortalEye()) continue;

                return false;
            }
        }

        return true;
    }

    @Override
    public boolean canHarvestWithHand() {
        return false;
    }

    @Override
    public Item toItem() {
        return new ItemBlock(this, 0);
    }

    @Override
    public BlockFace getBlockFace() {
        return CommonPropertyMap.CARDINAL_BLOCKFACE.get(this.getPropertyValue(CommonBlockProperties.MINECRAFT_CARDINAL_DIRECTION));
    }

    @Override
    public void setBlockFace(BlockFace face) {
        this.setPropertyValue(CommonBlockProperties.MINECRAFT_CARDINAL_DIRECTION, CommonPropertyMap.CARDINAL_BLOCKFACE.inverse().get(face));
    }

    @Override
    public boolean place(@NotNull Item item, @NotNull Block block, @NotNull Block target, @NotNull BlockFace face, double fx, double fy, double fz, @Nullable Player player) {
        if (player == null) {
            setBlockFace(BlockFace.SOUTH);
        } else {
            setBlockFace(player.getDirection().getOpposite());
        }

        this.getLevel().setBlock(block, this, true);
        return true;
    }

    public boolean isEndPortalEye() {
        return getPropertyValue(END_PORTAL_EYE_BIT);
    }

    public void setEndPortalEye(boolean endPortalEye) {
        setPropertyValue(END_PORTAL_EYE_BIT, endPortalEye);
    }
}
