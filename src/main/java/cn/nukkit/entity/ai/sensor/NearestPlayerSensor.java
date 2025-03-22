package cn.nukkit.entity.ai.sensor;

import cn.nukkit.Player;
import cn.nukkit.entity.EntityCanAttack;
import cn.nukkit.entity.EntityIntelligent;
import cn.nukkit.entity.ai.memory.CoreMemoryTypes;
import lombok.Getter;

//存储最近的玩家的Memory


@Getter
public class NearestPlayerSensor implements ISensor {

    protected double range;

    protected double minRange;

    protected int period;

    public NearestPlayerSensor(double range, double minRange) {
        this(range, minRange, 1);
    }

    public NearestPlayerSensor(double range, double minRange, int period) {
        this.range = range;
        this.minRange = minRange;
        this.period = period;
    }

    @Override
    public void sense(EntityIntelligent entity) {
        Player betterPlayer = null;
        double rangeSquared = this.range * this.range;
        double minRangeSquared = this.minRange * this.minRange;
        //寻找范围内最近的玩家
        for (Player p : entity.getLevel().getPlayers().values()) {
            if (!(entity.distanceSquared(p) <= rangeSquared) || !(entity.distanceSquared(p) >= minRangeSquared)) continue;
            if (entity instanceof EntityCanAttack && !((EntityCanAttack) entity).attackTarget(p)) continue;

            if (betterPlayer == null) {
                betterPlayer = p;
            } else if (entity.distanceSquared(p) < entity.distanceSquared(betterPlayer)) {
                betterPlayer = p;
            }
        }

        entity.getMemoryStorage().put(CoreMemoryTypes.NEAREST_PLAYER, betterPlayer);
    }

    @Override
    public int getPeriod() {
        return period;
    }
}
