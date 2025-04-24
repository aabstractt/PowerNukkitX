package cn.nukkit.entity.projectile;

import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.block.BlockFenceGate;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.EntityLiving;
import cn.nukkit.entity.item.EntityBoat;
import cn.nukkit.entity.item.EntityEnderCrystal;
import cn.nukkit.entity.item.EntityMinecartAbstract;
import cn.nukkit.event.entity.EntityCombustByEntityEvent;
import cn.nukkit.event.entity.EntityDamageByChildEntityEvent;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.event.entity.EntityDamageEvent.DamageCause;
import cn.nukkit.event.entity.ProjectileHitEvent;
import cn.nukkit.level.MovingObjectPosition;
import cn.nukkit.level.Position;
import cn.nukkit.level.format.IChunk;
import cn.nukkit.level.vibration.VibrationEvent;
import cn.nukkit.level.vibration.VibrationType;
import cn.nukkit.math.BlockFace;
import cn.nukkit.math.NukkitMath;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.tag.CompoundTag;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author MagicDroidX (Nukkit Project)
 */
public abstract class EntityProjectile extends Entity {
    public static final int PICKUP_NONE = 0;
    public static final int PICKUP_ANY = 1;
    public static final int PICKUP_CREATIVE = 2;

    public Entity shootingEntity;
    public boolean hadCollision;
    public boolean closeOnCollide;
    /**
     * It's inverted from {@link #getHasAge()} because of the poor architecture chosen by the original devs
     * on the entity construction and initialization. It's impossible to set it to true before
     * the initialization of the child classes.
     */
    private boolean noAge;

    private @Nullable BlockFace shootingDirection;

    private @Nullable Vector3 initialPosition;

    protected @Nullable Vector3 finalPosition;

    public EntityProjectile(IChunk chunk, CompoundTag nbt) {
        this(chunk, nbt, null);
    }

    public EntityProjectile(IChunk chunk, CompoundTag nbt, Entity shootingEntity) {
        super(chunk, nbt);
        this.shootingEntity = shootingEntity;
        if (shootingEntity != null) {
            this.setDataProperty(OWNER_EID, shootingEntity.getId());

            this.shootingDirection = shootingEntity.getDirection();
        }
    }

    protected double getDamage() {
        return namedTag.contains("damage") ? namedTag.getDouble("damage") : getBaseDamage();
    }

    protected double getBaseDamage() {
        return 0;
    }

    public int getResultDamage(@Nullable Entity entity) {
        return getResultDamage();
    }

    public int getResultDamage() {
        return NukkitMath.ceilDouble(Math.sqrt(this.motionX * this.motionX + this.motionY * this.motionY + this.motionZ * this.motionZ) * getDamage());
    }

    @Override
    public boolean attack(EntityDamageEvent source) {
        return source.getCause() == DamageCause.VOID && super.attack(source);
    }

    public void onCollideWithEntity(Entity entity) {
        ProjectileHitEvent projectileHitEvent = new ProjectileHitEvent(this, MovingObjectPosition.fromEntity(entity));
        this.server.getPluginManager().callEvent(projectileHitEvent);

        if (projectileHitEvent.isCancelled()) return;

        this.level.getVibrationManager().callVibrationEvent(new VibrationEvent(this, this.getVector3(), VibrationType.PROJECTILE_LAND));

        float damage = this.getResultDamage(entity);

        EntityDamageEvent ev;
        if (this.shootingEntity == null) {
            ev = new EntityDamageByEntityEvent(this, entity, DamageCause.PROJECTILE, damage);
        } else {
            ev = new EntityDamageByChildEntityEvent(this.shootingEntity, this, entity, DamageCause.PROJECTILE, damage);
        }
        if (entity.attack(ev)) {
            addHitEffect();
            this.hadCollision = true;

            if (this.fireTicks > 0) {
                EntityCombustByEntityEvent event = new EntityCombustByEntityEvent(this, entity, 5);
                this.server.getPluginManager().callEvent(event);
                if (!event.isCancelled()) {
                    entity.setOnFire(event.getDuration());
                }
            }
        }
        afterCollisionWithEntity(entity);
        if (closeOnCollide) {
            this.close();
        }
    }

