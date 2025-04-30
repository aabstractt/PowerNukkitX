package cn.nukkit.entity.ai.executor.enderdragon;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.EntityIntelligent;
import cn.nukkit.entity.ai.executor.EntityControl;
import cn.nukkit.entity.ai.executor.IBehaviorExecutor;
import cn.nukkit.entity.ai.memory.CoreMemoryTypes;
import cn.nukkit.entity.effect.Effect;
import cn.nukkit.entity.effect.PotionType;
import cn.nukkit.entity.item.EntityAreaEffectCloud;
import cn.nukkit.level.Location;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.nbt.tag.DoubleTag;
import cn.nukkit.nbt.tag.FloatTag;
import cn.nukkit.nbt.tag.ListTag;

import java.util.List;


public class PerchingExecutor implements EntityControl, IBehaviorExecutor {

    private int stayTick = -1;

    public PerchingExecutor() {}
    @Override
    public boolean execute(EntityIntelligent entity) {
        if (this.stayTick >= 0) this.stayTick++;

        Vector3 nearby = entity.getMemoryStorage().get(CoreMemoryTypes.STAY_NEARBY);
        if (nearby == null) {
            throw new NullPointerException("stayNearby is null");
        }

        Vector3 target = new Vector3(0, entity.getLevel().getHighestBlockAt(nearby.getFloorX(), nearby.getFloorZ()) + 1, 0);
        if (entity.distance(target) <= 10) {
            if (this.stayTick == -1) this.stayTick = 0;
            if (this.stayTick == 25) {
                entity.getViewers().values().stream()
                        .filter(player -> player.distance(target) <= 10)
                        .findAny()
                        .ifPresent(player -> {
                            removeRouteTarget(entity);
                            setLookTarget(entity, player);
                            Vector3 toPlayerVector = new Vector3(player.x - entity.x, player.y - entity.y, player.z - entity.z).normalize();
                            Location location = entity.getLocation().add(toPlayerVector.multiply(10));
                            location.y = location.level.getHighestBlockAt(location.toHorizontal()) + 1;
                            EntityAreaEffectCloud areaEffectCloud = (EntityAreaEffectCloud) Entity.createEntity(Entity.AREA_EFFECT_CLOUD, location.getChunk(),
                                    new CompoundTag().putList("Pos", new ListTag<>()
                                                    .add(new DoubleTag(location.x))
                                                    .add(new DoubleTag(location.y))
                                                    .add(new DoubleTag(location.z))
                                            )
                                            .putList("Rotation", new ListTag<>()
                                                    .add(new FloatTag(0))
                                                    .add(new FloatTag(0))
                                            )
                                            .putList("Motion", new ListTag<>()
                                                    .add(new DoubleTag(0))
                                                    .add(new DoubleTag(0))
                                                    .add(new DoubleTag(0))
                                            )
                                            .putInt("Duration", 20)
                                            .putFloat("InitialRadius", 6)
                                            .putFloat("Radius", 2)
                                            .putFloat("Height", 1)
                                            .putFloat("RadiusChangeOnPickup", 0)
                                            .putFloat("RadiusPerTick", 0)
                            );

                            List<Effect> effects = PotionType.get(PotionType.HARMING.id()).getEffects(false);
                            for (Effect effect : effects) {
                                if (effect != null && areaEffectCloud != null) {
                                    areaEffectCloud.cloudEffects.add(effect.setVisible(false).setAmbient(false));
                                    areaEffectCloud.spawnToAll();
                                }
                            }
                            areaEffectCloud.spawnToAll();
                        });
            }
        } else {
            setRouteTarget(entity, target);
            setLookTarget(entity, target);
        }

        if (this.stayTick > 100) return false;

        if (this.stayTick >= 0) {
            entity.teleport(target);
        }

        return true;
    }



    @Override
    public void onStart(EntityIntelligent entity) {
        Player player = entity.getMemoryStorage().get(CoreMemoryTypes.NEAREST_PLAYER);
        if(player == null) return;
        setLookTarget(entity, player);
        setRouteTarget(entity, player);
        stayTick = -1;
    }

    @Override
    public void onStop(EntityIntelligent entity) {
        entity.getMemoryStorage().put(CoreMemoryTypes.FORCE_PERCHING, false);
        entity.setEnablePitch(false);
    }

    @Override
    public void onInterrupt(EntityIntelligent entity) {
        onStop(entity);
    }

}
