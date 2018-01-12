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
package com.ichorpowered.guardian.service;

import com.abilityapi.sequenceapi.SequenceContext;
import com.ichorpowered.guardian.GuardianPlugin;
import com.ichorpowered.guardian.entry.GuardianPlayerEntry;
import com.ichorpowered.guardian.sequence.context.CommonContextKeys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Event;

import java.util.ArrayList;
import java.util.List;

public class InternalBypassTicket {

    private final GuardianPlugin plugin;
    private final Player player;
    private final Class<? extends Event> eventClass;
    private final Object owner;

    private List<Long> blockId = new ArrayList<>();
    private boolean closed = false;

    public InternalBypassTicket(GuardianPlugin plugin, Player player, Class<? extends Event> eventClass, Object owner) {
        this.plugin = plugin;
        this.player = player;
        this.eventClass = eventClass;
        this.owner = owner;

        final GuardianPlayerEntry<Player> playerEntry = GuardianPlayerEntry.of(player, player.getUniqueId());

        // Add new block.
        this.blockId.add(
                this.plugin.getSequenceManager().block(
                        SequenceContext.builder()
                                .owner(playerEntry.getUniqueId())
                                .root(eventClass)
                                .custom(CommonContextKeys.ENTITY_ENTRY, playerEntry)
                                .build()
                ));
    }

    public Player getPlayer() {
        return this.player;
    }

    public Class<? extends Event> getEventClass() {
        return this.eventClass;
    }

    public boolean isClosed() {
        return this.closed;
    }

    public void close() {
        this.closed = true;

        final GuardianPlayerEntry<Player> playerEntry = GuardianPlayerEntry.of(this.player, this.player.getUniqueId());

        for (Long index : this.blockId) {
            this.plugin.getSequenceManager().unblock(
                    SequenceContext.builder()
                            .id(index)
                            .owner(playerEntry.getUniqueId())
                            .root(this.eventClass)
                            .custom(CommonContextKeys.ENTITY_ENTRY, playerEntry)
                            .build()
            );
        }
    }
}