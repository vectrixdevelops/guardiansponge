/*
 * MIT License
 *
 * Copyright (c) 2017 Connor Hartley
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
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
