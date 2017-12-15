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
import com.abilityapi.sequenceapi.SequenceContext;
import com.abilityapi.sequenceapi.SequenceManager;
import com.abilityapi.sequenceapi.SequenceRegistry;
import com.ichorpowered.guardian.api.detection.DetectionPhase;
import com.ichorpowered.guardian.api.detection.heuristic.Heuristic;
import com.ichorpowered.guardian.api.detection.penalty.Penalty;
import com.ichorpowered.guardian.api.phase.type.PhaseTypes;
import com.ichorpowered.guardian.api.sequence.capture.Capture;
import io.ichorpowered.guardian.GuardianPlugin;
import io.ichorpowered.guardian.entry.GuardianEntityEntry;
import io.ichorpowered.guardian.sequence.context.CommonContextKeys;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.scheduler.Task;

public class GuardianSequenceManager extends SequenceManager<Event> {

    public GuardianSequenceManager(final SequenceRegistry<Event> sequenceRegistry) {
        super(sequenceRegistry);
    }

    @Override
    public boolean _invokeObserver(final Event event,
                                   final Sequence<Event> sequence,
                                   final SequenceContext sequenceContext) {
        boolean result = super._invokeObserver(event, sequence, sequenceContext);

        if (sequence.getState().equals(Sequence.State.FINISHED)) {
            this.transitionPhase(sequenceContext.get(CommonContextKeys.ENTITY_ENTRY), (GuardianSequence) sequence);
        }

        return result;
    }

    @Override
    public boolean _invokeScheduler(final Sequence<Event> sequence,
                                    final SequenceContext sequenceContext) {
        boolean result = super._invokeScheduler(sequence, sequenceContext);

        if (sequence.getState().equals(Sequence.State.FINISHED)) {
            this.transitionPhase(sequenceContext.get(CommonContextKeys.ENTITY_ENTRY), (GuardianSequence) sequence);
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
    private void transitionPhase(final GuardianEntityEntry<Player> entityEntry, final GuardianSequence sequence) {
        DetectionPhase<?, ?> detectionPhase = sequence.getOwner().getPhaseManipulator();

        while (detectionPhase.hasNext(PhaseTypes.HEURISTIC)) {
            Heuristic heuristic = detectionPhase.next(PhaseTypes.HEURISTIC);
            heuristic.getSupplier().apply(entityEntry, sequence.getOwner(), sequence.getSummary());
        }

        while (detectionPhase.hasNext(PhaseTypes.PENALTY)) {
            Penalty penalty = detectionPhase.next(PhaseTypes.PENALTY);
            penalty.getPredicate().test(entityEntry, sequence.getOwner(), sequence.getSummary());
        }
    }

    private void tickScheduler() {
        Sponge.getServer().getOnlinePlayers().forEach(player -> {
            final GuardianEntityEntry<Player> entityEntry = GuardianEntityEntry.of(player, player.getUniqueId());

            this.updateSchedulerIf(
                    SequenceContext.builder()
                            .id(entityEntry.getUniqueId())
                            .custom(CommonContextKeys.ENTITY_ENTRY, entityEntry)
                            .build(),

                    sequence -> {
                        final GuardianSequence guardianSequence = (GuardianSequence) sequence;

                        // Update captures.
                        if (guardianSequence.getState().equals(Sequence.State.ACTIVE)) {
                            for (Capture capture : guardianSequence.getCaptureRegistry()) {
                                capture.update(entityEntry, guardianSequence.getCaptureRegistry().getContainer());
                            }

                            return true;
                        }

                        return false;
                    }
            );
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
            this.task = Task.builder()
                    .name("Guardian - Sequence Tick")
                    .execute(() -> {
                        this.sequenceManager.clean(false);
                        this.sequenceManager.tickScheduler();
                    })
                    .intervalTicks(1)
                    .submit(this.plugin);
        }

        public void stop() {
            if (this.task != null) this.task.cancel();
        }
    }
}
