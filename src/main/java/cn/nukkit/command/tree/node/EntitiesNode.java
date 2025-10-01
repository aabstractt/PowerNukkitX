package cn.nukkit.command.tree.node;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.command.exceptions.SelectorSyntaxException;
import cn.nukkit.command.selector.EntitySelectorAPI;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.EntityHuman;
import com.google.common.collect.Lists;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Parsed as {@code List<Entity>} value <p>
 * All command parameter types are {@link cn.nukkit.command.data.CommandParamType#TARGET TARGET} If not manually specified {@link IParamNode}, This analysis will be used by default
 */
public class EntitiesNode extends TargetNode<Entity> {

    // TODO: Support UUID or xuid
    @Override
    public void fill(String arg) {
        List<Entity> entities;
        if (arg.isBlank()) {
            this.error();
        } else if (EntitySelectorAPI.getAPI().checkValid(arg)) {
            try {
                entities = EntitySelectorAPI.getAPI().matchEntities(paramList.getParamTree().getSender(), arg);
            } catch (SelectorSyntaxException exception) {
                error(exception.getMessage());
                return;
            }
            this.value = entities.stream()
                    .filter(entity -> !(entity instanceof EntityHuman))
                    .collect(Collectors.toList());
        } else {
            entities = Lists.newArrayList();
            Player player = Server.getInstance().getPlayer(arg);
            if (player != null) entities.add(player);
            
            this.value = entities;
        }
    }
}
