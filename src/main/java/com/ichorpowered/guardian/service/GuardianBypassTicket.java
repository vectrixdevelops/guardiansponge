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
import com.ichorpowered.guardian.detection.AbstractDetection;
import com.ichorpowered.guardian.entry.GuardianPlayerEntry;
import com.ichorpowered.guardian.sequence.context.CommonContextKeys;
import com.ichorpowered.guardianapi.detection.check.Check;
import com.ichorpowered.guardianapi.detection.check.CheckModel;
import com.me4502.precogs.detection.DetectionType;
import com.me4502.precogs.service.BypassTicket;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Event;

import java.util.ArrayList;
import java.util.List;

public class GuardianBypassTicket implements BypassTicket {

    private final GuardianPlugin plugin;
    private final Player player;
    private final List<DetectionType> detectionTypes;
    private final Object owner;

    private List<Long> blockId = new ArrayList<>();
    private boolean closed = false;

    public GuardianBypassTicket(GuardianPlugin plugin, Player player, List<DetectionType> detectionTypes, Object owner) {
        this.plugin = plugin;
        this.player = player;
        this.detectionTypes = detectionTypes;
        this.owner = owner;

        this.detectionTypes.forEach(detectionType -> {
            if (detectionType instanceof AbstractDetection) {
                AbstractDetection detection = (AbstractDetection) detectionType;

                while (detection.getStageCycle().next()) {
                    if (detection.getStageCycle().getModel().isPresent() && CheckModel.class.isAssignableFrom(detection.getStageCycle().getModel().get().getClass())) {
                        if (!detection.getStageCycle().<Check<Event>>getStage().isPresent()) continue;
                        final Check<Event> check = detection.getStageCycle().<Check<Event>>getStage().get();
                        final GuardianPlayerEntry<Player> playerEntry = GuardianPlayerEntry.of(player, player.getUniqueId());

                        // Add new block.
                        this.blockId.add(
                                this.plugin.getSequenceManager().block(
                                        SequenceContext.builder()
                                                .owner(playerEntry.getUniqueId())
                                                .root(check.getSequenceTrigger())
                                                .custom(CommonContextKeys.ENTITY_ENTRY, playerEntry)
                                                .build()
                                ));
                    }
                }
            }
        });
    }

    @Override
    public Player getPlayer() {
        return this.player;
    }

    @Override
    public List<DetectionType> getDetectionTypes() {
        return this.detectionTypes;
    }

    @Override
    public boolean isClosed() {
        return this.closed;
    }

    @Override
    public void close() {
        this.closed = true;

        this.detectionTypes.forEach(detectionType -> {
            if (detectionType instanceof AbstractDetection) {
                AbstractDetection detection = (AbstractDetection) detectionType;

                while (detection.getStageCycle().next()) {
                    if (detection.getStageCycle().getModel().isPresent() && CheckModel.class.isAssignableFrom(detection.getStageCycle().getModel().get().getClass())) {
                        if (!detection.getStageCycle().<Check<Event>>getStage().isPresent()) continue;
                        final Check<Event> check = detection.getStageCycle().<Check<Event>>getStage().get();
                        final GuardianPlayerEntry<Player> playerEntry = GuardianPlayerEntry.of(this.player, this.player.getUniqueId());

                        for (long id : this.blockId) {
                            this.plugin.getSequenceManager().unblock(
                                    SequenceContext.builder()
                                            .id(id)
                                            .owner(playerEntry.getUniqueId())
                                            .root(check.getSequenceTrigger())
                                            .custom(CommonContextKeys.ENTITY_ENTRY, playerEntry)
                                            .build()
                            );
                        }
                    }
                }
            }
        });
    }

}
