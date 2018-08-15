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
package com.ichorpowered.guardian.sponge.service;

import com.ichorpowered.guardian.api.detection.Detection;
import com.ichorpowered.guardian.api.detection.DetectionController;
import com.ichorpowered.guardian.api.detection.stage.process.Check;
import com.ichorpowered.guardian.api.sequence.SequenceController;
import com.ichorpowered.guardian.common.detection.stage.StageCycleImpl;
import com.ichorpowered.guardian.sponge.GuardianPlugin;
import com.ichorpowered.guardian.sponge.detection.DetectionProviderImpl;
import com.me4502.precogs.detection.DetectionType;
import com.me4502.precogs.service.BypassTicket;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Event;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class BypassTicketImpl implements BypassTicket {

    private final DetectionController detectionController;
    private final SequenceController sequenceController;
    private final GuardianPlugin plugin;
    private final Player player;
    private final List<DetectionProviderImpl> detections;
    private final Object owner;

    private List<Long> blockId = new ArrayList<>();
    private boolean closed = false;

    public BypassTicketImpl(final @NonNull DetectionController detectionController, final @NonNull SequenceController sequenceController,
                            final @NonNull GuardianPlugin plugin, final @NonNull Player player,
                            final @NonNull List<DetectionProviderImpl> detections, final @NonNull Object owner) {
        this.detectionController = detectionController;
        this.sequenceController = sequenceController;
        this.plugin = plugin;
        this.player = player;
        this.detections = detections;
        this.owner = owner;

        this.detections.forEach(detectionType -> {
            final Detection detection = detectionType.provide();

            while (detection.getStageCycle().next()) {
                if (detection.getStageCycle().getStage().isPresent() && StageCycleImpl.class.isAssignableFrom(detection.getStageCycle().getStage().get().getClass())) {
                    if (!detection.getStageCycle().<Check<Event>>getStageProcess().isPresent()) continue;
                    final Check<Event> check = detection.getStageCycle().<Check<Event>>getStageProcess().get();

                    this.sequenceController.getPlayerResource().get(player.getUniqueId().toString()).ifPresent(gameReference -> {
                        this.blockId.add(
                                sequenceController.avoidObserver(gameReference, check.getEventType())
                        );
                    });
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
        return this.detections.stream().map(detectionProvider -> (DetectionType) detectionProvider).collect(Collectors.toList());
    }

    @Override
    public boolean isClosed() {
        return this.closed;
    }

    @Override
    public void close() {
        this.detections.forEach(detectionType -> {
            final Detection detection = detectionType.provide();

            while (detection.getStageCycle().next()) {
                if (detection.getStageCycle().getStage().isPresent() && StageCycleImpl.class.isAssignableFrom(detection.getStageCycle().getStage().get().getClass())) {
                    if (!detection.getStageCycle().<Check<Event>>getStageProcess().isPresent()) continue;
                    final Check<Event> check = detection.getStageCycle().<Check<Event>>getStageProcess().get();

                    this.sequenceController.getPlayerResource().get(player.getUniqueId().toString()).ifPresent(gameReference -> {
                        for (long id : this.blockId) {
                            this.sequenceController.unavoidObserver(gameReference, check.getEventType(), id);
                        }
                    });
                }
            }
        });
    }
}
