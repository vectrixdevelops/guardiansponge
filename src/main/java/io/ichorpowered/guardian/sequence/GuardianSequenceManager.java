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

import com.abilityapi.sequenceapi.Sequence;
import com.abilityapi.sequenceapi.SequenceBlueprint;
import com.abilityapi.sequenceapi.SequenceManager;
import com.abilityapi.sequenceapi.SequenceRegistry;
import com.abilityapi.sequenceapi.context.SequenceContext;
import com.abilityapi.sequenceapi.context.SequenceContextKey;
import com.ichorpowered.guardian.api.detection.Detection;
import com.ichorpowered.guardian.api.detection.DetectionConfiguration;
import com.ichorpowered.guardian.api.detection.DetectionPhase;
import com.ichorpowered.guardian.api.detection.heuristic.Heuristic;
import com.ichorpowered.guardian.api.detection.penalty.Penalty;
import com.ichorpowered.guardian.api.phase.type.PhaseTypes;
import io.ichorpowered.guardian.GuardianPlugin;
import io.ichorpowered.guardian.entry.GuardianEntityEntry;
import io.ichorpowered.guardian.report.GuardianSummary;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.scheduler.Task;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GuardianSequenceManager extends SequenceManager<Event> {

    private Map<UUID, SequenceContext> contextHistory = new HashMap<>();

    public GuardianSequenceManager(final SequenceRegistry<Event> sequenceRegistry) {
        super(sequenceRegistry);
    }

    @Override
    public boolean _invokeObserver(final Event event,
                                   final Sequence<Event> sequence,
                                   final SequenceContext sequenceContext) {
        this.contextHistory.put((UUID) sequenceContext.getId(), sequenceContext);

        boolean result = super._invokeObserver(event, sequence, sequenceContext);

        if (sequence.getState().equals(Sequence.State.FINISHED)) {
            this.transitionPhase(sequence);
        }

        return result;
    }

    @Override
    public boolean _invokeScheduler(final Sequence<Event> sequence,
                                    final SequenceContext sequenceContext) {
        this.contextHistory.put((UUID) sequenceContext.getId(), sequenceContext);

        boolean result = super._invokeScheduler(sequence, sequenceContext);

        if (sequence.getState().equals(Sequence.State.FINISHED)) {
            this.transitionPhase(sequence);
        }

        return result;
    }

    /**
     * A temporary sequence transition phase.
     *
     * This will be removed later to be event driven.
     *
     * @param sequence the sequence
     */
    @Deprecated
    private void transitionPhase(final Sequence<Event> sequence) {
        DetectionPhase<?, ?> detectionPhase = ((AbstractSequenceBlueprint<?, ?>) sequence.getBlueprint()).getDetection().getPhaseManipulator();

        while (detectionPhase.hasNext(PhaseTypes.HEURISTIC)) {
            detectionPhase.next(PhaseTypes.HEURISTIC);
        }

        while (detectionPhase.hasNext(PhaseTypes.PENALTY)) {
            detectionPhase.next(PhaseTypes.PENALTY);
        }
    }

    private void tickScheduler() {
        Sponge.getServer().getOnlinePlayers().forEach(player -> {
            final GuardianEntityEntry<Player> playerEntry = GuardianEntityEntry.of(player, player.getUniqueId());

            this.updateSchedulerIf(
                    this.contextHistory.getOrDefault(player.getUniqueId(),

                    SequenceContext.builder()
                            .id(playerEntry.getUniqueId())
                            .custom(SequenceContextKey.of("entry", playerEntry), playerEntry)
                            .build()),

                    sequence -> ((Sequence) sequence).getState().equals(Sequence.State.ACTIVE));
        });
    }

    public static class SequenceTask {

        private final GuardianPlugin plugin;
        private final GuardianSequenceManager sequenceManager;

        private Task task;

        public SequenceTask(final GuardianPlugin plugin,
                            final GuardianSequenceManager sequenceManager) {
            this.plugin = plugin;
            this.sequenceManager = sequenceManager;
        }

        public void start() {
            this.task = Task.builder().execute(() -> {
                this.sequenceManager.clean(false);
                this.sequenceManager.tickScheduler();
            }).name("Guardian - Sequence Tick").intervalTicks(1).submit(this.plugin);
        }

        public void stop() {
            if (this.task != null) this.task.cancel();
        }
    }
}
