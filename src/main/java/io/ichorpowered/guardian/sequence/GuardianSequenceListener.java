package io.ichorpowered.guardian.sequence;

import com.abilityapi.sequenceapi.SequenceContext;
import io.ichorpowered.guardian.GuardianPlugin;
import io.ichorpowered.guardian.entry.GuardianEntityEntry;
import io.ichorpowered.guardian.sequence.context.CommonContextKeys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.api.event.filter.Getter;
import org.spongepowered.api.event.filter.type.Exclude;
import org.spongepowered.api.plugin.PluginContainer;

public class GuardianSequenceListener {

    private final GuardianPlugin plugin;

    public GuardianSequenceListener(final GuardianPlugin plugin) {
        this.plugin = plugin;
    }

    @Listener
    @Exclude(MoveEntityEvent.Teleport.class)
    public void moveEntityEvent(MoveEntityEvent event, @Getter("getTargetEntity") Player player) {
        final GuardianEntityEntry<Player> playerEntry = GuardianEntityEntry.of(player, player.getUniqueId());

        // TODO: Block for teleportation.

        this.plugin.getSequenceManager().invokeObserverIf(event,

                // TODO: Add more sequence context here from Sponge Causes.
                SequenceContext.builder()
                        .id(playerEntry.getUniqueId())
                        .source(event)
                        .custom(CommonContextKeys.ENTITY_ENTRY, playerEntry)
                        .build(),

                // Don't execute movement sequences if a plugin occurs in the cause.
                sequence -> !event.getCause().containsType(PluginContainer.class));
    }

}
