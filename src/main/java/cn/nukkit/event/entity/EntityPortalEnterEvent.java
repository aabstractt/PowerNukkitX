package cn.nukkit.event.entity;

import cn.nukkit.entity.Entity;
import cn.nukkit.event.Cancellable;
import cn.nukkit.event.HandlerList;
import cn.nukkit.level.Position;
import lombok.NonNull;
import org.jetbrains.annotations.NotNull;

public class EntityPortalEnterEvent extends EntityEvent implements Cancellable {
    private static final HandlerList handlers = new HandlerList();

    public static HandlerList getHandlers() {
        return handlers;
    }

    private final PortalType type;

    private @NonNull Position to;
    /**
     * If can build a new portal if the portal is not found
     */
    private boolean canBuildPortal = true;

    public EntityPortalEnterEvent(Entity entity, PortalType type, @NonNull Position to) {
        this.entity = entity;
        this.type = type;

        this.to = to;
    }

    /**
     * @return The position to teleport to
     */
    public @NotNull Position getTo() {
        return this.to;
    }

    /**
     * Set the position to teleport to
     * @param to The position to teleport to
     */
    public void setTo(@NonNull Position to) {
        this.to = to;
    }

    /**
     * @return If can build a new portal if the portal is not found
     */
    public boolean canBuildPortal() {
        return this.canBuildPortal;
    }

    /**
     * Set if can build a new portal if the portal is not found
     * @param canBuildPortal If can build a new portal if the portal is not found
     */
    public void setCanBuildPortal(boolean canBuildPortal) {
        this.canBuildPortal = canBuildPortal;
    }

    public PortalType getPortalType() {
        return type;
    }

    public enum PortalType {
        NETHER,
        END
    }
}
