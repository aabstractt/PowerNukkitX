package cn.nukkit.entity.ai.evaluator;

import cn.nukkit.Player;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.EntityIntelligent;
import cn.nukkit.entity.ai.memory.MemoryType;

public class EntityCheckEvaluator implements IBehaviorEvaluator {

    private MemoryType<? extends Entity> memoryType;


    public EntityCheckEvaluator(MemoryType<? extends Entity> type) {
        this.memoryType = type;
    }
    @Override
    public boolean evaluate(EntityIntelligent entity) {
        Entity e = entity.getMemoryStorage().get(memoryType);
        if (e == null) return false;

        if (e instanceof Player player)
            return (player.spawned && player.isOnline() && (player.isSurvival() || player.isAdventure()) && player.isAlive());

        return !e.isClosed();
    }
}