    protected void afterCollisionWithEntity(Entity entity) {

    }

    @Override
    protected void initEntity() {
        this.closeOnCollide = true;
        super.initEntity();

        this.setMaxHealth(1);
        this.setHealth(1);
        if (this.namedTag.contains("Age") && !this.noAge) {
            this.age = this.namedTag.getShort("Age");
        }

        this.initialPosition = this.getPosition();
    }

    @Override
    public boolean canCollideWith(Entity entity) {
        return (entity instanceof EntityLiving || entity instanceof EntityEnderCrystal || entity instanceof EntityMinecartAbstract || entity instanceof EntityBoat) && !this.onGround;
    }

    @Override
    public void saveNBT() {
        super.saveNBT();
        if (!this.noAge) {
            this.namedTag.putShort("Age", this.age);
        }
    }

    protected void updateMotion() {
        this.motionY -= this.getGravity();
        this.motionX *= 1 - this.getDrag();
        this.motionZ *= 1 - this.getDrag();
    }

    /**
     * Filters the entities that collide with projectile
     *
     * @param entity the collide entity
     * @return the boolean
     */
    protected boolean collideEntityFilter(Entity entity) {
        if ((entity == this.shootingEntity && this.ticksLived < 5) ||
                (entity instanceof Player player && player.getGamemode() == Player.SPECTATOR)
                || (this.shootingEntity instanceof Player && Optional.ofNullable(this.shootingEntity.riding).map(e -> e.equals(entity)).orElse(false))) {
            return false;
        } else return true;
    }

    @Override
    public boolean onUpdate(int currentTick) {
        if (this.closed) return false;

        int tickDiff = currentTick - this.lastUpdate;
        if (tickDiff <= 0 && !this.justCreated) return true;

        this.lastUpdate = currentTick;

        boolean hasUpdate = this.entityBaseTick(tickDiff);
        if (!this.isAlive()) return hasUpdate;

        MovingObjectPosition movingObjectPosition = null;

        if (!this.isCollided) {
            updateMotion();
        }

        Vector3 destination = new Vector3(this.x + this.motionX, this.y + this.motionY, this.z + this.motionZ);

        Entity[] list = this.getLevel().getCollidingEntities(this.getBoundingBox().addCoord(this.motionX, this.motionY, this.motionZ).expand(1, 1, 1), this);

        double nearDistance = Integer.MAX_VALUE;
        Entity nearEntity = null;
        for (Entity entity : list) {
            if (!collideEntityFilter(entity)) continue;

            MovingObjectPosition ob = entity.getBoundingBox().grow(0.3, 0.3, 0.3).calculateIntercept(this, destination);
            if (ob == null) continue;

            double distance = this.distanceSquared(ob.hitVector);
            if (distance < nearDistance) {
                nearDistance = distance;
                nearEntity = entity;
            }
        }

        if (nearEntity != null) {
            movingObjectPosition = MovingObjectPosition.fromEntity(nearEntity);
        }

        if (movingObjectPosition != null && movingObjectPosition.entityHit != null) {
            onCollideWithEntity(movingObjectPosition.entityHit);

            hasUpdate = true;
            if (closed) return true;
        }

        Position position = getPosition();
        Vector3 motion = getMotion();
        this.move(this.motionX, this.motionY, this.motionZ);

        if (this.isCollided && !this.hadCollision && this.initialPosition != null) {
            Block block = this.level.getBlock(destination);

            if (this.canPassThrough(block, destination, position)) {
                this.isCollided = this.isCollidedVertically = this.isCollidedHorizontally = this.onGround = false;

                this.updateMovement();

                return hasUpdate;
            }

            //collide with block
            this.hadCollision = true;

            this.motionX = 0;
            this.motionY = 0;
            this.motionZ = 0;

            /*System.out.println("DEBUG: Projectile collided with block");
            System.out.println("Initial position: " + this.initialPosition);
            System.out.println("Destination: " + destination);
            System.out.println("Position: " + position);*/

            Vector3 finalDestination = this.getVector3();
            if (this.level.getBlock(position).isAir()) {
                finalDestination = position;
            } else if (this.level.getBlock(destination).isAir()) {
                finalDestination = destination;
            }

            if (finalDestination.y > this.initialPosition.y) {
                for (int i = 0; i < 2; i++) {
                    if (this.level.getBlock(finalDestination.add(0, i)).isAir()) continue;

                    finalDestination = null;

                    break;
                }
            }

            if (finalDestination != null) this.finalPosition = Position.fromObject(finalDestination, this.level);

            this.server.getPluginManager().callEvent(new ProjectileHitEvent(this, MovingObjectPosition.fromBlock(this.getFloorX(), this.getFloorY(), this.getFloorZ(), BlockFace.UP, this)));
            onCollideWithBlock(position, motion);
            addHitEffect();
            return false;
        } else if (!this.isCollided && this.hadCollision) {
            this.hadCollision = false;
        }

        if (!this.hadCollision || Math.abs(this.motionX) > 0.00001 || Math.abs(this.motionY) > 0.00001 || Math.abs(this.motionZ) > 0.00001) {
            updateRotation();
            hasUpdate = true;
        }

        this.updateMovement();

        return hasUpdate;
    }

