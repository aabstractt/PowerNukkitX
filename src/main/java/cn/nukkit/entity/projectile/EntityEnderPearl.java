package cn.nukkit.entity.projectile;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.block.Block;
import cn.nukkit.entity.Entity;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.event.entity.ProjectileHitEvent;
import cn.nukkit.event.player.PlayerTeleportEvent.TeleportCause;
import cn.nukkit.level.MovingObjectPosition;
import cn.nukkit.level.format.IChunk;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.network.protocol.LevelEventPacket;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EntityEnderPearl extends EntityProjectile {

    @Override
    @NotNull public String getIdentifier() {
        return ENDER_PEARL;
    }

    public EntityEnderPearl(IChunk chunk, CompoundTag nbt) {
        this(chunk, nbt, null);
    }

    public EntityEnderPearl(IChunk chunk, CompoundTag nbt, Entity shootingEntity) {
        super(chunk, nbt, shootingEntity);
    }

    

    @Override
    public float getWidth() {
        return 0.25f;
    }

    @Override
    public float getLength() {
        return 0.25f;
    }

    @Override
    public float getHeight() {
        return 0.25f;
    }

    @Override
    protected float getGravity() {
        return Server.getInstance().getSettings().gameplaySettings().enderPearl().gravity();
    }

    @Override
    protected float getDrag() {
        return Server.getInstance().getSettings().gameplaySettings().enderPearl().drag();
    }

    @Override
    public boolean onUpdate(int currentTick) {
        if (this.closed) {
            return false;
        }
        boolean hasUpdate = super.onUpdate(currentTick);

        if (this.isCollided && this.shootingEntity instanceof Player) {
            boolean portal = false;
            for (Block collided : this.getCollisionBlocks()) {
                if (collided.getId() == Block.PORTAL) {
                    portal = true;
                }
            }
            if (!portal) {
                doTeleport(null);
            }
        }

        if (this.age > 1200 || this.isCollided) {
            this.kill();
            hasUpdate = true;
        }

        return hasUpdate;
    }

    @Override
    public void onCollideWithEntity(Entity entity) {
        if (this.shootingEntity instanceof Player) {
            doTeleport(entity);
        }

        super.onCollideWithEntity(entity);
    }

    private void doTeleport(@Nullable Vector3 destination) {
        if (!this.level.equals(this.shootingEntity.getLevel())) return;

        if (destination == null && this.finalPosition != null) {
            destination = this.finalPosition.subtract(0, 0.5);
        }

        if (destination == null) return;

        if (!this.level.getBlock(destination).isAir()) destination = destination.add(0, 1);

        ProjectileHitEvent projectileHitEvent = new ProjectileHitEvent(
                this,
                MovingObjectPosition.fromBlock(
                        destination.getFloorX(),
                        destination.getFloorY(),
                        destination.getFloorZ(),
                        this.getHorizontalFacing(),
                        destination
                )
        );
        this.server.getPluginManager().callEvent(projectileHitEvent);
        if (projectileHitEvent.isCancelled()) return;

        this.level.addLevelEvent(this.shootingEntity.add(0.5, 0.5, 0.5), LevelEventPacket.EVENT_SOUND_TELEPORT_ENDERPEARL);
        this.shootingEntity.teleport(destination, TeleportCause.ENDER_PEARL);

        if ((((Player) this.shootingEntity).getGamemode() & 0x01) == 0) {
            this.shootingEntity.attack(new EntityDamageByEntityEvent(this, shootingEntity, EntityDamageEvent.DamageCause.PROJECTILE, 5f, 0f));
        }

        this.level.addLevelEvent(this, LevelEventPacket.EVENT_PARTICLE_TELEPORT);
        this.level.addLevelEvent(this.shootingEntity.add(0.5, 0.5, 0.5), LevelEventPacket.EVENT_SOUND_TELEPORT_ENDERPEARL);
    }

    @Override
    public String getOriginalName() {
        return "Ender Pearl";
    }
}
