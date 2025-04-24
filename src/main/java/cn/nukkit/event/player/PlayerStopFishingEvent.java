package cn.nukkit.event.player;

import cn.nukkit.entity.item.EntityFishingHook;
import cn.nukkit.event.HandlerList;
import lombok.Getter;
import lombok.NonNull;
import org.jetbrains.annotations.NotNull;

public final class PlayerStopFishingEvent extends PlayerEvent {

    @Getter private static final HandlerList handlers = new HandlerList();

    private final @NonNull EntityFishingHook fishingHook;

    public PlayerStopFishingEvent(@NonNull cn.nukkit.Player player, @NotNull EntityFishingHook hook) {
        this.player = player;
        this.fishingHook = hook;
    }

    public @NonNull EntityFishingHook getFishingHook() {
        return this.fishingHook;
    }
}