    public void updateRotation() {
        double f = Math.sqrt((this.motionX * this.motionX) + (this.motionZ * this.motionZ));
        this.yaw = Math.atan2(this.motionX, this.motionZ) * 180 / Math.PI;
        this.pitch = Math.atan2(this.motionY, f) * 180 / Math.PI;
    }

    public void inaccurate(float modifier) {
        Random rand = ThreadLocalRandom.current();

        this.motionX += rand.nextGaussian() * 0.007499999832361937 * modifier;
        this.motionY += rand.nextGaussian() * 0.007499999832361937 * modifier;
        this.motionZ += rand.nextGaussian() * 0.007499999832361937 * modifier;
    }

    protected void onCollideWithBlock(Position position, Vector3 motion) {
        this.level.getVibrationManager().callVibrationEvent(new VibrationEvent(this, this.getVector3(), VibrationType.PROJECTILE_LAND));
        for (Block collisionBlock : level.getCollisionBlocks(getBoundingBox().grow(0.1, 0.1, 0.1))) {
            onCollideWithBlock(position, motion, collisionBlock);
        }
    }

    protected boolean onCollideWithBlock(Position position, Vector3 motion, Block collisionBlock) {
        return collisionBlock.onProjectileHit(this, position, motion);
    }

    protected void addHitEffect() {

    }

    public boolean getHasAge() {
        return !this.noAge;
    }

    public void setHasAge(boolean hasAge) {
        this.noAge = !hasAge;
    }

    @Override
    public void spawnToAll() {
        super.spawnToAll();
        //vibration: minecraft:projectile_shoot
        this.level.getVibrationManager().callVibrationEvent(new VibrationEvent(this.shootingEntity, this.getVector3(), VibrationType.PROJECTILE_SHOOT));
    }

    protected boolean canPassThrough(@NotNull Block block, @NotNull Vector3 destination, @Nullable Vector3 fallback) {
        if (block instanceof BlockFenceGate && ((BlockFenceGate) block).isOpen()) return true;

        /*System.out.println("DEBUG: Hit block: " + block.getName());
        System.out.println("DEBUG: Hit position: " + movingObjectPosition.hitVector);
        System.out.println("DEBUG: Distance Squared: " + destination.distanceSquared(movingObjectPosition.hitVector));*/

        return false;

        // TODO: Just handle this if the block of fallback is slab or stairs
        /*if (fallback == null) return false;
        if (fallbackBlock instanceof BlockSlab || fallbackBlock instanceof BlockStairs) return this.canPassThrough(block, fallback, null);*/
    }
}
