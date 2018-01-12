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
package com.ichorpowered.guardian.sequence;

import com.abilityapi.sequenceapi.SequenceContext;
import com.ichorpowered.guardian.GuardianPlugin;
import com.ichorpowered.guardian.entry.GuardianPlayerEntry;
import com.ichorpowered.guardian.sequence.context.CommonContextKeys;
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

    // Triggers

    @Listener
    @Exclude(MoveEntityEvent.Teleport.class)
    public void moveEntityEvent(MoveEntityEvent event, @Getter("getTargetEntity") Player player) {
        final GuardianPlayerEntry<Player> playerEntry = GuardianPlayerEntry.of(player, player.getUniqueId());

        this.plugin.getSequenceManager().invokeObserverIf(event,

                // TODO: Add more sequence context here from Sponge Causes.
                SequenceContext.builder()
                        .id(playerEntry.getUniqueId())
                        .source(event)
                        .custom(CommonContextKeys.ENTITY_ENTRY, playerEntry)
                        .build(),

                sequence -> !event.getCause().root().getClass().equals(PluginContainer.class)
        );
    }

    // Blocking

    @Listener
    public void teleportEntityEvent(MoveEntityEvent.Teleport event, @Getter("getTargetEntity") Player player) {
        final GuardianPlayerEntry<Player> playerEntry = GuardianPlayerEntry.of(player, player.getUniqueId());

        this.plugin.getInternalBypassService().requestTimeBypassTicket(player, MoveEntityEvent.class, this.plugin, 55);
    }
}
