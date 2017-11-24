package io.ichorpowered.guardian.sequence;

import com.abilityapi.sequenceapi.context.SequenceContext;
import io.ichorpowered.guardian.GuardianPlugin;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.api.event.filter.Getter;
import org.spongepowered.api.plugin.PluginContainer;

public class GuardianSequenceListener {

    private final GuardianPlugin plugin;

    public GuardianSequenceListener(final GuardianPlugin plugin) {
        this.plugin = plugin;
    }

    @Listener
    public void moveEntityEvent(MoveEntityEvent event, @Getter("getTargetEntity") Player player) {
        this.plugin.getSequenceManager().invokeObserverIf(event,

                // TODO: Add more sequence context here from Sponge Causes.
                SequenceContext.builder()
                        .id(player.getUniqueId())
                        .build(),

                // Don't execute movement sequences if a plugin occurs in the cause.
                sequence -> event.getCause().containsType(PluginContainer.class));
    }

}
