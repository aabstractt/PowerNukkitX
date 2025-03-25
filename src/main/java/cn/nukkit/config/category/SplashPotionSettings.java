package cn.nukkit.config.category;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.Comment;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(fluent = true)
public final class SplashPotionSettings extends OkaeriConfig {
    @Comment("nukkit.server.settings.splash-potion.grow-x")
    double growX = 4.125;
    @Comment("nukkit.server.settings.splash-potion.grow-y")
    double growY = 2.125;
    @Comment("nukkit.server.settings.splash-potion.grow-z")
    double growZ = 4.125;
    @Comment("nukkit.server.settings.splash-potion.equals-potency")
    double equalsPotency = 1.0;
    @Comment("nukkit.server.settings.splash-potion.distance")
    double distance = 16;
    @Comment("nukkit.server.settings.splash-potion.divisor")
    int divisor = 4;
    @Comment("nukkit.server.settings.splash-potion.gravity")
    float gravity = 0.04f;
    @Comment("nukkit.server.settings.splash-potion.drag")
    float drag = 0.02f;
    @Comment("nukkit.server.settings.splash-potion.throw-force")
    float throwForce = 0.5f;
}