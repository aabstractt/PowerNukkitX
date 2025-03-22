package cn.nukkit.item;

import cn.nukkit.Server;

/**
 * @author MagicDroidX (Nukkit Project)
 */
public class ItemSnowball extends ProjectileItem {

    public ItemSnowball() {
        this(0, 1);
    }

    public ItemSnowball(Integer meta) {
        this(meta, 1);
    }

    public ItemSnowball(Integer meta, int count) {
        super(SNOWBALL, 0, count, "Snowball");
    }

    @Override
    public int getMaxStackSize() {
        return 16;
    }

    @Override
    public String getProjectileEntityType() {
        return SNOWBALL;
    }

    @Override
    public float getThrowForce() {
        return Server.getInstance().getSettings().gameplaySettings().snowBall().throwForce();
    }
}
