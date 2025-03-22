package cn.nukkit.entity.item;

import cn.nukkit.Server;
import cn.nukkit.config.ServerSettings;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.effect.Effect;
import cn.nukkit.entity.effect.PotionType;
import cn.nukkit.entity.mob.EntityBlaze;
import cn.nukkit.entity.projectile.EntityProjectile;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.event.potion.PotionCollideEvent;
import cn.nukkit.level.Sound;
import cn.nukkit.level.format.IChunk;
import cn.nukkit.level.particle.Particle;
import cn.nukkit.level.particle.SpellParticle;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.utils.BlockColor;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

/**
 * @author xtypr
 */
public class EntitySplashPotion extends EntityProjectile {

    @Override
    @NotNull public String getIdentifier() {
        return SPLASH_POTION;
    }

    public static final int DATA_POTION_ID = 37;

    public int potionId;

    public EntitySplashPotion(IChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
    }

    public EntitySplashPotion(IChunk chunk, CompoundTag nbt, Entity shootingEntity) {
        super(chunk, nbt, shootingEntity);
    }

    @Override
    protected void initEntity() {
        super.initEntity();

        potionId = this.namedTag.getShort("PotionId");

        this.entityDataMap.put(AUX_VALUE_DATA, this.potionId);
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
        return Server.getInstance().getSettings().gameplaySettings().splashPotion().gravity();
    }

    @Override
    protected float getDrag() {
        return Server.getInstance().getSettings().gameplaySettings().splashPotion().drag();
    }

    @Override
    public void onCollideWithEntity(Entity entity) {
        this.splash(entity);
    }

    protected void splash(Entity collidedWith) {
        PotionCollideEvent event = new PotionCollideEvent(PotionType.get(this.potionId), this);
        this.server.getPluginManager().callEvent(event);

        if (event.isCancelled()) return;

        this.close();

        PotionType potion = event.getPotion();
        if (potion == null) return;

        if (potion.equals(PotionType.WATER) && collidedWith instanceof EntityBlaze) {
            collidedWith.attack(new EntityDamageByEntityEvent(this, collidedWith, EntityDamageEvent.DamageCause.MAGIC, 1));
        }

        int[] color = new int[3];
        int count = 0;

        if(!potion.getEffects(true).isEmpty()) {
            for (Effect effect : potion.getEffects(true)) {
                Color effectColor = effect.getColor();
                color[0] += effectColor.getRed() * effect.getLevel();
                color[1] += effectColor.getGreen() * effect.getLevel();
                color[2] += effectColor.getBlue() * effect.getLevel();
                count += effect.getLevel();
            }
        } else {
            BlockColor water = BlockColor.WATER_BLOCK_COLOR;
            color[0] = water.getRed();
            color[1] = water.getGreen();
            color[2] = water.getBlue();
            count = 1;
        }

        int r = (color[0] / count) & 0xff;
        int g = (color[1] / count) & 0xff;
        int b = (color[2] / count) & 0xff;
        Particle particle = new SpellParticle(this, r, g, b);

        this.getLevel().addParticle(particle);
        this.getLevel().addSound(this, Sound.RANDOM_GLASS);

        ServerSettings.SplashPotionSettings splashPotionSettings = Server.getInstance().getSettings().gameplaySettings().splashPotion();

        //Entity[] entities = this.getLevel().getNearbyEntities(this.getBoundingBox().grow(4.125, 2.125, 4.125));
        Entity[] entities = this.getLevel().getNearbyEntities(this.getBoundingBox().grow(
                splashPotionSettings.growX(),
                splashPotionSettings.growY(),
                splashPotionSettings.growX()
        ));
        for (Entity anEntity : entities) {
            double distance = anEntity.distanceSquared(this);
            if (distance > splashPotionSettings.distance()) continue;

            potion.applyEffects(
                    anEntity,
                    true,
                    anEntity.equals(collidedWith) ? splashPotionSettings.equalsPotency() : 1 - Math.sqrt(distance) / splashPotionSettings.divisor()
            );
        }
    }

    @Override
    public boolean onUpdate(int currentTick) {
        if (this.closed) return false;

        boolean hasUpdate = super.onUpdate(currentTick);

        if (this.age > 1200) {
            this.kill();
            hasUpdate = true;
        } else if (this.isCollided) {
            this.splash(null);
            hasUpdate = true;
        }
        return hasUpdate;
    }

    @Override
    public String getOriginalName() {
        return "Potion";
    }
}
