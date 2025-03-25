package cn.nukkit.config.category;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.Comment;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(fluent = true)
public final class SnowballSettings extends OkaeriConfig {
    @Comment("nukkit.server.settings.snow-ball.gravity")
    float gravity = 0.04f;
    @Comment("nukkit.server.settings.snow-ball.drag")
    float drag = 0.015f;
    @Comment("nukkit.server.settings.snow-ball.throw-force")
    float throwForce = 2.0f;
}